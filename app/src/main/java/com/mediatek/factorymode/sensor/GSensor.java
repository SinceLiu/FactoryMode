
package com.mediatek.factorymode.sensor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;
import android.util.Log;

public class GSensor extends TestActivity implements OnClickListener {
    private ImageView ivimg;
    private Button mBtOk;
    private Button mBtFailed;
    SharedPreferences mSp;
    SensorManager mSm = null;
    Sensor mGravitySensor;
    private final static int OFFSET = 2;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gsensor);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        ivimg = (ImageView) findViewById(R.id.gsensor_iv_img);
        mBtOk = (Button) findViewById(R.id.gsensor_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.gsensor_bt_failed);
        mBtFailed.setOnClickListener(this);
        mSm = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGravitySensor = mSm.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER); //TYPE_GRAVITY  //ellery modify
        mSm.registerListener(lsn, mGravitySensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    SensorEventListener lsn = new SensorEventListener() {
        public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent e) {
            if (e.sensor == mGravitySensor) {
                int x = (int) e.values[SensorManager.DATA_X];
                int y = (int) e.values[SensorManager.DATA_Y];
                int z = (int) e.values[SensorManager.DATA_Z];
				
                int absx = Math.abs(x);
                int absy = Math.abs(y);
                int absz = Math.abs(z);


               /* if (x - OFFSET > y && x - OFFSET > z) {
                    ivimg.setBackgroundResource(R.drawable.gsensor_x_2);
                } else if (y - OFFSET > x && y - OFFSET > z) {
                    ivimg.setBackgroundResource(R.drawable.gsensor_z);
                } else if (z - OFFSET > x && z - OFFSET > y) {
                    ivimg.setBackgroundResource(R.drawable.gsensor_y);
                } else if (x < -OFFSET) {
                    ivimg.setBackgroundResource(R.drawable.gsensor_x);
                }*/
				               if(absx > absy && absx + 7 > absz) {
                    if(x > OFFSET) {
                        ivimg.setBackgroundResource(R.drawable.gsensor_x);
                    } else if(x < -OFFSET) {
                        ivimg.setBackgroundResource(R.drawable.gsensor_x_2);
                    }
                } else if(absy > absx && absy + 7 > absz) {
                    if(y > OFFSET) {
                        ivimg.setBackgroundResource(R.drawable.gsensor_y);
                    } else if(y < -OFFSET) {
                        ivimg.setBackgroundResource(R.drawable.gsensor_y_2);
                    }
                } else if(absz > absx && absz > absy) {
                    if(z > OFFSET) {
                        ivimg.setBackgroundResource(R.drawable.gsensor_z);
                    } else if(z < -OFFSET) {
                        ivimg.setBackgroundResource(R.drawable.gsensor_z_2);
                    }
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.gsensor_name,
                (v.getId() == mBtOk.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        finish();
    }

    @Override
    protected void onDestroy() {
        mSm.unregisterListener(lsn);
        super.onDestroy();
    }
}
