package com.android.emu.cenv.fork;

import static unicorn.ArmConst.UC_ARM_REG_ENDING;
import static unicorn.ArmConst.UC_ARM_REG_INVALID;


import unicorn.Unicorn;

public class ForkInfo {
    private Unicorn uc;
    public long pid;
    private long [] registerValues;

    public ForkInfo(Unicorn uc,long pid){
        this.uc = uc;
        this.pid = pid;
        int size = UC_ARM_REG_ENDING - UC_ARM_REG_INVALID;
        this.registerValues = new long[size];
    }

    //We are forking, so save everything there is to save.
    public void saveState(){
        for (int i= UC_ARM_REG_INVALID;i<registerValues.length;i++){
            registerValues[i]= (long) uc.reg_read(i);
        }
    }

    public void loadState(){
        for (int i= UC_ARM_REG_INVALID;i<registerValues.length;i++){
            uc.reg_write(i,registerValues[i]);
        }
    }
}
