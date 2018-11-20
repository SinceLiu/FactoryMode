package com.mediatek.factorymode.audio;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

public class AudioTest extends TestActivity implements OnClickListener {
    private SharedPreferences mSp;
    private MediaPlayer mPlayer;
    private Button mBtOk;
    private Button mBtFailed;
    private AudioManager audioManager;
    private int nCurrentMusicVolume;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.keepScreenOn(this);
        setContentView(R.layout.audio_test);
        mBtOk = (Button) findViewById(R.id.audio_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.audio_bt_failed);
        mBtFailed.setOnClickListener(this);
        audioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
    }

    protected void onResume() {
        super.onResume();
        nCurrentMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mBtOk = (Button) findViewById(R.id.audio_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.audio_bt_failed);
        mBtFailed.setOnClickListener(this);
        InitStatus();
    }

    private void InitStatus() {
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nCurrentMusicVolume, AudioManager.FLAG_PLAY_SOUND);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        initMediaPlayer();
    }

    protected void onPause() {
        super.onPause();
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        if (audioManager != null) {
            nCurrentMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC); //获取最新音量（滑动调节？)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nCurrentMusicVolume, 0);
        }
    }

    private void initMediaPlayer() {
        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.tada);
        mPlayer.setLooping(true);
        mPlayer.start();
    }

    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.speaker_name,
                (v.getId() == mBtOk.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        nCurrentMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC); //获取最新音量
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (nCurrentMusicVolume < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
                    nCurrentMusicVolume++;
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nCurrentMusicVolume,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (nCurrentMusicVolume > 0)
                    nCurrentMusicVolume--;
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nCurrentMusicVolume,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
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
