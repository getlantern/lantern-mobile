package org.getlantern.lantern.model;

import android.util.Log;

import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import go.client.*;
import org.getlantern.lantern.config.LanternConfig;
import org.getlantern.lantern.service.LanternCustomVpn;

public class Lantern {

    private static final String TAG = "Lantern";

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

    public void processPacket(final ByteBuffer packet) {
        Log.d(TAG, "Processing a packet with Lantern");
        Client.GoCallback.Stub callback = new Client.GoCallback.Stub() {
            public void Do() {
                Log.d(TAG, "Lantern successfully started");
            }

            public void WritePacket(String destination, long port, String protocol) {
                try {
                    Log.d(TAG, "Destination -> " + destination + ":" + port + " " + protocol);
                } catch (Exception e) {
                    Log.e(TAG, "Exception processing packet " + e);
                }
            }
        };

        try {
            Client.CapturePacket(packet.array(), callback);
        } catch (final Exception e) {
            Log.e(TAG, "Unable to process incoming packet!");
        }
    }


    public static void stop() {
        try {
            Log.d(TAG, "Sending STOP signal to Lantern process");
            Client.StopClientProxy();
        } catch (final Exception e) {
            Log.e(TAG, "Fatal error while trying to stop Lantern: " + e);
            throw new RuntimeException(e);
        }
    }
}
