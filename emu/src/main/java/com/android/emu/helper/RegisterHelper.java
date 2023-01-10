package com.android.emu.helper;

import com.android.emu.utils.AlignUtils;

import unicorn.ArmConst;
import unicorn.Unicorn;

/*
* 寄存器读取和写入的帮助类
* */
public class RegisterHelper {

    public static long reg_read(Unicorn uc,int reg){
        return AlignUtils.maskLong((Long) uc.reg_read(reg));
    }

    public static void reg_write(Unicorn uc,int reg,long value){
        uc.reg_write(reg,value);
    }
}
