package com.android.emu.cenv.syscalls;

import com.android.emu.cenv.syscall.CallRet;

import unicorn.Unicorn;

public interface ISCallBack {

    CallRet callback(Unicorn uc,String name,long[] args) throws Exception;
}
