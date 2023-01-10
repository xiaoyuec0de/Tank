package com.android.emu.cenv.func;

import com.android.emu.cenv.syscall.CallRet;

import unicorn.Unicorn;

public interface ISymbolHookFunc {
    public CallRet callback(Unicorn uc);
}
