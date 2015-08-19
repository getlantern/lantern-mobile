package org.getlantern.lantern.model;

import android.util.Log;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import go.client.*;
import org.getlantern.lantern.service.LanternVpn;
import org.getlantern.lantern.config.LanternConfig;

public class Lantern extends Client.SocketProvider.Stub {

    private static final String TAG = "Lantern";
    private LanternVpn service;

    public Lantern(LanternVpn service) {
        this.service = service;
    }

    public void start(final InetAddress localIP, final int port) {
        try {
            Log.d(TAG, "About to start Lantern..");
            String lanternAddress = String.format("%s:%s", localIP.getHostAddress(), port);
            Client.RunClientProxy(lanternAddress,
                    LanternConfig.APP_NAME, null);

        } catch (final Exception e) {
            Log.e(TAG, "Fatal error while trying to run Lantern: " + e);
            throw new RuntimeException(e);
        }
    }

    // Protect is used to exclude a socket specified by fileDescriptor
    // from the VPN connection. Once protected, the underlying connection
    // is bound to the VPN device and won't be forwarded
    @Override
    public void Protect(long fileDescriptor) throws Exception {
        if (!this.service.protect((int) fileDescriptor)) {
            throw new Exception("protect socket failed");
        }
    }


    // Runs a simple HTTP GET to verify Lantern is able to open connections
    public void testConnect() {
        try {
            final String testAddr = "www.example.com:80";
            Client.TestConnect(this, testAddr);
        } catch (final Exception e) {

        }
    }

    // As packets arrive on the VpnService, processPacket sends the raw bytes
    // to Lantern for processing
    public void processPacket(final ByteBuffer packet) {
        Log.d(TAG, "Processing a packet with Lantern");
        Client.GoCallback.Stub callback = new Client.GoCallback.Stub() {
            public void Do() {
                Log.d(TAG, "Lantern successfully started");
            }

            public void WritePacket(String destination, long port, String protocol) {
                // Just used to demonstrate a callback after intercepting a packet
            }
        };

        try {
            Client.CapturePacket(packet.array(), callback);
        } catch (final Exception e) {
            Log.e(TAG, "Unable to process incoming packet!");
        }
    }
}
