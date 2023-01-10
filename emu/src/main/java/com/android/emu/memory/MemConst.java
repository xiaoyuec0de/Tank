package com.android.emu.memory;

public class MemConst {

//    Memory addresses

    public static final long STACK_ADDR = 0x00000000L;
    public static final long STACK_SIZE = 0x00100000L;

    public static final long HOOK_MEMORY_BASE = 0x20000000L;
    public static final long HOOK_MEMORY_SIZE = 0x00200000L;

    public static final long C_HOOK_MEMORY_BASE = 0x21000000L;
    public static final long C_HOOK_MEMORY_SIZE = 0x00200000L;

    public static final long JVM_HOOK_MEMORY_BASE = 0x30000000L;
    public static final long JVM_HOOK_MEMORY_SIZE = 0x00200000L;

    public static final long MODULES_MIN = 0xA0000000L;
    public static final long MODULES_MAX = 0xC0000000L;

    public static final long HEAP_MIN = 0xD0000000L;
    public static final long HEAP_MAX = 0xD0200000L;

    public static final long MAPPING_MIN = 0xE0000000L;
    public static final long MAPPING_MAX = 0xF0000000L;
//   Alignment
    public static final long UC_MEM_ALIGN = 0x1000L;
}
