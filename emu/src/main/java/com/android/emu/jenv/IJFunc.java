package com.android.emu.jenv;

import unicorn.Unicorn;

public interface IJFunc {

    long getHookAddr();
    void setHookAddr(long addr);
    int getAcount();
    String getName();
    JRet call(Unicorn uc,long [] args) throws Exception;
}
