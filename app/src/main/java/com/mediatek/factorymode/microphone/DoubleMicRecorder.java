package com.mediatek.factorymode.microphone;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioSystem;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DoubleMicRecorder extends TestActivity implements OnClickListener {

    private static final String TAG = "DoubleMicRecorder";

    private Button mRecord;
    private Button mBtMicOk;
    private Button mBtMicFailed;
    private TextView mTipsView;

    private SharedPreferences mSp;
    private boolean mMaserMic;
    private boolean mTestState = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.keepScreenOn(this);
        setContentView(R.layout.double_micrecorder);

        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mRecord = (Button) findViewById(R.id.mic_bt_start);
        mRecord.setOnClickListener(this);
        mBtMicOk = (Button) findViewById(R.id.mic_bt_ok);
        mBtMicOk.setOnClickListener(this);
        mBtMicOk.setEnabled(false);
        mBtMicFailed = (Button) findViewById(R.id.mic_bt_failed);
        mBtMicFailed.setOnClickListener(this);
        mTipsView = (TextView) findViewById(R.id.mic_tips);

        if ("micmode=2".equals(getIntent().getStringExtra("micmode"))) {
            mMaserMic = false;
        } else {
            mMaserMic = true;
        }
        mTipsView.setText(getString(R.string.choose_what)
                + (mMaserMic ? getString(R.string.btn_mic1) : getString(R.string.btn_mic2)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.mic_bt_start:
            if (mTestState) {
                AudioSystem.setParameters("SET_LOOPBACK_TYPE=0");
                mBtMicOk.setEnabled(true);
                mRecord.setText(R.string.start_test);
            } else {
                if (mMaserMic) {
                    AudioSystem.setParameters("SET_LOOPBACK_TYPE=1,3");
                } else {
                    AudioSystem.setParameters("SET_LOOPBACK_TYPE=3,3");
                }
                mBtMicOk.setEnabled(false);
                mRecord.setText(R.string.stop_test);
            }
            mTestState = !mTestState;
            break;
        case R.id.mic_bt_ok:
            Utils.SetPreferences(this, mSp, getResouceName(), AppDefine.FT_SUCCESS);
            finish();
            break;
        case R.id.mic_bt_failed:
            Utils.SetPreferences(this, mSp, getResouceName(), AppDefine.FT_FAILED);
            finish();
            break;

        }
    }

    private int getResouceName() {
        return mMaserMic ? R.string.btn_mic1 : R.string.btn_mic2;
    }

    @Override
    protected void onPause() {
        AudioSystem.setParameters("SET_LOOPBACK_TYPE=0");
        super.onPause();
    }
}
