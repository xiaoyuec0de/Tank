package tank.emu.test;

import static com.android.emu.memory.MemConst.MODULES_MAX;
import static com.android.emu.memory.MemConst.MODULES_MIN;

import android.content.Context;

import com.android.emu.Emulator;
import com.android.emu.helper.RegisterHelper;
import com.android.emu.module.Module;
import com.android.emu.utils.Utils;
import com.emu.log.Logger;
import com.emu.vm.RootDir;

import capstone.Capstone;
import local.myapp.testnativeapp.MainActivity;
import test.TestArm;
import unicorn.ArmConst;
import unicorn.CodeHook;
import unicorn.InHook;
import unicorn.Unicorn;

public class JiaGuTest {

    private static Capstone capstone;

    private static class MyCodeHook implements CodeHook {
        public void hook(Unicorn u, long address, int size, Object user_data) {
            byte [] ins = u.mem_read(address,size);
            String hex = Utils.bytesToHexString(ins);

            Logger.info(String.format(">>> Tracing instruction at 0x%x, instruction size = 0x%x,instruction:%s\n", address, size,hex));

            if (capstone == null){
                capstone = new Capstone(Capstone.CS_ARCH_ARM,Capstone.CS_MODE_THUMB);
            }
            Capstone.CsInsn [] csi = capstone.disasm(ins,0);
            for (Capstone.CsInsn c :csi){
                System.out.printf("0x%x:\t%s\t%s\n", c.address, c.mnemonic, c.opStr);
            }
//
            long r0 = RegisterHelper.reg_read(u, ArmConst.UC_ARM_REG_R0);
            long r1 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R1);
            long r2 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R2);
            long r3 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R3);
            long r4 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R4);
            long r5 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R5);
            long r6 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R6);

            Logger.info(String.format("R0:%x,R1:%x,R2:%x,R3:%x，R4:%x,R5:%x,R6:%x", r0,r1,r2,r3,r4,r5,r6));
            Logger.info("____________________________________");
        }
    }

    private static class MyInCodeHook implements InHook{

        @Override
        public int hook(Unicorn u, int address, int size,Object user) {
            long r0 = RegisterHelper.reg_read(u, ArmConst.UC_ARM_REG_R0);
            long r1 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R1);
            long r2 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R2);
            long r3 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R3);
            long pc = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_PC);

            Logger.info(String.format("PC:%x,R0:%x,R1:%x,R2:%x,R3:%x", pc,r0,r1,r2,r3));
            return 0;
        }

    }


    public static void test(Context context){
        Emulator emulator = new Emulator();

        RootDir rootDir = RootDir.getInstance();
        rootDir.setContext(context);
        rootDir.copyAssertDir("root");

        String dl = rootDir.getRootFile("root/system/lib/libdl.so");
        String conn = rootDir.getRootFile("root/system/lib/libvendorconn.so");
        String libc = rootDir.getRootFile("root/system/lib/libc.so");
        String stdc = rootDir.getRootFile("root/system/lib/libstdc++.so");
        String libm = rootDir.getRootFile("root/system/lib/libm.so");

        String libn = rootDir.getRootFile("root/test/libnative-lib_jni.so");

        emulator.loadLibrary(dl,true);
        emulator.loadLibrary(libc,true);

        emulator.loadLibrary(stdc,true);
        emulator.loadLibrary(libm,true);

        Module module = emulator.loadLibrary(libn,true);

        emulator.loadClass(MainActivity.class);

//        emulator.getUc().hook_add(new MyCodeHook(),MODULES_MIN,MODULES_MAX,null);
//        emulator.getUc().hook_add(new MyInCodeHook(),null);

        long vm_ptr = emulator.getJvm().getPtr();
        Logger.info(String.format("vmptr:%x", vm_ptr));

        long ret = emulator.callSymbol(module,"JNI_OnLoad",vm_ptr,0L);
        Logger.info(String.format("JNI_OnLoad ret:%x", ret));


//        emulator.callSymbol(module,"stringFromJNI");

        emulator.stop();



    }
}
