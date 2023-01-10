package com.android.emu.memory;

import com.android.emu.utils.MemUtils;


/*
增加型内存分配器
*/
public class IncrementalAllocator {

    private long pos;
    private long end;

    //初始化内存分配器的位置
    public IncrementalAllocator(long start,long end){
        this.pos = start;
        this.end = end;
    }

    //按照4k每页进行分配，保持对齐
    public MemChunk reserve(long size) throws MemError {

        MemChunk chunk = MemUtils.align(0,size,true);

        long size_aligned = chunk.size;

        long ret = this.pos;
        this.pos += size_aligned;

        if (ret > this.end){
            throw new MemError("空间不足");
        }

        return new MemChunk(ret,size_aligned);
    }


}
