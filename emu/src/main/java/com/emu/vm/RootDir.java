package com.emu.vm;

import android.content.Context;
import android.content.res.AssetManager;

import com.android.emu.R;
import com.emu.log.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class RootDir {

    private static final String ROOT = "root";

    private static RootDir instance;


    public static RootDir getInstance(){
        if (instance == null){
            instance = new RootDir();
        }

        return instance;
    }
    private  Context context;

    public void setContext(Context context){
        this.context = context;
    }

    public static void mkdirs(String path){
        File file = new File(path);
        file.getParentFile().mkdirs();
    }

    public String getRootFile(String name){
        String path = context.getApplicationContext().getFilesDir().getParent() +"/"+ name;
        return path;
    }

    public String getVirtualFile(String name){
        String path = context.getApplicationContext().getFilesDir().getParent() +"/root"+ name;
        return path;
    }

    public  void deleteRootFile(String name){
        String path = getRootFile(name);
        File file = new File(path);
        if (file.exists()){
            file.delete();
        }
    }

    /*
    * 复制或者获取文件
    *
    * */
    public String getRootFile2(String name){
        String path = getRootFile(name);
        File file = new File(path);
        if (!file.exists()){
            copyAssertFile(name);
        }

        return path;
    }

    public void copyAssertDir(String path){
        AssetManager asset = context.getAssets();

        try {
            String[] files = asset.list(path);
//            Logger.info(String.format("进入位置:%s", path));
            //为文件
            if(files == null || files.length == 0){
//                Logger.info(String.format("复制文件到位置:%s", path));
                copyAssertFile(path);
                return;
            }
            for (String f : files) {
                String sub = path+"/"+f;
                copyAssertDir(sub);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String copyAssertFile(String name) {
        InputStream in = null;
        FileOutputStream out = null;

        String path = getRootFile(name); // data/data目录

        mkdirs(path);

        File file = new File(path);
        if (!file.exists()) {
            try {
                in = context.getAssets().open(name); // 从assets目录下复制
                out = new FileOutputStream(file);
                int length = -1;
                byte[] buf = new byte[1024];
                while ((length = in.read(buf)) != -1) {
                    out.write(buf, 0, length);
                }
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        return path;
    }
}
