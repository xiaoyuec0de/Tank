package com.android.emu.cenv.syscall;

public class SyscallError extends Exception{

    public SyscallError(String msg){
        super(msg);
    }
}
