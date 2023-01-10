package com.emu.debug;

import com.android.emu.helper.RegisterHelper;
import com.android.emu.utils.Utils;
import com.emu.log.Logger;

import capstone.Capstone;
import unicorn.ArmConst;
import unicorn.BlockHook;
import unicorn.CodeHook;
import unicorn.Unicorn;

public class DebugUtils{
    private static Capstone capstone;
    public static CodeHook  codeHook = new CodeHook() {
        @Override
        public void hook(Unicorn u, long address, int size, Object o) {
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

            long r7 = RegisterHelper.reg_read(u, ArmConst.UC_ARM_REG_R7);
            long r8 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R8);
            long r9 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R9);
            long r10 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R10);
            long r11 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R11);
            long lr = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_LR);
            long pc = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_PC);

            Logger.info(String.format("R0:%x,R1:%x,R2:%x,R3:%x，R4:%x,R5:%x,R6:%x", r0,r1,r2,r3,r4,r5,r6));
            Logger.info(String.format("R7:%x,R8:%x,R9:%x,R10:%x，R11:%x", r7,r8,r9,r10,r11));
            Logger.info(String.format("LR:%x,PC:%x",lr,pc));
            Logger.info("____________________________________");
        }
    };

    public static BlockHook  blockHook = new BlockHook() {
        @Override
        public void hook(Unicorn u, long address, int size, Object o) {
            byte [] ins = u.mem_read(address,size);
            String hex = Utils.bytesToHexString(ins);


            Logger.info(String.format(">>> Tracing instruction at 0x%x, instruction size = 0x%x,instruction:%s\n", address, size,hex));

            if (capstone == null){
                capstone = new Capstone(Capstone.CS_ARCH_ARM,Capstone.CS_MODE_THUMB);
            }
            Capstone.CsInsn [] csi = capstone.disasm(ins,address);
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

            long r7 = RegisterHelper.reg_read(u, ArmConst.UC_ARM_REG_R7);
            long r8 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R8);
            long r9 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R9);
            long r10 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R10);
            long r11 = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_R11);
            long lr = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_LR);
            long pc = RegisterHelper.reg_read(u,ArmConst.UC_ARM_REG_PC);

            Logger.info(String.format("R0:%x,R1:%x,R2:%x,R3:%x，R4:%x,R5:%x,R6:%x", r0,r1,r2,r3,r4,r5,r6));
            Logger.info(String.format("R7:%x,R8:%x,R9:%x,R10:%x，R11:%x", r7,r8,r9,r10,r11));
            Logger.info(String.format("LR:%x,PC:%x",lr,pc));
            Logger.info("____________________________________");
        }
    };


}
