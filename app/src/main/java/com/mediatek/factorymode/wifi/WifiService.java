package com.mediatek.factorymode.wifi;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.mediatek.factorymode.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class WifiService extends Service {
    private WifiManager wm;
    IntentFilter intentFilter;
    private static final int START_SCAN_WLAN = 123;
    private static final int GET_SCAN_RESULTS = 124;
    List<ScanResult> scanResults = new ArrayList<ScanResult>();
    private MyHandler mHandler;
    private boolean isLocationOpen;
    public WifiService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("WifiService","onCreate()");
        //判断是否开启位置权限，android 8.0需要开启位置，wifiManager才能正常工作
        LocationManager locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!networkProvider && !gpsProvider){
            isLocationOpen = false;
            Settings.Secure.putInt(getContentResolver(),Settings.Secure.LOCATION_MODE, 1);  //开启位置
        }else {
            isLocationOpen = true;
        }
        mHandler = new MyHandler(this);
        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        this.registerReceiver(wifiReceiver, intentFilter);
        if (wm.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            if (wm.getWifiState() == WifiManager.WIFI_STATE_DISABLING) {
                Log.i("progress", "WifiManager.WIFI_STATE_DISABLING");
            } else {
                wm.setWifiEnabled(true);
                Log.e("WifiService","开启wifi");
                Log.i("progress", "WifiManager.setWifiEnabled(true)");
            }
        } else if (wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            //wm.startScan();
            mHandler.removeMessages(START_SCAN_WLAN);
            mHandler.sendMessage(mHandler.obtainMessage(START_SCAN_WLAN));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("WifiService","onDestroy()");
        mHandler.removeMessages(START_SCAN_WLAN);
        if (wm.isWifiEnabled() && !Utils.isWifiOpened()) {
            wm.setWifiEnabled(false);
            Log.e("WifiService","关闭wifi");

        }
        if(!isLocationOpen){
            Settings.Secure.putInt(getContentResolver(),Settings.Secure.LOCATION_MODE, 0);  //关闭位置
        }
        unregisterReceiver(wifiReceiver);
        mHandler.removeCallbacksAndMessages(null);  //把消息对象从消息队列移除
    }

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                if (wm.isWifiEnabled()) {
                    //wm.startScan();
                    mHandler.removeMessages(START_SCAN_WLAN);
                    mHandler.sendMessage(mHandler.obtainMessage(START_SCAN_WLAN));
                }
                Log.i("progress", "WifiManager.WIFI_STATE_CHANGED_ACTION enable=" + wm.isWifiEnabled());
            }
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.endsWith(action)) {
                scanResults = wm.getScanResults();
                mHandler.removeMessages(GET_SCAN_RESULTS);
                mHandler.sendMessage(mHandler.obtainMessage(GET_SCAN_RESULTS));
                Log.i("progress", "WifiManager.SCAN_RESULTS_AVAILABLE_ACTION result=" + scanResults.size());
            }
        }
    };

    private static class MyHandler extends Handler {
        private WeakReference<WifiService> reference;
        public MyHandler(WifiService service) {
            reference = new WeakReference<WifiService>(service);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            WifiService wifiService = reference.get();
            switch (msg.what) {
                case START_SCAN_WLAN:
                    wifiService.wm.startScan();
                    sendEmptyMessageDelayed(START_SCAN_WLAN, 10 * 1000);
                    break;
                default:
                    break;
            }
            Log.i("WifiService", "size: " + wifiService.scanResults.size());
            Utils.setScanResults(wifiService.scanResults);  //add for show wifi listView quickly by lxx 20180727
        }
    }
}

