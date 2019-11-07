package com.kaitusoft.ratel.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author frog.w
 * @version 1.0.0, 2019/1/25
 *          <p>
 *          write description here
 */
public class Md5Util {
    protected final static char[] ALPHABET = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    protected static MessageDigest messagedigest = null;
    public static final long FILE_BLOCK = 33554432L;

    public Md5Util() {
    }

    public static String decode(String code) throws Exception {
        throw new IllegalStateException("MD5 can not restore a str");
    }

    public static String encode(String str) throws Exception {
        return encode(str, Md5Util.Type.NORMAL);
    }

    public static String encode(String str, Md5Util.Type type) throws Exception {
        String md5 = getMD5String(str.getBytes());
        return type == Md5Util.Type.NORMAL?md5:md5.substring(8, 24);
    }

    public static String encode(String str, int startPos, int len) throws Exception {
        if(startPos >= 0 && len + startPos <= str.length()) {
            return getMD5String(str.substring(startPos, startPos + len).getBytes());
        } else {
            throw new ArrayIndexOutOfBoundsException("input string:" + str + ",from " + startPos + " to " + len);
        }
    }

    public static String getMD5String(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytes);
        return bufferToHex(md.digest());
    }

    public static String encode(File f) throws Exception {
        return encode(f, 0L, f.length());
    }

    public static String encode(File f, long start, long length) throws IOException, Exception {
        if(start >= 0L && start + length <= f.length()) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream in = new FileInputStream(f);
            FileChannel ch = in.getChannel();
            int bufferSize = 16384;
            long leftLength = length;
            long cursor = start;
            MappedByteBuffer byteBuffer = null;

            while(leftLength > 0L) {
                if((long)bufferSize > leftLength) {
                    bufferSize = (int)leftLength;
                }

                byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, cursor, (long)bufferSize);
                cursor += (long)bufferSize;
                leftLength -= (long)bufferSize;
                md.update(byteBuffer);
            }

            String code = bufferToHex(md.digest());
            ch.close();
            in.close();
            return code;
        } else {
            throw new ArrayIndexOutOfBoundsException("input file:" + f.length() + ",from " + start + " to " + length);
        }
    }

    private static String bufferToHex(byte[] bytes) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte[] bytes, int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;

        for(int l = m; l < k; ++l) {
            appendHexPair(bytes[l], stringbuffer);
        }

        return stringbuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = ALPHABET[(bt & 240) >> 4];
        char c1 = ALPHABET[bt & 15];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

    public static void main(String[] args) {
        try {
            System.out.println(encode("52wonder"));
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    static {
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException var1) {
            System.err.println("初始化失败，不支持MD5。");
            var1.printStackTrace();
        }

    }

    public enum Type {
        SHORT,
        NORMAL;

        private Type() {
        }
    }
}
