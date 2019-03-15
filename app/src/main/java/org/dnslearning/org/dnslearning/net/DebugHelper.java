package org.dnslearning.org.dnslearning.net;

import android.util.Log;

import java.nio.ByteBuffer;

public class DebugHelper {
    private static final String tag = "dnslearning/net";

    public static void dump(String s) {
        Log.d(tag, s);
    }

    public static void dump(ByteBuffer data) {
        StringBuilder s = new StringBuilder();

        s.append("[");
        s.append(data.position());
        s.append(":");
        s.append(data.limit());
        s.append("] ");

        for (int i = 0; i < data.limit(); i++) {
            s.append(String.format("%02x", data.get(i) & 0xFF));
        }

        Log.d(tag, s.toString());
    }
}
