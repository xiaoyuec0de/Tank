package com.android.emu.cenv.cpu;

import unicorn.Unicorn;

public interface IInterrupt {

    void handle(Unicorn uc);
}
