/*
 * Copyright 2022 Shang Yehua <niceshang@outlook.com>
 */
package com.github.thinwind.lang;

/**
 *
 * 一个字节数组的片段
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2022-02-08  10:27
 *
 */
public class BytesRange {

    public static final BytesRange NONE = new BytesRange(new byte[0], 0, 0);

    static {
        NONE.hexValue = "";
        NONE.bytes = NONE.data;
        NONE.hash = -1;
    }

    public final byte[] data;

    public final int offset;

    public final int length;

    private byte[] bytes;

    private String hexValue;

    private int hash;

    public static BytesRange of(byte[] data, int offset, int length) {
        if (data == null || data.length == 0) {
            return NONE;
        }

        //合法性校验
        if (offset < 0 || length < 0) {
            throw new IllegalArgumentException("offset or length is negative.");
        }
        if (data.length < offset || data.length < (offset + length)) {
            throw new IllegalArgumentException("The length of data is not long enough.");
        }

        return new BytesRange(data, offset, length);
    }

    public static BytesRange of(byte[] data) {
        if (data == null || data.length == 0) {
            return NONE;
        }
        return new BytesRange(data, 0, data.length);
    }

    public static BytesRange fromHex(String val) {
        if (val == null || val.isEmpty()) {
            return NONE;
        }
        BytesRange range = new BytesRange(BitUtil.hex2Bytes(val));
        range.hexValue = val;
        return range;
    }

    public static BytesRange join(BytesRange... ranges) {
        return join(0, ranges);
    }

    private static BytesRange join(int start, BytesRange... ranges) {
        if (ranges == null || ranges.length == 0 || start >= ranges.length) {
            return NONE;
        }
        if (ranges.length - start == 1) {
            return ranges[start];
        }
        BytesRange acc = ranges[start];
        for (int i = start + 1; i < ranges.length; i++) {
            if (acc.data == ranges[i].data) {
                acc = acc.join(ranges[i]);
            } else {
                return acc.join(join(i, ranges));
            }
        }
        return acc;
    }

    private BytesRange(byte[] data, int offset, int length) {
        this.data = data;
        this.offset = offset;
        this.length = length;
    }

    private BytesRange(byte[] data) {
        this(data, 0, data.length);
    }

    public byte[] bytesCopy() {
        byte[] bytes = new byte[length];
        System.arraycopy(data, offset, bytes, 0, length);
        return bytes;
    }

    public byte byteAt(int i) {
        return data[offset + i];
    }

    public byte[] getBytes() {
        if (length == data.length) {
            return data;
        }
        if (bytes == null) {
            bytes = new byte[length];
            System.arraycopy(data, offset, bytes, 0, length);
        }
        return bytes;
    }

    public BytesRange subrange(int offset, int newLen) {
        if (offset + newLen > length) {
            throw new IllegalArgumentException("The length of data is not long enough.");
        }
        if (newLen == 0) {
            return NONE;
        }
        if (offset == 0 && newLen == length) {
            return this;
        }
        return new BytesRange(data, this.offset + offset, newLen);
    }

    public BytesRange jump(int jump) {
        if (offset + jump < 0 || jump > length) {
            throw new IllegalArgumentException("Jump out of range(" + offset + jump + ").");
        }
        if (jump == 0) {
            return this;
        }
        if (jump == length) {
            return NONE;
        }
        return new BytesRange(data, this.offset + jump, length - jump);
    }

    /**
     * 子序列
     * @param newLen 子序列长度
     * @return 新的子序列
     *         与本序列offset相同
     */
    public BytesRange subrange(int newLen) {
        return subrange(0, newLen);
    }

    public BytesRange replace(final int offset, final BytesRange replacement) {
        if (offset < 0 || offset > this.offset + this.length) {
            throw new IllegalArgumentException("offset is negative or length is not enough.");
        }
        BytesRange prefix = this.subrange(offset);
        BytesRange postfix = NONE;
        int repSize = offset + replacement.length;
        if (repSize < this.length) {
            postfix = this.subrange(repSize, this.length - repSize);
        }
        return join(prefix, replacement, postfix);
    }

    public BytesRange join(BytesRange other) {
        if (this.length == 0) {
            return other;
        }
        if (other.length == 0) {
            return this;
        }
        //同底层数据优化
        if (this.data == other.data && (this.offset + this.length == other.offset)) {
            return new BytesRange(data, offset, this.length + other.length);
        }
        byte[] bytes = new byte[length + other.length];
        System.arraycopy(this.data, this.offset, bytes, 0, this.length);
        System.arraycopy(other.data, other.offset, bytes, this.length, other.length);
        return new BytesRange(bytes);
    }

    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        int hash = 1;
        for (int i = offset; i < offset + length; i++) {
            hash = 31 * hash + data[i];
        }

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BytesRange other = (BytesRange) obj;
        if (length != other.length) {
            return false;
        }
        if (data == other.data && offset == other.offset) {
            return true;
        }

        for (int i = offset; i < offset + length; i++) {
            if (data[i] != other.data[i - offset + other.offset]) {
                return false;
            }
        }
        return true;
    }

    public String getHexStr() {
        if (hexValue == null) {
            hexValue = BitUtil.toHexString(this);
        }
        return hexValue;
    }

}
