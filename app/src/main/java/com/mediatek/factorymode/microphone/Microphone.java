package com.mediatek.factorymode.microphone;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class Microphone extends TestActivity implements OnClickListener {
    private Button btok;
    private Button btfailed;
    private TextView tvstatus;
    private SharedPreferences sp;
    private RecordThread rt;
    private MyHandler mHandler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.microphone);

        sp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        tvstatus = (TextView) findViewById(R.id.mic_tv_status);
        btok = (Button) findViewById(R.id.mic_bt_ok);
        btok.setOnClickListener(this);
        btfailed = (Button) findViewById(R.id.mic_bt_failed);
        btfailed.setOnClickListener(this);
        mHandler = new MyHandler(this);
        rt = new RecordThread();

    }

    private static class MyHandler extends Handler {
        private WeakReference<Microphone> reference;

        public MyHandler(Microphone activity) {
            reference = new WeakReference<Microphone>(activity);//这里传入activity的上下文
        }
        @Override
        public void handleMessage(Message msg) {
            Microphone activity = reference.get();
            super.handleMessage(msg);
            activity.tvstatus.setText(String.valueOf(msg.arg1));
        }
    }

    public void onResume() {
        super.onResume();
        rt.start();
    }

    public void onPause() {
        super.onPause();
        rt.pause();
    }

    @SuppressWarnings("deprecation")
    public void onDestroy() {
        super.onDestroy();
        rt.destroy();
    }

    public class RecordThread extends Thread {
        private AudioRecord ar;
        private int bs;
        private static final int SAMPLE_RATE_IN_HZ = 44100;
        private boolean isRun = false;

        @SuppressWarnings("deprecation")
        public RecordThread() {
            super();
            bs = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            ar = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bs);
        }

        public void run() {
            super.run();
            ar.startRecording();
            byte[] buffer = new byte[bs];
            int r = ar.read(buffer, 0, bs);
            isRun = true;
            while (isRun) {
                int v = 0;

                for (int i = 0; i < buffer.length; i++) {
                    v += buffer[i] * buffer[i];
                }
                Message msg = new Message();
                msg.arg1 = (int) (v / (float) r);
                mHandler.sendMessage(msg);
            }
            ar.stop();
        }

        public void pause() {
            isRun = false;
        }

        public void start() {
            if (!isRun) {
                super.start();
            }
        }

    }

    public void onClick(View v) {
        Utils.SetPreferences(this, sp, R.string.Microphone,
                (v.getId() == btok.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        finish();
    }
}
