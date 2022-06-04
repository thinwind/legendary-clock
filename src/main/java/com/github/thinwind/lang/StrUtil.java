/*
 * Copyright 2022 Shang Yehua <niceshang@outlook.com>
 */
package com.github.thinwind.lang;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 *
 * TODO StrUtil说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2022-05-20  08:29
 *
 */
public final class StrUtil {
    private StrUtil() {};

    final static String[] ZEROS = {"", "0", "00", "000", "0000", "00000", "000000", "0000000",
            "00000000", "000000000", "0000000000", "00000000000", "000000000000", "0000000000000",
            "00000000000000", "000000000000000"};

    final static String[] SPACES = {"", " ", "  ", "   ", "    ", "     ", "      ", "       ",
            "        ", "         ", "          ", "           ", "            ", "             ",
            "              ", "               "};
    private final static Encoder base64Encoder = Base64.getEncoder();

    private final static Decoder base64Decoder = Base64.getDecoder();

    /**
     * 将字符串转成base64编码的字符串
     * 
     * @param data 要编码的数据
     * @return 使用ASCII编码的base64字符串
     */
    public static String toBase64String(byte[] data) {
        return BitUtil.toAsciiString(base64Encoder.encode(data));
    }

    /**
     * 将base64字符串解码为byte[]
     * 
     * @param data 要解码的数据
     * @return 解码后的字节数组
     */
    public static byte[] base64Decode(String data) {
        return base64Decoder.decode(BitUtil.toAsciiBytes(data));
    }

    public static String fillLeftWithZero(String str, int length) {
        return fillingLeftWithSignArray(str, length, ZEROS);
    }

    public static String fillLeftWithSpace(String str, int length) {
        return fillingLeftWithSignArray(str, length, SPACES);
    }

    public static String fillRightWithZero(String str, int length) {
        return fillingRightWithSignArray(str, length, ZEROS);
    }

    public static String fillRightWithSpace(String str, int length) {
        return fillingRightWithSignArray(str, length, SPACES);
    }

    /**
     * 填充字符串到目标长度
     * 
     * @param str 原始字符串
     * @param sign 要填充的字符
     * @param length 目标长度
     * @param dir 填充方向
     * @return 填充后的字符串
     *          如果原始字符串长度大于目标长度，则返回原始字符串
     *          反之，返回填充后的字符串，长度等于目标长度
     */
    public static String fillWithSign(String str, String sign, int length, FillingDirect dir) {
        if ("0".equals(sign)) {
            if (dir == FillingDirect.LEFT) {
                return fillLeftWithZero(str, length);
            } else {
                return fillRightWithZero(str, length);
            }
        }
        if (" ".equals(sign)) {
            if (dir == FillingDirect.LEFT) {
                return fillLeftWithSpace(str, length);
            } else {
                return fillRightWithSpace(str, length);
            }
        }
        String[] fillings = {"", sign, sign + sign};
        if (dir == FillingDirect.LEFT) {
            return fillingLeftWithSignArray(str, length, fillings);
        } else {
            return fillingRightWithSignArray(str, length, fillings);
        }
    }

    static String fillingLeftWithSignArray(String str, int length, String[] signArray) {
        if (str == null) {
            str = "";
        }
        final int fillingLen = length - str.length();
        if (fillingLen <= 0) {
            return str;
        }
        final StringBuilder builder = new StringBuilder();
        while (fillingLen - builder.length() > signArray.length) {
            builder.append(signArray[signArray.length - 1]);
        }
        builder.append(signArray[fillingLen - builder.length()]);
        builder.append(str);
        return builder.toString();
    }

    static String fillingRightWithSignArray(String str, final int length, String[] signArray) {
        if (str == null) {
            str = "";
        }
        if (length <= str.length()) {
            return str;
        }
        final StringBuilder builder = new StringBuilder(str);

        while (length - builder.length() > signArray.length) {
            builder.append(signArray[signArray.length - 1]);
        }
        builder.append(signArray[length - builder.length()]);

        return builder.toString();
    }

    public static enum FillingDirect {
        LEFT, RIGHT
    }
}
