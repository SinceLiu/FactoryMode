package com.mediatek.factorymode.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.text.TextUtils;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class Bluetooth extends TestActivity implements OnClickListener {
    private BluetoothAdapter bluetoothAdapter = null;

    private TextView mTvInfo = null;
    private TextView mTvScan = null;
    private TextView mTvResult = null;
    private TextView mTvCon = null;
    private TextView mTvMac = null;
    private Button mBtOk;
    private Button mBtFailed;
    private Button mBtDiscovery;

    private String mNameList = "";
    private List<String> devices;   //用于去重
    private SharedPreferences mSp;
    private IntentFilter intentFilter;
	private boolean mBlueFlag = false;
    HandlerThread mBlueThread = new HandlerThread("blueThread");
    BlueHandler mBlueHandler;
    Message msg = null;
    boolean isBluetoothOpened;  //记录之前蓝牙状态，退出时恢复
    private MyHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Bluetooth", "——————————onCreate()");
        setContentView(R.layout.ble_test);
        mHandler = new MyHandler(this);
        mTvInfo = (TextView) findViewById(R.id.ble_state_id);
        mTvScan = (TextView) findViewById(R.id.ble_scan);
        mTvResult = (TextView) findViewById(R.id.ble_result_id);
        mTvCon = (TextView) findViewById(R.id.ble_con_id);
        mTvMac = (TextView) findViewById(R.id.ble_mac);
        mBtDiscovery = (Button) findViewById(R.id.ble_discovery);
        mBtDiscovery.setOnClickListener(this);
        mBtDiscovery.setEnabled(false);
        mBtOk = (Button) findViewById(R.id.ble_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.ble_bt_failed);
        mBtFailed.setOnClickListener(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBlueThread.start();
        mBlueHandler = new BlueHandler(mBlueThread.getLooper());
        mBlueHandler.post(bluerunnable);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        devices = new ArrayList<String>();
        setBtStatus();
        if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
            isBluetoothOpened = true;
            mTvInfo.setText(R.string.Bluetooth_open);
        } else {
            isBluetoothOpened = false;
            mTvInfo.setText(R.string.Bluetooth_opening);
        }

    }

    @Override
    protected void onResume() {
        Log.e("Bluetooth", "——————————onResume()");
        super.onResume();
    }

    private void setBtStatus() {
        if (bluetoothAdapter != null) {
            String address = bluetoothAdapter.isEnabled() ? bluetoothAdapter.getAddress() : "";
            if (!TextUtils.isEmpty(address)) {
                mTvMac.setText(getString(R.string.Bluetooth_mac_device) + " " + address);
                mTvInfo.setText(R.string.Bluetooth_open);
            } else {
                mTvMac.setText(R.string.status_unavailable);
            }
        }
    }

    @Override
    protected void onPause(){
        Log.e("Bluetooth", "——————————onPause()");
        super.onPause();
        if(isFinishing()){
            if(bluetoothAdapter.isDiscovering()){
                bluetoothAdapter.cancelDiscovery();
            }
            if (!isBluetoothOpened && bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            }
        }
    }


    protected void onDestroy() {
        Log.e("Bluetooth", "——————————onDestroy()");
        super.onDestroy();
        if(mBlueFlag){
            unregisterReceiver(mReceiver);
        }
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        if (!isBluetoothOpened && bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("Bluetooth", action);
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    mTvInfo.setText(R.string.Bluetooth_open);
                } else if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    mTvInfo.setText(R.string.Bluetooth_closed);
                }
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                setBtStatus();
                mTvScan.setVisibility(View.VISIBLE);
                mTvScan.setText(R.string.Bluetooth_scaning);
                mTvCon.setVisibility(View.INVISIBLE);
                mNameList = "";
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e("Bluetooth", device.getName()+"---"+device.getAddress());
                if (device.getBondState() != BluetoothDevice.BOND_BONDED &&
                        !devices.contains(device.getAddress())) {   //去重
                    mNameList += device.getName() + "———" + getString(R.string.Bluetooth_mac) + device.getAddress()
                            + "\n";
                    mTvResult.setText(mNameList);
                    devices.add(device.getAddress());
                }
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                devices.clear();
                mTvScan.setVisibility(View.GONE);
                mTvCon.setVisibility(View.VISIBLE);
                mTvCon.setText(R.string.Bluetooth_scan_success);
                mBtDiscovery.setEnabled(true);
            }
        }
    };

    private static class MyHandler extends Handler {
        private WeakReference<Bluetooth> reference;

        public MyHandler(Bluetooth activity) {
            reference = new WeakReference<Bluetooth>(activity);//这里传入activity的上下文
        }
        @Override
        public void handleMessage(Message msg) {
            Bluetooth activity = reference.get();
            super.handleMessage(msg);
            activity.mBlueHandler.removeCallbacks(activity.bluerunnable);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            activity.registerReceiver(activity.mReceiver, intentFilter);
            activity.mBlueFlag = true;
            while (!activity.bluetoothAdapter.startDiscovery()) {
                activity.bluetoothAdapter.startDiscovery();
            }
        }
    }

    Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            if (bluetoothAdapter.enable()) {
                mHandler.removeCallbacks(myRunnable);
                init();
            } else {
                mHandler.post(myRunnable);
            }
        }
    };

    private void init() {
        bluetoothAdapter.enable();
        if (bluetoothAdapter.isEnabled()) {
            Log.e("Bluetooth","init()————isEnabled");
            msg = mHandler.obtainMessage();
            msg.sendToTarget();
        } else {
            Log.e("Bluetooth","init()————postDelayed");
            mBlueHandler.postDelayed(bluerunnable, 1000);
        }
    }

    class BlueHandler extends Handler {
        public BlueHandler() {
        }

        public BlueHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    Runnable bluerunnable = new Runnable() {
        @Override
        public void run() {
            init();
        }
    };

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ble_discovery:
                mTvResult.setText(null);
                mTvCon.setVisibility(View.GONE);
                mBlueHandler.post(bluerunnable);
                mBtDiscovery.setEnabled(false);
                break;

            case R.id.ble_bt_ok:
                Utils.SetPreferences(this, mSp, R.string.bluetooth_name, AppDefine.FT_SUCCESS);
                finish();
                break;

            case R.id.ble_bt_failed:
                Utils.SetPreferences(this, mSp, R.string.bluetooth_name, AppDefine.FT_FAILED);
                finish();
                break;
        }
    }
}
