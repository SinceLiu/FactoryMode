package com.mediatek.factorymode.keycode;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.FactoryMode;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

public class KeyCode extends TestActivity implements OnClickListener {
    private static final String TAG = "Keycode";
    SharedPreferences mSp;
    TextView mInfo;
    Button mBtOk;
    Button mBtFailed;
    String mKeycode = "";
    private GridView mGrid;
    private List<String> mListData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cust_keycode);

        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mInfo = (TextView) findViewById(R.id.keycode_info);
        mBtOk = (Button) findViewById(R.id.keycode_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.keycode_bt_failed);
        mBtFailed.setOnClickListener(this);
        mListData = new ArrayList<String>();
        mGrid = (GridView) findViewById(R.id.keycode_grid);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.v(TAG, "onKeyDown: keyCode=" + keyCode + " event=" + event);

        String keyCodeStr = "Unknown";
        switch (keyCode) {
        case KeyEvent.KEYCODE_HOME:
            keyCodeStr = "HOME";
            break;
        case KeyEvent.KEYCODE_POWER:
            keyCodeStr = "POWER";
            break;
        case KeyEvent.KEYCODE_MENU:
            keyCodeStr = "MENU";
            break;
        case KeyEvent.KEYCODE_BACK:
            keyCodeStr = "BACK";
            break;
        case KeyEvent.KEYCODE_VOLUME_UP:
            keyCodeStr = "VLUP";
            break;
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            keyCodeStr = "VLDOWN";
            break;
        case KeyEvent.KEYCODE_CAMERA:
            keyCodeStr = "CAMERA";
            break;
        case KeyEvent.KEYCODE_HEADSETHOOK:
            keyCodeStr = "HEADSET";
            break;
        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            keyCodeStr = "PLAY/PAUSE";
            break;
        case KeyEvent.KEYCODE_MEDIA_STOP:
            keyCodeStr = "STOP";
            break;
        case KeyEvent.KEYCODE_MEDIA_NEXT:
            keyCodeStr = "NEXT";
            break;
        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            keyCodeStr = "PREVIOUS";
            break;
        case KeyEvent.KEYCODE_SEARCH:
            keyCodeStr = "SEARCH";
            break;
        case KeyEvent.KEYCODE_STAR:
            keyCodeStr = "STAR";
            break;
        case KeyEvent.KEYCODE_POUND:
            keyCodeStr = "POUND";
            break;
        case KeyEvent.KEYCODE_VOLUME_MUTE:
            keyCodeStr = "MUTE";
            break;
        case KeyEvent.KEYCODE_FORWARD_DEL:
            keyCodeStr = "DEL";
            break;
        case KeyEvent.KEYCODE_CONTACTS:
            keyCodeStr = "CONTACTS";
            break;
        case KeyEvent.KEYCODE_ENVELOPE:
            keyCodeStr = "ENVELOPE";
            break;
        default:
            if ((keyCode >= KeyEvent.KEYCODE_0) && (keyCode <= KeyEvent.KEYCODE_9)) {
                String custKey = String.valueOf(keyCode - KeyEvent.KEYCODE_0);
                keyCodeStr = custKey;
            } else if((keyCode >= KeyEvent.KEYCODE_F1) && (keyCode <= KeyEvent.KEYCODE_F12)) {
                String custKey = String.valueOf(keyCode - KeyEvent.KEYCODE_F1 + 1);
                keyCodeStr = "F" + custKey;
            } else if((keyCode >= KeyEvent.KEYCODE_A) && (keyCode <= KeyEvent.KEYCODE_Z)) {
                String[] alpha = {"A", "B", "C", "D", "E", "F", "G", 
                        "H", "I", "J", "K", "L", "M", "N", 
                        "O", "P", "Q", "R", "S", "T", 
                        "U", "V", "W", "X", "Y", "Z"};
                String custKey = alpha[keyCode - KeyEvent.KEYCODE_A];
                keyCodeStr = custKey;
            }
            break;
        }

        if(mListData != null && mListData.contains(keyCodeStr)) {
            mListData.remove(keyCodeStr);
        }
        mListData.add(keyCodeStr);

        mGrid.setAdapter(new MyAdapter(this));
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return true;
    }

    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        public MyAdapter(FactoryMode factoryMode, int factoryButton) {
        }

        public int getCount() {
            if (mListData == null) {
                return 0;
            }
            return mListData.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = mInflater.inflate(R.layout.cust_keycode_grid, null);
            TextView mTextView = (TextView) convertView.findViewById(R.id.textview);
            mTextView.setText(mListData.get(position));
            return convertView;
        }
    }

    @Override
    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.keyCode_name,
                (v.getId() == mBtOk.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);

        finish();
    }

    @Override
    protected void onDestroy() {
        if (null != mListData) {
            mListData.clear();
        }
        super.onDestroy();
    }
}
