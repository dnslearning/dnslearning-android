package org.dnslearning;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class DNSLearningDeviceAdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
    }

    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
    }
}
