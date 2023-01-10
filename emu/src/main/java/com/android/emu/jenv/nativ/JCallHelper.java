package com.android.emu.jenv.nativ;

import com.android.emu.helper.NativeCallHelper;

import unicorn.Unicorn;

public class JCallHelper {

    public static void writeArgs(Unicorn uc,Long ... args){
        NativeCallHelper.writeArgs(uc,args);
    }

    public static long[] readArgs(Unicorn uc,int acount){
        return NativeCallHelper.readArgs(uc,acount);
    }

    public static long[] readVaArgs(Unicorn uc,int acount){
        return NativeCallHelper.readVaArgs(uc,12);
    }
}
