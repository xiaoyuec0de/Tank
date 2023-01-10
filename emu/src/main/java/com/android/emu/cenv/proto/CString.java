package com.android.emu.cenv.proto;

public class CString {

    public long addr;
    public byte [] data;
    public int len;

    public String getCString(){
        return new String(data,0,len);
    }
}
