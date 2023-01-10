package com.android.emu.jenv.mirror;

import com.android.emu.jenv.art.AsmClass;

import java.lang.reflect.Constructor;

public class JConstructor extends JExecutable{

    private Constructor constructor;
    private String signature;
    private boolean defaultConstructor;

    public JConstructor(Class klass, Constructor constructor) {
        super(klass);
        this.constructor = constructor;
        this.defaultConstructor = false;
    }

    public JConstructor(Class klass){
        super(klass);
        this.signature="()V";
        this.defaultConstructor = true;
    }

    public boolean match(String name, String signature) {
        String sig = getSignature();
        return "<init>".equals(name) && sig.equals(signature);
    }

    public String getName() {
        return constructor.getName();
    }

    public String getSignature(){
        if (defaultConstructor){
            return signature;
        }
        return AsmClass.getSignature(constructor)+"V";
    }

    public Object newInstance(Object[] args) throws Exception{
        if (defaultConstructor){
            return klass.newInstance();
        }
        return constructor.newInstance(args);
    }

    public Class<?> [] getParameterTypes(){
        return constructor.getParameterTypes();
    }
}
