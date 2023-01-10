package test;

import com.android.emu.jenv.IJCall;
import com.android.emu.jenv.JFunc;
import com.android.emu.jenv.JHandler;
import com.android.emu.jenv.JRet;

import unicorn.Unicorn;

public class KeystoneTest {

    public void test(){

        JHandler hooker = new JHandler(null);
        try {
            hooker.writeFunction(new JFunc("1", 1, new IJCall() {
                @Override
                public JRet call(Unicorn uc, long[] args) throws Exception {
                    return null;
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
