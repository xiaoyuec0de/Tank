package com.android.emu.utils;

public class AlignUtils {
    private static final long MASK_LONG = 0xFFFFFFFFL;
    private static final long MASK_BIT = 0xFFFFFFFEL;

    public static long xorBit(long v){
        return v & MASK_BIT;
    }

    public static long maskLong(long v){
        return v & MASK_LONG;
    }

    public static long ucRet(Object v){
        Number n = (Number) v;

        return n.longValue() & MASK_LONG;
    }

    /*
    * arm指令的pc偏移两个指令
    * */
    public static long pc(long v){
        return v & MASK_LONG ;
    }

    /*
    * 符号地址取整
    * */
    public static long sym(long v){
        return v & MASK_BIT;
    }
}
