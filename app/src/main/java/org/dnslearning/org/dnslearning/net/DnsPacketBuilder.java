package org.dnslearning.org.dnslearning.net;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DnsPacketBuilder {
    private final ArrayList<DnsQuestion> questions = new ArrayList<>();
    private final ArrayList<DnsResource> answers = new ArrayList<>();
    private final ArrayList<DnsResource> auths = new ArrayList<>();
    private final ArrayList<DnsResource> additional = new ArrayList<>();
    private final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    // private final HashMap<String, Integer> labels = new HashMap<>();
    private int id;
    private int flags;

    public void clear() {
        questions.clear();
        answers.clear();
        auths.clear();
        additional.clear();
        id = 0;
        flags = 0;
        byteStream.reset();
        // labels.clear();
    }

    public byte[] build() {
        // Clear any old information (possibly an exception was thrown)
        byteStream.reset();
        // labels.clear();

        // Write header
        writeShort(id);
        writeShort(questions.size());
        writeShort(answers.size());
        writeShort(auths.size());
        writeShort(additional.size());

        // Write questions and resource records
        for (DnsQuestion question : questions) { writeQuestion(question); }
        for (DnsResource answer : answers) { writeResource(answer); }
        for (DnsResource authority : auths) { writeResource(authority); }
        for (DnsResource resource : additional) { writeResource(resource); }

        // Finish packet
        byte[] packet = byteStream.toByteArray();

        // Forget data and compressed strings
        byteStream.reset();
        // labels.clear();

        return packet;
    }

    private void writeQuestion(DnsQuestion question) {
        writeString(question.name);
        writeShort(question.rtype);
        writeShort(question.rclass);
        write(question.data);
    }

    private void writeResource(DnsResource resource) {
        writeString(resource.name);
        writeShort(resource.rtype);
        writeShort(resource.rclass);
        writeInt(resource.ttl);
        writeShort(resource.data.capacity());
        write(resource.data);
    }

    private void writeByte(int value) {
        byteStream.write(value);
    }

    private void writeShort(int value) {
        byteStream.write((value >> 8) & 0xFF);
        byteStream.write(value & 0xFF);
    }

    private void writeInt(int value) {
        byteStream.write((value >> 24) & 0xFF);
        byteStream.write((value >> 16) & 0xFF);
        byteStream.write((value >> 8) & 0xFF);
        byteStream.write(value & 0xFF);
    }

    private void writeString(String str) {
        for (String part : str.split("\\.")) {
            writeStringLiteral(part);
        }

        writeByte(0);
    }

    private void writeStringLiteral(String str) {
        writeByte(str.length());

        for (int i = 0; i < str.length(); i++) {
            writeByte(str.charAt(i) & 0xFF);
        }
    }

    private void write(ByteBuffer data) {
        byteStream.write(data.array(), data.arrayOffset() + data.position(), data.remaining());
    }

    public void addQuestion(DnsQuestion question) {
        questions.add(question);
    }

    public void addAnswer(DnsResource answer) {
        answers.add(answer);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public List<DnsQuestion> getQuestions() {
        return questions;
    }

    public List<DnsResource> getAnswers() {
        return answers;
    }

    public List<DnsResource> getAuthorityRecords() {
        return auths;
    }

    public List<DnsResource> getAdditionalRecords() {
        return additional;
    }

    public boolean decode(ByteBuffer packet) {
        clear();

        DebugHelper.dump("DNS Packet Size: " + packet.remaining());
        DebugHelper.dump("DNS Packet Endian: " + packet.order().toString());

        // DNS header
        id = packet.getShort() & 0xFFFF;
        flags = packet.getShort() & 0xFFFF;
        int qdcount = packet.getShort() & 0xFFFF;
        int ancount = packet.getShort() & 0xFFFF;
        int nscount = packet.getShort() & 0xFFFF;
        int arcount = packet.getShort() & 0xFFFF;

        DebugHelper.dump("DNS ID: " + id);
        DebugHelper.dump("DNS Flags: " + flags);
        DebugHelper.dump("DNS Questions: " + qdcount);
        DebugHelper.dump("DNS Answers: " + ancount);
        DebugHelper.dump("DNS Nameservers: " + nscount);
        DebugHelper.dump("DNS Authority: " + arcount);

        for (int i=0; i < qdcount; i++) { questions.add(readQuestion(packet)); }
        for (int i=0; i < ancount; i++) { answers.add(readResource(packet)); }
        for (int i=0; i < nscount; i++) { auths.add(readResource(packet)); }
        for (int i=0; i < arcount; i++) { additional.add(readResource(packet)); }

        return true;
    }

    private String readString(ByteBuffer buffer) {
        byte c;
        StringBuilder s = new StringBuilder();

        do {
            c = buffer.get();

            if (c != 0 && s.length() != 0) {
                s.append('.');
            }

            if ((c & 0b11000000) != 0) {
                s.append(readStringCompressed(buffer, c));
                c = 0;
            } else if (c != 0) {
                s.append(readStringLiteral(buffer, c & 0xFF));
            }
        } while (c != 0);

        return s.toString();
    }

    private String readStringLiteral(ByteBuffer buffer, int len) {
        if (len == 0) {
            return "";
        } else if (len > 63) {
            throw new IllegalArgumentException("DNS literal strings cannot be longer than 63 bytes");
        }

        byte[] chunk = new byte[len];
        buffer.get(chunk);
        return new String(chunk, StandardCharsets.US_ASCII);
    }

    private String readStringCompressed(ByteBuffer buffer, byte c) {
        int offset = (c & 0b00111111) | (buffer.get() << 8);
        //int offset = ((c & 0b00111111) << 8) | buffer.get();

        if (offset > buffer.position()) {
            throw new IllegalArgumentException("DNS labels must point backwards (" + offset + ")");
        }

        buffer = buffer.duplicate();
        buffer.position(offset);
        return readString(buffer);
    }

    private DnsQuestion readQuestion(ByteBuffer packet) {
        DnsQuestion question = new DnsQuestion();
        question.name = readString(packet);
        question.rtype = packet.getShort() & 0xFFFF;
        question.rclass = packet.getShort() & 0xFFFF;
        return question;
    }

    private DnsResource readResource(ByteBuffer packet) {
        DnsResource res = new DnsResource();
        res.name = readString(packet);
        res.rtype = packet.getShort() & 0xFFFF;
        res.rclass = packet.getShort() & 0xFFFF;
        res.ttl = packet.getInt();

        int length = packet.getShort() & 0xFFFF;

        if (length > 512) {
            throw new IllegalArgumentException("DNS resource is too large");
        }

        res.data = extract(packet, length);
        return res;
    }

    private ByteBuffer extract(ByteBuffer packet, int length) {
        packet = packet.slice();
        packet.limit(length);
        return packet;
    }
}
