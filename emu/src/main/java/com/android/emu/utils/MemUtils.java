package com.android.emu.utils;

import static com.android.emu.memory.MemConst.UC_MEM_ALIGN;

import com.android.emu.memory.MemChunk;

public class MemUtils {

    /*
     * 初始位置向下对齐，尾端位置向后对齐
     * */
    public static MemChunk align(long addr, long size, boolean growl){

        long start = (addr / UC_MEM_ALIGN) * UC_MEM_ALIGN;

        if (growl){
            long m = (addr + size) / UC_MEM_ALIGN + 1;
            long end = m * UC_MEM_ALIGN;
            size = end - start;
        }

        return new MemChunk(start,size);
    }


}
