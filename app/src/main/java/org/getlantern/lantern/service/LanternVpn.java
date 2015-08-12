package org.getlantern.lantern.service;

import java.io.IOException;
import java.net.InetAddress;

import org.getlantern.lantern.Constants;
import org.getlantern.lantern.model.LanternProxy;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import java.net.UnknownHostException;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import org.torproject.android.vpn.Tun2Socks;

import com.runjva.sourceforge.jsocks.server.*;
import com.runjva.sourceforge.jsocks.protocol.*;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class LanternVPN extends VpnService implements Handler.Callback {
    private static final String TAG = "LanternVpnService";

    private Handler mHandler;
    private Thread mVpnThread;
	private ParcelFileDescriptor vpnInterface;

    private String mSessionName = "LanternVPN";
	private final String virtualGateway = "10.0.0.1";
	private final String virtualIP = "10.0.0.2";
	private final String virtualNetMask = "255.255.255.0";
	private final String localSocks = "127.0.0.1:" + Constants.HTTP_PROXY_PORT;
	private final String localDNS = "127.0.0.1:" + Constants.UDPGW_PORT;

	private ProxyServer mSocksProxyServer;
    private LanternProxy lanternProxy;
    
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

		if (action.equals(Constants.ENABLE_VPN)) {
			setupLantern();
		} else if (action.equals(Constants.ACTION_STATUS)) {
			Log.d(TAG,"refreshing LanternVPNService service!");
			stopLantern();
			setupLantern();
		} else if (action.equals(Constants.ACTION_STOP)) {
			Log.d(TAG, "Received STOP request");
			stopLantern();
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
        if (mVpnThread == null || (!mVpnThread.isAlive()))
        {
			startLantern();
        }

		setupTun2Socks();
    }

	private synchronized void startLantern() {
		Log.d(TAG, "Loading Lantern library");
		Thread thread = new Thread() {
			public void run() {
				try {
					lanternProxy = new LanternProxy();
					lanternProxy.start(InetAddress.getLocalHost(), Constants.HTTP_PROXY_PORT);
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
    	Log.d(TAG,"stopping LanternVPNService service!");
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

        if (vpnInterface != null) //stop tun2socks now to give it time to clean up
        {
        	Tun2Socks.Stop();
        }
        
    	mVpnThread = new Thread ()
    	{
    		
    		public void run ()
    		{
	    		try
		        {
					establishVpnConnection();

					Thread.sleep(4000);
					Tun2Socks.Start(vpnInterface, VPN_MTU, virtualIP,
							virtualNetMask, localSocks, localDNS, true);
		        	
		        }
		        catch (Exception e)
		        {
		        	Log.d(TAG,"tun2Socks has stopped",e);
		        }
	    	}
    		
    	};
    	
    	mVpnThread.start();
    }

	private void establishVpnConnection() {
		try {

			if (vpnInterface != null) {
				Log.d(TAG, "Stopping existing VPN interface");
				vpnInterface.close();
				vpnInterface = null;
			}

			Builder builder = new Builder();
			builder.setMtu(VPN_MTU);
			builder.addAddress("10.0.0.1", 28);
			builder.addRoute("0.0.0.0", 0);
			builder.addDnsServer("8.8.8.8");
			builder.setSession("lanternVPN");

			vpnInterface = builder.setSession(mSessionName)
					.establish();
		} catch (final IOException ioe) {
			Log.e(TAG, "Error closing existing VPN interface: " + ioe);
		}
	}
    
    @Override
    public void onRevoke() {
    	stopLantern();
        super.onRevoke();
    }
}
