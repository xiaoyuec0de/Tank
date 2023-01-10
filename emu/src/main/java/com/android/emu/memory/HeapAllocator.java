package com.android.emu.memory;

import static com.android.emu.cons.EmuConst.UC_PROT_READ;
import static com.android.emu.cons.EmuConst.UC_PROT_WRITE;

import com.android.emu.memory.heap.HeapBlock;
import com.android.emu.memory.heap.HeapBlockHolder;
import com.android.emu.memory.heap.HeapError;

import unicorn.Unicorn;

public class HeapAllocator {

    private long begin;
    private long end;
    private long pos;

    //
    private HeapBlock head;

    private Unicorn uc;

    public HeapAllocator(Unicorn uc,long begin,long end){
        this.uc = uc;
        this.begin = begin;
        this.end = end;
        this.pos = begin;
        this.head = null;

        this.uc.mem_map(begin,(end-begin),UC_PROT_READ | UC_PROT_WRITE);

    }

    /*
    * 分配一段空间，并且返回首地址
    * */
    public long allocate(long size) throws HeapError {
        if (size < 0){
            return 0;
        }

        HeapBlock block = null;
        if (this.head == null){
            block = createBlock(size,null);
            this.head = block;
        }else {
            // 从已有的块中寻找可用的块
            HeapBlockHolder holder = findFreeBlock(size);
            if (holder.block == null){
                block = createBlock(size,holder.prev);
            }else if (holder.block.getSize() >= size){
                block = splitBlock(holder.block,size);
            }


            block.setFree(false);
        }

        return block.getAddress();
    }

    /*
    * 进行内存块的释放
    * 尝试合并空的内存块
    * */
    public void free(long addr) throws HeapError {
        if (addr <= 0){
            return;
        }

        HeapBlockHolder holder = findBlock(addr);

        if (holder.block == null){
            throw new HeapError(String.format("Attempted to free non existing block at 0x%x", addr));
        }

        holder.block.setFree(true);

        mergeBlock(holder.block);
        mergeBlock(holder.prev);

    }


    private HeapBlockHolder findBlock(long addr) {
        HeapBlock prev = null;
        HeapBlock head = this.head;

        while(head != null){

            if (head.getAddress() == addr){
                break;
            }

            prev = head;
            head = head.getNext();
        }

        HeapBlockHolder holder = new HeapBlockHolder();
        holder.prev = prev;
        holder.block = head;

        return holder;

    }

    private void mergeBlock(HeapBlock block){
        if (block == null){
            return;
        }

        if (block.isFree()){
            HeapBlock next = block.getNext();
            if (next != null && next.isFree()){
                block.setSize(block.getSize() + next.getSize());
                block.setNext(next.getNext());
            }
        }
    }

    /*
    * 创建一个新的块，并且添加到链表中
    * */
    private HeapBlock createBlock(long size,HeapBlock prev){
        HeapBlock block = new HeapBlock();

        block.setAddress(incrementAddress(size));
        block.setSize(size);
        block.setFree(false);
        block.setNext(null);

        //添加到链表中
        if(prev != null){
            prev.setNext(block);
        }

        return block;
    }

    private HeapBlockHolder findFreeBlock(long size) {
        HeapBlock block = this.head;
        HeapBlock prev = null;

        while(block != null){
            if (block.isFree() && block.getSize() >= size){
                break;
            }
            prev = block;
            block = block.getNext();
        }

        HeapBlockHolder holder = new HeapBlockHolder();
        holder.prev = prev;
        holder.block = block;

        return holder;
    }

    /*
    * 切割已经存在的块
    *
    * */
    private HeapBlock splitBlock(HeapBlock block, long size) throws HeapError {
        if (!block.isFree()){
            throw new HeapError("Attempted to split non-free block");
        }

        HeapBlock newBlock = new HeapBlock();
        newBlock.setAddress(block.getAddress() + size);
        newBlock.setSize(block.getSize() - size);
        newBlock.setFree(true);
        newBlock.setNext(block.getNext());

        block.setNext(newBlock);
        block.setSize(size);

        return block;
    }


    private long incrementAddress(long size){
        long base = this.pos;
        this.pos += size;

        return base;
    }
}
