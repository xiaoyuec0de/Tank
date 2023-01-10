package com.android.emu.jenv;


import unicorn.Unicorn;

public class JFunc implements IJFunc {

    private int acount;
    private String name;
    private IJCall jcall;

    private long addr;

    public JFunc(String name,int acount,  IJCall jcall) {
        this.acount = acount;
        this.name = name;
        this.jcall = jcall;
    }


    @Override
    public long getHookAddr() {
        return addr;
    }

    public int getAcount(){
        return acount;
    }

    @Override
    public JRet call(Unicorn uc, long[] args) throws Exception {
        return jcall.call(uc,args);
    }

    public String getName(){return name;}

    @Override
    public void setHookAddr(long addr) {
        this.addr = addr;
    }

}
