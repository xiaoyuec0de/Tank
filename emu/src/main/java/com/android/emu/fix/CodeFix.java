package com.android.emu.fix;


import unicorn.CodeHook;
import unicorn.Unicorn;

public class CodeFix {

    public static CodeHook codeFix = new CodeHook() {
        @Override
        public void hook(Unicorn u, long address, int size, Object o) {
            if (size == 4) {
                byte[] ins = u.mem_read(address, size);

            }

        }
    };
}
