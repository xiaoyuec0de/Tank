package com.android.emu.jenv;

import com.android.emu.jenv.art.AsmClass;
import com.android.emu.jenv.mirror.JClass;
import com.emu.log.Logger;

import java.util.HashMap;

public class JClassLoader {

    private HashMap<Long, JClass> classes;
    private HashMap<String, JClass> classes2;

    public JClassLoader(){
        this.classes = new HashMap<>();
        this.classes2 =new HashMap<>();
    }

    public void addClass(JClass clzz){
        if (classes2.containsKey(clzz.getName())){
            Logger.warning(String.format("类已经注册：%s", clzz.getName()));
            return;
        }

        classes.put(clzz.getIndex(),clzz);
        classes2.put(AsmClass.getType(clzz.getKlass()),clzz);
    }

    public JClass findClass(long idx){
        return classes.get(idx);
    }

    public JClass findClass(Class klass){
        String name = AsmClass.getType(klass);
        return this.findClass(name);
    }

    public JClass findClass(String name){
        return classes2.get(name);
    }

    public boolean contains(Class klass) {
        String name = AsmClass.getType(klass);
        JClass c = findClass(name);

        return c!= null;

    }
}
