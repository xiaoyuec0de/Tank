package com.android.emu.memory;

public class MemChunk {

    public long addr;
    public long size;

    public MemChunk(long addr,long size){
        this.addr = addr;
        this.size = size;
    }
}
