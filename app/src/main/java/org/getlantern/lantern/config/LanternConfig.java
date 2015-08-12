package org.getlantern.lantern.config;

/**
 * Created by todd on 8/7/15.
 */
public interface LanternConfig {

    public final static String APP_NAME = "lantern";

    public final static int SOCKS_PROXY_PORT = 9051;
    public final static int HTTP_PROXY_PORT = 9051;
    public final static int UDPGW_PORT = 7300;
    public final static int DNS_PORT_DEFAULT = 5400;
    public final static String ENABLE_VPN = "org.getlantern.lantern.intent.action.ENABLE";
    public final static String DISABLE_VPN = "org.getlantern.lantern.intent.action.DISABLE";

    public final static String START_BUTTON_TEXT = "START";
    public final static String STOP_BUTTON_TEXT = "STOP";
    public final static String SHELL_CMD_PS = "toolbox ps";
    public final static String PREF_LANTERN_SHARED_PREFs = "org.getlantern.lantern.android_preferences";
    public final static String EXTRA_PACKAGE_NAME = "org.getlantern.lantern.intent.extra.PACKAGE_NAME";
    public final static String ACTION_STATUS = "org.getlantern.lantern.intent.action.STATUS";
    public final static String EXTRA_STATUS = "org.getlantern.lantern.intent.extra.STATUS";
    public final static String STATUS_STARTS_DISABLED = "STARTS_DISABLED";
}  
