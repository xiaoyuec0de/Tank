package com.android.emu.helper;

import com.android.emu.utils.DigitUtils;

import unicorn.Unicorn;

/*
* 向虚拟机内存中写入和读取数据的帮助类
* */
public class MemoryAccess {

    public static long LONG_MAX = 0xFFFFFFFFL;

    public static void write(Unicorn uc,long addr,long value,int size){
        uc.mem_write(addr, DigitUtils.long2bytes(value,size));
    }
    public static void write_u8(Unicorn uc,long addr,long value){
        write(uc,addr,value,1);
    }
    public static void write_u16(Unicorn uc,long addr,long value){
        write(uc,addr,value,2);
    }
    public static void write_u32(Unicorn uc,long addr,long value){
        write(uc,addr,value,4);
    }

    public static void writeBytes(Unicorn uc,long addr,byte [] data){
        uc.mem_write(addr,data);
    }
    public static void write_u64(Unicorn uc,long addr,long value){
        write(uc,addr,value,8);
    }

    public static byte [] readBytes(Unicorn uc,long addr,int size){
        return uc.mem_read(addr,size);
    }

    public static long read(Unicorn uc,long addr,int size){
        byte [] bytes = uc.mem_read(addr,size);
        return DigitUtils.bytes2Long(bytes) & LONG_MAX;
    }

    public static long read_u8(Unicorn uc,long addr){
        return read(uc,addr,1);
    }

    public static long read_u16(Unicorn uc,long addr){
        return read(uc,addr,2);
    }

    public static long read_u32(Unicorn uc,long addr){
        return read(uc,addr,4);
    }

    public static long read_u64(Unicorn uc,long addr){
        return read(uc,addr,8);
    }
}
