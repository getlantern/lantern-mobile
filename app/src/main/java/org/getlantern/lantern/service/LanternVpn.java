package org.getlantern.lantern.service;

import java.io.IOException;
import java.net.InetAddress;

import org.getlantern.lantern.config.LanternConfig;
import org.getlantern.lantern.model.Lantern;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.VpnService;
import android.app.PendingIntent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import java.net.UnknownHostException;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import org.torproject.android.vpn.Tun2Socks;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class LanternVpn extends VpnService implements Handler.Callback {
    private static final String TAG = "LanternVpn";
    private Thread mVpnThread;
	private ParcelFileDescriptor vpnInterface;
    private String mSessionName = "LanternVPN";
	private static final String vpnGateway = "10.0.0.1";
	private static final String defaultRoute = "0.0.0.0";
	private final String virtualIP = "10.0.0.2";
	private final String virtualNetMask = "255.255.255.0";
	private final String localSocks = "127.0.0.1:" + LanternConfig.HTTP_PROXY_PORT;
	private final String localDNS = "127.0.0.1:" + LanternConfig.DNS_PORT_DEFAULT;

    private Lantern lanternProxy;
    
    private final static int VPN_MTU = 1500;

	@Override
	public void onCreate() {
		super.onCreate();
		System.loadLibrary("tun2socks");
	}


	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent == null) {
			return START_STICKY;
		}

		String action = intent.getAction();                   
		Log.d(TAG, "Received " + intent.getAction() + " command");

		if (action.equals(LanternConfig.ENABLE_VPN)) {
			setupLantern();
		} else if (action.equals(LanternConfig.ACTION_STATUS)) {
			Log.d(TAG, "refreshing LanternVPNService service!");
			//stopLantern();
			//setupLantern();
		} else if (action.equals(LanternConfig.DISABLE_VPN)) {
			Log.d(TAG, "Received STOP request");
			//stopLantern();
		}
        
        return START_STICKY;
    }

    private void setupLantern() {

		Log.d(TAG, "Setting up Lantern!");

		// stop previously running instance of Lantern, if any
		if (vpnInterface != null) {
			stopLantern();
		}

		// Stop the previous session by interrupting the thread.
		if (mVpnThread == null || (!mVpnThread.isAlive())) {
			startLantern();

			try {

				if (vpnInterface != null) {
					Log.d(TAG, "Stopping previous Tun2Socks instance");
					Tun2Socks.Stop();
				}

				setupTun2Socks();
			} catch (Exception e) {
				Log.e(TAG, "Error: " + e);
			}
		}
	}

	private synchronized void startLantern() {
		Log.d(TAG, "Loading Lantern library");
		Thread thread = new Thread() {
			public void run() {
				try {
					lanternProxy = new Lantern();
					lanternProxy.start(InetAddress.getLocalHost(), LanternConfig.HTTP_PROXY_PORT);
					Log.d(TAG, "Lantern successfully started!");
				} catch (UnknownHostException uhe) {
					Log.e(TAG, "Error starting Lantern with given host: " + uhe);
				}
			}
		};
		thread.start();
	}

	private void stopLantern() {
		if (lanternProxy != null) {
			lanternProxy.stop();
			lanternProxy = null;
		}

		Log.d(TAG, "closing interface, destroying VPN interface");
		try {
			// close any existing VPN interface
			if (vpnInterface != null) {
				vpnInterface.close();
				vpnInterface = null;
			}
		} catch (final IOException ioe) {
			Log.e(TAG, "Error cleaning up previous VPN interface: " + ioe);
		}

		// Finally, shut down Tun2Socks
		Tun2Socks.Stop();
	}

	@Override
    public void onDestroy() {
    	Log.d(TAG,"Stopping LanternVPNService service!");
		stopLantern();
		super.onDestroy();
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message != null) {
            Toast.makeText(this, message.what, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

	private synchronized void setupTun2Socks()  {
        
    	mVpnThread = new Thread ()
    	{
    		
    		public void run ()
    		{
	    		try
		        {

					Builder builder = new Builder();
					builder.setMtu(VPN_MTU);
					builder.addAddress(vpnGateway, 28);
					builder.addRoute("0.0.0.0", 0);
					builder.setSession("lanternVPN");

					vpnInterface = buildVpnConnection();

					if (vpnInterface == null) {
						Log.e(TAG, "Could not instantiate VPN interface!");
						throw new RuntimeException();
					}

					Tun2Socks.Start(vpnInterface, VPN_MTU, virtualIP, virtualNetMask, localSocks, localDNS,
							true);
					Log.d(TAG, "Successfully started tun2socks");
		        }
		        catch (Exception e)
		        {
		        	Log.d(TAG,"tun2Socks has stopped",e);
		        }
	    	}
    		
    	};
    	mVpnThread.start();
    }

	private ParcelFileDescriptor buildVpnConnection() {
		try {
			Builder builder = new Builder();
			builder.setMtu(VPN_MTU);
			builder.addAddress(vpnGateway, 28);
			builder.addRoute(defaultRoute, 0);
			builder.setSession(mSessionName);
			ParcelFileDescriptor newInterface = builder.setSession(mSessionName)
					.establish();

			if (vpnInterface != null) {
				Log.d(TAG, "Stopping existing VPN interface");
				vpnInterface.close();
			}
			return newInterface;
		} catch (final Exception ioe) {
			Log.e(TAG, "Error closing existing VPN interface: " + ioe);
		}
		return null;
	}
    
    @Override
    public void onRevoke() {
    	//stopLantern();
        super.onRevoke();
    }

}
