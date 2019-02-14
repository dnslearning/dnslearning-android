package org.dnslearning.org.dnslearning.net;

import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class Ip4PacketReader {
    private ReadableByteChannel channel;
    private Ip4Header header;
    private ByteBuffer payload;
    private boolean useSharedHeader;

    public Ip4PacketReader(ReadableByteChannel channel) {
        this.channel = channel;
        this.header = new Ip4Header();
    }

    /**
     * Enable or disable the re-use of a single buffer for header data. By default a single buffer
     * is allocated that is the maximum size allowed ({@link Ip4Header#MAXIMUM_LENGTH}) and is used
     * to reduce the number of allocations. When using a shared buffer each time read() is invoked
     * the buffer will be re-used. You should disable shared header buffers if you plan to save
     * Ip4Packet objects between calls to read().
     *
     * @param enabled true to enable shared header buffers (default) or false to disable
     */
    public void enableSharedHeader(boolean enabled) {
        this.useSharedHeader = enabled;
    }

    /**
     * Reads the next available packet from the underlying stream.
     * @return the next packet or null if there is no packet available yet
     * @throws IOException
     */
    public Ip4Packet read() throws IOException {
        if (readHeader()) {
            if (readPayload()) {
                return createPacket();
            }
        }

        return null;
    }

    private boolean hasMinimalHeader() {
        return header.getData().position() >= 20;
    }

    private boolean readHeader() throws IOException {
        if (!read(header.getData())) {
            return false;
        }

        if (hasMinimalHeader()) {
            header.getData().limit(20 + header.getIHL() * 4);

            Log.d("dnslearning", "ip version " + header.getVersion());
            Log.d("dnslearning", "ip length " + header.getLength());
            Log.d("dnslearning", "ip packet fragment offset " + header.getFragmentOffset());
        }

        return read(header.getData()) && (header.getData().hasRemaining() == false);
    }

    private boolean readPayload() throws IOException {
        if (payload == null) {
            // TODO check header checksum
            // TODO what if we get a header of less than 20 bytes (spec says it's invalid)
            payload = ByteBuffer.allocate(header.getLength() - header.getIHL() * 4);
            Log.d("dnslearning", "ip packet header length " + header.getData().limit());
            Log.d("dnslearning", "ip packet header getLength() " + header.getLength());
            Log.d("dnslearning", "ip packet payload size " + payload.capacity());
        }

        return read(payload) && (payload.hasRemaining() == false);
    }

    private Ip4Packet createPacket() {
        Log.d("dnslearning", "finished an IP packet");

        Ip4Packet packet = new Ip4Packet(header, payload);

        if (useSharedHeader) {
            header.getData().position(0);
            header.getData().limit(20);
        } else {
            header = new Ip4Header();
        }

        payload = null;
        return packet;
    }

    private boolean read(ByteBuffer buffer) throws IOException {
        //Log.d("dnslearning", "Trying to read " + buffer.remaining() + " bytes");

        if (buffer.hasRemaining()) {
            int result = channel.read(buffer);

            if (result != 0) {
                Log.d("dnslearning", "Read " + result + " bytes");
            }

            if (result == -1) {
                return false;
            }
        }

        return !buffer.hasRemaining();
    }
}
