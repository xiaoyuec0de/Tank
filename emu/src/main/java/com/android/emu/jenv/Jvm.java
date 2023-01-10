package com.android.emu.jenv;

import com.android.emu.Emulator;
import com.android.emu.helper.MemoryAccess;
import com.android.emu.jenv.mirror.JClass;
import com.android.emu.jenv.mirror.JField;
import com.android.emu.jenv.mirror.JMethod;
import com.android.emu.jenv.mirror.JObject;
import com.emu.log.Logger;

import java.util.HashMap;

import unicorn.Unicorn;

//https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/invocation.html
public class Jvm {

    private long ptr;
    private long table;
    //JVM 结构体的函数
    private HashMap<Long, IJFunc> tables;

    private Emulator emulator;
    private Unicorn uc;
    private JniEnv jniEnv;
    private JClassLoader classLoader;
    private JHandler handler;

    public Jvm(Emulator emulator) {
        this.emulator = emulator;
        this.uc = emulator.getUc();
        this.handler = new JHandler(uc);
        this.classLoader = new JClassLoader();

        this.tables = new HashMap<>();
        initTables();

        try {
            TableRet ret = handler.writeFunctionTable(tables);
            this.ptr = ret.ptr;
            this.table = ret.table;

            this.jniEnv = new JniEnv(emulator, classLoader, handler);
            this.jniEnv.setJvm(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JniEnv getJniEnv(){
        return jniEnv;
    }

    private void addFunc(long idx, String name,int acount,IJCall callback) {
        tables.put(idx, new JFunc(name,acount,callback));
    }

    private void initTables() {
        addFunc(3,"DestroyJavaVM",3, this::DestroyJavaVM);
        addFunc(4, "AttachCurrentThread",3,this::AttachCurrentThread);
        addFunc(5,"DetachCurrentThread",3, this::DetachCurrentThread);
        addFunc(6,"GetEnv",3, this::GetEnv);
        addFunc(7,"DestroyJavaVM",3, this::AttachCurrentThreadAsDaemon);
    }

    public long getPtr(){
        return this.ptr;
    }

    public long getTable(){
        return this.table;
    }

    private JRet GetEnv(Unicorn uc,long [] args) throws Exception{
        long jvm = args[0];
        long jenv = args[1];
        int version = (int) args[2];

        Logger.info(String.format("vm:%x,env:%x,version:%d", jvm,jenv,version));

        MemoryAccess.write_u32(uc,jenv,this.jniEnv.getPtr());

        return new JRet(JConst.JNI_OK);
    }

    private JRet DetachCurrentThread(Unicorn uc,long [] args) throws Exception {
        throw new Exception("NotImplementedError");
    }

    private JRet AttachCurrentThread(Unicorn uc,long [] args) throws Exception {
        throw new Exception("NotImplementedError");
    }

    private JRet DestroyJavaVM(Unicorn uc,long [] args) throws Exception {
        throw new Exception("NotImplementedError");
    }

    private JRet AttachCurrentThreadAsDaemon(Unicorn unicorn, long[] longs) throws Exception {
        throw new Exception("NotImplementedError");
    }


    public void addClass(Class klass) {
        JClass clzz = new JClass(klass);
        this.classLoader.addClass(clzz);
    }

    public JObject newInstance(Class klass) throws Exception{
        if (!this.classLoader.contains(klass)){
            addClass(klass);
        }

        JClass clzz = this.classLoader.findClass(klass);

        return clzz.newInstance();
    }

    public Object getFiledValue(JObject object,String name,String signature) throws Exception {
        JField field = object.getJClass().findField(name,signature);
        return field.getValue(object.getValue());
    }

    public Object callMethod(JObject object,String name,String signature,Object... params) throws Exception{
        JMethod method = object.getJClass().findMethod(name,signature);

        return method.getMethod().invoke(object.getValue(),params);
    }

    public Object callNativeMethod(Emulator emulator,JObject object,String name,String signature,Object... params)throws Exception{
        return null;
    }
}
