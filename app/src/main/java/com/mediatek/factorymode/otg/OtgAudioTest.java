package com.mediatek.factorymode.otg;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.CenonGpioSetNative;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

public class OtgAudioTest extends TestActivity implements OnClickListener {
    private SharedPreferences mSp;
    private MediaPlayer mPlayer;
    private Button mBtOk;
    private Button mBtFailed;
    private AudioManager audioManager;
    private int backupVolume;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_test);
        mBtOk = (Button) findViewById(R.id.audio_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.audio_bt_failed);
        mBtFailed.setOnClickListener(this);

        audioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
        backupVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    protected void onResume() {
        super.onResume();
        CenonGpioSetNative.openDev();
        CenonGpioSetNative.CenonSetUsbHostOn();
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mBtOk = (Button) findViewById(R.id.audio_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.audio_bt_failed);
        mBtFailed.setOnClickListener(this);
        initStatus();
    }

    private void initStatus() {
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_PLAY_SOUND);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        initMediaPlayer();
    }

    protected void onPause() {
        super.onPause();
        mPlayer.stop();
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, backupVolume, AudioManager.FLAG_PLAY_SOUND);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CenonGpioSetNative.CenonSetUsbHostOff();
        CenonGpioSetNative.closeDev();
    }

    private void initMediaPlayer() {
        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.tada);
        mPlayer.setLooping(true);
        mPlayer.start();
    }

    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.otg_speaker_name,
                (v.getId() == mBtOk.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_UP:
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_PLAY_SOUND);
            break;
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_PLAY_SOUND);
            break;
        default:
            break;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
        }
        return true;
        // return super.onKeyDown(keyCode, event);
    }
}
