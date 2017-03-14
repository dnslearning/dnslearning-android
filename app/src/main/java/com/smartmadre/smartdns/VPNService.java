package com.smartmadre.smartdns;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.smartmadre.smartdns.preferences.PreferenceManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

public class VPNService extends VpnService {
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
                VPNService.this.stopThisService();
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
            Log.d("VPNService", "Stop VPNService");
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
                try {
                    Log.d("VPNService", "Use " + PreferenceManager.getDNS() + " as DNS server");
                    mInterface = builder.setSession("AltaDNS")
                            .addAddress("192.168.0.1", 24)
                            .addDnsServer(PreferenceManager.getDNS())
                            .establish();

                    tunnel = DatagramChannel.open();
                    tunnel.connect(new InetSocketAddress("127.0.0.1", 8087));

                    protect(tunnel.socket());

                    while (shouldRun) {
                        Thread.sleep(100L);
                    }
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
        }, "AltaDNS");
        mThread.start();
        return START_STICKY;
    }
}
