package com.mediatek.factorymode.otg;

import java.io.IOException;
import java.lang.ref.WeakReference;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.ShellExe;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class OtgState extends TestActivity implements OnClickListener {
    private static final String TAG = "OtgState";
    private TextView mStatus;
    private Button mBtOK;
    private Button mBtFailed;
    private SharedPreferences mSp;
    private static final int EVENT_TICK = 1;
    private MyHandler mHandler;

    private static class MyHandler extends Handler {
        private WeakReference<OtgState> reference;

        public MyHandler(OtgState activity) {
            reference = new WeakReference<OtgState>(activity);//这里传入activity的上下文
        }
        @Override
        public void handleMessage(Message msg) {
            OtgState activity = reference.get();
            switch (msg.what) {
            case EVENT_TICK:
                activity.updateOtgStates();
                sendEmptyMessageDelayed(EVENT_TICK, 1000);
                break;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otg_state_info);
        mHandler = new MyHandler(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mStatus = (TextView) findViewById(R.id.status);
        mBtOK = (Button) findViewById(R.id.otg_state_ok);
        mBtOK.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.otg_state_failed);
        mBtFailed.setOnClickListener(this);
        updateButtonStatus(false);

        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mHandler.sendEmptyMessageDelayed(EVENT_TICK, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeMessages(EVENT_TICK);
    }

    private int getOtgState() {
        String[] cmdx = { "/system/bin/sh", "-c", "cat /sys/class/switch/otg_state/state" }; // file
        int ret;
        try {
            ret = ShellExe.execCommand(cmdx);
            if (0 == ret) {
                // Toast.makeText(this, "ok", Toast.LENGTH_LONG).show();
            } else {
                // Toast.makeText(this, "failed!", Toast.LENGTH_LONG).show();
                return 0;
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return 0;
        }
        return Integer.valueOf(ShellExe.getOutput());
    }

    private void updateOtgStates() {
        if (getOtgState() == 1) {
            updateButtonStatus(true);
            mStatus.setText(R.string.otg_status_ok);
        } else {
            updateButtonStatus(false);
            mStatus.setText(R.string.otg_status_failed);
        }
    }

    private void updateButtonStatus(boolean status) {
        if (null != mBtOK) {
            mBtOK.setEnabled(status);
        }
    }

    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.otg_name,
                (v.getId() == mBtOK.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        finish();
    }
}
