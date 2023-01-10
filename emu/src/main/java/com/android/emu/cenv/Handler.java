package com.android.emu.cenv;

import com.android.emu.cons.LinuxConst;
import com.android.emu.helper.MemoryAccess;
import com.android.emu.helper.MemoryHelper;
import com.android.emu.helper.TimeHelper;
import com.android.emu.cenv.cpu.InterruptHandler;
import com.android.emu.cenv.fork.ForkInfo;
import com.android.emu.cenv.syscall.CallRet;
import com.android.emu.cenv.syscall.ISyscall;
import com.android.emu.cenv.syscall.SyscallHandler;
import com.android.emu.cenv.syscalls.ISCallBack;
import com.android.emu.cenv.syscalls.Syscall1;
import com.android.emu.memory.MemChunk;
import com.android.emu.memory.MemoryManager;
import com.android.emu.module.Modules;
import com.android.emu.cenv.socket.SocketInfo;
import com.android.emu.utils.MemUtils;
import com.emu.log.Logger;

import java.util.HashMap;
import java.util.Random;

import unicorn.Unicorn;

/*
* 不开放给用户的函数
*
* */
public class Handler {

    private static final boolean OVERRIDE_TIMEOFDAY = false;
    private static final long OVERRIDE_TIMEOFDAY_SEC = 0L;
    private static final long OVERRIDE_TIMEOFDAY_USEC = 0L;
    private static final boolean OVERRIDE_CLOCK = false;
    private static final long OVERRIDE_CLOCK_TIME = 0L;

    private Unicorn uc;

    private InterruptHandler interruptHandler;
    private SyscallHandler syscallHandler;

    //
    private ForkInfo fork;

    private long clock_start;
    private long clock_offset;
    private long socket_idx;
    private HashMap<Long,SocketInfo> sockets;

    private MemoryManager memoryManager;
    private Modules modules;


    public Handler(Unicorn uc,MemoryManager memoryManager,Modules modules){
        this.uc = uc;
        this.memoryManager = memoryManager;
        this.modules = modules;

        this.interruptHandler = new InterruptHandler(uc);
        this.syscallHandler = new SyscallHandler(uc);

        //系统调用走的是2号中断
        this.interruptHandler.addInterrput(2,this.syscallHandler);

        initSyscalls(this.modules);
    }

    public void addSyscallHandler(ISyscall syscall){
        this.syscallHandler.addSyscall(syscall);
    }

    public void addSyscall(int no, String name, int argsize,ISCallBack callBack){
        addSyscallHandler(new Syscall1(no,name,argsize,callBack));
    }


    public void initSyscalls(Modules modules){
        addSyscall(0x5B, "munmap", 2, this::munmap);
        addSyscall(0xDC, "madvise", 3, this::skip);
        addSyscall(0x7D, "mprotect", 3, this::mprotect);
        addSyscall(0xC0, "mmap2", 6, this::mmap2);

        addSyscall(0xB, "execve", 3, this::execve);
        addSyscall(0x43, "sigaction", 3, this::skip);
        addSyscall(0x48, "sigsuspend", 3, this::skip);
        addSyscall(0x14, "getpid", 0, this::getPid);
        addSyscall(0x4E, "gettimeofday", 2,this::getTimeOfDay);

        addSyscall(0x72, "wait4", 4,this::wait4);
        addSyscall(0xAC, "prctl", 5,this::prctl);

        addSyscall(0xAF, "sigprocmask", 3,this::skip);
        addSyscall(0xBE, "vfork", 0,this::vfork);
        addSyscall(0xF0, "futex", 6,this::futex);

        addSyscall(0xF8, "exit_group", 1, this::exitgroup);
        addSyscall(0x107, "clock_gettime", 2, this::clockgettime);

        addSyscall(0x119, "socket", 3, this::socket);
        addSyscall(0x11a, "bind", 3, this::bind);
        addSyscall(0x11b, "connect", 3, this::connect);

        addSyscall(0x14e, "faccessat", 4, this::faccessat);
        addSyscall(0x159, "getcpu", 3, this::getcpu);
        addSyscall(0x14, "getpid", 0, this::getpid);
        addSyscall(0xE0, "gettid", 0, this::gettid);
        addSyscall(0x180, "getrandom", 3, this::getrandom);

        this.clock_start = TimeHelper.currentSeconds();
        this.clock_offset = 1500L;
        this.socket_idx = 0x100000L;
        this.sockets = new HashMap<>();

    }

