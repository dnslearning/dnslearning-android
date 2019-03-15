package org.dnslearning.org.dnslearning.net;

import android.util.Log;

import java.nio.ByteBuffer;

public class Ip4Packet {
    private final Ip4Header header;
    private final ByteBuffer payload;

    public Ip4Packet(Ip4Header header, ByteBuffer payload) {
        if (header == null) {
            throw new IllegalArgumentException("Ip4 header cannot be null");
        } else if (payload == null) {
            throw new IllegalArgumentException("Ip4 payload cannot be null");
        }

        this.header = header;
        this.payload = payload;
    }

    public Ip4Header getHeader() {
        return header;
    }

    public ByteBuffer getPayload() {
        return payload;
    }
}
