/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.getlantern.lantern.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;


import org.getlantern.lantern.config.LanternConfig;
import org.getlantern.lantern.model.Lantern;
import ca.psiphon.PsiphonTunnel;

import java.net.InetAddress;

public class LanternVpn extends VpnService
    implements Handler.Callback {
    private static final String TAG = "LanternVpn";

    private PendingIntent mConfigureIntent;
    private String mSessionName = "LanternVpn";

    private Handler mHandler;
    private Thread mThread;
    private Lantern lantern = null;

    private ParcelFileDescriptor mInterface;

    /*@Override
    public void onCreate() {
        System.loadLibrary("tun2socks");
    }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            return START_STICKY;
        }

        // Stop the previous session by interrupting the thread.
        if (mThread != null) {
            mThread.interrupt();
        }

        String action = intent.getAction();

        // STOP button was pressed
        // shut down Lantern and close the VPN connection
        if (action.equals(LanternConfig.DISABLE_VPN)) {
            stopLantern();
            if (mHandler != null) {
                mHandler.postDelayed(new Runnable () {
                    public void run () { stopSelf();
                    }
                }, 1000);
            }
            return START_STICKY;
        }

        // The handler is only used to show messages.
        if (mHandler == null) {
            mHandler = new Handler(this);
        }


        // Make sure we check for null here
        // as on start command can run multiple times
        if (lantern == null) {
            startLantern();
            startit();
        }

        // Start a new session by creating a new thread.
        /*mThread = new Thread(this, "LanternVpnThread");
        try {
            mThread.sleep(5000);
            mThread.start();
        }
        catch (Exception e) {
            Log.d(TAG, "Couldn't configure VPN interface: " + e);
        }*/
        return START_STICKY;
    }

    private synchronized void startLantern() {

        Log.d(TAG, "Loading Lantern library");
        final LanternVpn service = this;
        Thread thread = new Thread() {
            public void run() {
                try {
                    lantern = new Lantern(service);
                    lantern.start(InetAddress.getLocalHost(), 9193);
                    Thread.sleep(3000);

                } catch (Exception uhe) {
                    Log.e(TAG, "Error starting Lantern with given host: " + uhe);
                }
            }
        };
        thread.start();
    }

    private void stopLantern() {
        try {
            if (mInterface != null) {
                mInterface.close();
                PsiphonTunnel.stopTun2Socks();
                mInterface = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not stop Lantern: " + e);
        }
    }
    @Override
    public void onDestroy() {
        if (mThread != null) {
            mThread.interrupt();
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message != null) {
            Toast.makeText(this, message.what, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public void startit() {
        try {
            if (!isRunning()) {
                Log.i(TAG, "Starting VPN");
                startRun();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error trying to start VPN: " + e);
            throw new RuntimeException("Couldn't configure VPN");
        }
    }

    private boolean isRunning() {
        return mInterface != null;
    }

    private synchronized void startRun() throws Exception {
        try {

            /*configure();

            final FileInputStream in = new FileInputStream(mInterface.getFileDescriptor());
            final FileOutputStream out = new FileOutputStream(
                    mInterface.getFileDescriptor());

            final ByteBuffer packet = ByteBuffer.allocate(32767);

            Log.d(TAG, "VPN interface is attached to Lantern");

            //lantern.configure(out);
            //lantern.start(InetAddress.getLocalHost(), 9192);
            //lantern.testConnect();*/

            new Thread ()
            {
                public void run ()
                {
                    try
                    {

                        // If the old interface has exactly the same parameters, use it!
                        if (mInterface != null) {
                            Log.i(TAG, "Using the previous interface");
                            return;
                        }

                        // Configure a builder while parsing the parameters.
                        Builder builder = new Builder();
                        builder.setMtu(1500);
                        builder.addDnsServer("8.8.4.4");
                        builder.addRoute("0.0.0.0", 0);
                        builder.addAddress("10.0.0.1", 28);

                        // Close the old interface since the parameters have been changed.
                        try {
                            mInterface.close();
                        } catch (Exception e) {
                            // ignore
                        }

                        // Create a new interface using the builder and save the parameters.
                        mInterface = builder.setSession(mSessionName)
                                .setConfigureIntent(mConfigureIntent)
                                .establish();
                        Log.i(TAG, "New interface: " + mInterface);

                        Thread.sleep(4000);

                        PsiphonTunnel.startTun2Socks(mInterface, 1500,
                                "10.0.0.2", "255.255.255.0",
                                "127.0.0.1:9192", "127.0.0.1:7300", false);
                        Log.d(TAG, "Successfully started Tun2Socks.....");

                        /*while (true) {
                            // Read any IP packet from the VpnService input stream
                            // and copy to a ByteBuffer
                            int length = in.read(packet.array());
                            if (length > 0) {
                                packet.limit(length);
                                // forward IP packet from TUN to Lantern
                                //lantern.processPacket(packet);
                                packet.clear();
                            }

                        }*/

                    }
                    catch (Exception e)
                    {
                        Log.e(TAG, "Got an exception receiving packets: " + e);
                    }

                }
            }.start();
            Log.i(TAG, "Started VPN mode");

        } catch (Exception e) {
            Log.e(TAG, "Error with VPN" + e);
        }

    }

    // Obtain a interface for the TUN. We can only have one
    // active TUN interface for a
    private void configure() throws Exception {
        // If the old interface has exactly the same parameters, use it!
        if (mInterface != null) {
            Log.i(TAG, "Using the previous interface");
            return;
        }

        // Configure a builder while parsing the parameters.
        Builder builder = new Builder();
        builder.setMtu(1500);
        builder.addDnsServer("8.8.4.4");
        builder.addRoute("0.0.0.0", 0);
        builder.addAddress("10.0.0.1", 28);

        // Close the old interface since the parameters have been changed.
        try {
            mInterface.close();
        } catch (Exception e) {
            // ignore
        }

        // Create a new interface using the builder and save the parameters.
        mInterface = builder.setSession(mSessionName)
                .setConfigureIntent(mConfigureIntent)
            .establish();
        Log.i(TAG, "New interface: " + mInterface);

        Thread.sleep(4000);

        //Tun2Socks.Start(mInterface, 1500, "10.0.0.1", "255.255.255.0",
        //        "127.0.0.1:9192", "127.0.0.1:7300", true);
        Log.d(TAG, "Successfully started Tun2Socks.....");
    }
}