    private CallRet mmap2(Unicorn uc, String name, long[] args) throws Exception {
        CallRet ret = new CallRet();
        ret.hasReturn = true;

        long addr = args[0];
        long size = args[1];
        int prot = (int) args[2];
        int flag = (int) args[3];
        long fd = args[4];
        long offset = args[5];


        Logger.info(String.format("mmap2 ,addr:%x,size:%d,prot:%x,flag:%x,fd:%x,offset:%x", addr,size,prot,flag,fd,offset));

        //需要新开辟一段空间

        if ((flag & 0x10) != 0){
            MemChunk c = MemUtils.align(addr,size,true);
            if (c.addr == addr){
                memoryManager.protect(c.addr,c.size,prot);
                ret.value = c.addr;
            }else {
                ret.value = -1L;
            }
        }else {
            ret.value =memoryManager.mmap(size, prot);
            // set errno
            long errno_addr = modules.findCSymbol("__errno");
            MemoryAccess.write_u32(uc,errno_addr,0);
        }

        return ret;
    }

    private CallRet mprotect(Unicorn uc, String name, long[] args) {
        MemChunk c = MemUtils.align(args[0],args[1],true);
        CallRet ret = new CallRet();
        ret.hasReturn = true;

        if (c.addr == args[0]){
            memoryManager.protect(c.addr,c.size, (int) args[2]);
            ret.value = 0L;

        }else {
            ret.value = -1L;
        }


        return ret;
    }

    private CallRet munmap(Unicorn uc, String name, long[] args) {
        memoryManager.unmap(args[0], args[1]);

        CallRet ret = new CallRet();
        ret.hasReturn = false;
        ret.value = 0;

        return ret;
    }

    private CallRet execve(Unicorn uc, String name, long[] args) {
        String pathname = MemoryHelper.readUTF8(uc,args[0]);
        long argv_addr = args[1];

        StringBuffer sb = new StringBuffer();
        while (true){
            long earg = MemoryHelper.readPtr32(uc,argv_addr);
            if (earg == 0 ){
                break;
            }

            String str = MemoryHelper.readUTF8(uc,argv_addr);
            sb.append(str);
            sb.append(" ");
            argv_addr += 4;
        }

        // set errno
        long errno_addr = modules.findCSymbol("__errno");
        MemoryAccess.write_u32(uc,errno_addr,13);

        Logger.warning(String.format("Exec %s %s", pathname,sb.toString()));

        CallRet ret = new CallRet();
        ret.hasReturn = true;
        ret.value = 0;

        return ret;
    }

    private CallRet getrandom(Unicorn uc, String name, long[] args) {
        long buffer = args[0];
        long count = args[1];

        byte [] rand = new byte[(int) count];
        Random r = new Random();
        r.nextBytes(rand);

        MemoryHelper.writeBytes(uc,buffer,rand);

        return new CallRet(0);
    }

    private CallRet getpid(Unicorn uc, String name, long[] args) {

        return new CallRet(21458L);
    }

    private CallRet getcpu(Unicorn uc, String name, long[] args) {

        if (args[0] != 0){
            MemoryAccess.write_u32(uc,args[0],1L);
        }

        return new CallRet(0);
    }

    private CallRet faccessat(Unicorn uc, String name, long[] args) {
        String file = MemoryHelper.readUTF8(uc,args[1]);

        return new CallRet(0);
    }

    /*
    *  If the connection or binding succeeds, zero is returned.
        On error, -1 is returned, and errno is set appropriately.
    * */
    private CallRet connect(Unicorn uc, String name, long[] args) throws Exception{
//        throw new Exception("NotImplementedError");

        return new CallRet(0);
    }

    private CallRet bind(Unicorn uc, String name, long[] args) throws Exception {
        SocketInfo socketInfo = this.sockets.get(args[0]);

        if (socketInfo == null){
            throw new Exception("Expected a socket");
        }

        if (socketInfo.domain != SocketInfo.AF_UNIX && socketInfo.domain != SocketInfo.SOCK_STREAM){
            throw new Exception("Unexpected socket domain / type.");
        }

        // The struct is confusing..

        long addr = args[1];
        long addr_len = args[2];

        byte [] bytes = uc.mem_read(addr+3,addr_len-3);
        socketInfo.addr = new String(bytes);
        Logger.info(String.format("Binding socket to ://%s", socketInfo.addr));

        return new CallRet(0);
    }

    private CallRet socket(Unicorn uc, String name, long[] args) {
        long socket_id = this.socket_idx +1;
        SocketInfo socketInfo = new SocketInfo();
        socketInfo.domain = args[0];
        socketInfo.type = args[1];
        socketInfo.protocol = args[2];

        this.sockets.put(socket_id,socketInfo);
        this.socket_idx = socket_id;

        return new CallRet(socket_id);

    }


