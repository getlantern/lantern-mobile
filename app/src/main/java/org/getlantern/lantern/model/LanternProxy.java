package org.getlantern.lantern.model;

import java.net.InetAddress;
import android.util.Log;
import android.content.Intent;
import org.getlantern.lantern.activity.PromptVPNActivity;

import go.client.*;
import org.getlantern.lantern.Constants;


/**
 * Created by todd on 4/25/15.
 */
public class LanternProxy {

    private static final String TAG = "LanternProxy";

    public void start(final InetAddress localIP, final int port) {
        try {

            Client.RunClientProxy("127.0.0.1:9121", "LanternAndroid",
                    new Client.GoCallback.Stub() {
                        public void Do() {

                        }
                    });

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void stop() {
        try {
                Client.StopClientProxy();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
