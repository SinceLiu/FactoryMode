package com.mediatek.factorymode.backlight;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.util.Log;
import android.os.Handler;
import android.os.Message;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.ShellExe;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

public class BackLight extends TestActivity implements OnClickListener {
    public static final String TAG = "BackLight";

    private Button mBtnLcdON;
    private Button mBtnLcdOFF;
    private Button mBtOk;
    private Button mBtFailed;

    private SharedPreferences mSp;

    private String lcdCmdON = "echo 255 >  /sys/class/leds/lcd-backlight/brightness";
    private String lcdCmdOFF = "echo 30 >  /sys/class/leds/lcd-backlight/brightness";

    private final int ERR_OK = 0;
    private final int ERR_ERR = 1;

    private static final int EVENT_TICK = 0x01;
    private boolean mCurrentLightStrong;
    private MyHandler mHandler;

    private static class MyHandler extends Handler {
        private WeakReference<BackLight> reference;

        public MyHandler(BackLight activity) {
            reference = new WeakReference<BackLight>(activity);//这里传入activity的上下文
        }
        @Override
        public void handleMessage(Message msg) {
            BackLight activity = reference.get();
            switch (msg.what) {
            case EVENT_TICK:
                activity.updateLightStates();
                sendEmptyMessageDelayed(EVENT_TICK, 1000);
                break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backlight);
        mHandler = new MyHandler(this);
        mBtnLcdON = (Button) findViewById(R.id.Display_lcd_on);
        mBtnLcdOFF = (Button) findViewById(R.id.Display_lcd_off);
        mBtOk = (Button) findViewById(R.id.display_bt_ok);
        mBtFailed = (Button) findViewById(R.id.display_bt_failed);

        mBtnLcdON.setOnClickListener(this);
        mBtnLcdOFF.setOnClickListener(this);
        mBtOk.setOnClickListener(this);
        mBtFailed.setOnClickListener(this);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);

        mCurrentLightStrong = false;
    }

    private void setLastError(int err) {
        Log.v(TAG, "setLastError(): " + err);
    }

    private void updateLightStates() {
        mCurrentLightStrong = !mCurrentLightStrong;
        setledlightStrength(mCurrentLightStrong);
    }

    private void setledlightStrength(boolean isStrong) {
        Log.d(TAG, "setledlightcolor isStrong=" + isStrong);
        try {
            String[] cmd = {
                "/system/bin/sh", "-c", isStrong ? lcdCmdON : lcdCmdOFF
            };
            int ret = ShellExe.execCommandOnServer(cmd);
            if (0 == ret) {
                setLastError(ERR_OK);
            } else {
                setLastError(ERR_ERR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    }

    private int getCurrentSystemBrightness(){
        int brightness = -1;
        try{
            brightness = Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS);
        } catch(SettingNotFoundException e) {
            e.printStackTrace();
        }
        return brightness;
    }

    public void onClick(View arg0) {
        try {
            if (arg0.getId() == mBtnLcdON.getId()) {
                String[] cmd = {
                    "/system/bin/sh", "-c", lcdCmdON
                };
                int ret = ShellExe.execCommandOnServer(cmd);
                if (0 == ret) {
                    setLastError(ERR_OK);
                } else {
                    setLastError(ERR_ERR);
                }
            } else if (arg0.getId() == mBtnLcdOFF.getId()) {
                String[] cmd = {
                    "/system/bin/sh", "-c", lcdCmdOFF
                };
                int ret = ShellExe.execCommandOnServer(cmd);
                if (0 == ret) {
                    setLastError(ERR_OK);
                } else {
                    setLastError(ERR_ERR);
                }
            } else if (arg0.getId() == mBtOk.getId()) {
                Utils.SetPreferences(this, mSp, R.string.backlight_name, AppDefine.FT_SUCCESS);
                finish();
            } else if (arg0.getId() == mBtFailed.getId()) {
                Utils.SetPreferences(this, mSp, R.string.backlight_name, AppDefine.FT_FAILED);
                finish();
            }
        } catch (IOException e) {
            setLastError(ERR_ERR);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(getCurrentSystemBrightness() != -1) {
            String brightness = "echo " + 
                Integer.valueOf(getCurrentSystemBrightness()) + " >  /sys/class/leds/lcd-backlight/brightness";
            try{
                String[] cmd = {
                    "/system/bin/sh", "-c", brightness};
                int ret = ShellExe.execCommandOnServer(cmd);
                if (0 == ret) {
                    setLastError(ERR_OK);
                } else {
                    setLastError(ERR_ERR);
                }
            } catch(IOException e) {
                setLastError(ERR_ERR);
            }
        }
    }
}
