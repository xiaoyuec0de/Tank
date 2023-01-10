package com.android.emu.helper;

import com.android.emu.cenv.proto.CString;
import com.android.emu.utils.DigitUtils;

import java.io.ByteArrayOutputStream;

import unicorn.Unicorn;

public class MemoryHelper {

    public static byte [] hexDump(Unicorn uc,long addr,long size){
       return uc.mem_read(addr,size);
    }

    public static long readPtr32(Unicorn uc,long addr){
        return MemoryAccess.read_u32(uc,addr);
    }

    public static void writePtr32(Unicorn uc,long addr,long value){
        MemoryAccess.write_u32(uc,addr,value);
    }

    public static void writeInt32(Unicorn uc,long addr,long value){
        MemoryAccess.write_u32(uc,addr,value);
    }

    public static void writeValue(Unicorn uc,long addr,long value,int size){
        MemoryAccess.write(uc,addr,value,size);
    }

    public static byte [] readByteArray(Unicorn uc,long addr,long size){
        return uc.mem_read(addr,size);
    }

    public static long readLong(Unicorn uc,long addr,long size){
        byte [] bb = uc.mem_read(addr,size);
        return DigitUtils.bytes2Long(bb);
    }

    public static String readUTF8(Unicorn uc,long addr){
        long bufferAddr = addr;
        int size = 32;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        while (true){
            byte [] buffer = uc.mem_read(bufferAddr,size);
            int np = nullPos(buffer);

            // 发现了结束符号
            if (np < buffer.length){
                bos.write(buffer,0,np);
                break;
            }else {
                bos.write(buffer,0,buffer.length);
            }

            bufferAddr += size;
        }

        byte [] utf = bos.toByteArray();
        String ret = null;
        try {
            ret = new String(utf, "UTF-8");
        }catch (Exception e){
            e.printStackTrace();
        }
//       String utf1 = bos.toString("utf-8");


        return ret;
    }

    private static int nullPos(byte [] buffer){
        int size = buffer.length;
        for (int i=0;i<size;i++){
            byte b = (byte) (buffer[i] & 0xFF);
            if (b == 0){
                return i;
            }
        }

        return size;

    }

    public static void writeUTF8(Unicorn uc,long addr,String value){
        try {
            byte[] bytes = value.getBytes("UTF-8");

            byte [] n_bytes = new byte[bytes.length+1];
            System.arraycopy(bytes,0,n_bytes,0,bytes.length);
            n_bytes[bytes.length] = 0;

            uc.mem_write(addr,n_bytes);


        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static CString readCString(Unicorn uc, long addr){
        long bufferAddr = addr;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        while (true){
            byte [] buffer = uc.mem_read(bufferAddr,1);
            if (buffer[0] == 0){
                break;
            }

            bos.write(buffer,0,1);
            bufferAddr += 1;
        }

        byte [] data = bos.toByteArray();

        CString cstr = new CString();
        cstr.addr = addr;
        cstr.data = data;
        cstr.len = data.length;

        return cstr;
    }

    public static void writeCString(Unicorn uc,CString cstr){
        byte [] n_bytes = new byte[cstr.len + 1];
        System.arraycopy(cstr.data,0,n_bytes,0,cstr.data.length);
        n_bytes[cstr.data.length] = 0;

        uc.mem_write(cstr.addr,n_bytes);
    }


    public static void writeBytes(Unicorn uc, long buffer, byte[] bytes) {
        uc.mem_write(buffer,bytes);
    }
}
