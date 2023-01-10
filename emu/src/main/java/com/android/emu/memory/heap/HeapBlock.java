package com.android.emu.memory.heap;

public class HeapBlock {

    private long address;
    private long size;
    private boolean free;

    private HeapBlock next;

    public long getAddress() {
        return address;
    }

    public void setAddress(long address) {
        this.address = address;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public HeapBlock getNext() {
        return next;
    }

    public void setNext(HeapBlock next) {
        this.next = next;
    }



    public HeapBlock(){
    }
}
