package com.android.emu.jenv;

import unicorn.Unicorn;

public interface IJCall {

    JRet call(Unicorn uc,long[] args) throws Exception;
}
