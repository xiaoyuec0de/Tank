package com.android.emu.cenv.func;



public abstract class NativeFunction implements IFunc{

    private long addr;
    private String name;
    private int argSize;


    public NativeFunction(long addr, String name, int argSize) {
        this.addr = addr;
        this.name = name;
        this.argSize = argSize;
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

}
