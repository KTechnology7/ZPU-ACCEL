package com.Bllod100.zpu.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import com.Bllod100.zpu.ZpuVpnService;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class DnsPacketHandler {

    public static byte[] handleDnsPacket(byte[] packet, int length, SavageNeuralFirewall firewall, Context context) {
        if (length < 28) return null;

        int ipHeaderLen = (packet[0] & 0x0F) * 4;
        int protocol = packet[9] & 0xFF;

        if (protocol != 17 && protocol != 6) return null;

        int dstPort = ((packet[ipHeaderLen + 2] & 0xFF) << 8) | (packet[ipHeaderLen + 3] & 0xFF);

        if (dstPort == 853) {
            ZpuHistoryLogger.logToFile(context, "DOT_BYPASS_BLOCKED", "Blocked encrypted DNS-over-TLS (Port 853 Private DNS) to enforce local ZPU Adult & Ad filter.");
            return null;
        }

        if (dstPort != 53) return null;

        int dnsOffset = ipHeaderLen + 8;
        if (length <= dnsOffset + 12) return null;

        String domain = parseDnsDomain(packet, dnsOffset + 12, length);
        if (domain == null || domain.isEmpty()) return null;

        boolean isAdultPreset = "94.140.14.15".equals(ZpuVpnService.currentUpstreamDns);
        boolean isBlocked = (isAdultPreset && AdultContentShield.isAdultContent(domain)) || DuckTrackerShield.isTrackerOrAd(domain) || (firewall != null && !firewall.isHostAllowed(domain));

        if (isBlocked) {
            ZpuHistoryLogger.logToFile(context, "ADULT_CONTENT_BLOCKED", "Successfully blocked restricted adult/tracker domain: " + domain);
            return buildBlockedDnsResponse(packet, length, ipHeaderLen, dnsOffset);
        }

        byte[] dnsPayload = new byte[length - dnsOffset];
        for (int i = 0; i < dnsPayload.length; i++) {
            dnsPayload[i] = packet[dnsOffset + i];
        }

        byte[] responsePayload = resolveUpstreamDnsWithFailover(dnsPayload, context);

        if (responsePayload == null) return null;

        return buildDnsResponsePacket(packet, ipHeaderLen, responsePayload);
    }

    private static String parseDnsDomain(byte[] packet, int offset, int length) {
        try {
            StringBuilder sb = new StringBuilder();
            int curr = offset;
            while (curr < length) {
                int len = packet[curr] & 0xFF;
                if (len == 0 || (len & 0xC0) != 0) break;
                if (sb.length() > 0) sb.append('.');
                if (curr + 1 + len > length) break;
                for (int i = 0; i < len; i++) {
                    sb.append((char) packet[curr + 1 + i]);
                }
                curr += 1 + len;
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] resolveUpstreamDnsWithFailover(byte[] dnsPayload, Context context) {
        List<String> upstreamServers = new ArrayList<>();
        String currentDns = ZpuVpnService.currentUpstreamDns;
        if (currentDns != null && !currentDns.isEmpty() && !"SYSTEM".equals(currentDns) && !currentDns.matches(".*[a-zA-Z].*")) {
            upstreamServers.add(currentDns);
        }
        String[] fallbackServers = {"1.1.1.1", "8.8.8.8", "94.140.14.14", "8.8.4.4", "9.9.9.9"};
        for (int i = 0; i < fallbackServers.length; i++) {
            if (!upstreamServers.contains(fallbackServers[i])) {
                upstreamServers.add(fallbackServers[i]);
            }
        }

        ConnectivityManager cm;
        Network activeNetwork = null;
        if (context != null) {
            try {
                cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    activeNetwork = cm.getActiveNetwork();
                }
            } catch (Exception ignored) {}
        }

        for (int i = 0; i < upstreamServers.size(); i++) {
            String server = upstreamServers.get(i);
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                ZpuVpnService.protectSocket(socket);
                
                if (activeNetwork != null) {
                    try { activeNetwork.bindSocket(socket); } catch (Exception ignored) {}
                }

                socket.setSoTimeout(800);

                InetAddress upstreamDns = InetAddress.getByName(server);
                DatagramPacket sendPacket = new DatagramPacket(dnsPayload, dnsPayload.length, upstreamDns, 53);
                socket.send(sendPacket);

                byte[] recvBuffer = new byte[2048];
                DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);
                socket.receive(recvPacket);

                byte[] result = new byte[recvPacket.getLength()];
                for (int j = 0; j < result.length; j++) {
                    result[j] = recvBuffer[j];
                }
                return result;
            } catch (Exception ignored) {
            } finally {
                if (socket != null) {
                    try { socket.close(); } catch (Exception ignored) {}
                }
            }
        }
        return null;
    }

    private static byte[] buildBlockedDnsResponse(byte[] packet, int length, int ipHeaderLen, int dnsOffset) {
        byte[] resp = new byte[length + 16];
        for (int i = 0; i < length; i++) {
            resp[i] = packet[i];
        }

        for (int i = 0; i < 4; i++) {
            resp[16 + i] = packet[12 + i];
            resp[12 + i] = packet[16 + i];
        }

        resp[ipHeaderLen] = packet[ipHeaderLen + 2];
        resp[ipHeaderLen + 1] = packet[ipHeaderLen + 3];
        resp[ipHeaderLen + 2] = packet[ipHeaderLen];
        resp[ipHeaderLen + 3] = packet[ipHeaderLen + 1];

        resp[dnsOffset + 2] = (byte) 0x81;
        resp[dnsOffset + 3] = (byte) 0x80;

        resp[dnsOffset + 6] = 0x00;
        resp[dnsOffset + 7] = 0x01;

        int ansOffset = length;
        resp[ansOffset] = (byte) 0xC0;
        resp[ansOffset + 1] = (byte) 0x0C;
        resp[ansOffset + 2] = 0x00; resp[ansOffset + 3] = 0x01;
        resp[ansOffset + 4] = 0x00; resp[ansOffset + 5] = 0x01;
        resp[ansOffset + 6] = 0x00; resp[ansOffset + 7] = 0x00; resp[ansOffset + 8] = 0x00; resp[ansOffset + 9] = 0x3C;
        resp[ansOffset + 10] = 0x00; resp[ansOffset + 11] = 0x04;
        resp[ansOffset + 12] = 0x00; resp[ansOffset + 13] = 0x00; resp[ansOffset + 14] = 0x00; resp[ansOffset + 15] = 0x00;

        int totalLen = length + 16;
        int udpLen = totalLen - ipHeaderLen;

        resp[2] = (byte) ((totalLen >> 8) & 0xFF);
        resp[3] = (byte) (totalLen & 0xFF);

        resp[ipHeaderLen + 4] = (byte) ((udpLen >> 8) & 0xFF);
        resp[ipHeaderLen + 5] = (byte) (udpLen & 0xFF);

        resp[ipHeaderLen + 6] = 0x00;
        resp[ipHeaderLen + 7] = 0x00;

        computeIpChecksum(resp, ipHeaderLen);

        return resp;
    }

    private static byte[] buildDnsResponsePacket(byte[] requestPacket, int ipHeaderLen, byte[] dnsResponsePayload) {
        int totalLen = ipHeaderLen + 8 + dnsResponsePayload.length;
        byte[] resp = new byte[totalLen];

        for (int i = 0; i < ipHeaderLen; i++) {
            resp[i] = requestPacket[i];
        }

        for (int i = 0; i < 4; i++) {
            resp[16 + i] = requestPacket[12 + i];
            resp[12 + i] = requestPacket[16 + i];
        }

        resp[ipHeaderLen] = requestPacket[ipHeaderLen + 2];
        resp[ipHeaderLen + 1] = requestPacket[ipHeaderLen + 3];
        resp[ipHeaderLen + 2] = requestPacket[ipHeaderLen];
        resp[ipHeaderLen + 3] = requestPacket[ipHeaderLen + 1];

        int udpLen = 8 + dnsResponsePayload.length;

        resp[2] = (byte) ((totalLen >> 8) & 0xFF);
        resp[3] = (byte) (totalLen & 0xFF);

        resp[ipHeaderLen + 4] = (byte) ((udpLen >> 8) & 0xFF);
        resp[ipHeaderLen + 5] = (byte) (udpLen & 0xFF);

        resp[ipHeaderLen + 6] = 0x00;
        resp[ipHeaderLen + 7] = 0x00;

        for (int i = 0; i < dnsResponsePayload.length; i++) {
            resp[ipHeaderLen + 8 + i] = dnsResponsePayload[i];
        }

        computeIpChecksum(resp, ipHeaderLen);

        return resp;
    }

    private static void computeIpChecksum(byte[] header, int headerLen) {
        header[10] = 0;
        header[11] = 0;
        int sum = 0;
        for (int i = 0; i < headerLen; i += 2) {
            int word = ((header[i] & 0xFF) << 8) | (header[i + 1] & 0xFF);
            sum += word;
        }
        while ((sum >> 16) > 0) {
            sum = (sum & 0xFFFF) + (sum >> 16);
        }
        int checksum = ~sum & 0xFFFF;
        header[10] = (byte) ((checksum >> 8) & 0xFF);
        header[11] = (byte) (checksum & 0xFF);
    }
}
