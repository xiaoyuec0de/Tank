package com.android.emu.cenv.cpu;

import com.emu.log.Logger;

import java.util.HashMap;

import unicorn.InterruptHook;
import unicorn.Unicorn;

public class InterruptHandler implements InterruptHook{

    private Unicorn uc;
    private HashMap<Integer, IInterrupt> handlers;

    public InterruptHandler(Unicorn uc){
        this.uc = uc;
        this.handlers = new HashMap<>();

        this.uc.hook_add(this,null);

    }

    public void addInterrput(Integer intno, IInterrupt h){
        handlers.put(intno,h);
        Logger.info(String.format("添加中断拦截，%d", intno));
    }

    @Override
    public void hook(Unicorn u, int intno, Object user) {
        if (handlers.containsKey(intno)){
            IInterrupt h = handlers.get(intno);
            h.handle(uc);
        }else {
            Logger.error(String.format("未知的中断号码:%d", intno));
            this.uc.emu_stop();
        }
    }
}
