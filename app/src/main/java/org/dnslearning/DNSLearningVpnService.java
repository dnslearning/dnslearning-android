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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

public class DNSLearningVpnService extends VpnService {
    private Thread mThread;
    private ParcelFileDescriptor mInterface;
    private VpnService.Builder builder = new VpnService.Builder();
    private DatagramChannel tunnel;
    private Boolean shouldRun = true;
    private final BroadcastReceiver stopServiceReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
        {
            if (paramAnonymousIntent.getAction().equals("STOP_SMART_DNS")) {
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
            tunnel.close();
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
            openTunnel();
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

    protected void buildVpn() {
        builder.setSession("DNSLearningApp");
        builder.addAddress("192.168.0.1", 24);

        SharedPreferences prefs = StaticContext.getPrefs();

        //Set<String> empty = new HashSet<>();
        //Set<String> trusted = new HashSet<String>();
        //trusted.add("45.32.192.233");
        //trusted.add("45.76.235.109");
        //
        //
        //trusted.addAll(prefs.getStringSet("trusted", empty));
        //
        //for (String address : trusted) {
        //    builder.addAddress(address, 24);
        //}

        String dns = prefs.getString("dns", "8.8.8.8");
        Log.d("DNSLearningVpnService", "Use " + dns + " as DNS server");

        builder.addDnsServer(dns);

        mInterface = builder.establish();
    }

    protected void openTunnel() throws IOException {
        tunnel = DatagramChannel.open();
        tunnel.connect(new InetSocketAddress("127.0.0.1", 8087));

        protect(tunnel.socket());
    }

    protected void sleep() throws InterruptedException {
        while (shouldRun) {
            Thread.sleep(100L);
        }
    }
}
