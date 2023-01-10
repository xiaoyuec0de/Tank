package com.android.emu.jenv.mirror;

import com.android.emu.Emulator;
import com.android.emu.jenv.art.AsmClass;
import com.android.emu.jenv.art.JModifier;
import com.emu.log.Logger;

import java.lang.reflect.Method;

public class JMethod extends JExecutable{

    private Method method;
    private long ptr;

    public JMethod(Class klass, Method method) {
        super(klass);
        this.method = method;
    }

    public boolean match(String name, String signature) {
        String fsig = AsmClass.getSignature(method);
        String fname = method.getName();
        return fname.equals(name) && fsig.equals(signature);
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

    public boolean isNative(){
        return JModifier.isNative(method.getModifiers());
    }


    public void setPtr(long ptr) {
        this.ptr = ptr;
    }

    /*
    * 或许需要找到Object与Reference的一一对应关系
    *
    * */
    public Object invoke(Object thiz, Object[] args) throws Exception {
        return method.invoke(thiz, args);
    }

    public long invoke(Emulator emulator,Long[] args) throws Exception {
        return emulator.callNative(this.ptr,args);
    }

}
