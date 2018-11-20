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
import java.io.UnsupportedEncodingException;
import android.util.Log;
import java.io.IOException;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.ShellExe;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

public class Fingerprint extends TestActivity implements OnClickListener{
    private static final String TAG = "Fingerprint";
    SharedPreferences mSp;

    private Button mBtOk;
    private Button mBtFailed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);

        mBtOk = (Button) findViewById(R.id.finger_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.finger_bt_failed);
        mBtFailed.setOnClickListener(this);

        Intent mIntent = new Intent();
        // 此处如果就一种指纹就注释掉下面的判断
        if(getFingerType("/dev/madev0") == 0) {
            mIntent.setClassName("ma.factory", "ma.factory.MAFactoryActivity");
        } else {
            mIntent.setClassName("com.swfp.factory", "com.swfp.activity.MainActivity");
        }
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (getPackageManager().resolveActivity(mIntent, 0) != null) {
            startActivity(mIntent);
            finish();
        }

    }

    private int getFingerType(String dev) {
        String[] cmdx = { "/system/bin/sh", "-c",
                "ls " + dev }; // file
        int ret = -1;
        try {
            ret = ShellExe.execCommand(cmdx);
            Log.i(TAG, "getFingerType ret=" + ret);
        } catch (IOException e) {
            Log.e(TAG, "getFingerType ex ret=" + ret);
        }
        return ret;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.fingerprint_recognition,
            (v.getId() == mBtOk.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        finish();
    }
    
}
