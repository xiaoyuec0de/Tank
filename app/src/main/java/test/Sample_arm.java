package test;/* Unicorn Emulator Engine */
/* By Nguyen Anh Quynh, 2015 */

/* Sample code to demonstrate how to emulate ARM code */

import com.emu.debug.DebugUtils;

import unicorn.BlockHook;
import unicorn.CodeHook;
import unicorn.Unicorn;

public class Sample_arm {

   // code to be emulated
   public static final byte[] ARM_CODE = {55,0,(byte)0xa0,(byte)0xe3,3,16,66,(byte)0xe0}; // mov r0, #0x37; sub r1, r2, r3
   public static final byte[] THUMB_CODE = {(byte)0x83, (byte)0xb0}; // sub    sp, #0xc
   public static final byte[] THUMB_CODE1 = {(byte) 0xD3, (byte) 0xE8, (byte) 0xEF, 0x4F}; // ldaex	r4, [r3]
    public static final byte[] THUMB_CODE2 = {(byte) 0xC3, (byte) 0xE8, (byte) 0xE5,0x2F}; // stlex	r5, r2, [r3]

   // memory address where emulation starts
   public static final int ADDRESS = 0x10000;

   public static final long toInt(byte val[]) {
      long res = 0;
      for (int i = 0; i < val.length; i++) {
         long v = val[i] & 0xff;
         res = res + (v << (i * 8));
      }
      return res;
   }

   private static class MyBlockHook implements BlockHook {
      public void hook(Unicorn u, long address, int size, Object user_data)
      {
          System.out.print(String.format(">>> Tracing basic block at 0x%x, block size = 0x%x\n", address, size));
      }
   }
      
   // callback for tracing instruction
   private static class MyCodeHook implements CodeHook {
      public void hook(Unicorn u, long address, int size, Object user_data) {
       System.out.print(String.format(">>> Tracing instruction at 0x%x, instruction size = 0x%x\n", address, size));   
      }
   }
   
   public static void test_arm()
   {
   
       Long r0 = 0x1234L; // R0 register
       Long r2 = 0x6789L; // R1 register
       Long r3 = 0x3333L; // R2 register
       Long r1;     // R1 register
   
       System.out.print("Emulate ARM code\n");
   
       // Initialize emulator in ARM mode
       Unicorn u = new Unicorn(Unicorn.UC_ARCH_ARM, Unicorn.UC_MODE_ARM|Unicorn.UC_MODE_THUMB);
   
       // map 2MB memory for this emulation
       u.mem_map(ADDRESS, 2 * 1024 * 1024, Unicorn.UC_PROT_ALL);
   
       // write machine code to be emulated to memory
       u.mem_write(ADDRESS, ARM_CODE);
   
       // initialize machine registers
       u.reg_write(Unicorn.UC_ARM_REG_R0, r0);
       u.reg_write(Unicorn.UC_ARM_REG_R2, r2);
       u.reg_write(Unicorn.UC_ARM_REG_R3, r3);
   
       // tracing all basic blocks with customized callback
       u.hook_add(new MyBlockHook(), 1, 0, null);
   
       // tracing one instruction at ADDRESS with customized callback
       u.hook_add(new MyCodeHook(), ADDRESS, ADDRESS, null);
   
       // emulate machine code in infinite time (last param = 0), or when
       // finishing all the code.
       u.emu_start(ADDRESS, ADDRESS + ARM_CODE.length, 0, 0);
   
       // now print out some registers
       System.out.print(">>> Emulation done. Below is the CPU context\n");
   
       r0 = (Long)u.reg_read(Unicorn.UC_ARM_REG_R0);
       r1 = (Long)u.reg_read(Unicorn.UC_ARM_REG_R1);
       System.out.print(String.format(">>> R0 = 0x%x\n", r0.intValue()));
       System.out.print(String.format(">>> R1 = 0x%x\n", r1.intValue()));
   
       u.close();
   }

    public static void test_thumb()
   {
   
       Long sp = 0x1234L; // R0 register
   
       System.out.print("Emulate THUMB code\n");
   
       // Initialize emulator in ARM mode
       Unicorn u = new Unicorn(Unicorn.UC_ARCH_ARM, Unicorn.UC_MODE_THUMB);
   
       // map 2MB memory for this emulation
       u.mem_map(ADDRESS, 2 * 1024 * 1024, Unicorn.UC_PROT_ALL);
   
       // write machine code to be emulated to memory
       u.mem_write(ADDRESS, THUMB_CODE);
   
       // initialize machine registers
       u.reg_write(Unicorn.UC_ARM_REG_SP, sp);
   
       // tracing all basic blocks with customized callback
       u.hook_add(new MyBlockHook(), 1, 0, null);
   
       // tracing one instruction at ADDRESS with customized callback
       u.hook_add(new MyCodeHook(), ADDRESS, ADDRESS, null);
   
       // emulate machine code in infinite time (last param = 0), or when
       // finishing all the code.
       u.emu_start(ADDRESS | 1, ADDRESS + THUMB_CODE.length, 0, 0);
   
       // now print out some registers
       System.out.print(">>> Emulation done. Below is the CPU context\n");
   
       sp = (Long)u.reg_read(Unicorn.UC_ARM_REG_SP);
       System.out.print(String.format(">>> SP = 0x%x\n", sp.intValue()));
   
       u.close();
   }

    public static void test_thumb1()
    {

        Long sp = 0x1234L; // R0 register

        System.out.print("Emulate THUMB code\n");

        // Initialize emulator in ARM mode
        Unicorn u = new Unicorn(Unicorn.UC_ARCH_ARM, Unicorn.UC_MODE_THUMB);

        // map 2MB memory for this emulation
        u.mem_map(ADDRESS, 2 * 1024 * 1024, Unicorn.UC_PROT_ALL);

        // write machine code to be emulated to memory
        u.mem_write(ADDRESS, THUMB_CODE1);

        // initialize machine registers
        u.reg_write(Unicorn.UC_ARM_REG_SP, sp);

        // tracing all basic blocks with customized callback
//        u.hook_add(new MyBlockHook(), 1, 0, null);

        // tracing one instruction at ADDRESS with customized callback
//        u.hook_add(new MyCodeHook(), ADDRESS, ADDRESS, null);

        u.hook_add(DebugUtils.blockHook,ADDRESS,ADDRESS,null);

        // emulate machine code in infinite time (last param = 0), or when
        // finishing all the code.
        long hold = ADDRESS+100;
        byte [] values = {1,1,1,1};
        u.mem_write(hold,values);
        u.reg_write(Unicorn.UC_ARM_REG_R3,hold);

        u.emu_start(ADDRESS | 1, ADDRESS + THUMB_CODE2.length, 0, 0);

        // now print out some registers
        System.out.print(">>> Emulation done. Below is the CPU context\n");

        sp = (Long)u.reg_read(Unicorn.UC_ARM_REG_R4);
        System.out.print(String.format(">>> R4 = 0x%x\n", sp.intValue()));

        u.close();
    }
   
   public static void main(String args[])
   {
       test_arm();
       System.out.print("==========================\n");
       test_thumb();   
   }

}
