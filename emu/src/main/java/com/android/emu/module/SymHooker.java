package com.android.emu.module;

import static com.android.emu.memory.MemConst.C_HOOK_MEMORY_BASE;
import static com.android.emu.memory.MemConst.C_HOOK_MEMORY_SIZE;

import static unicorn.ArmConst.UC_ARM_REG_R0;

import android.util.Log;

import com.android.emu.Emulator;
import com.android.emu.cenv.syscall.CallRet;
import com.android.emu.helper.MemoryAccess;
import com.android.emu.helper.MemoryHelper;
import com.android.emu.helper.NativeCallHelper;
import com.android.emu.helper.RegisterHelper;
import com.android.emu.utils.DigitUtils;
import com.android.emu.utils.Utils;
import com.emu.log.Logger;
import com.emu.vm.RootDir;

import java.util.HashMap;

import unicorn.ArmConst;
import unicorn.CodeHook;
import unicorn.Unicorn;


public class SymHooker implements CodeHook {

    private long hook_start;
    private long hook_current;
    private long current_id;
    private long size;

    private HashMap<Long, IS> hooks;

    private Emulator emulator;
    private Unicorn uc;
    private Modules modules;

    interface IC{
        CallRet call(Unicorn uc,long [] args) throws Exception;
    }

    class IS{
        public long addr;
        public String name;
        public int size;
        public IC ic;

        public IS(String name,int size,IC ic){
            this.name = name;
            this.size = size;
            this.ic = ic;
        }

        public void setAddr(long addr){
            this.addr = addr;
        }

        public CallRet call(Unicorn uc,long [] args) throws Exception{
            return this.ic.call(uc,args);
        }

    }


    public SymHooker(Emulator emulator,Unicorn uc,Modules modules){
        this.emulator = emulator;
        this.uc = uc;
        this.modules = modules;
        this.current_id = 0xFF00L;
        this.hook_start = C_HOOK_MEMORY_BASE + 4;
        this.size = C_HOOK_MEMORY_SIZE;
        this.hook_current = this.hook_start;

        this.hooks = new HashMap<>();

        uc.hook_add(this,this.hook_start,this.hook_start+size,"SymHooker");
        init();

    }

    public void init(){
        addHook("__system_property_get",2,this::SystemPropertyGet);
        addHook("dlopen",2,this::dlopen);
    }

    private CallRet dlopen(Unicorn uc, long[] args) {
        String path = MemoryHelper.readUTF8(uc,args[0]);
        Logger.info(String.format("dlopen:%s", path));

        if (path.equals("libvendorconn.so")){
            String vpath = RootDir.getInstance().getRootFile("root/system/lib/libvendorconn.so");
            Module m =emulator.loadLibrary(vpath,true);
            return new CallRet(m.getLoadBase());
        }

        return new CallRet(0);
    }

    private HashMap<String,String> properties = new HashMap<>();

    private void initProperty(){
        properties.put("emu","0");
    }

    private CallRet SystemPropertyGet(Unicorn uc, long[] args) {

        String name = MemoryHelper.readUTF8(uc,args[0]);
        long ptr = args[1];
        Logger.info(String.format("Called __system_property_get(%s, 0x%x)", name,ptr));

        return new CallRet(0);
    }

    public void addHook(String name,int size,IC ic ){
        IS is = new IS(name,size,ic);
        long addr = writeCFunction(is);
        modules.addSymbolHook(name,addr);
    }

    public long writeCFunction(IS is){
        long hook_idx = this.getNextId();
        long hook_addr = this.hook_current;


        byte [] asm_code = asmCode(hook_idx);

        Logger.info(String.format("编码完成，插入一个Sym hook，%x,%x，len:%d, ARM ASM:%s",hook_idx, hook_addr, asm_code.length, Utils.bytesToHexString(asm_code)));

        MemoryAccess.writeBytes(uc,hook_addr,asm_code);

        //保存结果

        this.hook_current += asm_code.length;
        this.hooks.put(hook_idx,is);

        is.setAddr(hook_addr);

        return hook_addr;

    }

    private byte [] asmCode(long addr){
        //10 B5 01 4C 24 46 10 BD 05 FF 00 00
//        PUSH {R4,LR}
//        LDR R4, =0x%x
//        MOV R4, R4
//        POP {R4,PC}

        byte [] bb = DigitUtils.long2bytes(addr,4);
        byte [] asm = {0x10, (byte) 0xB5,0x01,0x4C,0x24,0x46,0x10, (byte) 0xBD,bb[0],bb[1],bb[2],bb[3]};

        return asm;
    }

    private long getNextId(){
        long idx = this.current_id;
        this.current_id += 1;
        return idx;
    }


    @Override
    public void hook(Unicorn u, long address, int size, Object user) {
        // Check if instruction is "MOV R4, R4"
        if (size != 2){
//            Logger.warning("Not my Hook");
            return;
        }

        byte [] h =uc.mem_read(address,size);
        if (h[0] ==0x24 && h[1] ==0x46 ){
            //Find Hook
            long hook_idx = RegisterHelper.reg_read(uc, ArmConst.UC_ARM_REG_R4);

            Logger.info(String.format("符号调用拦截:%x", hook_idx));

            IS func = hooks.get(hook_idx);
            if (func != null){
                try {
                    Logger.info(String.format("符号调用:%s",func.name));
                    long[] args = NativeCallHelper.readArgs(uc,func.size);
                    CallRet ret = func.call(uc,args);
                    if (ret.hasReturn){
                        Logger.info(String.format("符号 %s 调用,结果:%x",func.name,ret.value));
                        uc.reg_write(UC_ARM_REG_R0,ret.value);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    uc.emu_stop();
                }
            }
        }
    }
}
