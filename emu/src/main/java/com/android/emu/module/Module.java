package com.android.emu.module;


import java.util.HashMap;


/*
* 对应elf文件
* 每一个so都是一个module
*
* */
public class Module {

    private String name;
    //开始地址和大小
    private long loadBase;
    private long size;
    //符号表
    private HashMap<String,ReslovedSymbol> reslovedSymbols;

    //init array 偏移值
    private long [] init_array;
    private long [] pre_init_array;

    public Module(String name){
        this.name = name;
        this.reslovedSymbols = new HashMap<>();

    }

    public void addSymbol(String name,ReslovedSymbol reslovedSymbol){
        this.reslovedSymbols.put(name,reslovedSymbol);
    }

    public long[] getInitArray() {
        return init_array;
    }

    public void setInitArray(long[] init_array) {
        this.init_array = init_array;
    }

    public long[] getPreInitArray() {
        return pre_init_array;
    }

    public void setPreInitArray(long[] pre_init_array) {
        this.pre_init_array = pre_init_array;
    }

    public String getName() {
        return name;
    }

    public long getLoadBase() {
        return loadBase;
    }

    public void setLoadBase(long loadBase) {
        this.loadBase = loadBase;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }


    public ReslovedSymbol lookupSymbol(String name) {
        if (reslovedSymbols.containsKey(name)){
            return reslovedSymbols.get(name);
        }

        return null;
    }

    public ReslovedSymbol findSymbol(String name){
        ReslovedSymbol reslovedSymbol = lookupSymbol(name);
        if (reslovedSymbol != null){
            return reslovedSymbol;
        }

        return null;
    }

    public long findSymbolAddr(String name){
        ReslovedSymbol reslovedSymbol = lookupSymbol(name);
        if (reslovedSymbol != null){
            return reslovedSymbol.address;
        }

        return 0;
    }

    public long getEndAddress() {
        return loadBase + size;
    }
}
