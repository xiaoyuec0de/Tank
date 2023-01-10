package com.android.emu.jenv.mirror;


import com.emu.log.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class JClass extends JObject{

    private HashMap<Long, JField> fields;
    private HashMap<Long, JMethod> methods;
    private HashMap<Long, JConstructor> constructors;

    private JClass parent;

    public JClass(Class klass){
        super(klass);
        register();
        setJClass(this);
    }
    /*
    * 从java原生类解析出JField和JMethod
    *
    * */
    private void register() {
//        if (klass.getSuperclass() != null){
//            parent = new JClass(klass.getSuperclass());
//            parent.register();
//        }
        this.fields = new HashMap<>();
        this.methods = new HashMap<>();
        this.constructors = new HashMap<>();
        //装载成员函数
        Field[] fields = klass.getDeclaredFields();
        for (Field field:fields){
            JField jField = new JField(klass,field);
            jField.setJClass(this);
            this.fields.put(jField.getIndex(),jField);
        }

        Constructor<?> [] constructors = klass.getDeclaredConstructors();
        for (Constructor<?> constructor: constructors){
            JConstructor jConstructor = new JConstructor(klass,constructor);
            jConstructor.setJClass(this);
            this.constructors.put(jConstructor.getIndex(),jConstructor);
        }

        addDefaultConstructor(klass);



        //装载方法成员
        Method[] methods = klass.getDeclaredMethods();
        for (Method method: methods){
            JMethod jMethod = new JMethod(klass,method);
            jMethod.setJClass(this);
            this.methods.put(jMethod.getIndex(),jMethod);
        }
    }

    /*
    * 添加默认的构造函数
    * */
    private void addDefaultConstructor(Class klass) {
        boolean hasDefault = false;
        for(JConstructor c : constructors.values()){
            if (c.getSignature().equals("()V")){
                hasDefault = true;
                break;
            }
        }

        if (!hasDefault){
            JConstructor constructor = new JConstructor(klass);
            constructors.put(constructor.getIndex(),constructor);
        }

    }

    public JMethod findMethod(String name, String signature){
        for(JMethod m:methods.values()){
            if (m.match(name,signature)){
                return m;
            }
        }

        return null;
    }


    public JMethod findMethod(long reference){
        return methods.get(reference);
    }

    public JConstructor findConstructor(String name, String signature){
        for(JConstructor c:constructors.values()){
            if (c.match(name,signature)){
                return c;
            }
        }

        return null;
    }


    public JConstructor findConstructor(long reference){
        return constructors.get(reference);
    }

    public JField findField(String name, String signature){
        for(JField f :fields.values()){
            if (f.match(name,signature)){
                return f;
            }
        }

        return null;
    }

    /*
     * 先从父类开始寻找
     * 再从本类寻找
     * */
    public JField findField(long reference){
        JField field = null;
        if (this.parent != null){
            field= this.parent.findField(reference);
        }

        if (field == null){
            field = fields.get(reference);
        }

        return field;
    }

    public String getName() {
        return klass.getName();
    }

    /*
     * 函数名及签名
     * 函数的UC内存地址
     * */
    public void registerNative(String name,String signature,long ptr){
        boolean found = false;
        JMethod fm = null;

        Logger.info(String.format("registerNative:%s,%s,%x", name,signature,ptr));

        for (JMethod m : methods.values()){
            if (m.match(name,signature)){
                m.setPtr(ptr);
                found = true;
                fm=m;
                break;
            }
        }

        if (found){
            Logger.info(String.format("Register native function ('%s', '%s') to %s.%s", name,signature,this.getName(),fm.getName()));
        }else {
            Logger.error(String.format("Register native ('%s', '%s') failed on class %s.", name,signature,this.getName()));
        }
    }

    public boolean isInstanceOf(JClass clzz){
        String name = getName();
        String o = clzz.getName();

        return name.equals(o);
    }

    /*
    * 创建一个JObject，模拟器内部的对象，都是JObject
    *
    * */
    public JObject newInstance() throws Exception{
        Object o = klass.newInstance();
        JObject object = new JObject(klass);
        object.setJClass(this);
        object.setValue(o);

        return object;
    }


}
