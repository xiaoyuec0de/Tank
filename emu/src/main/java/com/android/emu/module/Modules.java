package com.android.emu.module;

import static com.android.emu.cons.ArmConst.R_ARM_ABS32;
import static com.android.emu.cons.ArmConst.R_ARM_GLOB_DAT;
import static com.android.emu.cons.ArmConst.R_ARM_JUMP_SLOT;
import static com.android.emu.cons.ArmConst.R_ARM_RELATIVE;

import android.annotation.SuppressLint;

import com.android.emu.Emulator;
import com.emu.log.Logger;
import com.android.emu.memory.MemChunk;
import com.android.emu.memory.MemError;
import com.android.emu.utils.MemUtils;
import com.android.emu.memory.MemoryManager;
import com.android.emu.utils.DigitUtils;
import com.android.emu.utils.FileUtils;

import net.fornwall.jelf.ElfDynamicStructure;
import net.fornwall.jelf.ElfFile;
import net.fornwall.jelf.ElfInitArray;
import net.fornwall.jelf.ElfRelocation;
import net.fornwall.jelf.ElfSection;
import net.fornwall.jelf.ElfSegment;
import net.fornwall.jelf.ElfSymbol;
import net.fornwall.jelf.PtLoadData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class Modules {

    public static final long SYMBOL_UNDEF = -1;


    private Emulator emulator;
    private ArrayList<Module> modules;

    //被hook的符号表
    private HashMap<String,Long> hookedSymbols;

    public Modules(Emulator emulator){
        this.emulator = emulator;
        this.modules = new ArrayList<>();

        this.hookedSymbols = new HashMap<>();

    }

    //加载模块
    public Module loadModule(String fileName) throws IOException, MemError {
        Logger.info(String.format("加载模块:%s", fileName));

        Module module = loadElfModule(fileName);
        this.modules.add(module);

        return module;

    }

    public void addSymbolHook(String name,long addr){
        this.hookedSymbols.put(name,addr);
    }

    /*
    * so加载过程
    * 1、加载load段
    * 2、解析重定位段，dyn
    *
    *
    * */
    private Module loadElfModule(String name) throws IOException, MemError {
//        boolean dynamic = this.elfFile.
        Module module  = new Module(name);
        ByteBuffer buffer = FileUtils.readFile(name);

        ElfFile elfFile = ElfFile.fromBuffer(buffer);

        //判断是否是so共享库文件
        if (elfFile.file_type != ElfFile.FT_DYN){
            Logger.error("仅支持SO文件");
            return null;
        }

        //根据segments段，来加载so到内存
        writeSegmentsToMemory(elfFile,module);

        //4.解析重定位section
//        ElfSection relSection = null;
//        for (ElfSection section:elfFile.getSections()){
//            if (section.type == ElfSection.SHT_REL || section.type == ElfSection.SHT_RELA){
//                relSection = section;
//                break;
//            }
//        }

        //5、解析Section Header # Parse section header (Linking view).
//        ElfSection dynsym = elfFile.getDynamicSymbolTableSection();
//        ElfStringTable dynstr = elfFile.getDynamicStringTable();

        // 6、找到init_array # Find init array.
        resloveInitArray(elfFile,module);

        // 7、解析所有符号表
        resloveDynSymbols(elfFile,module);
        relocateDynSymbols(elfFile,module);

        Logger.info(String.format("模块加载完成:%s", name));

        return module;
    }

    //进行动态符号表的重定位
    private void relocateDynSymbols(ElfFile elfFile, Module module) throws IOException {
        ArrayList<ElfSection> sections = elfFile.getSections();
        for (ElfSection section : sections){
            //解析重定位表
            if (section.type == ElfSection.SHT_REL){
                relocateDynSymbol(section,module);
            }else if (section.type == ElfSection.SHT_RELA){
                relocateDynSymbol(section,module);
            }

        }


    }

    //进行重定位
    @SuppressLint("DefaultLocale")
    private void relocateDynSymbol(ElfSection section, Module module) throws IOException{
        int num = section.getNumberOfRelocations();

        for (int i=0;i<num;i++){
            ElfRelocation relocation = section.getELFRelocation(i);
            ElfSymbol symbol = relocation.symbol();
            long relAddr = module.getLoadBase() + relocation.offset();

            //修改内存中的重定位段的值
            if (relocation.type() == R_ARM_ABS32){
                //从内存中读取值
                long offset = DigitUtils.bytes2IntLittle(this.emulator.getUc().mem_read(relAddr,4));
                //替换值
                //64位不合适
                long value = offset + module.getLoadBase() + symbol.value;
                //写入内存
                byte [] vbytes = DigitUtils.longToBytesLittle(value);
                byte [] mbytes = DigitUtils.cut(vbytes,4);
                this.emulator.getUc().mem_write(relAddr,mbytes);
//                Logger.info(String.format("绝对重定位完成:%s,%x,%x", symbol.getName(),relAddr,value));
            }else if (relocation.type() == R_ARM_JUMP_SLOT || relocation.type() == R_ARM_GLOB_DAT){
                String name = symbol.getName();
                ReslovedSymbol rs = module.lookupSymbol(name);
                if (rs != null) {
                    byte [] vbytes = DigitUtils.longToBytesLittle(rs.address);
                    byte [] mbytes = DigitUtils.cut(vbytes,4);
                    this.emulator.getUc().mem_write(relAddr,mbytes);
//                    Logger.info(String.format("全局重定位完成:%d,%s,%x,%x", i, name, relAddr,rs.address));
                }

            }else if (relocation.type() == R_ARM_RELATIVE){
                if (symbol.value == 0){
//                    # Load address at which it was linked originally.
                    long offset = DigitUtils.bytes2IntLittle(this.emulator.getUc().mem_read(relAddr,4));
                    long value = module.getLoadBase() + offset;
                    byte [] vbytes = DigitUtils.longToBytesLittle(value);
                    byte [] mbytes = DigitUtils.cut(vbytes,4);
                    this.emulator.getUc().mem_write(relAddr,mbytes);
//                    Logger.info(String.format("相对重定位完成:%d,%x,%x",i,relAddr,value));

                }else {
                   Logger.error(String.format("重定位错误:%s", symbol.getName()));
                }
            }else {
               // Logger.error(String.format("未知重定位:%s", symbol.getName()));
            }
        }
    }

    /*
    * 解析动态符号表
    * 1、解析动态符号表，包括变量符号 函数符号
    * */
    private void resloveDynSymbols(ElfFile elfFile, Module module) throws IOException {
        ArrayList<ElfSection> sections = elfFile.getSections();

        for (ElfSection section : sections){
            //解析动态链接符号表
            if (section.type == ElfSection.SHT_DYNSYM){
                resloveSectionSymbols(section,module);
            }else if (section.type == ElfSection.SHT_SYMTAB){
                resloveSectionSymbols(section,module);
            }

        }
//        ElfSection section = elfFile.getDynamicSymbolTableSection();
//        resloveSectionSymbols(section,module);

    }

    //根据动态加载服务号表，解析需要加载的符号
    private void resloveSectionSymbols(ElfSection section,Module module) throws IOException {
        int size = section.getNumberOfSymbols();
        for (int i=0;i<size;i++){
            ElfSymbol sym = section.getELFSymbol(i);
            long symbolAddress = getSymValue(module.getLoadBase(),sym);
            if (symbolAddress != SYMBOL_UNDEF) {
                ReslovedSymbol reslovedSymbol = new ReslovedSymbol(symbolAddress, sym);
                module.addSymbol(sym.getName(), reslovedSymbol);
            }
        }
    }

    //获取符号在内存中的位置
    private long getSymValue(long loadBase,ElfSymbol symbol) throws IOException {
        String name = symbol.getName();
        if (name == null){
            return SYMBOL_UNDEF;
        }
        if (hookedSymbols.containsKey(name)){
            return hookedSymbols.get(name);
        }

        if (symbol.isUndef()){
            // External symbol, lookup value. 外部符号，需要从lookup模块获取
            ReslovedSymbol target = lookupSymbol(name);
            if (target == null){
                if (symbol.getBinding() == ElfSymbol.BINDING_WEAK){
                    return 0;
                }else {
                    Logger.error(String.format("未定义的外部符号:%s", name));
                    return SYMBOL_UNDEF;
                }
            }else {
                return target.address;
            }
        }else{
            // 内部符号 及绝对符号，都是这样的取值
            return loadBase + symbol.value;
        }

    }

    /*
     * 解析init array
     * */
    private void resloveInitArray(ElfFile elfFile,Module module) throws IOException, MemError{
        long loadBase = module.getLoadBase();
        ElfSegment segment = elfFile.getDynamicSegment();
        if (segment != null) {
            ElfDynamicStructure elfDynamicStructure = segment.getDynamicStructure();
            ElfInitArray initArray = elfDynamicStructure.getInitArray();
            if (initArray != null) {
                int size = initArray.array.length;
                long [] array = new long[size];
                for (int i=0;i<size;i++){
                    array[i] = loadBase + initArray.array[i];
                }
                module.setInitArray(array);
            }
            ElfInitArray preInitArray = elfDynamicStructure.getPreInitArray();
            if (preInitArray != null){
                int size = preInitArray.array.length;
                long [] array = new long[size];
                for (int i=0;i<size;i++){
                    array[i] = loadBase + preInitArray.array[i];
                }
                module.setPreInitArray(array);
                Logger.error("请注意，出现来预加载的init_array");
            }

        }

    }

    /*
     * 加载程序段
     * */
    private void writeSegmentsToMemory(ElfFile elf, Module module) throws IOException, MemError {
        //1.根据可加载段的大小，进行在内存中的计算
        long bound_low  = 0;
        long bound_high =0;
        ArrayList<ElfSegment> loadSegments = elf.getLoadSegments();
        for (ElfSegment segment:loadSegments) {

            if (segment.mem_size == 0){
                continue;
            }

            if (bound_low > segment.virtual_address){
                bound_low = segment.virtual_address;
            }

            long high = segment.virtual_address + segment.mem_size;
            if (bound_high < high){
                bound_high = high;
            }
        }

        // 2.获取此模块加载的首地址
        long moduleSize = bound_high - bound_low;
        MemoryManager memoryManager = this.emulator.getMemoryManager();
        MemChunk chunk = memoryManager.reserve_module(moduleSize);
        long load_base = chunk.addr;

        //设置模块加载位置，以及大小
        module.setLoadBase(load_base);
        module.setSize(moduleSize);

        Logger.info(String.format("模块 %s 加载首地址:%x,结束地址:%x", module.getName(),load_base,load_base+moduleSize));

        //3.设置代码的可执行权限
        for (ElfSegment segment:loadSegments) {
            if (segment.mem_size == 0){
                continue;
            }

            //计算出需要在虚拟内存中加载的大小
            long segDstAddr = load_base + segment.virtual_address;
            MemChunk memChunk = MemUtils.align(segDstAddr,segment.mem_size,true);
            long segAddr = memChunk.addr;
            long segSize = memChunk.size;

            int prot = this.get_segment_protection(segment.flags);

            //关键一步，进行emulator的内存映射,然后把数据写进去
            this.emulator.getUc().mem_map(segAddr,segSize,prot);
            Logger.info(String.format("映射首地址:%x,结束地址:%x,大小:%x,权限:%x",segAddr,segAddr+segSize,segSize,prot));


            PtLoadData pt = segment.getPtLoadData();
            if (pt != null) {
                byte [] datas = pt.getData(segment.file_size);
                // 注意，动态加载段的偏移值是针对加载首地址的
                this.emulator.getUc().mem_write(segDstAddr, datas);
            }

        }
    }

    /*
     * 获取段的执行权限，可读可写可执行
     * */
    private static final int PF_X = 0x1 ;
    private static final int PF_W = 0x2 ;
    private static final int PF_R = 0x4;
    private int get_segment_protection(int prot_in){
        int prot = 0;

        if ((prot_in | PF_R) != 0){
            prot |= 1;
        }

        if ((prot_in | PF_W) != 0){
            prot |= 2;
        }

        if ((prot_in | PF_X) != 0){
            prot |= 4;
        }

        return prot;


    }

    public ReslovedSymbol lookupSymbol(String name){
        for (Module m : modules){
            ReslovedSymbol symbol = m.lookupSymbol(name);
            if (symbol != null){
                return symbol;
            }
        }

        return null;
    }

    public long findCSymbol(String name){
        ReslovedSymbol symbol = lookupSymbol(name);
        if (symbol != null){
            return symbol.address;
        }

        return 0;
    }


}
