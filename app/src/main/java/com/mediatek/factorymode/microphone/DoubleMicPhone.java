package com.mediatek.factorymode.microphone;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DoubleMicPhone extends TestActivity implements OnClickListener {

    private static final String TAG = "DoubleMicPhone";
    private TextView mTvMic1 = null;
    private TextView mTvMic2 = null;
    private Button mBtnSuccess = null;
    private Button mBtnFailed = null;
    private SharedPreferences mSp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.double_micphonebase);
        initViews();
    }

    private void initViews() {
        mTvMic1 = (TextView) findViewById(R.id.tv_mic1);
        mTvMic1.setOnClickListener(this);
        mTvMic2 = (TextView) findViewById(R.id.tv_mic2);
        mTvMic2.setOnClickListener(this);
        mBtnSuccess = (Button) findViewById(R.id.button_ok);
        mBtnFailed = (Button) findViewById(R.id.button_fail);
        mBtnSuccess.setOnClickListener(this);
        mBtnFailed.setOnClickListener(this);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String mic1Ok = mSp.getString(getResources().getString(R.string.btn_mic1), AppDefine.FT_DEFAULT);
        if (mic1Ok.equals(AppDefine.FT_SUCCESS)) {
            mTvMic1.setBackgroundResource(R.drawable.btn_success_bg);
        } else if (mic1Ok.equals(AppDefine.FT_FAILED)) {
            mTvMic1.setBackgroundResource(R.drawable.btn_fail_bg);
        } else {
            mTvMic1.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
        String mic2Ok = mSp.getString(getResources().getString(R.string.btn_mic2), AppDefine.FT_DEFAULT);
        if (mic2Ok.equals(AppDefine.FT_SUCCESS)) {
            mTvMic2.setBackgroundResource(R.drawable.btn_success_bg);
        } else if (mic2Ok.equals(AppDefine.FT_FAILED)) {
            mTvMic2.setBackgroundResource(R.drawable.btn_fail_bg);
        } else {
            mTvMic2.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
        if (mic1Ok.equals(AppDefine.FT_SUCCESS) && mic2Ok.equals(AppDefine.FT_SUCCESS)) {
            mBtnSuccess.setEnabled(true);
        } else {
            mBtnSuccess.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
        case R.id.tv_mic1:
            intent = new Intent();
            intent.setClass(this, DoubleMicRecorder.class);
            intent.putExtra("micmode", "micmode=1");
            startActivity(intent);
            break;
        case R.id.tv_mic2:
            intent = new Intent();
            intent.setClass(this, DoubleMicRecorder.class);
            intent.putExtra("micmode", "micmode=2");
            startActivity(intent);
            break;
        case R.id.button_ok:
        case R.id.button_fail:
            Utils.SetPreferences(this, mSp, R.string.double_microphone_name,
                    (v.getId() == R.id.button_ok) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
            finish();
            break;
        }
    }
}
