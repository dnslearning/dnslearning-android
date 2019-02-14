package org.dnslearning.org.dnslearning.net;

import java.nio.ByteBuffer;

public class UdpHeader {
    private final ByteBuffer data;

    public UdpHeader(ByteBuffer data) {
        if (data == null) {
            throw new IllegalArgumentException("UDP header cannot be null");
        } else if (data.capacity() != 8) {
            throw new IllegalArgumentException("UDP headers are 8 bytes large");
        }

        this.data = data;
    }

    public ByteBuffer getData() {
        return data;
    }

    public int getSourcePort() {
        return data.getShort(0) & 0xFFFF;
    }

    public int getDestPort() {
        return data.getShort(2) & 0xFFFF;
    }

    public int getLength() {
        return data.getShort(4) & 0xFFFF;
    }

    public int getChecksum() {
        return data.getShort(6) & 0xFFFF;
    }
}
