package com.mediatek.factorymode.headset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;
import com.mediatek.factorymode.VUMeter;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class HeadSet extends TestActivity implements OnClickListener {
    private static final String TAG = "HeadSet";

    // private static final String HEADSET_STATE_PATH = "/sys/class/switch/h2w/state";
    private static final String HEADSET_STATE_PATH = "/sys/bus/platform/drivers/Accdet_Driver/state";
    private Button mRecord;
    private Button mBtOk;
    private Button mBtFailed;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private AudioManager mAudioManager;
    private int musicVolume;
    boolean mMicClick = false;
    boolean mSpkClick = false;
    private VUMeter mVUMeter;
    private int mHeadsetPlugState = 0;
    private int mCurHeadsetPlugState = 0;
    private SharedPreferences mSp;
    private final static int STATE_HEADSET_PLUG = 0;
    private final static int STATE_HEADSET_UNPLUG = 1;
    private final static int STATE_HEADSET_POLLING = 2;
    private MyHandler myHandler;
    private File file;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.headset);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mRecord = (Button) findViewById(R.id.mic_bt_start);
        mRecord.setOnClickListener(this);
        mRecord.setEnabled(false);
        mBtOk = (Button) findViewById(R.id.bt_ok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.bt_failed);
        mBtFailed.setOnClickListener(this);
        mVUMeter = (VUMeter) findViewById(R.id.uvMeter);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        updateButtonStatus(false);
        myHandler = new MyHandler(this);

        if (null != myHandler) {
            myHandler.sendEmptyMessage(STATE_HEADSET_POLLING);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        getVolumeBefore();  //获取当前音量
        setAudio();  //设置为最大音量
    }

    @Override
    protected void onPause(){
        super.onPause();
        recoverAudio();  //恢复音量
    }

    protected void onDestroy() {
        super.onDestroy();
        deleteRecordResource();
        stopRecording();
        stopPlaying();
        h.removeCallbacks(ra);
        if (null != myHandler) {
            myHandler.removeMessages(STATE_HEADSET_POLLING);
        }
    }

    Handler h = new Handler();

    Runnable ra = new Runnable() {
        @Override
        public void run() {
            mVUMeter.invalidate();
            h.postDelayed(this, 100);
        }
    };

    private void start() {
        h.post(ra);
        String sDcString = Environment.getExternalStorageState();
        if (!sDcString.equals(Environment.MEDIA_MOUNTED)) {
            mRecord.setText(R.string.sdcard_tips_failed);
            return;
        }
        deleteRecordResource();
        getFile();
        startRecord();
    }

    private void stopAndSave() {
        h.removeCallbacks(ra);
        mRecord.setText(R.string.Mic_playing);
        mRecord.setClickable(false);
        updateButtonStatus(true);
        mRecord.setTag("");
        mVUMeter.SetCurrentAngle(0);
        try {
            mRecorder.stop();
            mRecorder.release();
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException e " + e.toString());
        } catch (RuntimeException e) {
            Log.e(TAG, "RuntimeException e " + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "Exception e " + e.toString());
        }
        mRecorder = null;
        mVUMeter.setRecorder(null); // haiming fix canot click ok or fail button
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(file.getAbsolutePath());
            mPlayer.prepare();
            mPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mRecord.setText(R.string.Mic_start);
                mRecord.setClickable(true);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mRecord.getId()) {
            if (mRecord.getTag() == null || !mRecord.getTag().equals("ing")) {
                start();
                updateButtonStatus(false);
            } else {
                stopAndSave();
            }
        }
        if (v.getId() == mBtOk.getId()) {
            Utils.SetPreferences(this, mSp, R.string.headset_name, AppDefine.FT_SUCCESS);
            finish();
        } else if (v.getId() == mBtFailed.getId()) {
            Utils.SetPreferences(this, mSp, R.string.headset_name, AppDefine.FT_FAILED);
            finish();
        }
    }

    private static class MyHandler extends Handler {
        private WeakReference<HeadSet> reference;

        public MyHandler(HeadSet activity) {
            reference = new WeakReference<HeadSet>(activity);//这里传入activity的上下文
        }
        @Override
        public void handleMessage(Message msg) {
            HeadSet activity = reference.get();
            super.handleMessage(msg);
            switch (msg.what) {
            case STATE_HEADSET_PLUG:
                Log.d(TAG, "STATE_HEADSET_PLUG... ");
                activity.mRecord.setText(R.string.Mic_start);
                activity.mRecord.setEnabled(true);
                activity.mRecord.setClickable(true);
                break;
            case STATE_HEADSET_UNPLUG:
                Log.d(TAG, "STATE_HEADSET_UNPLUG... ");
                if (activity.mRecorder != null) {
                    try {
                        activity.mRecorder.stop();
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "IllegalStateException e " + e.toString());
                    } catch (RuntimeException e) {
                        Log.e(TAG, "RuntimeException e " + e.toString());
                    } catch (Exception e) {
                        Log.e(TAG, "Exception e " + e.toString());
                    }
                    activity.mRecord.setTag("");
                    activity.mRecorder = null;
                }
                activity.mVUMeter.SetCurrentAngle(0);
                if (activity.mPlayer != null) {
                    activity.mPlayer.stop();
                    activity.mPlayer.release();
                    activity.mPlayer = null;
                }
                activity.mRecord.setText(R.string.HeadSet_tips);
                activity.mRecord.setEnabled(false);
                break;
            case STATE_HEADSET_POLLING:
                activity.mCurHeadsetPlugState = activity.getHeadsetState();

                if (activity.mCurHeadsetPlugState != activity.mHeadsetPlugState) {
                    activity.mHeadsetPlugState = activity.mCurHeadsetPlugState;

                    if (activity.mHeadsetPlugState == 0) {
                        activity.myHandler.sendEmptyMessage(STATE_HEADSET_UNPLUG);
                    } else {
                        activity.myHandler.sendEmptyMessage(STATE_HEADSET_PLUG);
                    }
                }
                activity.myHandler.sendEmptyMessageDelayed(STATE_HEADSET_POLLING, 500);
                break;
            }
        }
    }

    private void updateButtonStatus(boolean status) {
        if (null != mBtOk) {
            mBtOk.setEnabled(status);
        }

    }

    private int getHeadsetState() {
        char[] buffer = new char[10];
        int newState = 0;

        try {
            FileReader file = new FileReader(HEADSET_STATE_PATH);
            int len = file.read(buffer, 0, 10);
            newState = Integer.valueOf((new String(buffer, 0, len)).trim());

            Log.e(TAG, "getHeadsetState newState = " + newState);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException e = " + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "Exception e = " + e.toString());
        }

        return newState;
    }

    private void getVolumeBefore(){
        musicVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private void setAudio(){
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
    }

    private void recoverAudio(){
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,musicVolume, 0);
    }

    private void getFile(){
        if(file == null){
            file = new File(Environment.getExternalStorageDirectory()+"/test.amr");
        }
    }

    private void deleteRecordResource(){
        if(file == null){
            return;
        }
        if(file.exists()){
            file.delete();
        }
    }

    private void stopPlaying(){
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void startRecord(){
        stopPlaying();
        try {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(file.getAbsolutePath());
            mVUMeter.setRecorder(mRecorder);
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                mRecorder.reset();
                mRecorder.release();
                mRecorder = null;
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
            return;
        }
        mRecorder.start();
        mRecord.setTag("ing");
        mRecord.setText(R.string.Mic_stop);
    }

    private void stopRecording() {
        if (null != mRecorder) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
        mVUMeter.setRecorder(null);
    }
}
