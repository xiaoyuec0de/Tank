﻿using Gee.External.Capstone;
using Gee.External.Capstone.X86;
using System;
using System.Diagnostics;
using System.Text;
using UnicornManaged;
using UnicornManaged.Const;

namespace UnicornSamples
{
    internal static class ShellcodeSample
    {
        private const long ADDRESS = 0x1000000;

        public static void X86Code32Self()
        {
            byte[] X86_CODE32_SELF =
            {
                0xeb, 0x1c, 0x5a, 0x89, 0xd6, 0x8b, 0x02, 0x66, 0x3d, 0xca, 0x7d, 0x75, 0x06, 0x66, 0x05, 0x03, 0x03,
                0x89, 0x02, 0xfe, 0xc2, 0x3d, 0x41, 0x41, 0x41, 0x41, 0x75, 0xe9, 0xff, 0xe6, 0xe8, 0xdf, 0xff, 0xff,
                0xff, 0x31, 0xd2, 0x6a, 0x0b, 0x58, 0x99, 0x52, 0x68, 0x2f, 0x2f, 0x73, 0x68, 0x68, 0x2f, 0x62, 0x69,
                0x6e, 0x89, 0xe3, 0x52, 0x53, 0x89, 0xe1, 0xca, 0x7d, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41
            };

            Run(X86_CODE32_SELF);
        }

        public static void X86Code32()
        {
            byte[] X86_CODE32 =
            {
                0xeb, 0x19, 0x31, 0xc0, 0x31, 0xdb, 0x31, 0xd2, 0x31, 0xc9, 0xb0, 0x04, 0xb3, 0x01, 0x59, 0xb2, 0x05,
                0xcd, 0x80, 0x31, 0xc0, 0xb0, 0x01, 0x31, 0xdb, 0xcd, 0x80, 0xe8, 0xe2, 0xff, 0xff, 0xff, 0x68, 0x65,
                0x6c, 0x6c, 0x6f
            };

            Run(X86_CODE32);
        }

        private static void Run(byte[] code)
        {
            Console.WriteLine();
            var stackTrace = new StackTrace();
            var stackFrame = stackTrace.GetFrames()[1];
            var methodName = stackFrame.GetMethod().Name;

            Console.WriteLine($"*** Start: {methodName}");
            RunTest(code, ADDRESS);
            Console.WriteLine($"*** End: {methodName}");
            Console.WriteLine();
        }


        private static void RunTest(byte[] code, long address)
        {
            try
            {
                using (var u = new Unicorn(Common.UC_ARCH_X86, Common.UC_MODE_32))
                using(var disassembler = CapstoneDisassembler.CreateX86Disassembler(X86DisassembleMode.Bit32))
                {
                    Console.WriteLine($"Unicorn version: {u.Version()}");

                    // map 2MB of memory for this emulation
                    u.MemMap(address, 2 * 1024 * 1024, Common.UC_PROT_ALL);

                    // write machine code to be emulated to memory
                    u.MemWrite(address, code);

                    // initialize machine registers
                    u.RegWrite(X86.UC_X86_REG_ESP, Utils.Int64ToBytes(address + 0x200000));

                    var regv = new byte[4];
                    u.RegRead(X86.UC_X86_REG_ESP, regv);

                    // tracing all instructions by having @begin > @end
                    u.AddCodeHook((uc, addr, size, userData) => CodeHookCallback(disassembler, uc, addr, size, userData), 1, 0);

                    // handle interrupt ourself
                    u.AddInterruptHook(InterruptHookCallback);

                    // handle SYSCALL
                    u.AddSyscallHook(SyscallHookCallback);

                    Console.WriteLine(">>> Start tracing code");

                    // emulate machine code in infinite time
                    u.EmuStart(address, address + code.Length, 0u, 0u);

                    Console.WriteLine(">>> Emulation Done!");
                }
            }
            catch (UnicornEngineException ex)
            {
                Console.Error.WriteLine("Emulation FAILED! " + ex.Message);
            }
        }

        private static void CodeHookCallback(
            CapstoneX86Disassembler disassembler,
            Unicorn u,
            long addr,
            int size,
            object userData)
        {
            Console.Write($"[+] 0x{addr:X}: ");

            var eipBuffer = new byte[4];
            u.RegRead(X86.UC_X86_REG_EIP, eipBuffer);

            var effectiveSize = Math.Min(16, size);
            var tmp = new byte[effectiveSize];
            u.MemRead(addr, tmp);

            var sb = new StringBuilder();
            foreach (var t in tmp)
            {
                sb.AppendFormat($"{(0xFF & t):X} ");
            }
            Console.Write($"{sb,-20}");
            Console.WriteLine(Utils.Disassemble(disassembler, tmp));
        }

        private static void SyscallHookCallback(Unicorn u, object userData)
        {
            var eaxBuffer = new byte[4];
            u.RegRead(X86.UC_X86_REG_EAX, eaxBuffer);
            var eax = Utils.ToInt(eaxBuffer);

            Console.WriteLine($"[!] Syscall EAX = 0x{eax:X}");

            u.EmuStop();
        }

        private static void InterruptHookCallback(Unicorn u, int intNumber, object userData)
        {
            // only handle Linux syscall
            if (intNumber != 0x80)
            {
                return;
            }

            var eaxBuffer = new byte[4];
            var eipBuffer = new byte[4];

            u.RegRead(X86.UC_X86_REG_EAX, eaxBuffer);
            u.RegRead(X86.UC_X86_REG_EIP, eipBuffer);

            var eax = Utils.ToInt(eaxBuffer);
            var eip = Utils.ToInt(eipBuffer);

            switch (eax)
            {
                default:
                    Console.WriteLine($"[!] Interrupt 0x{eip:X} num {intNumber:X}, EAX=0x{eax:X}");
                    break;
                case 1: // sys_exit
                    Console.WriteLine($"[!] Interrupt 0x{eip:X} num {intNumber:X}, SYS_EXIT");
                    u.EmuStop();
                    break;
                case 4: // sys_write

                    // ECX = buffer address
                    var ecxBuffer = new byte[4];

                    // EDX = buffer size
                    var edxBuffer = new byte[4];

                    u.RegRead(X86.UC_X86_REG_ECX, ecxBuffer);
                    u.RegRead(X86.UC_X86_REG_EDX, edxBuffer);

                    var ecx = Utils.ToInt(ecxBuffer);
                    var edx = Utils.ToInt(edxBuffer);

                    // read the buffer in
                    var size = Math.Min(256, edx);
                    var buffer = new byte[size];
                    u.MemRead(ecx, buffer);
                    var content = Encoding.Default.GetString(buffer);

                    Console.WriteLine($"[!] Interrupt 0x{eip:X}: num {ecx:X}, SYS_WRITE. buffer = 0x{edx:X}, size = {size:X}, content = '{content}'");

                    break;
            }
        }
    }
}
