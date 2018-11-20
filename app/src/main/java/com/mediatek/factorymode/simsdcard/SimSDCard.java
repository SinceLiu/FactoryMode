package com.mediatek.factorymode.simsdcard;

import java.lang.reflect.Method;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.FactoryModeFeatureOption;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SimSDCard extends TestActivity implements OnClickListener {

    private static final String TAG = "SimSDCard";
    private TextView mSIMCardInfo;
    private String mSimInfo = "";
    private boolean mSimStatus = false;
    private Button mSIMCardBtOk;
    private Button mSIMCardBtFailed;
    private TextView mSDCardInfo;
    private TextView mSDCardInfoEx;
    private Button mSDCardBtOk;
    private Button mSDCardBtFailed;
    private SharedPreferences mSDCardSp;
    boolean mSIMCardClick = false;
    boolean mSDCardClick = false;
    private boolean mSIMCardTestOk = false;
    private boolean mSDCardTestOk = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simsdcard);

        mSDCardSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mSimInfo = FactoryModeFeatureOption.get("persist.sys.sim_info");
        mSimStatus = FactoryModeFeatureOption.getBoolean("persist.sys.sim_status", false);

        mSIMCardInfo = (TextView) findViewById(R.id.simcard_sim_info);
        mSIMCardBtOk = (Button) findViewById(R.id.simcard_bt_ok);
        mSIMCardBtOk.setOnClickListener(this);
        mSIMCardBtOk.setEnabled(mSimStatus);
        mSIMCardBtFailed = (Button) findViewById(R.id.simcard_bt_failed);
        mSIMCardBtFailed.setOnClickListener(this);
        mSIMCardInfo.setText(mSimInfo);
        if (!mSimStatus) {
            mSIMCardBtFailed.performClick();
        }

        mSDCardInfo = (TextView) findViewById(R.id.sdcard_info);
        mSDCardInfoEx = (TextView) findViewById(R.id.sdcard_info_ex);
        mSDCardBtOk = (Button) findViewById(R.id.sdcard_bt_ok);
        mSDCardBtOk.setOnClickListener(this);
        mSDCardBtFailed = (Button) findViewById(R.id.sdcard_bt_failed);
        mSDCardBtFailed.setOnClickListener(this);
        sdCardSizeTest();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @SuppressWarnings("deprecation")
    public void sdCardSizeTest() {
        String primaryStoragePath = getPrimaryStoragePath();
        String secondaryStoragePath = getSecondaryStoragePath();

        if (primaryStoragePath != null) {
            StatFs statfs = new StatFs(primaryStoragePath);
            long nTotalBlocks = statfs.getBlockCount();
            long nBlocSize = statfs.getBlockSize();
            long nAvailaBlock = statfs.getAvailableBlocks();
            long nSDTotalSize = nTotalBlocks * nBlocSize / 1024 / 1024;
            long nSDFreeSize = nAvailaBlock * nBlocSize / 1024 / 1024;

            mSDCardInfo.setText(getString(R.string.sdcard_tips_success) + "\n" + getString(R.string.sdcard_totalsize)
                    + nSDTotalSize + "MB" + "\n" + getString(R.string.sdcard_freesize) + nSDFreeSize + "MB");
        }
        if (secondaryStoragePath != null) {
            StatFs statfs = new StatFs(secondaryStoragePath);
            long nTotalBlocks = statfs.getBlockCount();
            long nBlocSize = statfs.getBlockSize();
            long nAvailaBlock = statfs.getAvailableBlocks();
            long nSDTotalSize = nTotalBlocks * nBlocSize / 1024 / 1024;
            long nSDFreeSize = nAvailaBlock * nBlocSize / 1024 / 1024;

            mSDCardInfoEx.setText(getString(R.string.sdcard_tips_success_ex) + "\n" + getString(R.string.sdcard_totalsize) 
                    + nSDTotalSize + "MB" + "\n" + getString(R.string.sdcard_freesize) + nSDFreeSize + "MB");
        }
        if (primaryStoragePath == null && secondaryStoragePath == null) {
            mSDCardInfo.setText(getString(R.string.sdcard_tips_failed));
        }
    }

    public void isFinish() {
        if (mSIMCardClick == true && mSDCardClick == true) {
            if (mSIMCardTestOk && mSDCardTestOk) {
                Utils.SetPreferences(this, mSDCardSp, R.string.sim_sdcard_name, AppDefine.FT_SUCCESS);
            } else {
                Utils.SetPreferences(this, mSDCardSp, R.string.sim_sdcard_name, AppDefine.FT_FAILED);
            }
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.simcard_bt_ok:
            mSIMCardClick = true;
            mSIMCardTestOk = true;
            mSIMCardBtFailed.setBackgroundColor(this.getResources().getColor(R.color.gray));
            mSIMCardBtOk.setBackgroundColor(this.getResources().getColor(R.color.Green));
            Utils.SetPreferences(this, mSDCardSp, R.string.sim_sdcard_name, AppDefine.FT_SUCCESS);
            break;
        case R.id.simcard_bt_failed:
            mSIMCardClick = true;
            mSIMCardTestOk = false;
            mSIMCardBtOk.setBackgroundColor(this.getResources().getColor(R.color.gray));
            mSIMCardBtFailed.setBackgroundColor(this.getResources().getColor(R.color.Red));
            Utils.SetPreferences(this, mSDCardSp, R.string.sim_sdcard_name, AppDefine.FT_FAILED);
            break;
        case R.id.sdcard_bt_ok:
            mSDCardClick = true;
            mSDCardTestOk = true;
            mSDCardBtFailed.setBackgroundColor(this.getResources().getColor(R.color.gray));
            mSDCardBtOk.setBackgroundColor(this.getResources().getColor(R.color.Green));
            Utils.SetPreferences(this, mSDCardSp, R.string.sim_sdcard_name, AppDefine.FT_SUCCESS);
            break;
        case R.id.sdcard_bt_failed:
            mSDCardClick = true;
            mSDCardTestOk = false;
            mSDCardBtOk.setBackgroundColor(this.getResources().getColor(R.color.gray));
            mSDCardBtFailed.setBackgroundColor(this.getResources().getColor(R.color.Red));
            Utils.SetPreferences(this, mSDCardSp, R.string.sim_sdcard_name, AppDefine.FT_FAILED);
            break;
        }
        isFinish();
    }

    public String getPrimaryStoragePath() {
        try {
            StorageManager sm = (StorageManager) getSystemService(STORAGE_SERVICE);
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", null);
            String[] paths = (String[]) getVolumePathsMethod.invoke(sm, null);
            // first element in paths[] is primary storage path
            return paths[0];
        } catch (Exception e) {
            Log.e(TAG, "getPrimaryStoragePath() failed", e);
        }
        return null;
    }

    public String getSecondaryStoragePath() {
        try {
            StorageManager sm = (StorageManager) getSystemService(STORAGE_SERVICE);
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", null);
            String[] paths = (String[]) getVolumePathsMethod.invoke(sm, null);
            // second element in paths[] is secondary storage path
            return paths.length <= 1 ? null : paths[1];
        } catch (Exception e) {
            Log.e(TAG, "getSecondaryStoragePath() failed", e);
        }
        return null;
    }

    public String getStorageState(String path) {
        try {
            StorageManager sm = (StorageManager) getSystemService(STORAGE_SERVICE);
            Method getVolumeStateMethod = StorageManager.class.getMethod("getVolumeState",
                    new Class[] { String.class });
            String state = (String) getVolumeStateMethod.invoke(sm, path);
            return state;
        } catch (Exception e) {
            Log.e(TAG, "getStorageState() failed", e);
        }
        return null;
    }
}
