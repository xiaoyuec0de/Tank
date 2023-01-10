package com.emu.utils;

import android.content.Context;

import com.emu.log.Logger;
import com.emu.vm.RootDir;

public class KsTools {
    public static Context mContext;
    public static void init(Context context){
        mContext = context;
    }

    public static byte[] asm(String asm) throws Exception{
        String path = RootDir.getInstance().getRootFile2("kstool/armeabi-v7a/kstool");

        Runtime.getRuntime().exec("chmod a+x "+path);

//        ProcessBuilder p = new ProcessBuilder();
//        p.redirectErrorStream();
//        p.command(path + " -b thumb '"+asm+"'"+"\n");
        String cmd = path + " -b thumb '"+asm.toLowerCase()+"'"+"0 \n";
        Logger.info(cmd);
        Process pr =  Runtime.getRuntime().exec(cmd);

        byte [] buffer = new byte[4];
//        String str = new String(buffer);
//        Logger.error(str);

       pr.getInputStream().read(buffer);

//        byte [] out = new byte[size];
//        System.arraycopy(buffer,0,out,0,size);

        return buffer;
    }
}
