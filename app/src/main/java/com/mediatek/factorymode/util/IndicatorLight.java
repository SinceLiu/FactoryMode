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
import android.util.Log;
import android.os.Handler;
import android.os.Message;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;
import com.mediatek.factorymode.ShellExe;

public class IndicatorLight extends TestActivity implements OnClickListener{
    SharedPreferences mSp;

    String TAG = "indicatorlight";

    private Button mIndicatorBtOk;
    private Button mIndicatorBtFailed;

    private final int RED = 0;
    private final int BLUE = 1;
    private final int GREEN = 2;


    private final int NULL = -1;
    private static final int EVENT_TICK = 0x01;
    private int mCurrentColor = RED;
    String RED_LED_DEV = "/sys/class/leds/red/brightness";
    String BLUE_LED_DEV = "/sys/class/leds/blue/brightness";
    String GREEN_LED_DEV = "/sys/class/leds/green/brightness";

    private String CmdON = "echo 255 > ";
    private String CmdOFF = "echo 0 > ";


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case EVENT_TICK:
                updateLightStates();
                sendEmptyMessageDelayed(EVENT_TICK, 500);
                break;
            }
        }
    };

    private void updateLightStates() {
        setledlightcolor(mCurrentColor);
        mCurrentColor++;
        if(mCurrentColor > GREEN) {
            mCurrentColor = RED;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.indicator_light);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);

        mIndicatorBtOk = (Button) findViewById(R.id.indicator_bt_ok);
        mIndicatorBtOk.setOnClickListener(this);
        mIndicatorBtFailed = (Button) findViewById(R.id.indicator_bt_failed);
        mIndicatorBtFailed.setOnClickListener(this);
        mCurrentColor = RED;
    }

    @Override
    public void onResume() {
        super.onResume();

        mHandler.sendEmptyMessageDelayed(EVENT_TICK, 100);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeMessages(EVENT_TICK);
        setledlightcolor(NULL);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void setledlightcolor(int color) {
        Log.d(TAG, "setledlightcolor color="+color);

        boolean red = false, blue = false, green = false;
        switch (color) {
            case RED:
                red = true;
                break;
            case BLUE:
                blue = true;
                break;
            case GREEN:
                green = true;
                break;
            default:
                break;
        }

        try {
            String[] RedCmd = {
                "/system/bin/sh", "-c", (red ? CmdON : CmdOFF)+RED_LED_DEV
            };
            int ret = ShellExe.execCommandOnServer(RedCmd);

            String[] BlueCmd = {
                "/system/bin/sh", "-c", (blue ? CmdON : CmdOFF)+BLUE_LED_DEV
            };
            ret = ShellExe.execCommandOnServer(BlueCmd);

            String[] GreenCmd = {
                "/system/bin/sh", "-c", (green ? CmdON : CmdOFF)+GREEN_LED_DEV
            };
            ret = ShellExe.execCommandOnServer(GreenCmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.indicator_bt_ok: {
            Utils.SetPreferences(this, mSp, R.string.indicatorlight_name, AppDefine.FT_SUCCESS);
            finish();

            break;
        }
        case R.id.indicator_bt_failed: {
            Utils.SetPreferences(this, mSp, R.string.indicatorlight_name, AppDefine.FT_FAILED);
            finish();

            break;
        }

        default: {

        }
        }

    }
    
}
