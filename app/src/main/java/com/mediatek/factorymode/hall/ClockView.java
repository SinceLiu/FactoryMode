package com.mediatek.factorymode.hall;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Window;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;

public class ClockView extends TestActivity {

    private static final String ACTION_CLOSE_CLOCKVIEW = "com.cenon.hall.clockview.close";

    private BroadcastReceiver closeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_CLOSE_CLOCKVIEW.equals(intent.getAction())) {
                finish();
            }
        }
    };
    private IntentFilter closeFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.clock_view);

        closeFilter = new IntentFilter();
        closeFilter.addAction(ACTION_CLOSE_CLOCKVIEW);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(closeReceiver, closeFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(closeReceiver);
    }

}

