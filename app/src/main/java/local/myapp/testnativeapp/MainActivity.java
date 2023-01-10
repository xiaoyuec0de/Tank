package local.myapp.testnativeapp;

import com.emu.log.Logger;

public class MainActivity {

    public native String stringFromJNI();

    public void test(){
        Logger.info("test called");
    }
}
