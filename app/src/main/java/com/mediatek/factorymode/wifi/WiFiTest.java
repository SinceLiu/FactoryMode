package com.mediatek.factorymode.wifi;

import java.util.List;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.MyGridView;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class WiFiTest extends TestActivity implements OnClickListener {
    public static final int WIFI_STATE_DISABLING = 0;
    public static final int WIFI_STATE_DISABLED = 1;
    public static final int WIFI_STATE_ENABLING = 2;
    public static final int WIFI_STATE_ENABLED = 3;
    public static final int TIMEOUT = 15;
    private TextView mTvInfo = null;
    private TextView mTvResult = null;
    private TextView mTvCon = null;
    private TextView mTvResInfo = null;
    private TextView mTvMac = null;
    private Button mBtOk = null;
    private Button mBtFailed = null;
    private List<ScanResult> mWifiList = null;
    private SharedPreferences mSp = null;
    private Spanned mNetWorkName;
    boolean mResult = false;
    boolean mFlag = false;
    boolean mListFlag = false;
    boolean mWifiScan = false;
    private WiFiTools mWifiTools;
    private int mCount = 0;
    HandlerThread mWifiThread = new HandlerThread("wifiThread");
    WifiHandler mHandler;
    private final static int WIFI_STATE = 0;
    private final static int WIFI_LIST = 1;
    private final static int WIFI_CONNECTED = 2;
    private final static int WIFI_CONNECTING = 3;
    private final static int WIFI_FAILED = 4;
    private final static int WIFI_NOTFOUND_OPENAP = 5;
    private MyGridView mGridView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_test);

        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mTvInfo = (TextView) findViewById(R.id.wifi_state_id);
        mTvResult = (TextView) findViewById(R.id.wifi_result_tip);
        mTvCon = (TextView) findViewById(R.id.wifi_con_id);
        mTvResInfo = (TextView) findViewById(R.id.wifi_resinfo_id);
        mTvMac = (TextView) findViewById(R.id.wifi_mac);
        mBtOk = (Button) findViewById(R.id.wifi_bt_ok);
        mBtFailed = (Button) findViewById(R.id.wifi_bt_failed);
        mBtOk.setOnClickListener(this);
        mBtFailed.setOnClickListener(this);
        mWifiTools = new WiFiTools(this);
        mWifiTools.openWifi();
        mWifiThread.start();
        mHandler = new WifiHandler(mWifiThread.getLooper());
        mHandler.post(wifirunnable);
        mGridView = (MyGridView) findViewById(R.id.wifi_grid);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String macAddress = mWifiTools.GetWifiInfo() == null ? "" : mWifiTools.GetWifiInfo().getMacAddress();
        mTvMac.setText(getString(R.string.WiFi_mac_address) + " " + macAddress);
    }

    class WifiHandler extends Handler {
        public WifiHandler() {
        }

        public WifiHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWifiTools.closeWifi();
        mHandler.removeCallbacks(wifirunnable);
    }

    Runnable wifirunnable = new Runnable() {
        @Override
        public void run() {
            if (mCount >= TIMEOUT) {
                UiHandler.sendEmptyMessage(WIFI_FAILED);
                mWifiTools.closeWifi();
                mHandler.removeCallbacks(this);
            }
            if (mFlag == false) {
                boolean res = StartWifi();
                if (res == false && mListFlag == true) {
                    UiHandler.sendEmptyMessage(WIFI_NOTFOUND_OPENAP);
                    mHandler.removeCallbacks(this);
                    return;
                } else if (res != false) {
                    mFlag = true;
                    UiHandler.sendEmptyMessage(WIFI_CONNECTING);
                }
                mHandler.postDelayed(this, 3000);
            } else {
                if (mWifiTools.IsConnection()) {
                    UiHandler.sendEmptyMessage(WIFI_CONNECTED);
                } else {
                    mHandler.postDelayed(this, 3000);
                }
            }
            mCount++;
        }
    };

    Handler UiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case WIFI_STATE:
                mTvInfo.setText(mWifiTools.GetState());
                mTvResult.setText(R.string.WiFi_scaning);
                mTvResult.setVisibility(View.VISIBLE);
                break;
            case WIFI_LIST:
                mTvResult.setVisibility(View.GONE);
                break;
            case WIFI_CONNECTED:
                WifiInfo wifiInfo = mWifiTools.GetWifiInfo();
                Spanned spannedWifiInfo = Html.fromHtml("" + "SSID: <font color='#ff0000'>" + wifiInfo.getSSID()
                        + "</font><br/>" + "IP: " + mWifiTools.integer2Ip(wifiInfo.getIpAddress()) + "<br/>" + "BSSID: "
                        + wifiInfo.getBSSID() + "<br/>" + "MAC: " + wifiInfo.getMacAddress() + "<br/>" + "State: "
                        + wifiInfo.getSupplicantState() + "<br/>" + "RSSI: <font color='#ff0000'>" + wifiInfo.getRssi()
                        + "</font><br/>" + "LinkSpeed: " + wifiInfo.getLinkSpeed() + "Mbps<br/>" + "NetID: "
                        + wifiInfo.getNetworkId() + "<br/>" + "");
                mTvResInfo.setText(spannedWifiInfo);
                mTvCon.setText(getString(R.string.WiFi_success));
                break;
            case WIFI_CONNECTING:
                mTvCon.setText(getString(R.string.WiFi_connecting));
                mTvCon.setTextColor(WiFiTest.this.getResources().getColor(R.color.Green));
                break;
            case WIFI_FAILED:
                mTvCon.setText(getString(R.string.WiFi_failed));
                mTvCon.setTextColor(WiFiTest.this.getResources().getColor(R.color.Red));
                break;
            case WIFI_NOTFOUND_OPENAP:
                mTvCon.setText(getString(R.string.WiFi_notfound_openap));
                mTvCon.setTextColor(WiFiTest.this.getResources().getColor(R.color.Red));
                break;
            }
        }
    };

    public boolean StartWifi() {
        UiHandler.sendEmptyMessage(WIFI_STATE);
        mWifiList = mWifiTools.scanWifi();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGridView.setAdapter(new MyAdapter(WiFiTest.this, mWifiList));
            }
        });
        mNetWorkName = Html.fromHtml("");
        if (mWifiList == null || mWifiList.size() <= 0) {
            return false;
        }
        if (mWifiList.size() > 0) {
            for (int i = 0; i < mWifiList.size(); i++) {
                ScanResult sr = mWifiList.get(i);
                mNetWorkName = Html.fromHtml(Html.toHtml(mNetWorkName) + "<font color='#ff0000'>" + sr.SSID
                        + "</font><br />" + sr.BSSID.toUpperCase() + "<br />Level: <font color='#ff0000'><strong>"
                        + sr.level + " dBm</strong></font>" + "");
            }
            UiHandler.sendEmptyMessage(WIFI_LIST);
            for (int j = 0; j < mWifiList.size(); j++) {
                ScanResult sr = mWifiList.get(j);
                if (sr.SSID.equals("MTK-FACTORY")) {
                    mWifiTools.connectWifi(sr.SSID, "12345678", KeyMgmt.WPA_PSK);
                    return true;
                }
                if (sr.capabilities.equals("[WPS]") || sr.capabilities.equals("[WPS][ESS]")
                        || sr.capabilities.equals("")) {
                    mResult = mWifiTools.addWifiConfig(mWifiList, sr, "");
                    if (mResult) {
                        return true;
                    }
                }
            }
            mListFlag = true;
            return false;
        }
        return false;
    };

    @Override
    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.wifi_name,
                (v.getId() == mBtOk.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        finish();
    }

    class MyAdapter extends BaseAdapter {
        private Context context;
        private List<ScanResult> wifiList;

        public MyAdapter(Context context, List<ScanResult> wifiList) {
            this.context = context;
            this.wifiList = wifiList;
        }

        @Override
        public int getCount() {
            if (wifiList == null) {
                return 0;
            }
            return wifiList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.wifi_list_grid, null);
            TextView wifiSSID = (TextView) view.findViewById(R.id.wifi_ssid);
            TextView wifiMac = (TextView) view.findViewById(R.id.wifi_mac);
            TextView wifiLevel = (TextView) view.findViewById(R.id.wifi_level);
            ScanResult result = wifiList.get(position);
            wifiSSID.setText(result.SSID);
            wifiMac.setText(result.BSSID.toUpperCase());
            wifiLevel.setText(result.level + "");
            return view;
        }
    }
}
