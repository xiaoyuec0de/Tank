package test;

import android.content.Context;

import com.android.emu.Emulator;
import com.android.emu.module.Module;
import com.emu.vm.RootDir;

public class JiaGuTest {

    public static void test(Context context){
        Emulator emulator = new Emulator();
        RootDir rootDir = RootDir.getInstance();
        String dl = rootDir.getRootFile2("root/system/lib/libdl.so");
        String libc = rootDir.getRootFile2("root/system/lib/libc.so");
        String stdc = rootDir.getRootFile2("root/system/lib/libstdc++.so");
        String libm = rootDir.getRootFile2("root/system/lib/libm.so");

        String libn = rootDir.getRootFile2("root/test/libnative-lib_jni.so");

        emulator.loadLibrary(dl,false);
        emulator.loadLibrary(libc,false);
        emulator.loadLibrary(stdc,false);
        emulator.loadLibrary(libm,false);

        Module module = emulator.loadLibrary(libn,false);

        emulator.callSymbol(module,"JNI_OnLoad",0L,0L);


    }
}
