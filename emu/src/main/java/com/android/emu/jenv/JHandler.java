package com.android.emu.jenv;


import static com.android.emu.memory.MemConst.JVM_HOOK_MEMORY_BASE;
import static unicorn.ArmConst.UC_ARM_REG_R0;

import com.android.emu.helper.MemoryAccess;
import com.android.emu.helper.RegisterHelper;
import com.android.emu.jenv.nativ.JCallHelper;
import com.android.emu.utils.DigitUtils;
import com.emu.log.Logger;

import java.util.Collections;
import java.util.HashMap;

import unicorn.ArmConst;
import unicorn.CodeHook;
import unicorn.Unicorn;



/*
* java的拦截器
* 用于构造虚拟环境
*
* */
public class JHandler implements CodeHook{

    private Unicorn uc;

    private long size;
    private long current_id;

    private long hook_start;
    private long hook_magic;
    private long hook_current;

    private HashMap<Long, IJFunc> hooks;

    public JHandler(Unicorn uc) {
        this.uc = uc;
        this.current_id = 0xFF00L;
        this.hook_magic = JVM_HOOK_MEMORY_BASE;
        this.hook_start = hook_magic + 4;
        this.size = JVM_HOOK_MEMORY_BASE;
        this.hook_current = this.hook_start;
        this.hooks = new HashMap<>();

        uc.hook_add(this,this.hook_start,this.hook_start+size,"JvmHooker");

    }

    @Override
    public void hook(Unicorn uc, long addr, int size, Object user) {
        // Check if instruction is "MOV R4, R4"
        if (size != 2){
//            Logger.warning("Not my Hook");
            return;
        }

        byte [] h =uc.mem_read(addr,size);
        if (h[0] ==0x24 && h[1] ==0x46 ){
            //Find Hook
            long hook_idx = RegisterHelper.reg_read(uc,ArmConst.UC_ARM_REG_R4);

            Logger.info(String.format("Java调用拦截:%x", hook_idx));

            IJFunc func = hooks.get(hook_idx);
            if (func != null){
                try {
                    // 获取参数，跟c语言一样
                    /*
                    * 可变参数，类似于va list
                    * 应该采用 va list的读取方法
                    *
                    * */
                    int acount = func.getAcount();
                    long [] args = JCallHelper.readVaArgs(uc,acount);

                    Logger.info(String.format("Java调用:%s",func.getName()));

                    JRet ret = func.call(uc,args);
                    if (ret.hasReturn){
                        Logger.info(String.format("Java %x,%s 调用,结果:%x", func.getHookAddr(),func.getName(),ret.value));
                        uc.reg_write(UC_ARM_REG_R0,ret.value);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    uc.emu_stop();
                }
            }else {
                Logger.info(String.format("Java调用未注册:%x", hook_idx));
            }

        }else {
//            Logger.info("Not Jvm Hook");
        }

    }

    /*
    * 构造一段特殊的代码，用于拦截c中调用java
    *
    * */
    public boolean writeFunction(IJFunc func){
        /*
        * PUSH {R4,LR}
        * MOV R4, #func
        * MOV R4, R4
        * POP {R4,PC}
        * */
//        Logger.info("尝试写JAVA函数");
        long hook_idx = this.getNextId();
        long hook_addr = this.hook_current;

//        String asm = String.format("PUSH {R4,LR}\nLDR R4, =0x%x\nMOV R4, R4\nPOP {R4,PC}\n", hook_idx);

//        String asm = String.format("MOV R4, #%x", hook_idx);

//        byte [] asm_code = Keystone.asm(Keystone.KS_ARCH_ARM,Keystone.KS_MODE_THUMB,asm.getBytes(), 0);
//        if (asm_code == null || asm_code.length == 0){
////            Logger.info(String.format("立即数:%x非法,重新生成",hook_idx));
//            return false;
//        }

        byte [] asm_code = asmCode(hook_idx);

//        if (asm_code.length != 12){
//            Logger.info(String.format("汇编码长度:%d非法,重新生成",asm_code.length));
//            return false;
//        }

//        Logger.info(String.format("编码完成，%x,%x，len:%d, ARM ASM:%s",hook_idx, hook_addr, asm_code.length,Utils.bytesToHexString(asm_code)));

        func.setHookAddr(hook_addr);
        MemoryAccess.writeBytes(uc,hook_addr,asm_code);

        //保存结果

        this.hook_current += asm_code.length;
        this.hooks.put(hook_idx,func);

        return true;

    }

    /*
    * 把jvm的结构体，写入到UC的内存中
    * 1、table是一个结构体
    * 2、function里面写入具体的代码
    * */
    public TableRet writeFunctionTable(HashMap<Long, IJFunc> table)throws Exception{
        long index_max = Collections.max(table.keySet())+1;

        // First, we write every function and store its result address.

        for (long idx=0;idx<index_max;idx++){
            IJFunc func = table.get(idx);
            if (func != null) {
                writeFunction(func);
            }
        }

        // Then we write the function table.
        long table_address = this.hook_current;
        for (long i=0;i<index_max;i++){
            IJFunc func = table.get(i);
            if (func != null) {
                long func_addr = func.getHookAddr()+1; // 变成thumb指令集
//                Logger.info(String.format("%x FUNC:%x",this.hook_current, func_addr));
                MemoryAccess.write_u32(uc,this.hook_current,func_addr);
            }else {
//                Logger.info(String.format("%x FUNC:%x",this.hook_current, 0));
                MemoryAccess.write_u32(uc,this.hook_current,0);
            }
            this.hook_current += 4;
        }

        //写入table的首地址到uc的内存中


        //Then we write the a pointer to the table.
        long ptr_address = this.hook_current;
        MemoryAccess.write_u32(uc,ptr_address,table_address);
        this.hook_current += 4;

        TableRet ret = new TableRet();
        ret.ptr   = ptr_address;
        ret.table = table_address;

        return ret;

    }

    private long getNextId(){
        long idx = this.current_id;
        this.current_id += 1;
        return idx;
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

    private byte [] addr2bytes(long addr){
        byte [] asm = new byte[10];
        // 10 B5 01 4C 24 46 10 BD 05 FF 00 00
        asm[0] = 0x10;
        asm[1] = (byte) 0xb5;
        asm[2] = 0x4f;
        asm[3] = (byte) 0xf4;
        asm[4] = (byte) 0xc8; // addr
        asm[5] = 0x34; //addr
        asm[6] = 0x24;
        asm[7] = 0x46;
        asm[8] = (byte) 0x10;
        asm[9] = (byte) 0xbd;

        return asm;
    }
}
