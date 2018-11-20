package com.mediatek.factorymode.touchscreen;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;
import com.mediatek.factorymode.R;


public class NewTouchScreen extends TestActivity implements View.OnClickListener {
    private Button mBtOk;
    private Button mBtFailed;
    private SharedPreferences mSp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.touchscreen_new);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mBtOk = (Button) findViewById(R.id.touchscreen_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.touchscreen_bt_failed);
        mBtFailed.setOnClickListener(this);
    }

    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.touchscreen_name,
                (v.getId() == mBtOk.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        finish();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    public void finish() {
        super.finish();
        DrawView drawView = (DrawView)findViewById(R.id.draw_view);
        drawView.exit();   //停止绘制
    }
}
