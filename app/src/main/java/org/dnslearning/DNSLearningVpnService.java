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
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
    private DatagramChannel dnsChannel;
    private boolean studyMode = true;

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
                tick();
            } catch (IOException e) {
                Log.e("dnslearning", "Error", e);
            }

            Thread.sleep(100L);
        }
    }

    private void tick() throws IOException {
        Ip4Packet ipPacket = packetReader.read();

        if (ipPacket == null) {
            return;
        }

        ByteBuffer ipPacketData = ipPacket.getPayload();
        ipPacketData.rewind();

        UdpPacket udpPacket = new UdpPacket(ipPacketData);
        dnsBuilder.decode(udpPacket.getPayload());

        if (checkBlocked()) {
            sendBlockedResponse();
        } else {
            forwardDnsRequest(udpPacket);
        }
    }

    private boolean checkBlocked() {
        if (!studyMode) {
            return false;
        }

        for (DnsQuestion question : dnsBuilder.getQuestions()) {
            Log.d("dnslearning", "DNS Question " + question.name);

            if (!alwaysAllowedDomains.contains(question.name)) {
                return true;
            }
        }

        return false;
    }

    private void sendBlockedResponse() {

    }

    private void forwardDnsRequest(UdpPacket packet) throws IOException {
        InetAddress host = null;
        int port = 0;
        InetSocketAddress address = new InetSocketAddress(host, port);
        dnsChannel.send(packet.getPayload(), address);
    }
}
