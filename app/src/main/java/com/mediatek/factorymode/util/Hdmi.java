package com.mediatek.factorymode.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import java.io.FileOutputStream;
import android.os.Handler;
import android.widget.Toast;
import android.os.Message;
import android.util.Log;
import java.io.IOException;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.ShellExe;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

public class Hdmi extends TestActivity implements OnClickListener{
    private static final String TAG = "Hdmi";

    SharedPreferences mSp;

    private TextView mStatus;
    private Button mHdmiBtOk;
    private Button mHdmiBtFailed;
    private static final int EVENT_TICK = 0x01;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case EVENT_TICK:
                updateHdmiStates();
                sendEmptyMessageDelayed(EVENT_TICK, 1000);
                break;
            }
        }
    };

    private int getHdmiState() {
        String[] cmdx = { "/system/bin/sh", "-c",
                "cat /sys/class/switch/hdmi/state" }; // file
        int ret = 0;
        try {
            ret = ShellExe.execCommand(cmdx);
            if (0 == ret) {
               // Toast.makeText(this, "ok", Toast.LENGTH_LONG).show();
            } else {
              //  Toast.makeText(this, "failed!", Toast.LENGTH_LONG).show();
                return 0;
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return 0;
        }
        return Integer.valueOf(ShellExe.getOutput());
    }

    private void updateHdmiStates() {
        if(getHdmiState() == 1) {
            mStatus.setText(R.string.hdmi_status_ok);
        } else {
            mStatus.setText(R.string.hdmi_status_failed);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hdmi);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);

        mStatus = (TextView) findViewById(R.id.status);
        mHdmiBtOk = (Button) findViewById(R.id.hdmi_bt_ok);
        mHdmiBtOk.setOnClickListener(this);
        mHdmiBtFailed = (Button) findViewById(R.id.hdmi_bt_failed);
        mHdmiBtFailed.setOnClickListener(this);

    }
    @Override
    public void onResume() {
        super.onResume();

        mHandler.sendEmptyMessageDelayed(EVENT_TICK, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeMessages(EVENT_TICK);
    }


    @Override
    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.hdmi_name,
            (v.getId() == mHdmiBtOk.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);

        finish();

    }
    
}
