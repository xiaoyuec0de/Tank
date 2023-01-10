package test;

import android.content.Context;

import com.android.emu.Emulator;
import com.android.emu.helper.RegisterHelper;
import com.android.emu.jenv.Jvm;
import com.android.emu.jenv.mirror.JObject;
import com.android.emu.module.Module;
import com.android.emu.utils.Utils;
import com.emu.log.Logger;
import com.emu.vm.RootDir;

import sample.JniTest;
import sample.ObjectA;
import sample.Sample;
import unicorn.ArmConst;
import unicorn.CodeHook;
import unicorn.Unicorn;

public class JTest {

    private static class MyCodeHook implements CodeHook {
        public void hook(Unicorn u, long address, int size, Object user_data) {
            byte [] ins = u.mem_read(address,size);
            String hex = Utils.bytesToHexString(ins);
            long r0 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R0);
            long r1 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R1);
            long r2 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R2);
            long r3 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R3);

            Logger.info(String.format(">>> Tracing instruction at 0x%x, instruction size = 0x%x,instruction:%s\n", address, size,hex));
            Logger.info(String.format("R0:%x,R1:%x,R2:%x,R3:%x", r0,r1,r2,r3));
        }
    }

    public static void test(Context context) throws Exception {
        RootDir rootDir = RootDir.getInstance();
        rootDir.deleteRootFile("root/test/libsample.so");
        String dl = rootDir.getRootFile2("root/system/lib/libdl.so");
        String libc = rootDir.getRootFile2("root/system/lib/libc.so");
        String stdc = rootDir.getRootFile2("root/system/lib/libstdc++.so");
        String libm = rootDir.getRootFile2("root/system/lib/libm.so");
        String sample = rootDir.getRootFile2("root/test/libsample.so");

        Emulator emulator = new Emulator();

        emulator.loadClass(Sample.class);
        emulator.loadClass(ObjectA.class);
        emulator.loadClass(JniTest.class);

//        emulator.loadLibrary(dl,false);
        emulator.loadLibrary(libc,false);
//        emulator.loadLibrary(stdc,false);
//        emulator.loadLibrary(libm,false);

        Module module = emulator.loadLibrary(sample,true);
//        emulator.getUc().hook_add(new MyCodeHook(),JVM_MEMORY_BASE,JVM_MEMORY_SIZE,null);

//        String name = "func2";
//        Long ret = emulator.callSymbol(module,name,emulator.getJvm().getPtr());

        String name = "JNI_OnLoad";
        Long ret = emulator.callSymbol(module,name,emulator.getJvm().getPtr(),0L);

        Logger.info(String.format("emulator func1 ret:%d", ret));

        name = "Java_sample_Sample_func3";
        ret = emulator.callSymbol(module,name,emulator.getJniEnv().getPtr(),0L);

        Logger.info(String.format("emulator func3 ret:%d", ret));

        //
        Jvm jvm = emulator.getJvm();
        JObject n = jvm.newInstance(Sample.class);

        int a = (int) jvm.getFiledValue(n,"s","I");
        Logger.info(String.format("jvm field s:%d", a));

        int b = (int) jvm.callMethod(n,"aTest","()I");

        Logger.info(String.format("jvm call aTest:%d", b));

        name = "Java_sample_JniTest_testObjectArray";
        ret = emulator.callSymbol(module,name,emulator.getJniEnv().getPtr(),0L);

        name = "Java_sample_JniTest_testBooleanArray";
        ret = emulator.callSymbol(module,name,emulator.getJniEnv().getPtr(),0L);

        emulator.getUc().emu_stop();

    }
}
