package com.mediatek.factorymode.lcd;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

public class LCD extends TestActivity {
    private TextView mText1 = null;
    private int mNum = 0;
    private Timer timer;
    private SharedPreferences mSp;
    private MyHandler myHandler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lcd);
        myHandler = new MyHandler(this);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        timer = new Timer();
        initView();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    private static class MyHandler extends Handler {
        private WeakReference<LCD> reference;

        public MyHandler(LCD activity) {
            reference = new WeakReference<LCD>(activity);//这里传入activity的上下文
        }
        public void handleMessage(Message msg) {
           final LCD activity = reference.get();
            if (msg.what == 0) {
                activity.mNum++;
                if (activity.mNum >= 4) {
                    activity.timer.cancel();
                    activity.myHandler.removeMessages(0);
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(R.string.FMRadio_notice);
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.Success, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Utils.SetPreferences(activity.getApplicationContext(), activity.mSp, R.string.lcd_name, AppDefine.FT_SUCCESS);
                            activity.finish();
                        }
                    });
                    builder.setNegativeButton(activity.getResources().getString(R.string.Failed),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Utils.SetPreferences(activity.getApplicationContext(), activity.mSp, R.string.lcd_name,
                                            AppDefine.FT_FAILED);
                                    activity.finish();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(20);

                } else {
                    activity.changeColor(activity.mNum);
                }
            }
        }
    }

    private void initView() {
        mText1 = (TextView) findViewById(R.id.test_color_text1);

        timer.schedule(new TimerTask() {
            public void run() {
                Message msg = new Message();
                msg.what = 0;
                myHandler.sendMessage(msg);
            }
        }, 1000, 1000);
    }

    private void changeColor(int num) {
        switch (num % 4) {
        case 0:
            mText1.setBackgroundColor(Color.RED);
            break;
        case 1:
            mText1.setBackgroundColor(Color.GREEN);
            break;
        case 2:
            mText1.setBackgroundColor(Color.BLUE);
            break;
        case 3:
            mText1.setBackgroundColor(Color.WHITE);
            break;
        }
    }
}
