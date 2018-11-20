package com.mediatek.factorymode.wifi;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class Wifi extends TestActivity implements OnClickListener {
    private WifiManager wm;
    private ProgressDialog mProgressDialog;
    IntentFilter intentFilter;
    private Button discovery_wifipoint_button;
    private static final String TAG = "Wifi";

    private static final int START_SCAN_WLAN = 123;
    private static final int GET_SCAN_RESULTS = 124;
    private Button passButton;
    private Button failButton;
    private boolean isExit = false;
    private boolean stopScan = false;
    List<ScanResult> scanResults = new ArrayList<ScanResult>();

    private ListView mWifiListView;
    private WifiAdapter mAdapter;
    private MyHandler mHandler;
    private SharedPreferences mSp = null;
    private boolean isLoactionOpen;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"onCreate()");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.wifi);
        mHandler = new MyHandler(this);
        passButton = (Button) findViewById(R.id.wifi_bt_ok);
        passButton.setEnabled(false);
        passButton.setOnClickListener(this);
        failButton = (Button) findViewById(R.id.wifi_bt_failed);
        failButton.setOnClickListener(this);
        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        discovery_wifipoint_button = (Button) findViewById(R.id.btn_discovery_wifipoint);
        discovery_wifipoint_button.setOnClickListener(this);
        discovery_wifipoint_button.setText(R.string.discovery);

        mWifiListView = (ListView) findViewById(R.id.wifi_list);
        mAdapter = new WifiAdapter();
        mWifiListView.setAdapter(mAdapter);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);

        //text_wifi_info=(TextView) findViewById(R.id.tv_wifi_info);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.wait));
        //add for show wifi listView quickly by lxx 20180727

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        registerReceiver(wifiReceiver, intentFilter);
        Log.e("Wifi", "onCreate enable=" + wm.isWifiEnabled());

    }

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("Wifi",action);
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                if (wm.isWifiEnabled()) {
                    mHandler.removeMessages(START_SCAN_WLAN);
                    mHandler.sendMessage(mHandler.obtainMessage(START_SCAN_WLAN));
                } else {
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                }
                Log.e("Wifi", "WifiManager.WIFI_STATE_CHANGED_ACTION enable=" + wm.isWifiEnabled());
            }
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.endsWith(action)) {
                scanResults = wm.getScanResults();
                sortByLevel(scanResults);
                Utils.setScanResults(scanResults);
                mAdapter.notifyDataSetChanged();

                mHandler.removeMessages(GET_SCAN_RESULTS);
                mHandler.sendMessage(mHandler.obtainMessage(GET_SCAN_RESULTS));
                discovery_wifipoint_button.setText(R.string.discovery);
                Log.e("Wifi", "WifiManager.SCAN_RESULTS_AVAILABLE_ACTION result=" + scanResults.size());
            }
        }
    };

    @Override
    protected void onResume(){
        super.onResume();
        Log.e(TAG,"onResume()");
        isExit = false;
        if (Utils.getScanResults() != null && Utils.getScanResults().size() > 0) {
            passButton.setEnabled(true);
            scanResults = Utils.getScanResults();
            sortByLevel(scanResults);
            mAdapter.notifyDataSetChanged();
        } else {
            mProgressDialog.show();
        }

        //判断是否开启位置权限，android 8.0需要开启位置，wifiManager才能正常工作
        LocationManager locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!networkProvider && !gpsProvider){
            isLoactionOpen = false;
            Settings.Secure.putInt(getContentResolver(),Settings.Secure.LOCATION_MODE, 1);  //开启位置
        }else {
            isLoactionOpen = true;
        }

        if (wm.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            if (wm.getWifiState() == WifiManager.WIFI_STATE_DISABLING) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                Log.e("Wifi", "WifiManager.WIFI_STATE_DISABLING");
                discovery_wifipoint_button.setText(R.string.Wifi_open);
            } else {
                wm.setWifiEnabled(true);
                wm.startScan();
                Log.e("Wifi", "WifiManager.setWifiEnabled(true)");
            }
        } else if (wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
//            wm.startScan();
            if (scanResults == null) {
                mHandler.removeMessages(START_SCAN_WLAN);
                mHandler.sendMessage(mHandler.obtainMessage(START_SCAN_WLAN));
            }
        }
    }

    @Override
    protected void onPause() {
        Log.e(TAG,"onPause()");
        isExit = true;
        mHandler.removeMessages(START_SCAN_WLAN);
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if(!isLoactionOpen){
            Settings.Secure.putInt(getContentResolver(),Settings.Secure.LOCATION_MODE, 0);  //关闭位置
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG,"onDestroy()");
        super.onDestroy();
        unregisterReceiver(wifiReceiver);
        mAdapter = null;
        mHandler.removeCallbacksAndMessages(null);  //把消息对象从消息队列移除
    }

    private static class MyHandler extends Handler {
        private WeakReference<Wifi> reference;

        public MyHandler(Wifi activity) {
            reference = new WeakReference<Wifi>(activity);//这里传入activity的上下文
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Wifi wifiActivity = reference.get();
            switch (msg.what) {
                case START_SCAN_WLAN:
                    if (wifiActivity.isExit) {
                        break;
                    }
                    wifiActivity.wm.startScan();
                    sendEmptyMessageDelayed(wifiActivity.START_SCAN_WLAN, 10 * 1000);
                    break;
                case GET_SCAN_RESULTS:
                    if (wifiActivity.isExit) {
                        break;
                    }
                    int size = wifiActivity.scanResults.size();
                    if (size > 0) {
                        wifiActivity.stopScan = true;
                        wifiActivity.passButton.setEnabled(true);
                        if (wifiActivity.mProgressDialog.isShowing()) {
                            wifiActivity.mProgressDialog.dismiss();
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_discovery_wifipoint:
                mProgressDialog.show();
                stopScan = false;
                if (wm.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
                    wm.setWifiEnabled(true);
                } else {
                    mHandler.removeMessages(START_SCAN_WLAN);
                    mHandler.sendMessage(mHandler.obtainMessage(START_SCAN_WLAN));
                }
                break;

            case R.id.wifi_bt_ok:
                Utils.SetPreferences(this, mSp, R.string.wifi_name, AppDefine.FT_SUCCESS);
                finish();
                break;

            case R.id.wifi_bt_failed:
                Utils.SetPreferences(this, mSp, R.string.wifi_name, AppDefine.FT_FAILED);
                finish();
                break;
        }

    }

    private class WifiAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return scanResults.size();
        }

        @Override
        public Object getItem(int position) {
            return scanResults.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.wifi_item, parent, false);
                viewHolder.name = (TextView) convertView.findViewById(R.id.wifi_item_name);
                viewHolder.level = (TextView) convertView.findViewById(R.id.tv_wifi_item_level);
                viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.pb_wifi_item_level);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ScanResult result = scanResults.get(position);
            if (result != null) {
                Log.v("Wlan", "position = " + position + "-----wifi name is " + result.SSID);
                viewHolder.name.setText(result.SSID);
                viewHolder.level.setText(result.level + "dB");
                viewHolder.progressBar.setProgress(getProgress(result.level));
            }
            return convertView;
        }

        public class ViewHolder {
            TextView name;
            TextView level;
            ProgressBar progressBar;
        }

    }

    private int getProgress(int level) {
        return (int) ((level + 100.0) / 0.6);
    }

    private void sortByLevel(List<ScanResult> list) {
        int length = list.size();
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                if (Math.abs(list.get(i).level) > Math.abs(list.get(j).level)) {
                    ScanResult temp = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, temp);
                }
            }
        }
    }


}