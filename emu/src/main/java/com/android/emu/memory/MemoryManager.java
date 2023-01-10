package com.android.emu.memory;

import static com.android.emu.cons.EmuConst.UC_PROT_ALL;
import static com.android.emu.memory.MemConst.*;

import com.android.emu.memory.heap.HeapError;

import java.util.Random;

import unicorn.ArmConst;
import unicorn.Unicorn;

public class MemoryManager {

    private Unicorn uc;
    private IncrementalAllocator incrementalAllocator;
    private HeapAllocator heapAllocator;

    public MemoryManager(Unicorn uc){
        this.uc = uc;

        // 栈初始化
        this.uc.mem_map(STACK_ADDR,STACK_SIZE, UC_PROT_ALL);
        this.uc.reg_write(ArmConst.UC_ARM_REG_SP,STACK_ADDR+STACK_SIZE);

        // 堆初始化
        this.heapAllocator = new HeapAllocator(uc,HEAP_MIN,HEAP_MAX);

        //ELF 模块加载初始化
        this.incrementalAllocator = new IncrementalAllocator(MODULES_MIN,MODULES_MAX);

        // Hooker模块 内存初始化
        this.uc.mem_map(HOOK_MEMORY_BASE,HOOK_MEMORY_SIZE,UC_PROT_ALL);
        this.uc.mem_map(C_HOOK_MEMORY_BASE,C_HOOK_MEMORY_SIZE,UC_PROT_ALL);

        // JVM 模块初始化
        this.uc.mem_map(JVM_HOOK_MEMORY_BASE, JVM_HOOK_MEMORY_SIZE,UC_PROT_ALL);
    }

    //计算出来一个合适的内存页面大小
    public MemChunk reserve_module(long size) throws MemError {
        return this.incrementalAllocator.reserve(size);
    }

    public long allocate(long size) throws HeapError {
        return this.heapAllocator.allocate(size);
    }

    public void free(long addr) throws HeapError {
        this.heapAllocator.free(addr);
    }

    public long mmap(long size,int prot) throws MemError {
        MemChunk c = this.reserve_module(size);

        this.uc.mem_map(c.addr,c.size,prot);

        return c.addr;
    }

    public void unmap(long addr,long size){
        if (addr >= MAPPING_MIN && (addr+size) <= MAPPING_MAX){
            this.uc.mem_unmap(addr,size);
        }
    }

    public void protect(long addr,long size,int prot){
        if (addr >= MAPPING_MIN && (addr+size) <= MAPPING_MAX){
            this.uc.mem_protect(addr,size,prot);
        }
    }

    public long getHookRandomAddr(){
        Random r = new Random();
        return (long) (HOOK_MEMORY_BASE + r.nextFloat()*(HOOK_MEMORY_SIZE)) | 1;
    }

    public long getLoadRandomAddr(){
        Random r = new Random();
        return (long) (MODULES_MIN + r.nextFloat()*(MODULES_MAX-MODULES_MIN)) | 1;
    }
}
