package com.Bllod100.zpu;

import android.util.Log;
import com.Bllod100.zpu.core.SavageNeuralFirewall;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ZPU HIGH-SPEED USB TETHERING & FIREWALL PROXY GATEWAY
 * High-performance, multithreaded HTTP/HTTPS CONNECT proxy engine.
 * Includes 256KB TCP Window Scaling, IPTOS_LOWDELAY QoS, Windows NCSI Fast-Path & Full-Duplex Join Thread Piping.
 */
public class SavageProxyEngine {
    private ServerSocket serverSocket;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final SavageNeuralFirewall firewall;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private int currentPort = 8080;

    public SavageProxyEngine(SavageNeuralFirewall firewall) {
        this.firewall = firewall;
    }

    public void start(int port) {
        final int targetPortToUse = (port == 60000 || port == 60001 || port == 60002) ? 8080 : port;
        if (running.get() && currentPort == targetPortToUse) return;
        stop();
        this.currentPort = targetPortToUse;
        running.set(true);
        
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.setReceiveBufferSize(262144); // 256 KB Server RCV Window
                serverSocket.bind(new InetSocketAddress(targetPortToUse));
                Log.d("ZPU_PROXY", "USB TETHERING FIREWALL GATEWAY ACTIVE ON PORT: " + targetPortToUse);
                
                while (running.get()) {
                    try {
                        Socket client = serverSocket.accept();
                        if (com.Bllod100.zpu.core.ZpuHardwareSafetyManager.isSoftUsbDisabled()) {
                            try { client.close(); } catch (Exception ignored) {}
                            continue;
                        }
                        tuneSocket(client);
                        executor.execute(() -> handleClientTunnel(client));
                    } catch (IOException e) {
                        if (!running.get()) break;
                    }
                }
            } catch (Exception e) {
                Log.e("ZPU_PROXY", "Failed to start Proxy Gateway on port " + port, e);
                running.set(false);
            }
        }, "ZPU-Proxy-Listener").start();
    }

    private void tuneSocket(Socket s) {
        if (s == null) return;
        try {
            ZpuVpnService.protectSocket(s);
            s.setTcpNoDelay(true); // Disable Nagle algorithm for instant HTTP/TLS header flushing
            s.setKeepAlive(true);
            s.setSoTimeout(30000); // 30s read timeout
            s.setReceiveBufferSize(262144); // 256 KB TCP Window Scaling
            s.setSendBufferSize(262144);
            s.setTrafficClass(0x10); // IPTOS_LOWDELAY for low-latency web navigation
        } catch (Exception ignored) {}
    }

    private void handleClientTunnel(Socket client) {
        Socket targetSocket = null;
        try {
            InputStream clientIn = new BufferedInputStream(client.getInputStream(), 8192);
            OutputStream clientOut = client.getOutputStream();

            // 1. Read HTTP request headers byte-by-byte from memory buffer
            byte[] headerBytes = readHeaders(clientIn);
            if (headerBytes.length == 0) {
                client.close();
                return;
            }

            String headerText = new String(headerBytes, StandardCharsets.UTF_8);
            String[] lines = headerText.split("\r?\n");
            if (lines.length == 0) {
                client.close();
                return;
            }

            String firstLine = lines[0];
            String[] requestParts = firstLine.split(" ");
            if (requestParts.length < 2) {
                client.close();
                return;
            }

            String method = requestParts[0].toUpperCase();
            String hostAndPort = requestParts[1];
            String targetHost = "";
            int targetPort = 80;

            if ("CONNECT".equalsIgnoreCase(method)) {
                // HTTPS Tunneling (CONNECT host:port HTTP/1.1)
                String[] hp = hostAndPort.split(":");
                targetHost = hp[0];
                int parsedPort = 443;
                if (hp.length > 1) {
                    try {
                        parsedPort = Integer.parseInt(hp[1].replaceAll("[^0-9]", ""));
                    } catch (NumberFormatException ignored) {}
                }
                targetPort = (parsedPort > 0 && parsedPort <= 65535) ? parsedPort : 443;
            } else {
                // Standard HTTP Request (GET http://host:port/path HTTP/1.1 or GET /path HTTP/1.1)
                if (hostAndPort.toLowerCase().startsWith("http://")) {
                    String cleanUrl = hostAndPort.substring(7);
                    int slashIdx = cleanUrl.indexOf('/');
                    String hostPortStr = (slashIdx != -1) ? cleanUrl.substring(0, slashIdx) : cleanUrl;
                    String[] hp = hostPortStr.split(":");
                    targetHost = hp[0];
                    int parsedPort = 80;
                    if (hp.length > 1) {
                        try {
                            parsedPort = Integer.parseInt(hp[1].replaceAll("[^0-9]", ""));
                        } catch (NumberFormatException ignored) {}
                    }
                    targetPort = (parsedPort > 0 && parsedPort <= 65535) ? parsedPort : 80;
                } else {
                    for (String line : lines) {
                        if (line.toLowerCase().startsWith("host:")) {
                            String hVal = line.substring(5).trim();
                            String[] hp = hVal.split(":");
                            targetHost = hp[0];
                            int parsedPort = 80;
                            if (hp.length > 1) {
                                try {
                                    parsedPort = Integer.parseInt(hp[1].replaceAll("[^0-9]", ""));
                                } catch (NumberFormatException ignored) {}
                            }
                            targetPort = (parsedPort > 0 && parsedPort <= 65535) ? parsedPort : 80;
                            break;
                        }
                    }
                    if (targetHost.isEmpty()) {
                        String[] hp = hostAndPort.split(":");
                        targetHost = hp[0];
                        int parsedPort = 80;
                        if (hp.length > 1) {
                            try {
                                parsedPort = Integer.parseInt(hp[1].replaceAll("[^0-9]", ""));
                            } catch (NumberFormatException ignored) {}
                        }
                        targetPort = (parsedPort > 0 && parsedPort <= 65535) ? parsedPort : 80;
                    }
                }
            }

            if (targetHost.isEmpty()) {
                client.close();
                return;
            }

            // 2. WINDOWS NCSI FAST-PATH: Instant response to Windows 10/11 Internet Detection probes
            if (targetHost.contains("msftncsi") || targetHost.contains("msftconnecttest")) {
                if ("GET".equalsIgnoreCase(method)) {
                    if (firstLine.contains("/ncsi.txt")) {
                        String ncsiResp = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: 14\r\nConnection: close\r\n\r\nMicrosoft NCSI";
                        clientOut.write(ncsiResp.getBytes(StandardCharsets.UTF_8));
                        clientOut.flush();
                        client.close();
                        return;
                    } else if (firstLine.contains("/connecttest.txt")) {
                        String ctResp = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: 21\r\nConnection: close\r\n\r\nMicrosoft Connect Test";
                        clientOut.write(ctResp.getBytes(StandardCharsets.UTF_8));
                        clientOut.flush();
                        client.close();
                        return;
                    }
                }
            }

            // 3. FIREWALL INSPECTION: Inspect target host against firewall blocklist
            if (firewall != null && !firewall.isHostAllowed(targetHost)) {
                Log.w("ZPU_FIREWALL", "USB TETHERING TRAFFIC BLOCKED: " + targetHost);
                String forbiddenResp = "HTTP/1.1 403 Forbidden\r\n" +
                        "Content-Type: text/html; charset=UTF-8\r\n" +
                        "Connection: close\r\n\r\n" +
                        "<!DOCTYPE html><html><body style='background:#000;color:#0f0;font-family:monospace;padding:40px;'>" +
                        "<h2>[ZPU FIREWALL GATEWAY BLOCK]</h2>" +
                        "<p>Access to <b>" + targetHost + "</b> has been restricted by ZPU Firewall Settings.</p>" +
                        "</body></html>";
                clientOut.write(forbiddenResp.getBytes(StandardCharsets.UTF_8));
                clientOut.flush();
                client.close();
                return;
            }

            // 4. Connect to Target Remote Host over Phone's Active Wi-Fi Connection
            java.net.InetAddress resolvedIp = com.Bllod100.zpu.core.FastDnsResolver.resolve(targetHost);
            targetSocket = new Socket();
            tuneSocket(targetSocket);
            targetSocket.connect(new InetSocketAddress(resolvedIp, targetPort), 8000);

            InputStream targetIn = new BufferedInputStream(targetSocket.getInputStream(), 8192);
            OutputStream targetOut = targetSocket.getOutputStream();

            if ("CONNECT".equalsIgnoreCase(method)) {
                String okResp = "HTTP/1.1 200 Connection Established\r\n" +
                        "Proxy-Agent: ZPU-Tether-Gateway/1.0\r\n\r\n";
                clientOut.write(okResp.getBytes(StandardCharsets.UTF_8));
                clientOut.flush();
            } else {
                targetOut.write(headerBytes);
                targetOut.flush();
            }

            // 5. High-Speed Full-Duplex Data Piping (Shutdown Output on EOF prevents thread deadlock)
            final InputStream cIn = clientIn;
            final OutputStream tOut = targetOut;
            final InputStream tIn = targetIn;
            final OutputStream cOut = clientOut;
            final Socket cSocket = client;
            final Socket tSocket = targetSocket;

            Thread c2t = new Thread(() -> pipeStream(cIn, tOut, cSocket, tSocket), "ZPU-Proxy-C2T");
            Thread t2c = new Thread(() -> pipeStream(tIn, cOut, tSocket, cSocket), "ZPU-Proxy-T2C");

            c2t.start();
            t2c.start();

            try { c2t.join(); } catch (InterruptedException ignored) {}
            try { t2c.join(); } catch (InterruptedException ignored) {}

        } catch (Exception e) {
            // Quietly handle connection close/reset
        } finally {
            try { if (client != null) client.close(); } catch (Exception ignored) {}
            try { if (targetSocket != null) targetSocket.close(); } catch (Exception ignored) {}
        }
    }

    private byte[] readHeaders(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        int b;
        int b1 = -1, b2 = -1, b3 = -1, b4 = -1;
        while ((b = in.read()) != -1) {
            baos.write(b);
            b1 = b2; b2 = b3; b3 = b4; b4 = b;
            if (b1 == '\r' && b2 == '\n' && b3 == '\r' && b4 == '\n') {
                break;
            }
            if (b3 == '\n' && b4 == '\n') {
                break;
            }
            if (baos.size() > 65536) {
                break;
            }
        }
        return baos.toByteArray();
    }

    private void pipeStream(InputStream in, OutputStream out, Socket srcSocket, Socket dstSocket) {
        byte[] buffer = new byte[32768]; // 32 KB High-Speed Buffer
        int read;
        try {
            while (running.get() && (read = in.read(buffer)) != -1) {
                if (firewall != null) {
                    firewall.addTetheredBytes(read);
                }
                out.write(buffer, 0, read);
                out.flush();
            }
        } catch (Exception ignored) {
        } finally {
            try { if (dstSocket != null && !dstSocket.isOutputShutdown()) dstSocket.shutdownOutput(); } catch (Exception ignored) {}
            try { if (srcSocket != null && !srcSocket.isInputShutdown()) srcSocket.shutdownInput(); } catch (Exception ignored) {}
        }
    }

    public void stop() {
        running.set(false);
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
    }

    public boolean isRunning() {
        return running.get();
    }

    public int getPort() {
        return currentPort;
    }
}
