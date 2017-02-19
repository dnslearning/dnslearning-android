package com.smartmadre.smartdns;

import android.app.Service;
import android.content.Intent;
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("VPNService", "Use " + PreferenceManager.getDNS() + " as DNS server");
                    mInterface = builder.setSession("SmartDNS")
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
        }, "SmartDNS");
        mThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mThread != null) {
            mThread.interrupt();
        }
        super.onDestroy();
    }
}
