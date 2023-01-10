package com.android.emu.jenv.mirror;

public class JExecutable extends JObject{

    protected JClass delacringClass;
    protected JClass declaringClassOfOverriddenMethod;

    protected JArray parameters;

    //java 索引
    protected long artMethod;
    protected int accessFlags;
    protected long dexMethodIndex;


    public JExecutable(Class klass) {
        super(klass);
    }
}
