package com.mediatek.factorymode.hall;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;
//import android.util.PhoneKpdNative;  //there is no function, 2015.05.28


public class Hall extends TestActivity implements OnClickListener {

    private TextView mStatus;
    private Button mBtOK;
    private Button mBtFailed;
    private SharedPreferences mSp;
    private static final int EVENT_TICK = 1;
    private static final int EVENT_TICK_DOWN = 2;
    private static final int EVENT_TICK_UP = 3;
    private boolean isKeyOk = false;
    String flag = "";
    private MyHandler mHandler;

    private static class MyHandler extends Handler {
        private WeakReference<Hall> reference;

        public MyHandler(Hall activity) {
            reference = new WeakReference<Hall>(activity);//这里传入activity的上下文
        }
        @Override
        public void handleMessage(Message msg) {
            Hall activity = reference.get();
            switch (msg.what) {
            case EVENT_TICK:
//                activity.updateHallStats();
//                sendEmptyMessageDelayed(EVENT_TICK, 1000);
                break;
            case EVENT_TICK_DOWN:
                //mStatus.setText(R.string.hall_on);
                break;
            case EVENT_TICK_UP:
                activity.mStatus.setText(R.string.hall_on);
                break;
            }
        }
    }

    private void updateHallStats() {
        boolean mIsHallOn = true;//false;//PhoneKpdNative.kpd_is_on();//there is no function, 2015.05.28
        mStatus.setText(mIsHallOn ? R.string.hall_on : R.string.hall_off);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hall_info);
        mStatus = (TextView) findViewById(R.id.status);
        mBtOK = (Button) findViewById(R.id.hall_ok);
        mBtOK.setOnClickListener(this);
        mBtOK.setEnabled(false);
        mBtFailed = (Button) findViewById(R.id.hall_failed);
        mBtFailed.setOnClickListener(this);
        mHandler = new MyHandler(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mHandler.sendEmptyMessageDelayed(EVENT_TICK, 1000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_F1) {
            mHandler.sendEmptyMessage(EVENT_TICK_DOWN);
            isKeyOk = true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_F1) {
            mHandler.sendEmptyMessage(EVENT_TICK_UP);
            if(isKeyOk) {
                mBtOK.setEnabled(true);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeMessages(EVENT_TICK);
    }

    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.hall_name, (v.getId() == mBtOK
                .getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        finish();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.menu_exit);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        setResult(Activity.RESULT_FIRST_USER);
        finish();
        return true;
    }
}
