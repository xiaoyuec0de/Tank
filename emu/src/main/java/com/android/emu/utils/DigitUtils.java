package com.android.emu.utils;

public class DigitUtils {

    /**
     * 将int转为高字节在前，低字节在后的byte数组（大端）
     * @param n int
     * @return byte[]
     */
    public static byte[] intToByteBig(int n) {
        byte[] b = new byte[4];
        b[3] = (byte) (n & 0xff);
        b[2] = (byte) (n >> 8 & 0xff);
        b[1] = (byte) (n >> 16 & 0xff);
        b[0] = (byte) (n >> 24 & 0xff);
        return b;
    }
    /**
     * 将int转为低字节在前，高字节在后的byte数组（小端）
     * @param n int
     * @return byte[]
     */
    public static byte[] intToByteLittle(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }
    /**
     * byte数组到int的转换(小端)
     * @param bytes
     * @return
     */
    public static int bytes2IntLittle(byte[] bytes )
    {
        int int1=bytes[0]&0xff;
        int int2=(bytes[1]&0xff)<<8;
        int int3=(bytes[2]&0xff)<<16;
        int int4=(bytes[3]&0xff)<<24;

        return int1|int2|int3|int4;
    }
    /**
     * byte数组到int的转换(大端)
     * @param bytes
     * @return
     */
    public static int bytes2IntBig(byte[] bytes )
    {
        int int1=bytes[3]&0xff;
        int int2=(bytes[2]&0xff)<<8;
        int int3=(bytes[1]&0xff)<<16;
        int int4=(bytes[0]&0xff)<<24;

        return int1|int2|int3|int4;
    }
    /**
     * 将short转为高字节在前，低字节在后的byte数组（大端）
     * @param n short
     * @return byte[]
     */
    public static byte[] shortToByteBig(short n) {
        byte[] b = new byte[2];
        b[1] = (byte) (n & 0xff);
        b[0] = (byte) (n >> 8 & 0xff);
        return b;
    }

    /**
     * 将short转为低字节在前，高字节在后的byte数组(小端)
     * @param n short
     * @return byte[]
     */
    public static byte[] shortToByteLittle(short n) {
        byte[] b = new byte[2];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        return b;
    }
    /**
     *  读取小端byte数组为short
     * @param b
     * @return
     */
    public static short byteToShortLittle(byte[] b) {
        return (short) (((b[1] << 8) | b[0] & 0xff));
    }
    /**
     *  读取大端byte数组为short
     * @param b
     * @return
     */
    public static short byteToShortBig(byte[] b) {
        return (short) (((b[0] << 8) | b[1] & 0xff));
    }
    /**
     * long类型转byte[] (大端)
     * @param n
     * @return
     */
    public static byte[] longToBytesBig(long n) {
        byte[] b = new byte[8];
        b[7] = (byte) (n & 0xff);
        b[6] = (byte) (n >> 8  & 0xff);
        b[5] = (byte) (n >> 16 & 0xff);
        b[4] = (byte) (n >> 24 & 0xff);
        b[3] = (byte) (n >> 32 & 0xff);
        b[2] = (byte) (n >> 40 & 0xff);
        b[1] = (byte) (n >> 48 & 0xff);
        b[0] = (byte) (n >> 56 & 0xff);
        return b;
    }
    /**
     * long类型转byte[] (小端)
     * @param n
     * @return
     */
    public static byte[] longToBytesLittle(long n) {
        byte[] b = new byte[8];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8  & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        b[4] = (byte) (n >> 32 & 0xff);
        b[5] = (byte) (n >> 40 & 0xff);
        b[6] = (byte) (n >> 48 & 0xff);
        b[7] = (byte) (n >> 56 & 0xff);
        return b;
    }
    /**
     * byte[]转long类型(小端)
     * @param array
     * @return
     */
    public static long bytesToLongLittle( byte[] array )
    {
        return ((((long) array[ 0] & 0xff) << 0)
                | (((long) array[ 1] & 0xff) << 8)
                | (((long) array[ 2] & 0xff) << 16)
                | (((long) array[ 3] & 0xff) << 24)
                | (((long) array[ 4] & 0xff) << 32)
                | (((long) array[ 5] & 0xff) << 40)
                | (((long) array[ 6] & 0xff) << 48)
                | (((long) array[ 7] & 0xff) << 56));
    }

    /**
     * byte[]转long类型(大端)
     * @param array
     * @return
     */
    public static long bytesToLongBig( byte[] array )
    {
        return ((((long) array[ 0] & 0xff) << 56)
                | (((long) array[ 1] & 0xff) << 48)
                | (((long) array[ 2] & 0xff) << 40)
                | (((long) array[ 3] & 0xff) << 32)
                | (((long) array[ 4] & 0xff) << 24)
                | (((long) array[ 5] & 0xff) << 16)
                | (((long) array[ 6] & 0xff) << 8)
                | (((long) array[ 7] & 0xff) << 0));
    }

    /*
    *
    * 复制出来前几个字节
    * */
    public static byte [] cut(byte [] array,int size){
        byte [] bytes = new byte[size];
        System.arraycopy(array,0,bytes,0,size);

        return bytes;
    }

    /*
    * 获取前四个字节
    * 用于32位机器
    * 小端字节
    * */
    public static byte [] long2bytes(long n,int size){
        byte[] b = new byte[size];

        for (int i=0;i<size;i++){
            b[i] = (byte) (n >> (i*8) & 0xff);
        }

        return b;

    }

    public static long bytes2Long(byte[] array)
    {
        long value = 0;
        for (int i=0;i<array.length;i++){
            value |= (array[i] & 0xff) << (i*8);
        }

        return value;
    }


}
