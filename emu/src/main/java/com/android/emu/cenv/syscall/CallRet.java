package com.android.emu.cenv.syscall;

public class CallRet {

    public boolean hasReturn;
    public long value;

    public CallRet(){

    }

    public CallRet(long value){
        this.hasReturn =true;
        this.value = value;
    }

}
