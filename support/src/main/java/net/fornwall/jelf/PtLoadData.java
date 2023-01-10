package net.fornwall.jelf;


import java.nio.ByteBuffer;

public class PtLoadData {

    private final ByteBuffer buffer;

    PtLoadData(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public byte [] getData(long size){
        byte [] data = new byte[(int) size];
        buffer.get(data);
        return data;
    }


}
