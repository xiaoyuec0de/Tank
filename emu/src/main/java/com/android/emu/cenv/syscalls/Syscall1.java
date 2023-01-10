package com.android.emu.cenv.syscalls;

import com.android.emu.cenv.syscall.CallRet;
import com.android.emu.cenv.syscall.ISyscall;

import unicorn.Unicorn;

public class Syscall1 implements ISyscall {
    private Integer no;
    private String name;
    private int argsize;

    private ISCallBack callBack;

    public Syscall1(int no, String name, int argsize,ISCallBack callBack){
        this.no = no;
        this.name = name;
        this.argsize = argsize;

        this.callBack = callBack;
    }

    @Override
    public Integer getNo() {
        return no;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getArgSize() {
        return argsize;
    }

    @Override
    public CallRet callback(Unicorn uc, long[] args) throws Exception {
        return callBack.callback(uc,name,args);
    }
}
