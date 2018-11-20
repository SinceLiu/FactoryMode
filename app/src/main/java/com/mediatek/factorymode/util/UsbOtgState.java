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

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

public class UsbOtgState extends TestActivity implements OnClickListener{
    SharedPreferences mSp;

    private Button mUsbOtgBtOk;
    private Button mUsbOtgBtFailed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usb_otg);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);

        mUsbOtgBtOk = (Button) findViewById(R.id.usb_otg_bt_ok);
        mUsbOtgBtOk.setOnClickListener(this);
        mUsbOtgBtFailed = (Button) findViewById(R.id.usb_otg_bt_failed);
        mUsbOtgBtFailed.setOnClickListener(this);
    
        Intent mIntent = new Intent(Intent.ACTION_MAIN);
        mIntent.setClassName("com.cenon.usbotg", "com.cenon.usbotg.MainActivity");
        if (getPackageManager().resolveActivity(mIntent, 0) != null) { 
            startActivity(mIntent);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.usb_otg_name,
            (v.getId() == mUsbOtgBtOk.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);

        finish();

    }
    
}
