package com.mediatek.factorymode.earphone;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;

public class Earphone extends TestActivity implements OnClickListener, OnKeyListener {
    private SharedPreferences mSp;

    private MediaPlayer mPlayer;
    private Button mBtOk;
    private Button mBtFailed;
    private AudioManager mAudioManager;
    private int backupVoiceCallVolume;
    private int backupMusicVolume;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_test);

        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mBtOk = (Button) findViewById(R.id.audio_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.audio_bt_failed);
        mBtFailed.setOnClickListener(this);

        mAudioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);

        backupVolume();
    }

    protected void onResume() {
        super.onResume();

        InitStatus();

        mBtOk = (Button) findViewById(R.id.audio_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.audio_bt_failed);
        mBtFailed.setOnClickListener(this);
    }

    private void backupVolume() {
        backupVoiceCallVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        backupMusicVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private void restoreVolume() {
        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, backupVoiceCallVolume,
                AudioManager.FLAG_PLAY_SOUND);

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, backupMusicVolume, AudioManager.FLAG_PLAY_SOUND);
    }

    private void InitStatus() {
        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FLAG_PLAY_SOUND);

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_PLAY_SOUND);

        mAudioManager.setSpeakerphoneOn(false); // false //bob modify
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        mAudioManager.setMode(AudioManager.MODE_IN_CALL);

        initMediaPlayer();
    }

    protected void onPause() {
        super.onPause();

        mPlayer.stop();
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        restoreVolume();
    }

    private void initMediaPlayer() {
        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.earphone);
        mPlayer.setLooping(true);
        mPlayer.start();
    }

    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.earphone_name,
                (v.getId() == mBtOk.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        finish();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_UP:
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_PLAY_SOUND);
            break;
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_PLAY_SOUND);
            break;
        default:
            break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
