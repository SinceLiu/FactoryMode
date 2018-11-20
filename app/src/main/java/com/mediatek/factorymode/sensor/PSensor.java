package com.mediatek.factorymode.sensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PSensor extends TestActivity implements /*SensorEventListener, */OnClickListener {
    public static final String LOG_TAG = "Sensor";

    private Button mBtOk;
    private Button mFailed;
    private TextView mTvPsensor;
//    private SensorManager sensorManager;

    private int[] mAllPsensor = new int[1000];
    private static int mCount = 0;
    private int mPrePsensor = 0;
    private int mAverage = 0;
    private char[] mWrint = new char[1];
    private int mSumPsensor = 0;

    private SharedPreferences mSp;
    private int sensorEventCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.psensor);

//        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mBtOk = (Button) findViewById(R.id.psensor_bt_ok);
        mBtOk.setOnClickListener(this);
        mFailed = (Button) findViewById(R.id.psensor_bt_failed);
        mFailed.setOnClickListener(this);
        mTvPsensor = (TextView) findViewById(R.id.proximity);
        sensorEventCount = 0;
        mBtOk.setEnabled(false);
    }

    protected void onResume() {
        super.onResume();
//        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
//        for (Sensor s : sensors) {
//            sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
//        }
        mHandler.post(myRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        sensorManager.unregisterListener(this);
        mHandler.removeCallbacks(myRunnable);
    }

    private Handler mHandler = new Handler();
    private Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            File file = new File("/sys/bus/platform/drivers/als_ps/ps");
            Log.v(LOG_TAG, "psensor(als_ps): mPrePsensor=" + mPrePsensor);

            if (!file.exists()) {
                Log.v(LOG_TAG, "psensor(als_ps_cm3623): mPrePsensor=" + mPrePsensor);
                file = new File("/sys/bus/platform/drivers/als_ps_cm3623/ps");
            }

            if (file.exists()) {
                String pSensorValues2 = readFile(file);

                if (pSensorValues2.matches("^\\D.*")) {
                    mPrePsensor = 0;
                    mAllPsensor[mCount] = mPrePsensor;
                    mCount++;
                    mTvPsensor.setText(getResources().getString(R.string.proximity) + " " + "Error");
                } else {
                    mPrePsensor = Integer.parseInt(pSensorValues2.trim(), 16); // bob.chen
                    mAllPsensor[mCount] = mPrePsensor;
                    mCount++;
                    mTvPsensor.setText(getResources().getString(R.string.proximity) + " " + mPrePsensor);
                }
                Log.v(LOG_TAG, "psensor: mPrePsensor2=" + mPrePsensor);
            }

            for (int i = 0; i < mCount; i++) {
                mSumPsensor = mSumPsensor + mAllPsensor[i];
                mAllPsensor[i] = 0;
            }

            if (mCount > 0) {
                mAverage = mSumPsensor / mCount + 1;
                mWrint[0] = (char) mAverage;
            }

            mCount = 0;
            mSumPsensor = 0;

            mHandler.postDelayed(myRunnable, 100);
        }
    };

    private static String readFile(File fn) {
        FileReader f;
        int len;

        f = null;
        try {
            f = new FileReader(fn);
            String s = "";
            char[] cbuf = new char[200];
            while ((len = f.read(cbuf, 0, cbuf.length)) >= 0) {
                s += String.valueOf(cbuf, 0, len);
            }
            s = s.substring(2, s.length() - 1); // ellery add
            return s;
        } catch (IOException ex) {
            return "0";
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException ex) {
                    return "0";
                }
            }
        }
    }

//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        if (sensor.getType() == Sensor.TYPE_PROXIMITY) {
//            mTvPsensor.setText(getString(R.string.LSensor_accuracy) + accuracy);
//        }
//    }
//
//    public void onSensorChanged(SensorEvent event) {
//        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
//            float[] values = event.values;
//            mTvPsensor.setText(getString(R.string.proximity) + values[0]);
//            sensorEventCount++;
//            if (sensorEventCount > 1 && mBtOk != null) {
//                mBtOk.setEnabled(true);
//            }
//        }
//    }

    @Override
    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.psensor_name,
                (v.getId() == mBtOk.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        finish();
    }
}
