package com.android.emu.jenv.mirror;


public class JObject {


    /*
    * 1、对于Primitive类型，此处存放其值
    * 2、对于引用类型，此处存放其索引
    * */

    private static long referenceIndex = 0xFF00;

    //对象在内存中的索引号码
    private long index;
    protected Class klass;

    private JClass jClass;

    private Object value;

    public JObject(Class klass){
        this.klass = klass;
        incrementReferenceIndex();
    }


    public JClass getJClass(){
        return jClass;
    }

    public void setJClass(JClass value){
        this.jClass = value;
    }

    public Class getKlass(){
        return this.klass;
    }

    public long getIndex(){
        return this.index;
    }

    protected void incrementReferenceIndex(){
        this.index = referenceIndex;
        referenceIndex++;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value){
        this.value = value;
    }
}
