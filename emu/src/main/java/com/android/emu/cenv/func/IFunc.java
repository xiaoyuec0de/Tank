package com.android.emu.cenv.func;

import com.android.emu.cenv.syscall.CallRet;

import unicorn.Unicorn;

public interface IFunc {

    public long getAddr();
    public String getName();
    public int getArgSize();

    public CallRet callback(Unicorn uc);
}
