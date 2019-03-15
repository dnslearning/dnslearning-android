package org.dnslearning.org.dnslearning.net;

import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class Ip4PacketReader {
    private ReadableByteChannel channel;
    private final ByteBuffer buffer;

    public Ip4PacketReader(ReadableByteChannel channel) {
        this.channel = channel;
        this.buffer = ByteBuffer.allocate(1024 * 16);
    }

    /**
     * Reads the next available packet from the underlying stream.
     * @return the next packet or null if there is no packet available yet
     * @throws IOException
     */
    public Ip4Packet read() throws IOException {
        buffer.clear();
        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            DebugHelper.dump("EOF from tunnel");
        }

        if (bytesRead <= 0) {
            return null;
        }

        DebugHelper.dump("read bytes " + bytesRead);

        Ip4Header header = extractHeader(buffer);

        if (header == null) {
            return null;
        }

        ByteBuffer payload = extractPayload(buffer, header);

        if (payload == null) {
            return null;
        }

        return new Ip4Packet(header, payload);
    }

    public static Ip4Header extractHeader(ByteBuffer buffer) {
        ByteBuffer headerData = buffer.duplicate();
        headerData.rewind();
        headerData.limit(20);
        Ip4Header header = new Ip4Header(headerData);
        Log.d("dnslearning", "IPv4 Header Extra Length: " + header.getIHL());
        return header;
    }

    public static ByteBuffer extractPayload(ByteBuffer buffer, Ip4Header header) {
        int payloadLength = header.getLength() - header.getIHL() * 4;
        DebugHelper.dump("payloadLength: " + payloadLength);

        ByteBuffer payload = buffer.duplicate();
        payload.position(header.getIHL() * 4);
        payload.limit(payload.position() + header.getLength());
        payload = payload.slice();


        DebugHelper.dump("ip packet header length " + header.getData().limit());
        DebugHelper.dump("ip packet header getLength() " + header.getLength());
        DebugHelper.dump("ip packet payload size " + payload.capacity());

        return payload;
    }
}
