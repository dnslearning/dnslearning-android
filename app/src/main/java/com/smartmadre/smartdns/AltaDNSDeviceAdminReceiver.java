package com.smartmadre.smartdns;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by dzmitry on 15/03/2017.
 */

public class AltaDNSDeviceAdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    public void onEnabled(Context context, Intent intent) {};

    public void onDisabled(Context context, Intent intent) {};
}
