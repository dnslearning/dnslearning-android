package org.dnslearning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import org.dnslearning.helper.StaticContext;
import org.dnslearning.org.dnslearning.net.DebugHelper;
import org.dnslearning.org.dnslearning.net.DnsPacketBuilder;
import org.dnslearning.org.dnslearning.net.DnsQuestion;
import org.dnslearning.org.dnslearning.net.Ip4Packet;
import org.dnslearning.org.dnslearning.net.Ip4PacketReader;
import org.dnslearning.org.dnslearning.net.Ip4PacketWriter;
import org.dnslearning.org.dnslearning.net.UdpPacket;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Objects;

public class DNSLearningVpnService extends VpnService {
    private Thread mThread;
    private ParcelFileDescriptor mInterface;
    private VpnService.Builder builder = new VpnService.Builder();
    private Boolean shouldRun = true;
    private FileDescriptor fd;
    private FileInputStream inputStream;
    private FileOutputStream outputStream;
    private FileChannel inputChannel;
    private FileChannel outputChannel;
    private Ip4PacketReader packetReader;
    private Ip4PacketWriter packetWriter;
    private DnsPacketBuilder dnsBuilder = new DnsPacketBuilder();
    private HashSet<String> alwaysAllowedDomains = new HashSet<>();
    private HashSet<String> alwaysBlockedDomains = new HashSet<>();
    private DatagramChannel dnsChannel;
    private boolean studyMode = false;
    private ByteBuffer receiveBuffer = ByteBuffer.allocate(16 * 1024);

    private final BroadcastReceiver stopServiceReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
        {
            if (Objects.equals(paramAnonymousIntent.getAction(), "STOP_SMART_DNS")) {
                DNSLearningVpnService.this.stopThisService();
            }
        }
    };

    private void registerBroadcast() {
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction("STOP_SMART_DNS");
        registerReceiver(stopServiceReceiver, localIntentFilter);
    }

    private void stopThisService() {
        shouldRun = false;

        try {
            //tunnel.close();
            mInterface.close();

            if (this.mThread != null) {
                this.mThread.interrupt();
            }

            unregisterReceiver(stopServiceReceiver);
            stopSelf();
            Log.d("DNSLearningVpnService", "Stop DNSLearningVpnService");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerBroadcast();
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                runThread();
            }
        }, "DNSLearningApp");
        mThread.start();
        return START_STICKY;
    }

    protected void runThread() {
        try {
            buildVpn();
            //openTunnel();
            sleep();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mInterface != null) {
                try {
                    mInterface.close();
                    mInterface = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void buildVpn() throws IOException {
        SharedPreferences prefs = StaticContext.getPrefs();

        String virtualGateway = "10.10.10.1";
        String dummyDns = "8.8.8.8";

        builder.setSession("DNSLearningApp");
        builder.addAddress(virtualGateway, 32);
        builder.addDnsServer(dummyDns);
        builder.addRoute(dummyDns, 32);

        // Establish the VPN
        mInterface = builder.establish();

        // Open channels to read and write the tunnel
        fd = mInterface.getFileDescriptor();
        inputStream = new FileInputStream(fd);
        outputStream = new FileOutputStream(fd);
        inputChannel = inputStream.getChannel();
        outputChannel = outputStream.getChannel();

        // Create objects for reading and writing Ip4 packets
        packetReader = new Ip4PacketReader(inputChannel);
        packetWriter = new Ip4PacketWriter(outputChannel);

        // Open a UDP socket used for sending real DNS packets and protect it from being routed
        // through the VPN tunnel
        dnsChannel = DatagramChannel.open();
        protect(dnsChannel.socket());
    }

    protected void sleep() throws InterruptedException {
        while (shouldRun) {
            try {
                readTunnel();
                receiveDnsPackets();
            } catch (IOException e) {
                Log.e("dnslearning", "Error", e);
            }

            Thread.sleep(100L);
        }
    }

    private void readTunnel() throws IOException {
        Ip4Packet ipPacket = packetReader.read();

        if (ipPacket == null) {
            return;
        }

        UdpPacket udpPacket = new UdpPacket(ipPacket.getPayload());
        dnsBuilder.decode(udpPacket.getPayload());

//        if (!studyMode) {
//            sendBlockedResponse();
//            return;
//        }

        for (DnsQuestion question : dnsBuilder.getQuestions()) {
            Log.d("dnslearning", "DNS Question " + question.name);

            //if (!studyMode!alwaysAllowedDomains.contains(question.name)) {
            if (!checkDomain(question.name)) {
                sendBlockedResponse();
                return;
            }
        }

        forwardDnsRequest(ipPacket, udpPacket);
    }

    private boolean checkDomain(String domain) {
        if (studyMode) {
            return alwaysAllowedDomains.contains(domain);
        } else {
            return !alwaysBlockedDomains.contains(domain);
        }
    }

    private void sendBlockedResponse() {
        DebugHelper.dump("Blocked");
    }

    private void forwardDnsRequest(Ip4Packet ipPacket, UdpPacket udpPacket) throws IOException {
        InetAddress host = Inet4Address.getByAddress(ipPacket.getHeader().getDestIpBytes());
        int port = udpPacket.getHeader().getDestPort();
        InetSocketAddress address = new InetSocketAddress(host, port);
        DebugHelper.dump("send to " + address.toString());

        int localPort = udpPacket.getHeader().getSourcePort();
        InetAddress localHost = null;
        InetSocketAddress localAddress = new InetSocketAddress(localHost, localPort);

        //dnsChannel.socket().bind(localAddress);
        dnsChannel.send(udpPacket.getPayload(), address);
    }

    private void receiveDnsPackets() throws IOException {
        receiveBuffer.clear();
        SocketAddress from = dnsChannel.receive(receiveBuffer);

        if (from == null) {
            return;
        }

        DebugHelper.dump("received packet " + from.toString());

        // TODO create IPv4 packet from DNS packet
        // TODO maintain mapping of virtual ports
    }
}
