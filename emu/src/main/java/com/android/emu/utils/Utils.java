package com.android.emu.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class Utils {


    /** Returns val represented by the specified number of hex digits. */
    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    public static String toUUID(byte[] data) {
        if (data == null) {
            return null;
        }

        long msb = 0;
        long lsb = 0;
        assert data.length == 16 : "data must be 16 bytes in length";
        for (int i=0; i<8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i=8; i<16; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);
        long mostSigBits = msb;
        long leastSigBits = lsb;

        return (digits(mostSigBits >> 32, 8) + "-" +
                digits(mostSigBits >> 16, 4) + "-" +
                digits(mostSigBits, 4) + "-" +
                digits(leastSigBits >> 48, 4) + "-" +
                digits(leastSigBits, 12)).toUpperCase();
    }

    /**
     * Reads an signed integer from {@code buffer}.
     */
    public static long readSignedLeb128(ByteBuffer buffer, int size) {
        int shift = 0;
        long value = 0;
        long b;
        do {
            b = buffer.get() & 0xff;
            value |= ((b & 0x7f) << shift);
            shift += 7;
        } while((b & 0x80) != 0);

        if (shift < size && ((b & 0x40) != 0)) {
            value |= -(1L << shift);
        }

        return value;
    }

    public static BigInteger readULEB128(ByteBuffer buffer) {
        BigInteger result = BigInteger.ZERO;
        int shift = 0;
        while (true) {
            byte b = buffer.get();
            result = result.or(BigInteger.valueOf(b & 0x7f).shiftLeft(shift));
            if ((b & 0x80) == 0) {
                break;
            }
            shift += 7;
        }
        return result;
    }

    public static ByteBuffer mapBuffer(File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file); FileChannel channel = inputStream.getChannel()) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
        }
    }

    public static File getClassLocation(Class<?> clazz) {
        return new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath());
    }

    public static long parseNumber(String str) {
        if (str.startsWith("0x")) {
            return Long.parseLong(str.substring(2).trim(), 16);
        } else {
            return Long.parseLong(str);
        }
    }

    public static String decodeVectorRegister(byte[] data) {
        if (data.length != 16) {
            throw new IllegalStateException("data.length=" + data.length);
        }
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(data);
        buffer.flip();
        boolean twoDouble = false;
        for (int i = 8; i < 16; i++) {
            if (data[i] != 0) {
                twoDouble = true;
                break;
            }
        }
        if (twoDouble) {
            return String.format("(%s, %s)", buffer.getDouble(), buffer.getDouble());
        }

        boolean isDouble = false;
        for (int i = 4; i < 8; i++) {
            if (data[i] != 0) {
                isDouble = true;
                break;
            }
        }
        return String.format("(%s)", isDouble ? buffer.getDouble() : buffer.getFloat());
    }

    public static String bytesToHexString(byte[] bArray) {
        StringBuilder sb = new StringBuilder(bArray.length);
        String sTemp;
        for (byte b : bArray) {
            sb.append(" ");
            sb.append(String.format("%02x", b).toUpperCase());
        }
        return sb.toString();
    }

}
