package com.mediatek.factorymode.wifi;

import java.util.List;

import com.mediatek.factorymode.R;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WiFiTools {
    private static final String TAG = "WiFiTools";
    public static final int WIFI_STATE_DISABLING = 0;
    public static final int WIFI_STATE_DISABLED = 1;
    public static final int WIFI_STATE_ENABLING = 2;
    public static final int WIFI_STATE_ENABLED = 3;
    private static WifiManager mWifiManager = null;
    private NetworkInfo mNetworkInfo;
    private ConnectivityManager mConnectivityManager;
    static boolean mResult = false;
    private String info = "";
    private Context context;
    private WifiInfo wifiinfo;

    public WiFiTools(Context context) {
        this.context = context;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiinfo = mWifiManager.getConnectionInfo();
        mWifiManager.startScan();
    }

    public String GetState() {
        int state = -1;
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
            state = mWifiManager.getWifiState();
            switch (state) {
            case WIFI_STATE_DISABLING:
                info = context.getString(R.string.WiFi_info_closeing);
                break;
            case WIFI_STATE_DISABLED:
                info = context.getString(R.string.WiFi_info_close);
                break;
            case WIFI_STATE_ENABLING:
                info = context.getString(R.string.WiFi_info_opening);
                break;
            case WIFI_STATE_ENABLED:
                info = context.getString(R.string.WiFi_info_open);
                break;
            default:
                info = context.getString(R.string.WiFi_info_unknown);
                break;
            }
        } else {
            info = context.getString(R.string.WiFi_info_open);
        }
        return info;
    }

    public boolean openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            boolean wifistate = mWifiManager.setWifiEnabled(true);
            return wifistate;
        } else {
            return true;
        }
    }

    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    public boolean addWifiConfig(List<ScanResult> wifiList, ScanResult srt, String pwd) {
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = "\"" + srt.SSID + "\"";
        wc.allowedKeyManagement.set(KeyMgmt.NONE);
        wc.status = WifiConfiguration.Status.ENABLED;
        wc.networkId = mWifiManager.addNetwork(wc);
        return mWifiManager.enableNetwork(wc.networkId, true);
    }

    public List<ScanResult> scanWifi() {
        return mWifiManager.getScanResults();
    }

    public WifiInfo GetWifiInfo() {
        wifiinfo = mWifiManager.getConnectionInfo();
        return wifiinfo;
    }

    public Boolean IsConnection() {
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState() == NetworkInfo.State.CONNECTED) {
            return true;
        }
        return false;
    }

    public boolean connectWifi(String wifiSsid, String wifiPwd, int ciperType) {

        WifiConfiguration wifiCfg = new WifiConfiguration();

        Log.e(TAG, "data_sync_wifi, ciperType: " + ciperType);
        switch (ciperType) {
        case KeyMgmt.WPA_PSK:
        case KeyMgmt.WPA_EAP:
            wifiCfg.SSID = "\"" + wifiSsid + "\"";
            wifiCfg.preSharedKey = "\"" + wifiPwd + "\"";
            wifiCfg.hiddenSSID = true;
            wifiCfg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiCfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiCfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiCfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiCfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wifiCfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiCfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            wifiCfg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiCfg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiCfg.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiCfg.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiCfg.status = WifiConfiguration.Status.ENABLED;
            break;

        case KeyMgmt.NONE:
            wifiCfg.SSID = "\"" + wifiSsid + "\"";
            wifiCfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiCfg.status = WifiConfiguration.Status.ENABLED;
            break;

        default:
            break;
        }

        List<WifiConfiguration> wifiConf = mWifiManager.getConfiguredNetworks();

        for (int k = 0; k < wifiConf.size(); k++) {
            if (wifiCfg.SSID.equals(wifiConf.get(k).SSID) && wifiConf.get(k).getAuthType() != ciperType) {
                mWifiManager.removeNetwork(wifiConf.get(k).networkId);
            }
        }
        int netID = mWifiManager.addNetwork(wifiCfg);
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        mWifiManager.disconnect();
        boolean bRet = mWifiManager.enableNetwork(netID, true);
        return bRet;
    }

    public static String integer2Ip(int ip) {
        StringBuilder sb = new StringBuilder();
        int num = 0;
        boolean needPoint = false;
        for (int i = 3; i >= 0; i--) {
            if (needPoint) {
                sb.append('.');
            }
            needPoint = true;
            int offset = 8 * (3 - i);
            num = (ip >> offset) & 0xff;
            sb.append(num);
        }
        return sb.toString();
    }
}
