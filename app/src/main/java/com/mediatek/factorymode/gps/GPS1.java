package com.mediatek.factorymode.gps;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import java.lang.ref.WeakReference;

public class GPS1 extends TestActivity implements OnClickListener {

    private TextView mStView;
    private TextView mSatelliteNumView;
    private TextView mSignalView;
    private Chronometer mTimeView;
    private TextView mResultView;

    private Button mBtOk;
    private Button mBtFailed;
    private SharedPreferences mSp;
    private GpsUtil mGpsUtil;
    private MyHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gps1);
        mGpsUtil = new GpsUtil(this);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mStView = (TextView) findViewById(R.id.gps_state_id);
        mSatelliteNumView = (TextView) findViewById(R.id.gps_satellite_id);
        mSignalView = (TextView) findViewById(R.id.gps_signal_id);
        mResultView = (TextView) findViewById(R.id.gps_result_id);
        mTimeView = (Chronometer) findViewById(R.id.gps_time_id);
        mBtOk = (Button) findViewById(R.id.gps_bt_ok);
        mBtFailed = (Button) findViewById(R.id.gps_bt_failed);
        mBtOk.setOnClickListener(this);
        mBtOk.setEnabled(false);
        mBtFailed.setOnClickListener(this);
        mTimeView.setFormat(getResources().getString(R.string.GPS_time));
        mStView.setText(R.string.GPS_connect);
        mTimeView.start();
        getSatelliteInfo();
        mHandler = new MyHandler(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    private int count = 0;
    private static class MyHandler extends Handler {
        private WeakReference<GPS1> reference;

        public MyHandler(GPS1 activity) {
            reference = new WeakReference<GPS1>(activity);//这里传入activity的上下文
        }
        @Override
        public void handleMessage(Message msg) {
            GPS1 activity = reference.get();
            switch (msg.what) {
                case 0:
                    activity.count++;
                    if(activity.count > 60) { // failed
                        activity.getSatelliteErrorInfo();
                    } else { // continue
                        activity.getSatelliteInfo();
                    }
                    break;
            }
        }
    };

    protected void onDestroy() {
        mGpsUtil.closeLocation();
        super.onDestroy();
    }

    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.gps1_name,
                (v.getId() == mBtOk.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        finish();
    }

    public void getSatelliteInfo() {
        int num35 = mGpsUtil.getSatelliteSNR35Number();
        int num = num35;
        if (num35 >= 2) {
            mHandler.removeMessages(0);
            mTimeView.stop();
            mResultView.setText("S35: " + getResources().getString(R.string.GPS_Success));
            mBtOk.setEnabled(true);
        } else {
            mHandler.sendEmptyMessageDelayed(0, 2000);
            mBtOk.setEnabled(false);
        }
        mSatelliteNumView.setText(getString(R.string.GPS_satelliteNum) + num);
        mSignalView.setText(getString(R.string.GPS_Signal)
                + mGpsUtil.getSatelliteSignals());
    }

    public void getSatelliteErrorInfo() {
        int num35 = mGpsUtil.getSatelliteSNR35Number();
        int num = num35;
        if (num35 >= 2) {
            mHandler.removeMessages(0);
            mResultView.setText("S35: " + getResources().getString(R.string.GPS_Success));
        } else {
            mResultView.setText("S35: " + getResources().getString(R.string.GPS_Fail));
        }
        mTimeView.stop();
        mSatelliteNumView.setText(getString(R.string.GPS_satelliteNum) + num);
        mSignalView.setText(getString(R.string.GPS_Signal)
                + mGpsUtil.getSatelliteSignals());
        mBtOk.setEnabled(false);
    }
}
