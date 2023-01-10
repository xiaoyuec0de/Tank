package com.android.emu;


import static com.android.emu.cons.EmuConst.UC_PROT_ALL;
import static com.android.emu.memory.MemConst.MODULES_MAX;
import static com.android.emu.memory.MemConst.MODULES_MIN;

import com.android.emu.cenv.Handler;
import com.android.emu.cenv.Hooker;
import com.android.emu.module.SymHooker;
import com.android.emu.cenv.func.IFunc;
import com.android.emu.cenv.func.ISymbolHookFunc;
import com.android.emu.cenv.func.SymbolHookFunc;
import com.android.emu.fix.CodeFix;
import com.android.emu.jenv.JniEnv;
import com.android.emu.jenv.Jvm;
import com.android.emu.module.Module;
import com.android.emu.module.Modules;
import com.android.emu.module.ReslovedSymbol;
import com.android.emu.helper.NativeCallHelper;
import com.android.emu.utils.AlignUtils;
import com.android.emu.utils.HexUtils;
import com.emu.log.Logger;
import com.android.emu.memory.MemoryManager;
import com.android.emu.tracer.Tracer;
import com.android.emu.vfs.VirtualFileSystem;

import unicorn.ArmConst;
import unicorn.Unicorn;
import unicorn.UnicornConst;
import unicorn.UnicornException;


public class Emulator {

    //模拟器核心
    private Unicorn uc;

    //Executalbe data 可执行代码相关
    private Modules modules;
    private MemoryManager memoryManager;

    //CPU 中断，系统调用拦截
    private Handler handler;

    //虚拟文件系统
    private VirtualFileSystem virtualFileSystem;

    // Hooker
    private Hooker hooker;
    private SymHooker symHooker;

    //Tracer
    private Tracer tracer;

    //jvm
    private Jvm jvm;



    public Emulator(){
        this.uc = new Unicorn(UnicornConst.UC_ARCH_ARM,UnicornConst.UC_MODE_ARM);

        this.enableVFP();
        // 内存初始化
        this.memoryManager = new MemoryManager(this.uc);

        //Executable data.
        this.modules = new Modules(this);

        //中断及系统调用拦截
        this.handler = new Handler(this.uc,this.memoryManager,this.modules);

        //虚拟文件系统
        this.virtualFileSystem = new VirtualFileSystem(this.handler);

        // Hooker
        this.hooker = new Hooker(this.uc);

        this.symHooker = new SymHooker(this,this.uc,this.modules);
//        this.symbolHandler = new SymbolHandler(this.hooker);
        //Tracer
        this.tracer = new Tracer();

        this.jvm = new Jvm(this);

        initKernel();

//        fixCode();
    }

    private void fixCode() {
        this.uc.hook_add(CodeFix.codeFix,MODULES_MIN,MODULES_MAX,null);
    }

    private void initKernel(){
       long addr =  modules.findCSymbol("_ZL25kernel_has_MADV_MERGEABLE");
       Logger.info(String.format("发现符号:%s,%x", "_ZL25kernel_has_MADV_MERGEABLE",addr));
    }


    public void loadClass(Class klass){
        this.jvm.addClass(klass);
    }

