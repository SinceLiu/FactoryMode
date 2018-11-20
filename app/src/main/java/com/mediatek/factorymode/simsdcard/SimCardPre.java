package com.mediatek.factorymode.simsdcard;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
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
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimCardPre extends TestActivity {

    private boolean sim1State = false;
    private boolean sim2State = false;
    private Phone mPhone = null;
    SharedPreferences mSp;
    private String mSimStatus = "";
    private static final String TAG = "SimCardPre";
    private static boolean finishActivity = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSimStatus = "";
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        if (FactoryModeFeatureOption.MTK_GEMINI_SUPPORT) {
            mPhone = PhoneFactory.getDefaultPhone();
            sim1State = isSimInserted(PhoneConstants.SIM_ID_1);
            sim2State = isSimInserted(PhoneConstants.SIM_ID_2);

            if (sim1State) {
                mSimStatus += getString(R.string.sim1_info_ok) + "\n";
            } else {
                mSimStatus += getString(R.string.sim1_info_failed) + "\n";
            }
            if (sim2State) {
                mSimStatus += getString(R.string.sim2_info_ok) + "\n";
            } else {
                mSimStatus += getString(R.string.sim2_info_failed) + "\n";
            }
            FactoryModeFeatureOption.set("persist.sys.sim_status", String.valueOf(sim1State && sim2State));
        } else {
            sim1State = TelephonyManager.getDefault().hasIccCard();
            if (sim1State) {
                mSimStatus += getString(R.string.sim_info_ok) + "\n";
            } else {
                mSimStatus += getString(R.string.sim_info_failed) + "\n";
            }
            FactoryModeFeatureOption.set("persist.sys.sim_status", String.valueOf(sim1State));
        }
        FactoryModeFeatureOption.set("persist.sys.sim_info", mSimStatus);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(SimCardPre.this);
//        builder.setTitle(R.string.FMRadio_notice);
//        builder.setMessage(mSimStatus);
//        builder.setPositiveButton(R.string.Success, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                Utils.SetPreferences(SimCardPre.this, mSp, R.string.sim_name, AppDefine.FT_SUCCESS);
//                isFinish();
//            }
//        });
//        builder.setNegativeButton(getResources().getString(R.string.Failed), new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                Utils.SetPreferences(SimCardPre.this, mSp, R.string.sim_name, AppDefine.FT_FAILED);
//                isFinish();
//            }
//        });
//        builder.create().show();
        isFinish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.hideSystemUi(this);  //隐藏系统状态栏，下拉显示，自动隐藏
        finishActivity = finishActivity ? false : true;
        if (finishActivity) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void isFinish() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        String classname = "com.mediatek.factorymode.simsdcard.SimSDCard";
        intent.setClassName(this, classname);
        this.startActivity(intent);
        // finish();
    }

    private boolean isSimInserted(int slotId) { // added by ellery
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telephonyManager.getSimState(slotId);
        boolean isSimInsert = true;

        switch (simState) {
        case TelephonyManager.SIM_STATE_ABSENT:
        case TelephonyManager.SIM_STATE_UNKNOWN:
            isSimInsert = false;
            break;
        }

        return isSimInsert;
    }
}
