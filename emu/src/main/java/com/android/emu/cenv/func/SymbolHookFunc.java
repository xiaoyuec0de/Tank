package com.android.emu.cenv.func;

import com.android.emu.cenv.syscall.CallRet;
import com.android.emu.utils.AlignUtils;

import unicorn.Unicorn;

public class SymbolHookFunc implements IFunc{
    private long addr;
    private String name;
    private int argSize;
    private ISymbolHookFunc func;

    public SymbolHookFunc(long addr, String name, int argSize, ISymbolHookFunc func) {
        this.addr = AlignUtils.sym(addr);
        this.name = name;
        this.argSize = argSize;
        this.func = func;
    }

    @Override
    public long getAddr() {
        return addr;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getArgSize() {
        return argSize;
    }

    @Override
    public CallRet callback(Unicorn uc) {
        return func.callback(uc);
    }
}
