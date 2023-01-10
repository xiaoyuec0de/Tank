package com.android.emu.vfs;

import com.android.emu.helper.MemoryHelper;
import com.android.emu.utils.FileUtils;
import com.emu.log.Logger;
import com.emu.vm.RootDir;

import org.json.JSONObject;

import java.io.File;
import unicorn.Unicorn;

public class FileStatHelper {

    public static JSONObject getStat(String name)throws Exception{
        String meta_name = name+".meta_emu";
        Logger.info(String.format("获取文件映射：%s", meta_name));
        String vpath = RootDir.getInstance().getVirtualFile(meta_name);
        Logger.info(String.format("虚拟文件映射：%s", vpath));
        File file = new File(vpath);
        if (!file.exists()){
            throw new Exception(String.format("文件不存在：%s", vpath));
        }
        String str = FileUtils.readString(vpath);

        JSONObject jb =  new JSONObject(str);
//        Logger.info(jb.toString(4));

        return jb;
    }

    public static void writeStat2Memory(Unicorn uc,JSONObject stat, long ptr,boolean writeTimes)throws Exception{
        MemoryHelper.writeValue(uc, ptr,stat.getLong("st_dev"),8);
        MemoryHelper.writeValue(uc, ptr + 8, 0,4) ; // PAD 4
        MemoryHelper.writeValue(uc, ptr + 12, stat.getLong("__st_ino"),4);
        MemoryHelper.writeValue(uc, ptr + 16, stat.getLong("st_mode"),4);
        MemoryHelper.writeValue(uc, ptr + 20, stat.getLong("st_nlink"),4);
        MemoryHelper.writeValue(uc, ptr + 24, stat.getLong("st_uid"),4);
        MemoryHelper.writeValue(uc, ptr + 28, stat.getLong("st_gid"),4);
        MemoryHelper.writeValue(uc, ptr + 32, stat.getLong("st_rdev"),8);
        MemoryHelper.writeValue(uc, ptr + 40, 0,4) ; // PAD 4
        MemoryHelper.writeValue(uc, ptr + 44, 0,4) ; // PAD 4
        MemoryHelper.writeValue(uc, ptr + 48, stat.getLong("st_size"),8);
        MemoryHelper.writeValue(uc, ptr + 56, stat.getLong("st_blksize"),4);
        MemoryHelper.writeValue(uc, ptr + 60, 0,4) ; // PAD 4
        MemoryHelper.writeValue(uc, ptr + 64, stat.getLong("st_blocks"),8);

        if (writeTimes) {
           MemoryHelper.writeValue(uc, ptr + 72, stat.getLong("st_atime"),4);
           MemoryHelper.writeValue(uc, ptr + 76, stat.getLong("st_atime_ns"),4);
           MemoryHelper.writeValue(uc, ptr + 80, stat.getLong("st_mtime"),4);
           MemoryHelper.writeValue(uc, ptr + 84, stat.getLong("st_mtime_ns"),4);
           MemoryHelper.writeValue(uc, ptr + 88, stat.getLong("st_ctime"),4);
           MemoryHelper.writeValue(uc, ptr + 92, stat.getLong("st_ctime_ns"),4);
        } else {
           MemoryHelper.writeValue(uc, ptr + 72, 0,4) ; // PAD 4
           MemoryHelper.writeValue(uc, ptr + 76, 0,4) ; // PAD 4
           MemoryHelper.writeValue(uc, ptr + 80, 0,4) ; // PAD 4
           MemoryHelper.writeValue(uc, ptr + 84, 0,4) ; // PAD 4
           MemoryHelper.writeValue(uc, ptr + 88, 0,4) ; // PAD 4
           MemoryHelper.writeValue(uc, ptr + 92, 0,4) ; // PAD 4
        }

        MemoryHelper.writeValue(uc, ptr +96,stat.getLong("st_ino"),8);

    }
}
