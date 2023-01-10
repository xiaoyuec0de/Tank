package com.android.emu.cenv.syscall;

import unicorn.Unicorn;

public interface ISyscall {

    Integer getNo();
    String getName();
    int getArgSize();

    CallRet callback(Unicorn uc,long[] args) throws Exception;
}