    /*
    * The functions clock_gettime() retrieve the time of the specified clock clk_id.

      The clk_id argument is the identifier of the particular clock on which to act. A clock may be system-wide and
      hence visible for all processes, or per-process if it measures time only within a single process.

      clock_gettime(), clock_settime() and clock_getres() return 0 for success, or -1 for failure (in which case
      errno is set appropriately).
    * */
    private CallRet clockgettime(Unicorn uc, String name, long[] args)throws Exception {
        int clkid = (int) args[0];
        long tp_ptr = args[1];

        if (clkid == LinuxConst.CLOCK_REALTIME){
            long time = TimeHelper.currentSeconds();
            MemoryAccess.write_u32(uc,tp_ptr,time);
            MemoryAccess.write_u32(uc,tp_ptr+4,0);
            return new CallRet(0);
        }else if(clkid == LinuxConst.CLOCK_MONOTONIC || clkid == LinuxConst.CLOCK_MONOTONIC_COARSE){
            if (OVERRIDE_CLOCK){
                MemoryAccess.write_u32(uc,tp_ptr,OVERRIDE_CLOCK_TIME);
                MemoryAccess.write_u32(uc,tp_ptr+4,0);
            }else {
                long clock_add = TimeHelper.currentSeconds()- this.clock_start;

                MemoryAccess.write_u32(uc,tp_ptr,clock_add);
                MemoryAccess.write_u32(uc,tp_ptr+4,0);
            }
            return new CallRet(0);
        }

        throw new Exception(String.format("Unsupported clk_id: %d (%x)", clkid,clkid));
    }

    private CallRet exitgroup(Unicorn uc, String name, long[] args) throws Exception {
        if (this.fork != null){
            long pid = this.fork.pid;
            this.fork.loadState();
            this.fork = null;
            return new CallRet(pid);
        }

        throw new Exception(String.format("Application shutdown all threads, status %x", args[0]));

    }

    private CallRet futex(Unicorn unicorn, String name, long[] args) {
//        """
//      See: https://linux.die.net/man/2/futex
//      """

        return new CallRet(0);
    }

    /*
    * Upon successful completion, vfork() shall return 0 to the child process
        and return the process ID of the child process to the parent process.

        Otherwise, -1 shall be returned to the parent, no child process shall be created,
        and errno shall be set to indicate the error.
    *
    * */
    private CallRet vfork(Unicorn uc, String s, long[] longs) throws Exception {

        if (this.fork != null){
            throw  new Exception("Already forked.");
        }

        this.fork = new ForkInfo(uc,getPid(uc,null,null).value+1);

//      # Current execution becomes the fork, save all registers so we can return to vfork later for the main process.
//       # See exit_group.

        this.fork.saveState();

        return new CallRet(0);

    }


    private CallRet gettid(Unicorn unicorn, String s, long[] longs) {
        return new CallRet(0x2211L);
    }


    private CallRet skip(Unicorn uc,String name,long[] args){
        Logger.warning(String.format("Skipping syscall: %s ", name));

        CallRet ret = new CallRet();
        ret.hasReturn = true;
        ret.value = 0;

        return ret;
    }

    private CallRet getPid(Unicorn uc,String name,long[] args){
        CallRet ret = new CallRet();
        ret.hasReturn = true;
        ret.value = 23456;

        return ret;
    }

    /*
    * #include <sys/time.h>
    int gettimeofday(struct timeval*tv, struct timezone *tz);
    *
    * */
    private CallRet getTimeOfDay(Unicorn uc,String name,long[] args){
        long tv = args[0];
        long tz = args[1];

        if (tv != 0){
            if (OVERRIDE_TIMEOFDAY){
                MemoryAccess.write_u32(uc,tv,OVERRIDE_TIMEOFDAY_SEC);
                MemoryAccess.write_u32(uc,tv+4,OVERRIDE_TIMEOFDAY_USEC);
            }else {
                long current = System.nanoTime();

                //获取微秒
                long usec = (current / 1000) % 1000;
                // 获取秒
                long sec = (current / 1000000L);
                MemoryAccess.write_u32(uc,tv,sec);
                MemoryAccess.write_u32(uc,tv+4,usec);
            }
        }

        if (tz != 0){
            MemoryAccess.write_u32(uc,tz,-120L);
            MemoryAccess.write_u32(uc,tz+4,0);
        }


        CallRet ret = new CallRet();
        ret.hasReturn = true;
        ret.value = 0;

        return ret;
    }

    /*
      on success, returns the process ID of the terminated child; on error, -1 is returned.
    */
    private CallRet wait4(Unicorn unicorn, String s, long[] args) {
        long upid = args[0];

        CallRet ret = new CallRet();
        ret.hasReturn = true;
        ret.value = upid;

        return ret;
    }

    private CallRet prctl(Unicorn unicorn, String s, long[] args) throws Exception {
        long option = args[0];

        long PR_SET_VMA = 0x53564d41L;

        CallRet ret = new CallRet();
        ret.hasReturn = true;

        if (option == PR_SET_VMA){
            ret.value = 0;
        }else {
            throw new Exception(String.format("Unsupported prctl option %d (0x%x)", option,option));
        }

        return ret;
    }
}
