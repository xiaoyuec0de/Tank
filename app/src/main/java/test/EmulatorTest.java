package test;

import com.android.emu.Emulator;
import com.android.emu.helper.NativeCallHelper;
import com.android.emu.cenv.func.NativeFunction;
import com.android.emu.cenv.syscall.CallRet;
import com.android.emu.module.Module;
import com.android.emu.utils.AlignUtils;
import com.emu.log.Logger;
import com.android.emu.utils.Utils;

import unicorn.CodeHook;
import unicorn.Unicorn;

public class EmulatorTest {

    public void test(){
        Emulator emulator = new Emulator();
//        emulator.loadLibrary("/data/local/tmp/libnative-lib_jni.so",false);
//        emulator.loadLibrary("/data/local/tmp/libthook.so",false);
        emulator.loadLibrary("/data/local/tmp/libc.so",true);

    }

    private static class MyCodeHook implements CodeHook {
        public void hook(Unicorn u, long address, int size, Object user_data) {
            byte [] ins = u.mem_read(address,size);
            String hex = Utils.bytesToHexString(ins);
            Logger.info(String.format(">>> Tracing instruction at 0x%x, instruction size = 0x%x,instruction:%s\n", address, size,hex));
        }
    }





    public void testSample(){
        Emulator emulator = new Emulator();

        emulator.loadLibrary("/data/local/tmp/libc.so",false);

        Module module = emulator.loadLibrary("/data/local/tmp/libsample.so",true);

//        emulator.getUc().hook_add(new MyCodeHook(),module.getLoadBase(),module.getSize(),null);

//        Long ret = emulator.callSymbol(module,"add",259L,345L);
//
//        Logger.info(String.format("emulator add:%d", ret));
//        ret = emulator.callSymbol(module,"mul",3L,3L);
//
//        Logger.info(String.format("emulator mul:%d", ret));

        String name = "func1";
        long addr = AlignUtils.xorBit(module.findSymbolAddr(name) );

        if (addr > 0 ) {
            emulator.addFuncHook(new NativeFunction(addr,name,2) {
                @Override
                public CallRet callback(Unicorn uc) {
                    long [] args = NativeCallHelper.readArgs(uc,getArgSize());

                    Logger.info(String.format("hook call: %s", getName()));
                    Logger.info(String.format("args:%d,%d", args[0],args[1]));

                    CallRet ret = new CallRet();
                    ret.hasReturn = false;
                    ret.value = 0;

                    return ret;
                }
            });
        }

        Long ret = emulator.callSymbol(module,name,259L,3L);

        Logger.info(String.format("emulator func1 ret:%d", ret));

        emulator.getUc().emu_stop();
    }
}
