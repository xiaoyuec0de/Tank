package com.android.emu.module;

import net.fornwall.jelf.ElfSymbol;

public class ReslovedSymbol {

    public long address;
    public ElfSymbol symbol;

    public ReslovedSymbol(long address,ElfSymbol symbol){
        this.address = address;
        this.symbol = symbol;
    }
}
