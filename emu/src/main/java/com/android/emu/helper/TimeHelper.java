package com.android.emu.helper;

public class TimeHelper {

    public static long currentSeconds(){
        long c = System.currentTimeMillis();

        return c / 1000;
    }
}
