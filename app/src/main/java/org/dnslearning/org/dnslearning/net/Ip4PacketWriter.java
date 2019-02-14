package org.dnslearning.org.dnslearning.net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.WritableByteChannel;

public class Ip4PacketWriter {
    private WritableByteChannel channel;

    public Ip4PacketWriter(WritableByteChannel channel) {
        this.channel = channel;
    }

    public void write(Ip4Packet packet) throws IOException {
        channel.write(packet.getHeader().getData());
        channel.write(packet.getPayload());
    }
}
