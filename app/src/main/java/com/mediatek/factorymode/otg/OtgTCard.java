package com.mediatek.factorymode.otg;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.ShellExe;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class OtgTCard extends TestActivity implements OnClickListener {
    private static final String TAG = "OtgTCard";
    private TextView headstatus = null;
    private TextView storage_detail = null;
    private Button btn_open = null;
    private Button btn_close = null;
    private Button btn_ok = null;
    private Button btn_failed = null;
    private boolean test_result = false;
    private SharedPreferences mSp = null;
    private static final int EVENT_TICK = 1;
    private static final String EX_OTGUSB_STORAGE = "/storage/usbotg";
    private MyHandler mHandler;

    private static class MyHandler extends Handler {
        private WeakReference<OtgTCard> reference;

        public MyHandler(OtgTCard activity) {
            reference = new WeakReference<OtgTCard>(activity);//这里传入activity的上下文
        }
        @Override
        public void handleMessage(Message msg) {
            OtgTCard activity = reference.get();
            switch (msg.what) {
            case EVENT_TICK:
                activity.updateOtgStates();
                sendEmptyMessageDelayed(EVENT_TICK, 1000);
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_layout);
        mHandler = new MyHandler(this);
        init();
        headstatus.setText(R.string.media_unmounted);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessageDelayed(EVENT_TICK, 1000);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopListener();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.btn_head_open:
            Log.d(TAG, "btn_head_open");
            break;
        case R.id.btn_head_close:
            Log.d(TAG, "btn_head_close");
            break;
        case R.id.btn_head_ok:
            Log.d(TAG, "btn_head_ok");
            setResult(true);
            showResult();
            break;
        case R.id.btn_head_failed:
            Log.d(TAG, "btn_head_failed");
            setResult(false);
            showResult();
            break;
        default:
            break;
        }
    }

    private void init() {
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        headstatus = (TextView) findViewById(R.id.headstatus);
        storage_detail = (TextView) findViewById(R.id.storage_detail);
        btn_open = (Button) findViewById(R.id.btn_head_open);
        btn_close = (Button) findViewById(R.id.btn_head_close);
        btn_ok = (Button) findViewById(R.id.btn_head_ok);
        btn_failed = (Button) findViewById(R.id.btn_head_failed);
        btn_open.setOnClickListener(this);
        btn_close.setOnClickListener(this);
        btn_ok.setOnClickListener(this);
        btn_failed.setOnClickListener(this);
        btn_open.setVisibility(View.GONE);
        btn_close.setVisibility(View.GONE);
        startListener();
    }

    private void setResult(boolean test_result) {
        this.test_result = test_result;
    }

    private boolean getResult() {
        return test_result;
    }

    private void showResult() {
        boolean result = getResult();
        Utils.SetPreferences(this, mSp, R.string.otg_flashcard, result ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        OtgTCard.this.finish();
    }

    private void startListener() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.setPriority(1000);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_NOFS);
        intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentFilter.addDataScheme("file");
        registerReceiver(MyBroadcasrReciever, intentFilter);
    }

    private void stopListener() {
        unregisterReceiver(MyBroadcasrReciever);
        mHandler.removeMessages(EVENT_TICK);
    }

    private final BroadcastReceiver MyBroadcasrReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action = " + action);
            int intof_name = 0;
            // mHandler.removeMessages(EVENT_TICK);
            if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                intof_name = R.string.media_unmounted;
                storage_detail.setVisibility(View.GONE);
            } else if (action.equals(Intent.ACTION_MEDIA_CHECKING)) {
                intof_name = R.string.media_checking;
            } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                intof_name = R.string.media_mouned;
            } else if (action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)) {
                intof_name = R.string.media_removed;
                storage_detail.setVisibility(View.GONE);
            }
            if (intof_name != 0) {
                headstatus.setText(intof_name);
                if (headstatus.getText().toString().equals(getString(R.string.media_mouned))) {
                    if (file_isExist(EX_OTGUSB_STORAGE)) {
                        usbOtgCardSize();
                    } else {
                        storage_detail.setVisibility(View.VISIBLE);
                        storage_detail.setText(R.string.sdcard_tips_failed);
                    }
                }
            }
        }
    };

    private int getOtgState() {
        String[] cmdx = { "/system/bin/sh", "-c", "cat /sys/class/switch/otg_state/state" }; // file
        int ret = 0;
        try {
            ret = ShellExe.execCommand(cmdx);
            if (0 == ret) {
                // Toast.makeText(this, "ok", Toast.LENGTH_LONG).show();
            } else {
                // Toast.makeText(this, "failed!", Toast.LENGTH_LONG).show();
                return 0;
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return 0;
        }
        return Integer.valueOf(ShellExe.getOutput());
    }

    private void updateOtgStates() {
        if (getOtgState() == 1) {
            if (file_isExist(EX_OTGUSB_STORAGE)) {
                headstatus.setText(R.string.media_mouned);
                usbOtgCardSize();
            } else {
                storage_detail.setVisibility(View.VISIBLE);
                storage_detail.setText(R.string.sdcard_tips_failed);
            }
        } else {
            headstatus.setText(R.string.media_unmounted);
        }
    }

    private void getVolumeOfStorage(String path) {
        StatFs statfs = new StatFs(path);
        long nTotalBlocks = statfs.getBlockCount();
        long nBlocSize = statfs.getBlockSize();
        long nAvailaBlock = statfs.getAvailableBlocks();
        long nSDTotalSize = nTotalBlocks * nBlocSize / 1024 / 1024;
        long nSDFreeSize = nAvailaBlock * nBlocSize / 1024 / 1024;
        storage_detail.setVisibility(View.VISIBLE);
        storage_detail.setText("\r\n" + getString(R.string.sdcard_tips_success) + getString(R.string.ex_usb_sdcard)
                + "\n" + getString(R.string.sdcard_totalsize) + nSDTotalSize + "MB" + "\n"
                + getString(R.string.sdcard_freesize) + nSDFreeSize + "MB" + "\n");
    }

    public void usbOtgCardSize() {
        StorageManager mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] volumes = mStorageManager.getVolumeList();
        String path = EX_OTGUSB_STORAGE;
        if (mStorageManager.getVolumeState(path).equals(Environment.MEDIA_MOUNTED)) {
            getVolumeOfStorage(path);
        }
    }

    public boolean file_isExist(String path) {
        boolean result = false;
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            if (file.list() != null) {
                if (file.list().length > 0) {
                    result = true;
                } else {
                    result = false;
                }
            }
        }
        return result;
    }
}