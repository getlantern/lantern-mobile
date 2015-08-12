package org.getlantern.lantern.model;

import android.util.Log;
import java.net.InetAddress;

import go.client.*;
import org.getlantern.lantern.config.LanternConfig;

public class Lantern {

    private static final String TAG = "Lantern";
    private  Client.GoCallback.Stub lanternCallback;

    public void start(final InetAddress localIP, final int port) {
        try {

            Log.d(TAG, "About to start Lantern..");

            Client.GoCallback.Stub callback = new Client.GoCallback.Stub() {
                public void Do() {
                    Log.d(TAG, "Lantern successfully started");
                }
            };

            Client.RunClientProxy(localIP.getHostAddress() + ":" + port,
                    LanternConfig.APP_NAME, callback);

        } catch (final Exception e) {
            Log.e(TAG, "Fatal error while trying to run Lantern: " + e);
            throw new RuntimeException(e);
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
