package com.android.emu.cenv.syscall;


import static unicorn.ArmConst.UC_ARM_REG_PC;
import static unicorn.ArmConst.UC_ARM_REG_R0;
import static unicorn.ArmConst.UC_ARM_REG_R7;

import com.android.emu.helper.NativeCallHelper;
import com.android.emu.cenv.cpu.IInterrupt;
import com.android.emu.utils.AlignUtils;
import com.emu.log.Logger;

import java.util.HashMap;

import unicorn.Unicorn;

/*
* 系统调用处理器
* 处理器表示的不拦截系统调用
* */
public class SyscallHandler implements IInterrupt {

    private Unicorn uc;
    private HashMap<Integer, ISyscall> syscalls;

    public SyscallHandler(Unicorn uc){
        this.uc = uc;
        // 添加中断的Hook，用于拦截系统调用
        this.syscalls = new HashMap<>();
    }

    public void addSyscall(ISyscall syscall){
        this.syscalls.put(syscall.getNo(),syscall);
    }

    @Override
    public void handle(Unicorn uc) {
        Long reg = (Long) uc.reg_read(UC_ARM_REG_R7);
        int no = reg.intValue();
        if (syscalls.containsKey(no)){
            ISyscall s = syscalls.get(no);
            assert s != null;

            int argSize = s.getArgSize();
            long [] args = NativeCallHelper.readSyscallArgs(uc,argSize);
            long pc = AlignUtils.ucRet(uc.reg_read(UC_ARM_REG_PC));

            Logger.info(String.format("进行系统调用:%s,%x", s.getName(),pc));
            CallRet ret = null;
            try {
                ret = s.callback(uc,args);
            } catch (Exception e) {
                e.printStackTrace();
                Logger.error(String.format("系统调用出错:%x,停止模拟器", no));
                this.uc.emu_stop();
            }

            assert ret != null;
            if (ret.hasReturn){
                Logger.info(String.format("系统调用:%s,结果:%x", s.getName(),ret.value));
                uc.reg_write(UC_ARM_REG_R0,ret.value);
            }

        }else {
            Logger.error(String.format("未知的系统调用号码:%x,停止模拟器", no));
            this.uc.emu_stop();
        }
    }
}
