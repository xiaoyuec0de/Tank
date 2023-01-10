package com.android.emu.vfs;

import com.emu.log.Logger;
import com.emu.vm.RootDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/*
* 虚拟文件
* */
public class VirtualFile {
    private String name;
    private long fd;

    //虚拟路径
    private String vpath;
    private File file;
    private FileInputStream fis;
    private FileOutputStream fos;

    public VirtualFile(String name ,long fd){
        this.name = name;
        this.fd = fd;
        this.vpath = RootDir.getInstance().getVirtualFile(name);
        this.file = new File(vpath);
//        Logger.info(String.format("vpath:%s", vpath));

    }

    public String getName(){
        return name;
    }

    public String getVpath(){
        return vpath;
    }

    public long getFd(){
        return fd;
    }

    public boolean exists(){
        return file.exists();
    }

    public FileInputStream getInputStream() throws Exception{
        if (fis == null){
            fis = new FileInputStream(file);
        }

        return fis;
    }

    public int read(byte [] bytes)throws Exception{
        File file = new File(this.vpath);
        FileInputStream fis = new FileInputStream(file);

        int size= fis.read(bytes);

        fis.close();

        return size;
    }

    public FileOutputStream getOutputStream() throws Exception{
        if (fos == null){
            fos = new FileOutputStream(file);
        }

        return fos;
    }

    public void close()throws Exception{
        if (fis != null){
            fis.close();
        }

        if (fos != null){
            fos.close();
        }
    }

    public void create() throws Exception {
        if (file.exists()){
            file.delete();
        }
        file.createNewFile();
    }
}
