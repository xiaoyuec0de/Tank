package com.android.emu.utils;

public class HexUtils {

    //16进制字符串转byte数组
    public static byte[] Hex2Byte(String inHex) {

        String[] hex=inHex.split(" ");//将接收的字符串按空格分割成数组
        byte[] byteArray=new byte[hex.length];

        for(int i=0;i<hex.length;i++) {
            //parseInt()方法用于将字符串参数作为有符号的n进制整数进行解析
            byteArray[i]=(byte)Integer.parseInt(hex[i],16);
        }

        return byteArray;

    }

    //byte数组转16进制字符串
    public static String Byte2Hex(byte[] inByte) {

        StringBuilder sb=new StringBuilder();
        String hexString;

        for(int i=0;i<inByte.length;i++) {

            //toHexString方法用于将16进制参数转换成无符号整数值的字符串
            String hex=Integer.toHexString(inByte[i]);

            if(hex.length()==1) {
                sb.append("0");//当16进制为个位数时，在前面补0
            }
            sb.append(hex);//将16进制加入字符串
            sb.append(" ");//16进制字符串后补空格区分开

        }

        hexString=sb.toString();
        hexString=hexString.toUpperCase();//将16进制字符串中的字母大写

        return hexString;

    }

}
