package com.android.emu.cons;

public class ConfigConst {

    public static final long STACK_ADDR = 0x00000000;
    public static final long STACK_SIZE = 0x00100000;

    public static final long HOOK_MEMORY_BASE = 0x1000000;
    // 2 * 1024 * 1024 - 2MB
    public static final long HOOK_MEMORY_SIZE = 0x0200000;

    public static final long HEAP_BASE = 0x2000000;
    // 2 * 1024 * 1024 - 2MB
    public static final long HEAP_SIZE = 0x0200000;

    public static final long BASE_ADDR = 0xCBBCB000;

    public static final boolean WRITE_FSTAT_TIMES = true;
}
