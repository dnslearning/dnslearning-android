package org.dnslearning.org.dnslearning.net;

import java.nio.ByteBuffer;

public class DnsQuestion {
    public String name;
    public int rtype;
    public int rclass;
    public ByteBuffer data;
}
