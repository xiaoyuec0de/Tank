package com.android.emu.utils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;

public class FileUtils {

    public static ByteBuffer readFile(String name) {
        ByteBuffer buffer = null;
        FileInputStream fis = null;
        try {
            File file = new File(name);

             fis = new FileInputStream(file);

            byte[] bytes = new byte[(int) file.length()];

            fis.read(bytes);

            buffer = ByteBuffer.wrap(bytes);
            return buffer;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (fis != null) {
                try {
                    fis.close();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }

        return buffer;

    }

    public static String readString(String name) {
       ByteBuffer b = readFile(name);
       byte [] bb = b.array();
       return new String(bb);

    }
}
