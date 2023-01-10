package com.android.emu.cenv;



import static unicorn.ArmConst.UC_ARM_REG_LR;
import static unicorn.ArmConst.UC_ARM_REG_PC;
import static unicorn.ArmConst.UC_ARM_REG_R0;

import com.android.emu.cenv.func.IFunc;
import com.android.emu.cenv.syscall.CallRet;
import com.android.emu.utils.AlignUtils;
import com.emu.log.Logger;

import java.util.HashMap;

import unicorn.CodeHook;
import unicorn.Unicorn;

public class Hooker implements CodeHook{

    private Unicorn uc;
    private HashMap<Long,IFunc> hookers;



    public Hooker(Unicorn uc){
        this.uc = uc;
        this.hookers = new HashMap<>();
    }

    public void addFuncHook(IFunc func){
        Logger.info(String.format("函数级Hook,%x,%s", func.getAddr(),func.getName()));
        this.hookers.put(func.getAddr(),func);
        addUCHook(func);
    }

    private void addUCHook(IFunc func){
        this.uc.hook_add(this, func.getAddr(), 4,null);
    }

    @Override
    public void hook(Unicorn uc, long v, int i, Object o) {

        Long addr = AlignUtils.pc((Long) uc.reg_read(UC_ARM_REG_PC));
//        Logger.info(String.format("符号函数调用:%x", addr));
        Long lr = (Long) uc.reg_read(UC_ARM_REG_LR);
        if (hookers.containsKey(addr)){
            IFunc f = hookers.get(addr);
            assert f != null;

            Logger.info(String.format("函数调用:%s,%x", f.getName(),addr));
            CallRet ret = null;
            try {
                ret = f.callback(uc);
            } catch (Exception e) {
                e.printStackTrace();
                Logger.error(String.format("函数调用出错:%s,停止模拟器", f.getName()));
                this.uc.emu_stop();
            }

            assert ret != null;
            if (ret.hasReturn){
                Logger.info(String.format("函数调用:%s,结果:%x", f.getName(),ret.value));
                uc.reg_write(UC_ARM_REG_R0,ret.value);

                //强制写lr到pc，进行函数出来
                uc.reg_write(UC_ARM_REG_PC,lr);
            }

        }
    }
}
