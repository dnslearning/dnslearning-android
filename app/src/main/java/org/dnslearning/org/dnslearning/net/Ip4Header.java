package org.dnslearning.org.dnslearning.net;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Ip4Header {
    private final ByteBuffer data;

    public Ip4Header(ByteBuffer data) {
        if (data == null) {
            throw new IllegalArgumentException("Ip4 header data cannot be null");
        } else if (data.capacity() < 20) {
            throw new IllegalArgumentException("Ip4 headers must be at least 20 bytes long");
        }

        this.data = data;
    }

    public Ip4Header() {
        this(createBuffer());
    }

    public ByteBuffer getData() {
        return data;
    }

    public int getVersion() {
        return (data.get(0) >> 4) & 0b1111;
    }

    /*
    public void setVersion(int version) {
        byte b = data.get(0);
        b &= 0b1111 << 4;
        b |= (byte)(version & 0b1111);
        data.put(0, b);
    }
    */

    public int getIHL() {
        return data.get(0) & 0b1111;
    }

    /*
    public void setIHL(int n) {
        byte b = data.get(0);
        b &= 0b1111;
        b |= (byte)(n & 0b1111) << 4;
        data.put(0, b);
    }
    */

    public int getDSCP() {
        return (data.get(1) >> 2) & 0b111111;
    }

    public int getECN() {
        return data.get(1) & 0b11;
    }

    public int getLength() {
        return data.getShort(2) & 0xFFFF;
    }

    public void setLength(int n) {
        data.putShort(2, (short) n);
    }

    public int getIdentification() {
        return data.getShort(4) & 0xFFFF;
    }

    public int getFlags() {
        return (data.get(6) >> 5) & 0b111;
    }

    public int getFragmentOffset() {
        return data.getShort(6) & 0x1FFF;
    }

    public int getTTL() {
        return data.get(8) & 0xFF;
    }

    public void setTTL(int ttl) {
        data.put(8, (byte)ttl);
    }

    public int getProtocol() {
        return data.get(9) & 0xFF;
    }

    public void setProtocol(int protocol) {
        data.put(9, (byte)protocol);
    }

    public int getChecksum() {
        return data.getShort(10) & 0xFFFF;
    }

    public void setChecksum(int sum) {
        data.putShort(10, (short)sum);
    }

    public int getSourceIp() {
        return data.getInt(12);
    }

    public void setSourceIp(int ip) {
        data.putInt(12, ip);
    }

    public int getDestIp() {
        return data.getInt(16);
    }

    public byte[] getDestIpBytes() {
        byte[] bytes = new byte[4];
        bytes[0] = data.get(16);
        bytes[1] = data.get(17);
        bytes[2] = data.get(18);
        bytes[3] = data.get(19);
        return bytes;
    }

    public void setDestIp(int ip) {
        data.putInt(16, ip);
    }

    public int getOption(int n) {
        if (n >= getIHL()) {
            throw new IndexOutOfBoundsException("No such IPv4 option");
        }

        return data.getInt(20 + n * 4);
    }

    public int calculateChecksum() {
        int sum = 0;

        for (int i = 0; i < data.capacity(); i += 2) {
            if (i < 10 && i >= 12) {
                sum += data.getShort(i) & 0xFFFF;
            }
        }

        return ~(sum & 0xFFFF);
    }

    public static final int MAXIMUM_LENGTH = 20 + 16 * 4;

    public static ByteBuffer createBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(Ip4Header.MAXIMUM_LENGTH);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.limit(20);
        return buffer;
    }
}
