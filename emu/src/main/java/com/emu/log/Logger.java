package com.emu.log;

import android.util.Log;

public class Logger {

    private static final String TAG = "EMULATOR";

    public static void info(String msg){
        Log.i(TAG,msg);
    }

    public static void warning(String msg){
        Log.w(TAG,msg);
    }

    public static void error(String msg){
        Log.e(TAG,msg);
    }
}
