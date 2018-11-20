package com.mediatek.factorymode.flashlamp;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class FlashLamp extends TestActivity implements OnClickListener {

    private Button mBtOk;
    private Button mBtFailed;
    private Button mOpen;
    private Button mClose;
    private Camera mCameraDevice;
    private Parameters parameters;
    private SharedPreferences mSp;
    private final int ERR_OK = 0;
    private final int ERR_ERR = 1;
    private boolean batteryLow = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flash_lamp);

        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        batteryLow = getIntent().getBooleanExtra("batterylow", false);
        mOpen = (Button) findViewById(R.id.open_light);
        mOpen.setOnClickListener(this);
        mClose = (Button) findViewById(R.id.close_light);
        mClose.setOnClickListener(this);
        mBtOk = (Button) findViewById(R.id.flash_lamp_ok);
        mBtOk.setOnClickListener(this);
        if (batteryLow) {
            Toast.makeText(this, R.string.flash_lamp_battery_low, Toast.LENGTH_LONG).show();
        }
        mBtOk.setEnabled(!batteryLow);
        mBtFailed = (Button) findViewById(R.id.flash_lamp_failed);
        mBtFailed.setOnClickListener(this);
    }

    public void onClick(View arg0) {
        try {
            if (arg0.getId() == mOpen.getId()) {
                lightopen();
            } else if (arg0.getId() == mClose.getId()) {
                lightclose();
            } else if (arg0.getId() == mBtOk.getId()) {
                Utils.SetPreferences(this, mSp, R.string.flash_lamp, AppDefine.FT_SUCCESS);
                finish();
            } else if (arg0.getId() == mBtFailed.getId()) {
                Utils.SetPreferences(this, mSp, R.string.flash_lamp, AppDefine.FT_FAILED);
                finish();
            }
        } catch (Exception e) {
        }
    }

    private void lightopen() {
        mCameraDevice = Camera.open();
        if (mCameraDevice == null) {
            Toast.makeText(this, R.string.flash_lamp_exp, Toast.LENGTH_LONG).show();
            mBtOk.setEnabled(false);
            return;
        }

        if (batteryLow) {
            return;
        }

        Parameters params = mCameraDevice.getParameters();
        params.setFlashMode(Parameters.FLASH_MODE_TORCH);
        mCameraDevice.setParameters(params);
        mCameraDevice.startPreview();
    }

    private void lightclose() {
        if (batteryLow) {
            return;
        }

        if (mCameraDevice != null) {
            mCameraDevice.stopPreview();
            mCameraDevice.release();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mCameraDevice != null) {
                mCameraDevice.release();
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lightopen();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mCameraDevice != null) {
            mCameraDevice.release();
            finish();
        }
    }
}
