package com.android.emu.helper;

import static com.android.emu.cons.EmuConst.STACK_OFFSET;
import static unicorn.ArmConst.*;

import com.android.emu.utils.AlignUtils;
import com.android.emu.utils.DigitUtils;
import com.android.emu.utils.Utils;
import com.emu.log.Logger;

import unicorn.Unicorn;

/*
* 对于so函数的帮助类
*
* */
public class NativeCallHelper {

    private static final long MASK_LONG = 0xFFFFFFFFL;

    public static void writeArgs(Unicorn uc,Long ... args){
        int amount = args.length;

        if (amount == 0){
            return;
        }

        /*
        * 先传r0-r3寄存器
        * */
        uc.reg_write(UC_ARM_REG_R0,args[0]);

        if (amount >=2){
            uc.reg_write(UC_ARM_REG_R1,args[1]);
        }

        if (amount >=3){
            uc.reg_write(UC_ARM_REG_R2,args[2]);
        }

        if (amount >=4){
            uc.reg_write(UC_ARM_REG_R3,args[3]);
        }

        /*
        * 把传递的参数，写入堆栈中
        *
        * */

        if (amount >=5){
            Long sp_start = (Long) uc.reg_read(UC_ARM_REG_SP);

            // Need to offset because our hook pushes one register on the stack
            long sp_current = sp_start - STACK_OFFSET;

            //计算需要开辟的堆栈大小
            sp_current = sp_current - (4L *(amount - 4L));
            Long sp_end = sp_current;

            //依次写入寄存器
            for (int i=4;i<amount;i++){
                Long arg = (Long) args[i];

                byte [] mbytes = DigitUtils.longToBytesLittle(arg);
                byte [] bytes = DigitUtils.cut(mbytes,4);

                uc.mem_write(sp_current,bytes);
            }

            uc.reg_write(UC_ARM_REG_SP,sp_end);
        }


    }

    public static long [] readArgs(Unicorn uc, int amount){
        if (amount == 0)return null;
        long [] args = new long[amount];

        args[0] = (long) uc.reg_read(UC_ARM_REG_R0)&MASK_LONG;

        if (amount >=2){
            args[1] = (long) uc.reg_read(UC_ARM_REG_R1)&MASK_LONG;
        }

        if (amount >=3){
            args[2] = (long) uc.reg_read(UC_ARM_REG_R2)&MASK_LONG;
        }

        if (amount >=4){
            args[3] = (long) uc.reg_read(UC_ARM_REG_R3)&MASK_LONG;
        }

        Long sp = (Long) uc.reg_read(UC_ARM_REG_SP)&MASK_LONG;

        sp = sp + STACK_OFFSET;

        if (amount >= 5){
            for(int i=4;i<amount;i++){
               byte [] bytes = uc.mem_read(sp+i*4,4);
               Long v = DigitUtils.bytes2Long(bytes);
               args[i] = v;
            }
        }

        return args;
    }

    public static long [] readVaArgs(Unicorn uc, int amount){
        if (amount == 0)return null;
        long [] args = new long[amount];

        args[0] = (long) uc.reg_read(UC_ARM_REG_R0)&MASK_LONG;

        if (amount >=2){
            args[1] = (long) uc.reg_read(UC_ARM_REG_R1)&MASK_LONG;
        }

        if (amount >=3){
            args[2] = (long) uc.reg_read(UC_ARM_REG_R2)&MASK_LONG;
        }

        if (amount >=4){
            args[3] = (long) uc.reg_read(UC_ARM_REG_R3)&MASK_LONG;
        }

        Long sp = (Long) uc.reg_read(UC_ARM_REG_SP)&MASK_LONG;

        sp = sp + STACK_OFFSET;

        if (amount >= 5){
            for(int i=4;i<amount;i++){
                byte [] bytes = uc.mem_read(sp+i*4,4);
//                Logger.info(Utils.bytesToHexString(bytes));
                Long v = DigitUtils.bytes2Long(bytes);
                args[i] = v;
            }
        }

        return args;
    }

    public static long [] readSyscallArgs(Unicorn uc,int size) {
        long[] args = new long[size];

        for (int i=0;i<size;i++){
            args[i] = AlignUtils.ucRet(uc.reg_read(UC_ARM_REG_R0 + i));
        }
//        args[0] = (long) uc.reg_read(UC_ARM_REG_R0);
//        args[1] = (long) uc.reg_read(UC_ARM_REG_R1);
//        args[2] = (long) uc.reg_read(UC_ARM_REG_R2);
//        args[3] = (long) uc.reg_read(UC_ARM_REG_R3);
//
//        args[4] = (long) uc.reg_read(UC_ARM_REG_R4);
//        args[5] = (long) uc.reg_read(UC_ARM_REG_R5);
//        args[6] = (long) uc.reg_read(UC_ARM_REG_R6);
//        args[7] = (long) uc.reg_read(UC_ARM_REG_R7);

        return args;

    }
}
