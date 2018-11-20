package com.mediatek.factorymode.sensor;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.FactoryModeFeatureOption;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class GSensorWithCalibrate extends TestActivity implements OnClickListener {
    private TextView tvSensorX;
    private TextView tvSensorY;
    private TextView tvSensorZ;
    private TextView tvSensorResult;
    private Button btnSensorCalicate;
    private Button btnSensorReTest;

    private Button mBtOk;
    private Button mBtFailed;
    private MyHandler mHandler;
    SharedPreferences mSp;
    SensorManager mSm = null;

    Sensor mGravitySensor;

    private final static int OFFSET = 2;

    private static final int MSG_GSENSOR_UPDATING = 0x11;
    private static final int MSG_GSENSOR_RESULT = 0x12;
    private static final int MSG_GSENSOR_RETEST = 0x13;
    private static final int MSG_DO_CALIBRATION_20 = 0x14;
    private static final int MSG_DO_CALIBRATING = 0x15;
    private static final int MSG_CALIBRATION_SUCCESS = 0x16;
    private static final int MSG_CALIBRATION_FAIL = 0x17;
    private int count = 0;
    private boolean result = false;

    private static class MyHandler extends Handler {
        private WeakReference<GSensorWithCalibrate> reference;

        public MyHandler(GSensorWithCalibrate activity) {
            reference = new WeakReference<GSensorWithCalibrate>(activity);//这里传入activity的上下文
        }

        @Override
        public void handleMessage(Message msg) {
            GSensorWithCalibrate activity = reference.get();
            switch (msg.what) {
                case MSG_GSENSOR_UPDATING:
                    activity.tvSensorResult.setText(activity.getResources().getString(R.string.GSensor_with_calibrate_result)
                            + (activity.count % 3 == 0 ? "." : (activity.count % 3 == 1 ? ".." : (activity.count % 3 == 2 ? "..." : ""))));

                    if (activity.count < 10) {
                        activity.mHandler.sendEmptyMessageDelayed(MSG_GSENSOR_UPDATING, 200);
                        activity.count++;
                    } else {
                        activity.count = 0;
                        activity.mHandler.sendEmptyMessageDelayed(MSG_GSENSOR_RESULT, 200);
                    }
                    break;
                case MSG_GSENSOR_RESULT:
                    if (activity.result) {
                        activity.tvSensorResult.setText(R.string.Success);
                        activity.btnSensorCalicate.setVisibility(View.GONE);
                        activity.mBtOk.setEnabled(true);
                    } else {
                        activity.tvSensorResult.setText(R.string.Failed);
                        activity.btnSensorCalicate.setVisibility(View.VISIBLE);
                        activity.mBtOk.setEnabled(false);
                    }
                    activity.count = 0;
                    break;
                case MSG_GSENSOR_RETEST:
                    activity.mHandler.sendEmptyMessageDelayed(MSG_GSENSOR_UPDATING, 20);
                    activity.btnSensorReTest.setVisibility(View.GONE);
                    activity.count = 0;
                    break;
                case MSG_DO_CALIBRATION_20:
                    activity.btnSensorCalicate.setEnabled(false);
                    activity.mHandler.sendEmptyMessageDelayed(MSG_DO_CALIBRATING, 20);
                    activity.count = 0;
                    break;
                case MSG_DO_CALIBRATING:
                    activity.tvSensorResult.setText(activity.getString(R.string.GSensor_with_calibrate_do_calibrate)
                            + (activity.count % 3 == 0 ? "." : (activity.count % 3 == 1 ? ".." : (activity.count % 3 == 2 ? "..." : ""))));

                    if (activity.count < 50) {
                        activity.mHandler.sendEmptyMessageDelayed(MSG_DO_CALIBRATING, 200);
                        activity.count++;
                    } else {
                        activity.count = 0;
                        activity.mHandler.sendEmptyMessageDelayed(MSG_CALIBRATION_FAIL, 200);
                    }
                    break;
                case MSG_CALIBRATION_SUCCESS:
                    activity.tvSensorResult.setText(R.string.proximity_success);
                    activity.mHandler.removeMessages(MSG_DO_CALIBRATING);
                    activity.count = 0;
                    activity.btnSensorCalicate.setEnabled(true);
                    activity.btnSensorCalicate.setVisibility(View.GONE);
                    activity.btnSensorReTest.setVisibility(View.VISIBLE);
                    break;
                case MSG_CALIBRATION_FAIL:
                    activity.tvSensorResult.setText(R.string.proximity_fail);
                    activity.mHandler.removeMessages(MSG_DO_CALIBRATING);
                    activity.count = 0;
                    activity.btnSensorCalicate.setEnabled(true);
                    activity.btnSensorCalicate.setVisibility(View.VISIBLE);
                    activity.btnSensorReTest.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.mediatek.factorymode.action_calibration_result".equals(action)) {
                boolean result = intent.getBooleanExtra("result", false);
                if (result) {
                    // success
                    mHandler.sendEmptyMessage(MSG_CALIBRATION_SUCCESS);
                } else {
                    // failed
                    mHandler.sendEmptyMessage(MSG_CALIBRATION_FAIL);
                }
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gsensor_with_calibrate);
        mHandler = new MyHandler(this);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        tvSensorX = (TextView) findViewById(R.id.gsensor_tv_x);
        tvSensorY = (TextView) findViewById(R.id.gsensor_tv_y);
        tvSensorZ = (TextView) findViewById(R.id.gsensor_tv_z);
        tvSensorResult = (TextView) findViewById(R.id.gsensor_tv_result);
        mHandler.sendEmptyMessageDelayed(MSG_GSENSOR_UPDATING, 200);
        btnSensorCalicate = (Button) findViewById(R.id.gsensor_btn_calicate);
        btnSensorCalicate.setOnClickListener(this);
        btnSensorReTest = (Button) findViewById(R.id.gsensor_btn_retest);
        btnSensorReTest.setOnClickListener(this);
        btnSensorReTest.setVisibility(View.GONE);
        mBtOk = (Button) findViewById(R.id.gsensor_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtOk.setEnabled(false);
        mBtFailed = (Button) findViewById(R.id.gsensor_bt_failed);
        mBtFailed.setOnClickListener(this);
        mSm = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGravitySensor = mSm.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER); //TYPE_GRAVITY  //ellery modify
        mSm.registerListener(lsn, mGravitySensor, SensorManager.SENSOR_DELAY_GAME);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.mediatek.factorymode.action_calibration_result");
        registerReceiver(mReceiver, intentFilter);
    }

    protected void onDestroy() {
        mSm.unregisterListener(lsn);
        unregisterReceiver(mReceiver);
        mHandler.removeCallbacksAndMessages(null);  //把消息对象从消息队列移除
        super.onDestroy();
    }

    SensorEventListener lsn = new SensorEventListener() {
        public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent e) {
            float xx = e.values[SensorManager.DATA_X];
            float yy = e.values[SensorManager.DATA_Y];
            float zz = e.values[SensorManager.DATA_Z];
            tvSensorX.setText("X: " + ((xx >= 0.0) ? "+" : "") + xx);
            tvSensorY.setText("Y: " + ((yy >= 0.0) ? "+" : "") + yy);
            tvSensorZ.setText("Z: " + ((zz >= 0.0) ? "+" : "") + zz);

            if (e.sensor == mGravitySensor) {
                float x = (float) e.values[SensorManager.DATA_X];
                float y = (float) e.values[SensorManager.DATA_Y];
                float z = (float) e.values[SensorManager.DATA_Z];

                float absx = Math.abs(x);
                float absy = Math.abs(y);
                float absz = Math.abs(z);

                boolean zzz = (absz >= 7.0 && absz <= 13.0);
                if (FactoryModeFeatureOption.get("ro.cenon_factorymode_feature").equals("1")) {
                    zzz = (absz >= 5.0 && absz <= 15.0);
                }
                result = (absx <= 5.0 && absy <= 5.0 && zzz);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gsensor_bt_ok:
                Utils.SetPreferences(this, mSp, R.string.gsensor_with_calibrate_name, AppDefine.FT_SUCCESS);
                finish();
                break;
            case R.id.gsensor_bt_failed:
                Utils.SetPreferences(this, mSp, R.string.gsensor_with_calibrate_name, AppDefine.FT_FAILED);
                finish();
                break;
            case R.id.gsensor_btn_calicate:
                mHandler.sendEmptyMessage(MSG_DO_CALIBRATION_20);
                Intent intent = new Intent();
                intent.setAction("com.mediatek.engineermode.action_sensor_emsensor");
                sendBroadcast(intent);
                break;
            case R.id.gsensor_btn_retest:
                mHandler.sendEmptyMessageDelayed(MSG_GSENSOR_RETEST, 200);
                break;
        }
    }
}