package sample;

import com.emu.log.Logger;

public class Sample {
    private int s = 100;

    private static int a = 101;

    public native int func3();

    public native Sample call(Sample in);

    public void cTest(){
        Logger.error("cTest called from uc success");
    }

    public static void dTest(){
        Logger.error("dTest called from uc success");
    }

    public int aTest(){
        Logger.error("Sample called from uc success");
        cTest();
//        func3(); // 加上一个java层的hook，可以完成
        return 1;
    }

    public static int bTest(){
        Logger.error("bTest called from uc success");
        dTest();
//        func3(); // 加上一个java层的hook，可以完成
        return 1;
    }
}