    public Module loadLibrary(String fileName,boolean callInit){
        try {
           Module module =  this.modules.loadModule(fileName);

           if (callInit){
                callModuleInit(module);
           }

           return module;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void callModuleInit(Module module){
        Logger.info(String.format("Calling init for : %s", module.getName()));
        long [] initArray = module.getInitArray();
        if (initArray != null) {
            for (int i=0;i<initArray.length;i++) {
                long func = initArray[i];
                Logger.info(String.format("Calling init in : %x,offset:%x", func,func-module.getLoadBase()));
                this.callNative(func);
            }
        }else {
            Logger.info(String.format("No init array in %s",module.getName()));
        }
    }

    public Long callNativeSymbol(String symbolName,Long... args){
        ReslovedSymbol symbol = modules.lookupSymbol(symbolName);
        if (symbol == null){
            Logger.error(String.format("符号 %s 未找到", symbolName));
            return null;
        }

        Logger.info(String.format("调用符号:%s,%x", symbolName,symbol.address));

        return callNative(symbol.address,args);
    }


    public Long callSymbol(Module module,String symbolName,Long... args){

        ReslovedSymbol symbol = module.findSymbol(symbolName);
        if (symbol == null){
            Logger.error(String.format("符号 %s 未找到", symbolName));
            return null;
        }

        Logger.info(String.format("调用符号:%s,%x", symbolName,symbol.address));

        return callNative(symbol.address,args);
    }

    public Long callNative(long addr,Long... args) throws UnicornException {
            NativeCallHelper.writeArgs(this.uc, args);
            long stop_pos = this.memoryManager.getHookRandomAddr();
            long ins_until = AlignUtils.xorBit(stop_pos);
            long timeout = 0;
            long ins_num = 0;

            Logger.info(String.format("instruction start : %x,end : %x",addr, ins_until));

            this.uc.reg_write(ArmConst.UC_ARM_REG_LR, stop_pos);

            this.uc.emu_start(addr,ins_until,timeout,ins_num);

            //调用结束，读取返回值
            Long ret = (Long) this.uc.reg_read(ArmConst.UC_ARM_REG_R0);

            // 最多64位
            ret = AlignUtils.maskLong(ret);

            return ret;

    }

    public Unicorn getUc(){
        return this.uc;
    }

    public Jvm getJvm(){
        return jvm;
    }

    public JniEnv getJniEnv(){return jvm.getJniEnv();}

    public MemoryManager getMemoryManager(){return this.memoryManager;}

    private void dumpRegisters(){
        Long pc = (Long) this.uc.reg_read(ArmConst.UC_ARM_REG_PC);
        Logger.info(String.format("PC :%x", pc & 0xFFFFFFFFL ));

        Long lr = (Long) this.uc.reg_read(ArmConst.UC_ARM_REG_LR);
        Logger.info(String.format("LR :%x", lr & 0xFFFFFFFFL ));
    }

    public void addFuncHook(IFunc func){
        this.hooker.addFuncHook(func);
    }

    public void addSymbolHook(String sym, int size,ISymbolHookFunc func){
        long addr = modules.findCSymbol(sym);
        if (addr != 0) {
            Logger.info(String.format("添加一个符号Hook:%s,%x", sym,AlignUtils.sym(addr)));
            addFuncHook(new SymbolHookFunc(addr,sym,size,func));
        }
    }

    public void stop() {
        this.uc.emu_stop();
    }

    /*
    * NEON和VFP指令集是ARM指令集的扩展，多用于多媒体编程与浮点计算
    * */
    private void enableVFP(){
//        # MRC p15, #0, r1, c1, c0, #2
//        # ORR r1, r1, #(0xf << 20)
//        # MCR p15, #0, r1, c1, c0, #2
//        # MOV r1, #0
//        # MCR p15, #0, r1, c7, c5, #4
//        # MOV r0,#0x40000000
//        # FMXR FPEXC, r0

        StringBuffer sb = new StringBuffer();
        sb.append("11 EE 50 1F");
        sb.append(" 41 F4 70 01");
        sb.append(" 01 EE 50 1F");
        sb.append(" 4F F0 00 01");
        sb.append(" 07 EE 95 1F");
        sb.append(" 4F F0 80 40");
        sb.append(" E8 EE 10 0A");
//        # vpush {d8}
        sb.append(" 2D ED 02 8B");

        long address = 0x1000;
        long mem_size = 0x1000;
        byte [] code = HexUtils.Hex2Byte(sb.toString());

        try{
            this.uc.mem_map(address, mem_size, UC_PROT_ALL);
            this.uc.mem_write(address, code);
            this.uc.reg_write(ArmConst.UC_ARM_REG_SP, address + mem_size);
            this.uc.emu_start(address|1,address+code.length,0,0);
        }finally {
            this.uc.mem_unmap(address,mem_size);
        }


    }
}
