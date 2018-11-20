package com.mediatek.factorymode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Properties;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class BatteryLog extends TestActivity implements OnClickListener {

    private TextView mStatus;
    private TextView mTvLevel;
    private TextView mLevel;
    private TextView mScale;
    private TextView mHealth;
    private TextView mVoltage;
    private TextView mBatteryTemp;
    private TextView mCpuTemp;
    private TextView mCharging; // ellery add for charging
    private TextView mTechnology;
    private TextView mUptime;

    private Button mBtOK;
    private Button mBtFailed;
    private SharedPreferences mSp;

    private static final int EVENT_TICK = 1;
    private static final int EVENT_MA = 2;

    private MyHandler mHandler;

    String flag = "";

    private static class MyHandler extends Handler {
        private WeakReference<BatteryLog> reference;

        public MyHandler(BatteryLog activity) {
            reference = new WeakReference<BatteryLog>(activity);//这里传入activity的上下文
        }
        @Override
        public void handleMessage(Message msg) {
            BatteryLog activity = reference.get();
            switch (msg.what) {
            case EVENT_TICK:
                activity.updateBatteryStats();
                sendEmptyMessageDelayed(EVENT_TICK, 1000);
                break;
            case EVENT_MA:
                activity.updateChargingStatus();
                activity.updateBatteryTemperatureStatus();
                activity.updateCpuTemperatureStatus();
                sendEmptyMessageDelayed(EVENT_MA, 100);
                break;
            }
        }
    }

    private final String tenthsToFixedString(int x) {
        int tens = x / 10;
        return "" + tens + "." + (x - 10 * tens);
    }

    private IntentFilter mIntentFilter;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            String action = arg1.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int plugType = arg1.getIntExtra("plugged", 0);

                mLevel.setText("" + arg1.getIntExtra("level", 0));
                mScale.setText("" + arg1.getIntExtra("scale", 0));
                mVoltage.setText("" + arg1.getIntExtra("voltage", 0) + " "
                        + getString(R.string.battery_info_voltage_units));
                // mTemperature.setText("25"/* + tenthsToFixedString(arg1.getIntExtra("temperature", 0))*/
                //         + getString(R.string.battery_info_temperature_units));

                if(arg1.getIntExtra("level", 0)<=10){
                    mLevel.setTextColor(getResources().getColor(R.color.Red));
                    mLevel.setTextColor(getResources().getColor(R.color.Red));
                }else {
                    mLevel.setTextColor(getResources().getColor(R.color.black));
                    mLevel.setTextColor(getResources().getColor(R.color.black));
                }
                if(plugType == BatteryManager.BATTERY_PLUGGED_USB) {   // ellery add charging
                    //mCharging.setText("500 mA");
                } else if (plugType == BatteryManager.BATTERY_PLUGGED_AC) {
                    //mCharging.setText("750 mA");
                } else {
                    //mCharging.setText("0 mA");
                    //mCharging.setText("" + tenthsToFixedString(20 * arg1.getIntExtra("temperature", 0))
                    //        + " mA");  // ellery add charging
                }
                mTechnology.setText("" + arg1.getStringExtra("technology"));

                int status = arg1.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                if (status == BatteryManager.BATTERY_STATUS_FULL) {   // ellery add charging
                    mCharging.setText("0 mA");
                }
                String statusString;
                switch (status) {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        statusString = getString(R.string.battery_info_status_charging);
                        updateButtonStatus(true);
                        if (plugType > 0) {
                            statusString = statusString
                                    + " "
                                    + getString((plugType == BatteryManager.BATTERY_PLUGGED_AC) ? R.string.battery_info_status_charging_ac
                                    : R.string.battery_info_status_charging_usb);
                        }
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        statusString = getString(R.string.battery_info_status_discharging);
                        updateButtonStatus(false);
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        statusString = getString(R.string.battery_info_status_not_charging);
                        updateButtonStatus(true);
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        statusString = getString(R.string.battery_info_status_full);
                        updateButtonStatus(true);
                        break;
                    default:
                        statusString = getString(R.string.battery_info_status_unknown);
                        break;
                }
                mStatus.setText(statusString);

                int health = arg1.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN);
                String healthString;
                switch (health) {
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        healthString = getString(R.string.battery_info_health_good);
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        healthString = getString(R.string.battery_info_health_overheat);
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        healthString = getString(R.string.battery_info_health_dead);
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        healthString = getString(R.string.battery_info_health_over_voltage);
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        healthString = getString(R.string.battery_info_health_unspecified_failure);
                        break;
                    default:
                        healthString = getString(R.string.battery_info_health_unknown);
                        break;
                }
                mHealth.setText(healthString);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.battery_info);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mHandler = new MyHandler(this);
        updateButtonStatus(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mStatus = (TextView) findViewById(R.id.status);
        mLevel = (TextView) findViewById(R.id.level);
        mTvLevel = (TextView) findViewById(R.id.tv_level);
        mScale = (TextView) findViewById(R.id.scale);
        mHealth = (TextView) findViewById(R.id.health);
        mTechnology = (TextView) findViewById(R.id.technology);
        mVoltage = (TextView) findViewById(R.id.voltage);
        mBatteryTemp = (TextView) findViewById(R.id.battery_temperature);
        mCpuTemp = (TextView) findViewById(R.id.cpu_temperature);
        mCharging = (TextView)findViewById(R.id.charging);  // ellery add for charging
        mUptime = (TextView) findViewById(R.id.uptime);
        mBtOK = (Button) findViewById(R.id.battery_bt_ok);
        mBtOK.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.battery_bt_failed);
        mBtFailed.setOnClickListener(this);

        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mHandler.sendEmptyMessageDelayed(EVENT_TICK, 1000);
        mHandler.sendEmptyMessageDelayed(EVENT_MA, 100);
        registerReceiver(mIntentReceiver, mIntentFilter);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        Utils.hideSystemUi(this);  //隐藏系统状态栏，下拉显示，自动隐藏
    }

    private void updateBatteryStats() {
        long uptime = SystemClock.elapsedRealtime();
        mUptime.setText(DateUtils.formatElapsedTime(uptime / 1000));
    }

    private void updateChargingStatus() {
        // String[] cmd = {"/system/bin/sh", "-c", "cat /sys/class/power_supply/battery/uevent"};
        String[] cmd = { "/system/bin/sh", "-c",
                "cat /sys/devices/platform/battery/FG_Battery_CurrentConsumption" };
        try {
            int ret = ShellExe.execCommandOnServer(cmd);
            if (0 == ret) {
                String result = ShellExe.getOutput();
                if (result != null) {
//                        ByteArrayInputStream bain = new ByteArrayInputStream(result.getBytes());
//                        Properties properties = new Properties();
//                        try {
//                            properties.load(bain);
//                        } catch (IOException e) {
//                            mCharging.setText("unknown-0");
//                        }
//                        String currentMa = properties.getProperty("POWER_SUPPLY_BATTERYAVERAGECURRENT");
                    String currentMa = Integer.parseInt(result) / 10 + "";
                    mCharging.setText(currentMa + " mA");
//                    if (Integer.parseInt(currentMa) >= 500) {
//                        updateButtonStatus(true);
//                    } else {
//                        updateButtonStatus(false);
//                    }
                } else {
                    mCharging.setText("unknown-1");
                }
            } else {
                updateButtonStatus(false);
                mCharging.setText("unknown-avc");
            }
        } catch (IOException e) {
            mCharging.setText("unknown-2");
        } catch (NumberFormatException e) {
            mCharging.setText("unknown-3");
        }
    }

    private void updateBatteryTemperatureStatus() {
        // battery temp
        String[] cmd = { "/system/bin/sh", "-c", "cat /sys/class/thermal/thermal_zone0/temp" };
        try {
            int ret = ShellExe.execCommandOnServer(cmd);
            if (ret == 0) {
                String result = ShellExe.getOutput();
                if (result != null) {
                    int value = Integer.parseInt(result);
                    String temp = value / 1000 + "." + (value / 100) % 10;
                    mBatteryTemp.setText(temp + getString(R.string.battery_info_temperature_units));
                } else {
                    mBatteryTemp.setText("unknown-1");
                }
            } else {
                mBatteryTemp.setText("unknown-avc");
            }
        } catch (IOException e) {
            mBatteryTemp.setText("unknown-2");
        } catch (NumberFormatException e) {
            mBatteryTemp.setText("unknown-3");
        }
    }

    private void updateCpuTemperatureStatus() {
        // cpu temp
        String[] cmd = { "/system/bin/sh", "-c", "cat /sys/class/thermal/thermal_zone1/temp" };
        try {
            int ret = ShellExe.execCommandOnServer(cmd);
            if (ret == 0) {
                String result = ShellExe.getOutput();
                if (result != null) {
                    int value = Integer.parseInt(result);
                    String temp = value / 1000 + "." + (value / 100) % 10;
                    mCpuTemp.setText(temp + getString(R.string.battery_info_temperature_units));
                } else {
                    mCpuTemp.setText("unknown-1");
                }
            } else {
                mCpuTemp.setText("unknown-avc");
            }
        } catch (IOException e) {
            mCpuTemp.setText("unknown-2");
        } catch (NumberFormatException e) {
            mCpuTemp.setText("unknown-3");
        }
    }

    private void updateButtonStatus(boolean status) {
        if (null != mBtOK) {
            mBtOK.setEnabled(status);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);  //把消息对象从消息队列移除
        // we are no longer on the screen stop the observers
        unregisterReceiver(mIntentReceiver);
    }

    public void onClick(View v) {
        Utils.SetPreferences(this, mSp, R.string.battery_name,
                (v.getId() == mBtOK.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
        finish();
    }

//    public boolean onCreateOptionsMenu(Menu menu) {
//        menu.add(0, 1, 1, R.string.menu_exit);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    public boolean onOptionsItemSelected(MenuItem item) {
//        setResult(Activity.RESULT_FIRST_USER);
//        finish();
//        return true;
//    }
}
