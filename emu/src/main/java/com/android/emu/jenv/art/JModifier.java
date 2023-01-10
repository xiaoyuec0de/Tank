package com.android.emu.jenv.art;

public class JModifier {

    //https://docs.oracle.com/javase/7/docs/api/constant-values.html;
    public static final int PUBLIC = 1;
    public static final int PRIVATE = 2;
    public static final int PROTECTED = 4;
    public static final int STATIC = 8;
    public static final int FINAL = 16;
    public static final int SYNCHRONIZED = 32;
    public static final int VOLATILE = 64;
    public static final int TRANSIENT = 128;
    public static final int NATIVE = 256;
    public static final int INTERFACE = 512;
    public static final int ABSTRACT = 1024;
    public static final int STRICT = 2048;
    static final int BRIDGE = 64;
    static final int VARARGS = 128;
    static final int SYNTHETIC = 4096;
    static final int ANNOTATION = 8192;
    static final int ENUM = 16384;
    static final int MANDATED = 32768;
    private static final int CLASS_MODIFIERS = 3103;
    private static final int INTERFACE_MODIFIERS = 3087;
    private static final int CONSTRUCTOR_MODIFIERS = 7;
    private static final int METHOD_MODIFIERS = 3391;
    private static final int FIELD_MODIFIERS = 223;
    private static final int PARAMETER_MODIFIERS = 16;
    static final int ACCESS_MODIFIERS = 7;

    public static boolean isPublic(int mod) {
        return (mod & 1) != 0;
    }

    public static boolean isPrivate(int mod) {
        return (mod & 2) != 0;
    }

    public static boolean isProtected(int mod) {
        return (mod & 4) != 0;
    }

    public static boolean isStatic(int mod) {
        return (mod & 8) != 0;
    }

    public static boolean isFinal(int mod) {
        return (mod & 16) != 0;
    }

    public static boolean isSynchronized(int mod) {
        return (mod & 32) != 0;
    }

    public static boolean isVolatile(int mod) {
        return (mod & 64) != 0;
    }

    public static boolean isTransient(int mod) {
        return (mod & 128) != 0;
    }

    public static boolean isNative(int mod) {
        return (mod & 256) != 0;
    }

    public static boolean isInterface(int mod) {
        return (mod & 512) != 0;
    }

    public static boolean isAbstract(int mod) {
        return (mod & 1024) != 0;
    }

    public static boolean isStrict(int mod) {
        return (mod & 2048) != 0;
    }

    public static String toString(int mod) {
        StringBuffer sj = new StringBuffer(" ");
        if ((mod & 1) != 0) {
            sj.append("public");
        }

        if ((mod & 4) != 0) {
            sj.append("protected");
        }

        if ((mod & 2) != 0) {
            sj.append("private");
        }

        if ((mod & 1024) != 0) {
            sj.append("abstract");
        }

        if ((mod & 8) != 0) {
            sj.append(" ");
            sj.append("static");
        }

        if ((mod & 16) != 0) {
            sj.append(" ");
            sj.append("final");
        }

        if ((mod & 128) != 0) {
            sj.append(" ");
            sj.append("transient");
        }

        if ((mod & 64) != 0) {
            sj.append(" ");
            sj.append("volatile");
        }

        if ((mod & 32) != 0) {
            sj.append(" ");
            sj.append("synchronized");
        }

        if ((mod & 256) != 0) {
            sj.append(" ");
            sj.append("native");
        }

        if ((mod & 2048) != 0) {
            sj.append(" ");
            sj.append("strictfp");
        }

        if ((mod & 512) != 0) {
            sj.append(" ");
            sj.append("interface");
        }

        return sj.toString();
    }

    static boolean isSynthetic(int mod) {
        return (mod & 4096) != 0;
    }

    static boolean isMandated(int mod) {
        return (mod & '耀') != 0;
    }

    public static int classModifiers() {
        return 3103;
    }

    public static int interfaceModifiers() {
        return 3087;
    }

    public static int constructorModifiers() {
        return 7;
    }

    public static int methodModifiers() {
        return 3391;
    }

    public static int fieldModifiers() {
        return 223;
    }

    public static int parameterModifiers() {
        return 16;
    }


}
