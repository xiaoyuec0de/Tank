package test;

import android.content.Context;

import com.android.emu.Emulator;
import com.android.emu.module.Module;
import com.emu.vm.RootDir;

public class SampleSoTest {

    public static void test(Context context){
        RootDir rootDir = RootDir.getInstance();
        rootDir.setContext(context);
        rootDir.deleteRootFile("root/test/libsample.so");
        rootDir.deleteRootFile("root/system/lib/libc.so");

        rootDir.copyAssertDir("root");

        String dl = rootDir.getRootFile2("root/system/lib/libdl.so");
        String libc = rootDir.getRootFile2("root/system/lib/libc.so");
//        String stdc = rootDir.getRootFile2("root/system/lib/libstdc++.so");
//        String libm = rootDir.getRootFile2("root/system/lib/libm.so");
        String sample = rootDir.getRootFile2("root/test/libsample.so");

        Emulator emulator = new Emulator();

//        emulator.getUc().hook_add(DebugUtils.codeHook,MODULES_MIN,MODULES_MAX,null);

        emulator.loadLibrary(dl,true);
        Module cModule = emulator.loadLibrary(libc,false);

//        emulator.addSymbolHook("pthread_key_create", 2, uc -> new CallRet(0));
//        emulator.addSymbolHook("pthread_mutex_lock", 2, uc -> new CallRet(0));
//        emulator.addSymbolHook("pthread_mutex_unlock", 2, uc -> new CallRet(0));
//        emulator.addSymbolHook("pthread_key_create", 2, uc -> new CallRet(0));

        emulator.callModuleInit(cModule);

        emulator.loadLibrary(sample,true);

//        long ret =  emulator.callSymbol("test_mmap");
//        long ret = emulator.callNativeSymbol("mmap",0L,1024L,3L,0x22L,0L,0L);
//        Logger.info(String.format("ret:%x", ret));

        emulator.stop();
    }
}
