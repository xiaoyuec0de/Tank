package com.android.emu.vfs;

import com.android.emu.cenv.Handler;
import com.android.emu.cenv.syscall.CallRet;
import com.android.emu.helper.MemoryHelper;
import com.emu.log.Logger;
import com.emu.vm.RootDir;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Random;

import unicorn.Unicorn;

//        #define O_ACCMODE   00000003
//        #define O_RDONLY    00000000
//        #define O_WRONLY    00000001
//        #define O_RDWR      00000002
//        #define O_CREAT     00000100    /* not fcntl */
//        #define O_EXCL      00000200    /* not fcntl */
//        #ifndef O_NOCTTY
//        #define O_NOCTTY    00000400    /* not fcntl */
//        #define O_TRUNC     00001000    /* not fcntl */
//        #define O_APPEND    00002000
//        #define O_NONBLOCK  00004000
//        #define O_SYNC      00010000
//        #define FASYNC      00020000    /* fcntl, for BSD compatibility */
//        #define O_DIRECT    00040000    /* direct disk access hint */
//        #define O_LARGEFILE 00100000
//        #define O_DIRECTORY 00200000    /* must be a directory */
//        #define O_NOFOLLOW  00400000    /* don't follow links */
//        #define O_NOATIME   01000000
//        #define O_CLOEXEC   02000000    /* set close_on_exec */


public class VirtualFileSystem {

    private static long O_CREAT = 00000100L;
    private static long O_APPEND = 00002000L;

    private static boolean OVERRIDE_URANDOM = false;
    private static char    OVERRIDE_URANDOM_BYTE = '0';
    private static boolean WRITE_FSTAT_TIMES = true;

    private Handler handler;


//    private int fdDescriptor;
    private HashMap<Long,VirtualFile> files;

    public VirtualFileSystem(Handler handler){
        this.handler = handler;
//        this.fdDescriptor = 3;
        this.files = new HashMap<>();

        this.files.put(0L,new VirtualFile("stdin",0));
        this.files.put(1L,new VirtualFile("stdout",1));
        this.files.put(2L,new VirtualFile("stderr",2));

        initFileSystem();
    }

    private void initFileSystem(){
        handler.addSyscall(0x3, "read", 3, this::read);
        handler.addSyscall(0x5, "open", 3, this::openat);
        handler.addSyscall(0x6, "close", 1, this::close);
        handler.addSyscall(0x21, "access", 2, this::access);
        handler.addSyscall(0x92, "writev", 3, this::writev);
        handler.addSyscall(0xC3, "stat64", 2, this::stat64);
        handler.addSyscall(0xC5, "fstat64", 2, this::fstat64);
        handler.addSyscall(0x142, "openat", 4, this::openat);
        handler.addSyscall(0x147, "fstatat64", 4, this::fstatat64);
    }

    private CallRet fstatat64(Unicorn uc, String name, long[] args) {
        String path = MemoryHelper.readUTF8(uc,args[0]);
        Logger.info(String.format("fstatat64:%s", path));

        File file = new File(virtualPath(path));
        if (!file.exists()){
            return new CallRet(-1);
        }

        return new CallRet(0);
    }

    private CallRet fstat64(Unicorn uc, String name, long[] args) throws Exception {
        Logger.info(String.format("调用C的API fstat64"));

        long fd = args[0];
        long buf_ptr = args[1];

        VirtualFile file = files.get(fd);
        if (file == null){
            return new CallRet(-1);
        }

        Logger.info(String.format("获取文件状态，fstat64:%s", file.getName()));

        JSONObject stat = FileStatHelper.getStat(file.getName());
        FileStatHelper.writeStat2Memory(uc,stat,buf_ptr,WRITE_FSTAT_TIMES);

        return new CallRet(0);
    }

    private CallRet stat64(Unicorn uc, String name, long[] args) {
        String path = MemoryHelper.readUTF8(uc,args[0]);
        Logger.info(String.format("stat64:%s", path));

        File file = new File(virtualPath(path));
        if (!file.exists()){
            return new CallRet(-1);
        }

        return new CallRet(0);
    }

    private CallRet writev(Unicorn uc, String name, long[] args) {
        long fd = args[0];
        long buffer = args[1];
        long count = args[2];

        VirtualFile file = files.get(fd);
        if (file == null){
            return new CallRet(-1);
        }

        File f = new File(virtualPath(file.getName()));
        try {
            FileOutputStream fos = new FileOutputStream(f);

            byte [] bytes = MemoryHelper.readByteArray(uc,buffer,count);
            fos.write(bytes);
            fos.close();

        }catch (Exception e){
            e.printStackTrace();
        }

        return new CallRet(0);
    }

    private String virtualPath(String name){
        return RootDir.getInstance().getVirtualFile(name);
    }

    private CallRet access(Unicorn uc, String name, long[] args) {
        String path = MemoryHelper.readUTF8(uc,args[1]);
        File file = new File(virtualPath(path));
        if (file.exists()){
            return new CallRet(0);
        }

        return new CallRet(-1);
    }

    private CallRet close(Unicorn uc, String name, long[] args) throws Exception {
        long fd = args[0];
        VirtualFile f = files.get(fd);
        if (f != null){
            Logger.info(String.format("关闭文件:%s", f.getName()));
            f.close();
            files.remove(fd);
            return new CallRet(0);
        }

        return new CallRet(0);
    }

    private long nextFd(){
        long fd = 3;
        while (files.containsKey(fd)){
            fd++;
        }
        return fd;
    }

    private CallRet store(String name,long flag) throws Exception{
        long fd = nextFd();
        VirtualFile file = new VirtualFile(name,fd);
        files.put(fd,file);

        if ((flag & O_CREAT) != 0){
            file.create();
        }

        return new CallRet(fd);
    }

    private CallRet open(String path,long flag,long mode) throws Exception{
        Logger.info(String.format("打开文件:%s", path));
        if (path.equals("/dev/urandom")){
            return store(path,0);
        }


        return store(path,flag);
    }

    private CallRet openat(Unicorn uc, String name, long[] args) throws Exception{
        String path = MemoryHelper.readUTF8(uc,args[1]);
        long flag = args[1];
        long mode = args[2];

        return open(path,flag,mode);
    }

    private CallRet read(Unicorn uc, String name, long[] args) throws Exception {
        long fd =  args[0];
        long buffer = args[1];
        long count = args[2];



        if (fd <=2){
            throw new Exception(String.format("Unsupported read operation for file descriptor %d.", fd));
        }

        if (!files.containsKey(fd)){
            Logger.error(String.format("No such file descriptor index %s in VirtualFileSystem", fd));
            return new CallRet(-1L);
        }

        VirtualFile file = files.get(fd);
        Logger.info(String.format("尝试从文件:%s,读取:%d个字节", file.getName(),count));

        String vpath = file.getVpath();
        byte [] bytes = new byte[(int) count];
        int tsize = 0;
        if (file.getName().equals("/dev/urandom")){
            if (OVERRIDE_URANDOM){

            }else {
                new Random().nextBytes(bytes);
            }
        }else {
            tsize = file.getInputStream().read(bytes);
            if (tsize >= 0) {
                Logger.info(String.format("从文件:%s，读取%d个字节", vpath, tsize));
                MemoryHelper.writeBytes(uc, buffer, bytes);
            }else {
                Logger.info(String.format("读取到%d个字节", tsize));
//                throw new Exception(String.format("读取%s", vpath));
            }
        }


        return new CallRet(tsize);
    }




}
