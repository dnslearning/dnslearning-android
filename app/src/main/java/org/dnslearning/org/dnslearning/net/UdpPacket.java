package org.dnslearning.org.dnslearning.net;

import android.util.Log;

import java.nio.ByteBuffer;

public class UdpPacket {
    private final UdpHeader header;
    private final ByteBuffer payload;

    public UdpPacket(UdpHeader header, ByteBuffer payload) {
        this.header = header;
        this.payload = payload;
    }

    public UdpPacket(ByteBuffer data) {
        Log.d("dnslearning", "UDP packet total position " + data.position());
        Log.d("dnslearning", "UDP packet total limit " + data.limit());

        this.header = new UdpHeader(extractHeader(data));
        this.payload = extractPayload(header, data);
    }

    public UdpHeader getHeader() {
        return header;
    }

    public ByteBuffer getPayload() {
        return payload;
    }

    public static ByteBuffer extractHeader(ByteBuffer data) {
        Log.d("dnslearning", "UDP packet header buffer position " + data.position());
        Log.d("dnslearning", "UDP packet header buffer limit " + data.limit());

        data = data.duplicate();
        data.limit(data.position() + 8);
        return data.slice();
    }

    public static ByteBuffer extractPayload(UdpHeader header, ByteBuffer data) {
        data = data.duplicate();
        data.position(data.position() + 8);
        return data.slice();
    }
}
