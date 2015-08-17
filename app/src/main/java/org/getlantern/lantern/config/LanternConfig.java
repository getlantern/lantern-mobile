package org.getlantern.lantern.config;

/**
 * Created by todd on 8/7/15.
 */
public interface LanternConfig {

    public final static String APP_NAME = "lantern";

    public final static int SOCKS_PROXY_PORT = 9050;
    public final static int HTTP_PROXY_PORT = 8161;
    public final static int DNS_PORT_DEFAULT = 53;
    public final static String ENABLE_VPN = "org.getlantern.lantern.intent.action.ENABLE";
    public final static String DISABLE_VPN = "org.getlantern.lantern.intent.action.DISABLE";
    public final static String START_BUTTON_TEXT = "START";
    public final static String STOP_BUTTON_TEXT = "STOP";
    public final static String ACTION_STATUS = "org.getlantern.lantern.intent.action.STATUS";
}  
