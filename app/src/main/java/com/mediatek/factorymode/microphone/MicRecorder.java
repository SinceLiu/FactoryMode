package com.mediatek.factorymode.microphone;

import java.io.File;
import java.io.IOException;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;
import com.mediatek.factorymode.VUMeter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MicRecorder extends TestActivity implements OnClickListener {

    private Button mRecord;
    private Button mBtMicOk;
    private Button mBtMicFailed;
    private Button mBtSpkOk;
    private Button mBtSpkFailed;

    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private AudioManager mAudioManager;
    private int musicVolume;

    boolean mMicClick = false;
    boolean mSpkClick = false;
    boolean mMicTestOk = false;
    boolean mSpkTestOk = false;

    private VUMeter mVUMeter;
    private SharedPreferences mSp;
    private File mSampleFile;

    private String RECORD_SOURCE_NAME_PREFIX = "mictest";
    private String RECORD_SOURCE_NAME_SUFFIX = ".amr";

    private static final int HANDLER_MSG_START_RECORD = 1;
    private static final int HANDLER_MSG_STOP_RECORD_AND_PLAY = 2;

    private RecoderControlHandler mRecoderControlHandler;

    Handler VUMeter_hander = new Handler();
    Runnable VUMeter_runnable = new Runnable() {
        @Override
        public void run() {
            mVUMeter.invalidate();
            VUMeter_hander.postDelayed(this, 100);
        }
    };

    class RecoderControlHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
            case HANDLER_MSG_START_RECORD:
                VUMeter_hander.post(VUMeter_runnable);
                start();
                break;
            case HANDLER_MSG_STOP_RECORD_AND_PLAY:
                mRecord.setText(R.string.Mic_playing);
                mRecord.setClickable(false);
                mVUMeter.SetCurrentAngle(0);
                VUMeter_hander.removeCallbacks(VUMeter_runnable);
                stopRecording();
                new Thread(new Runnable() {
                    public void run() {
                        stopAndPlay();
                    }
                }).start();
                break;
            default:
                break;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.micrecorder);

        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mRecord = (Button) findViewById(R.id.mic_bt_start);
        mRecord.setOnClickListener(this);
        mBtMicOk = (Button) findViewById(R.id.mic_bt_ok);
        mBtMicOk.setOnClickListener(this);
        mBtMicOk.setEnabled(false);
        mBtMicFailed = (Button) findViewById(R.id.mic_bt_failed);
        mBtMicFailed.setOnClickListener(this);
        mBtSpkOk = (Button) findViewById(R.id.speaker_bt_ok);
        mBtSpkOk.setOnClickListener(this);
        mBtSpkOk.setEnabled(false);
        mBtSpkFailed = (Button) findViewById(R.id.speaker_bt_failed);
        mBtSpkFailed.setOnClickListener(this);
        mVUMeter = (VUMeter) findViewById(R.id.uvMeter);

        mRecoderControlHandler = new RecoderControlHandler();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    protected void onResume(){
        super.onResume();
        getVolumeBefore();  //获取当前音量
        setAudio();  //设置为最大音量

        if (mAudioManager.isWiredHeadsetOn())   //提示摘掉耳机
            showWarningDialog(getString(R.string.HeadSet_remove));
    }

    @Override
    protected void onPause(){
        super.onPause();
        recoverAudio();  //恢复音量
    }

    protected void onDestroy() {
        super.onDestroy();

        stopRecording();
        stopPlaying();
        deleteRecordResource();
    }

    public void isFinish() {
        if (mMicClick && mSpkClick) {
            if (mMicTestOk && mSpkTestOk) {
                Utils.SetPreferences(this, mSp, R.string.microphone_name, AppDefine.FT_SUCCESS);
            } else {
                Utils.SetPreferences(this, mSp, R.string.microphone_name, AppDefine.FT_FAILED);
            }
            deleteRecordResource();
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mRecord.getId()) {
            if (mAudioManager.isWiredHeadsetOn()){      //提示摘掉耳机
                showWarningDialog(getString(R.string.HeadSet_remove));
                return;
            }

            if (mRecord.getTag() == null || !mRecord.getTag().equals("ing")) {
                mRecoderControlHandler.sendEmptyMessage(HANDLER_MSG_START_RECORD);
                mBtMicOk.setEnabled(false);
                mBtSpkOk.setEnabled(false);
            } else {
                mRecoderControlHandler.sendEmptyMessage(HANDLER_MSG_STOP_RECORD_AND_PLAY);
                mBtMicOk.setEnabled(true);
                mBtSpkOk.setEnabled(true);
            }
        }

        if (v.getId() == mBtMicOk.getId()) {
            mMicClick = true;
            mMicTestOk = true;
            mBtMicFailed.setBackgroundColor(this.getResources().getColor(R.color.gray));
            mBtMicOk.setBackgroundColor(this.getResources().getColor(R.color.Green));
            Utils.SetPreferences(this, mSp, R.string.microphone_name, AppDefine.FT_SUCCESS);
        } else if (v.getId() == mBtMicFailed.getId()) {
            mMicClick = true;
            mMicTestOk = false;
            mBtMicOk.setBackgroundColor(this.getResources().getColor(R.color.gray));
            mBtMicFailed.setBackgroundColor(this.getResources().getColor(R.color.Red));
            Utils.SetPreferences(this, mSp, R.string.microphone_name, AppDefine.FT_FAILED);
        }
        if (v.getId() == mBtSpkOk.getId()) {
            mSpkClick = true;
            mSpkTestOk = true;
            mBtSpkFailed.setBackgroundColor(this.getResources().getColor(R.color.gray));
            mBtSpkOk.setBackgroundColor(this.getResources().getColor(R.color.Green));
            Utils.SetPreferences(this, mSp, R.string.microphone_name, AppDefine.FT_SUCCESS);
        } else if (v.getId() == mBtSpkFailed.getId()) {
            mSpkClick = true;
            mSpkTestOk = false;
            mBtSpkOk.setBackgroundColor(this.getResources().getColor(R.color.gray));
            mBtSpkFailed.setBackgroundColor(this.getResources().getColor(R.color.Red));
            Utils.SetPreferences(this, mSp, R.string.microphone_name, AppDefine.FT_FAILED);
        }
        isFinish();
    }

    private void start() {
        mRecord.setText(R.string.Mic_stop);
        String sDcString = Environment.getExternalStorageState();
        if (!sDcString.equals(Environment.MEDIA_MOUNTED)) {
            mRecord.setText(R.string.sdcard_tips_failed);
            return;
        }
        deleteRecordResource();
        getSampleFile();
        startRecord();
        mVUMeter.setRecorder(mRecorder);
        mRecord.setTag("ing");
    }

    private void startRecord() {
        stopPlaying();
        try {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(mSampleFile.getAbsolutePath());

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
    }

    private void stopRecording() {
        if (null != mRecorder) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
        mVUMeter.setRecorder(null);
    }

    private void startPlay() {
        try {
            if (mPlayer == null) {
                mPlayer = new MediaPlayer();
            }
            mPlayer.setDataSource(mSampleFile.getAbsolutePath());
            mPlayer.prepare();
            mPlayer.start();

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                    mRecord.setText(R.string.Mic_start);
                    mRecord.setClickable(true);
                }
            });
            mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    stopPlaying();
                    return false;
                }
            });
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void stopAndPlay() {
        mRecord.setTag("stop");
        startPlay();
    }

    private void deleteRecordResource() {
        if (mSampleFile == null) {
            return;
        }
        if (mSampleFile.exists()) {
            mSampleFile.delete();
        }
    }

    private void getSampleFile() {
        if (null == mSampleFile) {
            String sampleDir = Environment.getExternalStorageDirectory() + "/";
            mSampleFile = new File(sampleDir + RECORD_SOURCE_NAME_PREFIX + RECORD_SOURCE_NAME_SUFFIX);
        }
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

    void showWarningDialog(String title) {

        new AlertDialog.Builder(this).setTitle(title).setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();

    }
}
