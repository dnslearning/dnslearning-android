package org.dnslearning.org.dnslearning.net;

import java.nio.ByteBuffer;

public class DnsResource {
    public String name;
    public int rtype;
    public int rclass;
    public int ttl;
    public ByteBuffer data;

    public static final int TYPE_A = 1; // a host address
    public static final int TYPE_NS = 2; // an authoritative name server
    public static final int TYPE_MD = 3; //a mail destination (Obsolete - use MX)
    public static final int TYPE_MF = 4; // a mail forwarder (Obsolete - use MX)
    public static final int TYPE_CNAME = 5; // the canonical name for an alias
    public static final int TYPE_SOA = 6; //marks the start of a zone of authority
    public static final int TYPE_MB = 7; // a mailbox domain name (EXPERIMENTAL)
    public static final int TYPE_MG = 8; // a mail group member (EXPERIMENTAL)
    public static final int TYPE_MR = 9; // a mail rename domain name (EXPERIMENTAL)
    public static final int TYPE_NULL = 10; // a null RR (EXPERIMENTAL)
    public static final int TYPE_WKS = 11; // well known service description
    public static final int TYPE_PTR = 12; // a domain name pointer
    public static final int TYPE_HINFO = 13; //host information
    public static final int TYPE_MINFO = 14; // mailbox or mail list information
    public static final int TYPE_MX = 15; // mail exchange
    public static final int TYPE_TXT = 16; //text strings
}
