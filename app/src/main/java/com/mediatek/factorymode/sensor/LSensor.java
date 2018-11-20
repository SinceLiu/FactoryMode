package com.mediatek.factorymode.sensor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

public class LSensor extends TestActivity implements SensorEventListener, View.OnClickListener {
    private TextView mAccuracyView = null;
    private TextView mValueX = null;
    private Button mBtOk;
    private Button mBtFailed;
    private SharedPreferences mSp;
    private Sensor mLightSensor = null;
    private SensorManager mSensorManager = null;
    private int sensorEventCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lsensor);

        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mAccuracyView = (TextView) findViewById(R.id.lsensor_accuracy);
        mValueX = (TextView) findViewById(R.id.lsensor_value);
        mBtOk = (Button) findViewById(R.id.lsensor_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.lsensor_bt_failed);
        mBtFailed.setOnClickListener(this);
        sensorEventCount = 0;
        mBtOk.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mLightSensor);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_LIGHT) {
            mAccuracyView.setText(getString(R.string.LSensor_accuracy) + accuracy);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float[] values = event.values;
            mValueX.setText(getString(R.string.LSensor_value) + values[0]);
            sensorEventCount++;
            if (sensorEventCount > 1 && mBtOk != null) {
                mBtOk.setEnabled(true);
            }
        }
    }

    @Override
    public void onClick(View v) {
        Utils.SetPreferences(getApplicationContext(), mSp, R.string.lsensor_name,
                (v.getId() == mBtOk.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        finish();
    }
}
