package com.android.emu.jenv;

/*
* 模仿C语言的返回值
*
* */
public class JRet {

    public boolean hasReturn;
    public long value;

    public JRet(){
        this.hasReturn = false;
    }

    public JRet(long value){
        this.hasReturn = true;
        this.value = value;
    }

    public void setValue(long value) {
        this.hasReturn = true;
        this.value = value;
    }
}
