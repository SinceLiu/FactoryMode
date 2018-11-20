package com.mediatek.factorymode.sdcard;

import java.lang.reflect.Method;
import java.util.List;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SDCard extends TestActivity implements OnClickListener {
    private static final String TAG = "SDCard";
    private TextView mSDCardInfo;
    private TextView mSDCardInfoEx;
    private Button mBtOk;
    private Button mBtFailed;
    private SharedPreferences mSp;
    private BroadcastReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdcard);

        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mSDCardInfo = (TextView) findViewById(R.id.sdcard_info);
        mSDCardInfoEx = (TextView) findViewById(R.id.sdcard_info_ex);
        mBtOk = (Button) findViewById(R.id.sdcard_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtOk.setEnabled(false);
        mBtFailed = (Button) findViewById(R.id.sdcard_bt_failed);
        mBtFailed.setOnClickListener(this);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("SDCard",intent.getAction());
                if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {  //插入sd卡
                    try {
//                        Thread.sleep(500);
                        sdCardSizeExTest();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if(intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)){  //拔出sd卡
                    mSDCardInfoEx.setText(getString(R.string.sdcard_tips_success_ex) + "\n         " + "Fail" + "\n ");
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme(ContentResolver.SCHEME_FILE);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sdCardSizeTest();
        sdCardSizeExTest();
    }

    public void sdCardSizeTest() {
        String primaryStoragePath = getPrimaryStoragePath();
        if (primaryStoragePath != null) {
            StatFs statfs = new StatFs(primaryStoragePath);
            double nTotalBlocks = statfs.getBlockCount();
            double nBlocSize = statfs.getBlockSize();
            double nAvailaBlock = statfs.getAvailableBlocks();
            double nSDTotalSize = Math.round(nTotalBlocks * nBlocSize / 1024 / 1024 / 1024 * 100) / 100.00;
            double nSDFreeSize = Math.round(nAvailaBlock * nBlocSize / 1024 / 1024 / 1024 * 100) / 100.00;
            mSDCardInfo.setText(getString(R.string.sdcard_tips_success) + "\n" + getString(R.string.sdcard_totalsize)
                    + nSDTotalSize + "GB" + "\n" + getString(R.string.sdcard_freesize) + nSDFreeSize + "GB");
        } else {
            mSDCardInfo.setText(getString(R.string.sdcard_tips_success) + "\n         " + "Fail" + "\n ");
        }
    }

    public void sdCardSizeExTest() {
        String secondaryStoragePath = getSecondaryStoragePath();
        if (secondaryStoragePath != null) {
            StatFs statfs = new StatFs(secondaryStoragePath);
            double nTotalBlocks = statfs.getBlockCount();
            double nBlocSize = statfs.getBlockSize();
            double nAvailaBlock = statfs.getAvailableBlocks();
            double nSDTotalSize = Math.round(nTotalBlocks * nBlocSize / 1024 / 1024 / 1024 * 100) / 100.00;
            double nSDFreeSize = Math.round(nAvailaBlock * nBlocSize / 1024 / 1024 / 1024 * 100) / 100.00;
            mSDCardInfoEx.setText(getString(R.string.sdcard_tips_success_ex) + "\n" + getString(R.string.sdcard_totalsize)
                    + nSDTotalSize + "GB" + "\n" + getString(R.string.sdcard_freesize) + nSDFreeSize + "GB");
            mBtOk.setEnabled(true);
        } else {
            mSDCardInfoEx.setText(getString(R.string.sdcard_tips_success_ex) + "\n         " + "Fail" + "\n ");
        }
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
                    new Class[]{String.class});
            String state = (String) getVolumeStateMethod.invoke(sm, path);
            return state;
        } catch (Exception e) {
            Log.e(TAG, "getStorageState() failed", e);
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.sdcard_name,
                (v.getId() == mBtOk.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }
}
