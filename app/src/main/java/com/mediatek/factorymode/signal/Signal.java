package com.mediatek.factorymode.signal;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.FactoryModeFeatureOption;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Signal extends TestActivity implements OnClickListener {
    private Button mBtOk;
    private Button mBtFailed;
    private Button mBtEmergencyCall;
    private Button mBtServiceCall;
    private Button mBtNormalCall;

    private SharedPreferences mSp;
    private boolean unknownSim = false;
    private static final boolean DIALOG_SUPPORT = !SystemProperties.get("ro.cenon_mt6735_a889_carboy").equals("1");
    private static final boolean shouldLoudSpeaker = SystemProperties.get("ro.cenon_mt6735_a889_carboy").equals("1");
    private boolean isDialed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signal);

        isDialed = false;
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mBtOk = (Button) findViewById(R.id.signal_bt_ok);
        mBtOk.setEnabled(isDialed);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.signal_bt_failed);
        mBtFailed.setOnClickListener(this);
        mBtEmergencyCall = (Button) findViewById(R.id.emergency_call);
        mBtEmergencyCall.setOnClickListener(this);
        mBtServiceCall = (Button) findViewById(R.id.service_call);
        mBtServiceCall.setOnClickListener(this);
        mBtNormalCall = (Button) findViewById(R.id.normal_call);
        mBtNormalCall.setOnClickListener(this);
        if(SystemProperties.getInt("ro.cenon.project.a865", 0) == 1) {
            mBtNormalCall.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String simOper = SystemProperties.get("gsm.sim.operator.numeric", "46099");
        if("46000".equals(simOper) || "46002".equals(simOper) || "46007".equals(simOper) || "46008".equals(simOper)) {
            unknownSim = false;
        } else if("46001".equals(simOper) || "46006".equals(simOper) || "46009".equals(simOper)) {
            unknownSim = false;
        } else if("46003".equals(simOper) || "46005".equals(simOper) || "46011".equals(simOper)) {
            unknownSim = false;
        } else {
            unknownSim = true;
        }
        if(unknownSim) {
            mBtServiceCall.setEnabled(false);
        } else {
            mBtServiceCall.setEnabled(true);
        }
        if(shouldLoudSpeaker) {
            OpenSpeaker();
        }
    }

    @Override
    public void onDestroy() {
        if(shouldLoudSpeaker) {
            CloseSpeaker();
        }
        super.onDestroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        isDialed = true;
        mBtOk.setEnabled(isDialed);

        if(FactoryModeFeatureOption.CENON_PROJECT_A865){//tj.lee a865 remove this func
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.FMRadio_notice);
        builder.setMessage(R.string.HeadSet_hook_message);
        builder.setPositiveButton(R.string.Success,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Utils.SetPreferences(Signal.this, mSp, R.string.headsethook_name,
                                AppDefine.FT_SUCCESS);
                    }
                });
        builder.setNegativeButton(R.string.Failed, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Utils.SetPreferences(Signal.this, mSp, R.string.headsethook_name,
                        AppDefine.FT_FAILED);
            }
        });
        if(requestCode == AppDefine.FT_HOOKSETID){
            builder.create().show();
        }
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.emergency_call:
                Intent intent112 = new Intent(Intent.ACTION_CALL_PRIVILEGED);
                intent112.setData(Uri.fromParts("tel", "112", null));
                startActivityForResult(intent112, (DIALOG_SUPPORT) ? AppDefine.FT_HOOKSETID : (AppDefine.FT_HOOKSETID + 100));
                break;
            case R.id.service_call:
                Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
                String simOper = SystemProperties.get("gsm.sim.operator.numeric", "46099");
                if("46000".equals(simOper) || "46002".equals(simOper) || "46007".equals(simOper) || "46008".equals(simOper)) {
                    intent.setData(Uri.fromParts("tel", "10086", null));
                } else if("46001".equals(simOper) || "46006".equals(simOper) || "46009".equals(simOper)) {
                    intent.setData(Uri.fromParts("tel", "10010", null));
                } else if("46003".equals(simOper) || "46005".equals(simOper) || "46011".equals(simOper)) {
                    intent.setData(Uri.fromParts("tel", "10000", null));
                } else {
                    intent.setData(Uri.fromParts("tel", "112", null));
                }
                startActivityForResult(intent, (DIALOG_SUPPORT) ? AppDefine.FT_HOOKSETID : (AppDefine.FT_HOOKSETID + 100));
                break;
            case R.id.normal_call:
                Intent intentNormal = new Intent(Intent.ACTION_DIAL);
                intentNormal.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intentNormal, (DIALOG_SUPPORT) ? AppDefine.FT_HOOKSETID : (AppDefine.FT_HOOKSETID + 100));
                break;
            case R.id.signal_bt_ok:
                Utils.SetPreferences(this, mSp, R.string.telephone_name, AppDefine.FT_SUCCESS);
                finish();
                break;
            case R.id.signal_bt_failed:
                Utils.SetPreferences(this, mSp, R.string.telephone_name, AppDefine.FT_FAILED);
                finish();
                break;
        }
    };
    
    int currVolume = 0;
    public void OpenSpeaker() {
        try {
            AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.ROUTE_SPEAKER);
            currVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);

            if (!audioManager.isSpeakerphoneOn()) {
                audioManager.setSpeakerphoneOn(true);

                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                        AudioManager.STREAM_VOICE_CALL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CloseSpeaker() {
        try {
            AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                if (audioManager.isSpeakerphoneOn()) {
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currVolume,
                            AudioManager.STREAM_VOICE_CALL);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
