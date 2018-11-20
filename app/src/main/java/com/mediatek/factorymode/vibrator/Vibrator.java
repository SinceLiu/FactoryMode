package com.mediatek.factorymode.vibrator;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Vibrator extends TestActivity implements OnClickListener {
    private android.os.Vibrator mVibrator;
    private Button mBtOk;
    private Button mBtFailed;
    private SharedPreferences mSp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vibrator);

        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mBtOk = (Button) findViewById(R.id.vibrator_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.vibrator_bt_failed);
        mBtFailed.setOnClickListener(this);

        mVibrator = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
        mVibrator.vibrate(10000);
    }

    public void onDestroy() {
        super.onDestroy();
        mVibrator.cancel();
    }

    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.vibrator_name,
                (v.getId() == mBtOk.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        finish();
    }
}
