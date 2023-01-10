package com.android.emu.jenv.art;


import java.lang.reflect.Method;

public class JArtMethod{

    private Method method;

    private long native_addr;

    public JArtMethod(Method method){
        this.method = method;
    }

    public void setNativeAddr(long addr){
        this.native_addr = addr;
    }


    /*
    * 桥接虚拟java类与真实的java类
    * */
    public Object invoke(Object thiz,Object[] args) throws Exception {
        return method.invoke(thiz, args);
    }

    public boolean match(String name, String signature) {
        return method.getName().equals(name) && AsmClass.getSignature(method).equals(signature);
    }

    public String getName() {
        return method.getName();
    }

    public Class<?> getType(){
        return method.getReturnType();
    }

    public Method getMethod() {
        return method;
    }

    public boolean isStatic() {
        return JModifier.isStatic(method.getModifiers());
    }
}
