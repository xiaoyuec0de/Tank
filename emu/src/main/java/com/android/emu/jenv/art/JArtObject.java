package com.android.emu.jenv.art;

import java.lang.reflect.Type;

public class JArtObject {

    private static long clzzIdx=0xFF01L;
    private static long fieldIdx=0xd2000000L;
    private static long methodIdx=0xe2000000L;
    private static long objectIdx=0xf2000000L;

    protected int modifier;
    protected long idx;
    protected String name;
    protected String signature;
    protected Type type;

    private boolean ignore;

    private Object value;

    public JArtObject(){

    }

    public JArtObject(String name, String signature, int modifier){
        this.name = name;
        this.signature = signature;
        this.modifier = modifier;
    }

    public JArtObject clone(){
        JArtObject n = new JArtObject();
        n.name = this.name;
        n.signature = this.signature;
        n.modifier =this.modifier;
        initObjectInternal();
        return n;
    }

    public Object getValue(){
        return this.value;
    }

    public void setValue(Object value){
        this.value = value;
    }

    public void setIdx(long idx){
        this.idx = idx;
    }

    public long getIndex(){
        return idx;
    }

    public String getName(){
        return name;
    }

    public boolean isIgnore(){
        return ignore;
    }

    public String getSignature(){
        return signature;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    protected void initClassInternal(){
        this.idx = clzzIdx;
        clzzIdx++;
    }

    protected void initFieldInternal(){
        this.idx = fieldIdx;
        fieldIdx +=4;
    }

    protected void initMethodInternal(){
        this.idx = methodIdx;
        methodIdx +=4;
    }
    protected void initObjectInternal(){
        this.idx = objectIdx;
        objectIdx +=4;
    }

    public int getModifier(){
        return modifier;
    }

    public boolean equal(JArtObject f){
        return name.equals(f.name) && signature.equals(f.signature);
    }

    public boolean equal(String name,String signature){
        return this.name.equals(name) && this.signature.equals(signature);
    }

    public boolean equal(String name,String signature,boolean is_static){
        return this.name.equals(name) && this.signature.equals(signature) && JModifier.isStatic(this.modifier) == is_static;
    }
}
