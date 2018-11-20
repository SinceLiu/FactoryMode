
package com.mediatek.factorymode;

import java.util.ArrayList;
import java.util.List;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import android.util.Log;

import com.mediatek.factorymode.wifi.WifiService;

public class FactoryMode extends TestActivity implements OnItemClickListener {
    /** Called when the activity is first created. */
    public static final String TAG = "FactoryMode";
    public static final int PASS_COLOR = 0xFF698B22;

    private List<String> mListData;
    private SharedPreferences mSp = null;

    private GridView mGrid;

    private MyAdapter mAdapter;

    public static int itemString[] = {
        R.string.touchscreen_name,
        R.string.lcd_name,
        R.string.battery_name,
//            R.string.keyCode_name,
        R.string.speaker_name,
        R.string.headset_name,
//        R.string.double_microphone_name,
        R.string.microphone_name,
        //R.string.earphone_name,
        //R.string.hall_name,
        R.string.WiFi,
        R.string.bluetooth_name,
        //R.string.vibrator_name,
        R.string.telephone_name,
        R.string.backlight_name,
        R.string.camera_name,
        R.string.subcamera_name,
        R.string.gsensor_with_calibrate_name,
//        R.string.gsensor_name,
//        R.string.nfc,
//        R.string.lsensor_name,
//        R.string.psensor_name,
        //R.string.msensor_name,
        //R.string.gysensor_name,
//            R.string.fmradio_name,
        //R.string.rgb_led_name,
        R.string.sdcard_name,
//        R.string.sim_sdcard_name,
//        R.string.gps_name,
//        R.string.gps1_name,
//        R.string.gps2_name,
        //R.string.kpd_light_name,
        //R.string.flash_lamp,
        R.string.otg_name,
//        R.string.usb_otg_name,
//        R.string.otg_speaker_name,
        //R.string.hdmi_name,
        //R.string.gpio_name,
//        R.string.indicatorlight_name,
        //R.string.color_light,
        //R.string.fingerprint_recognition,
        R.string.device_info
    };

    private Button mBtAll;
//    private Button mBtAuto;

    private boolean batteryLow = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("FactoryMode","onCreate()");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

//        mBtAuto = (Button) findViewById(R.id.main_bt_autotest);
//        mBtAuto.setOnClickListener(cl);
        mBtAll = (Button) findViewById(R.id.main_bt_alltest);
        mBtAll.setOnClickListener(cl);
        mGrid = (GridView) findViewById(R.id.main_grid);
        mListData = getData();
        mAdapter = new MyAdapter(this);

        IntentFilter batteryIntentFilter = new IntentFilter();
        batteryIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, batteryIntentFilter);

        //记录wifi的状态，用于退出CIT时恢复
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
            Utils.setIsWifiOpened(true);
        else
            Utils.setIsWifiOpened(false);
        //开始后台搜索wifi
        Intent startWifiService = new Intent(FactoryMode.this, WifiService.class);
        startService(startWifiService);
    }

    @Override
    protected void onResume() {
        Log.i("FactoryMode","onResume()");
        super.onResume();
        mGrid.setAdapter(mAdapter);
        mGrid.setOnItemClickListener(this);
    }

    @Override
    protected void onPause(){
        Log.i("FactoryMode","onPause()");
        super.onPause();
        if(isFinishing()){
            Intent stopWifiService = new Intent(FactoryMode.this,WifiService.class);
            stopService(stopWifiService);
            if(mSp!=null){   //清除数据
                Editor editor = mSp.edit();
                editor.clear();
                editor.apply();
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.i("FactoryMode","onDestroy()");
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
        if(isServiceRunning("com.mediatek.factorymode.wifi.WifiService")){
            Log.i("FactoryMode","stop WifiService");
            Intent stopWifiService = new Intent(FactoryMode.this,WifiService.class);
            stopService(stopWifiService);
        }
        if(mSp!=null){   //清除数据
            Editor editor = mSp.edit();
            editor.clear();
            editor.apply();
        }
//        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public View.OnClickListener cl = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent();
            int reqId = -1;
//            if (v.getId() == mBtAuto.getId()) {
//                intent.setClassName("com.mediatek.factorymode", "com.mediatek.factorymode.AutoTest");
//                reqId = AppDefine.FT_AUTOTESTID;
//            }
            if (v.getId() == mBtAll.getId()) {
                intent.setClassName("com.mediatek.factorymode", "com.mediatek.factorymode.AllTest");
                reqId = AppDefine.FT_ALLTESTID;
            }
            startActivityForResult(intent, reqId);
        }
    };

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
            convertView = mInflater.inflate(R.layout.main_grid, null);
            TextView textview = (TextView) convertView.findViewById(R.id.factor_button);
            textview.setText(mListData.get(position));
            SetColor(textview);
            return convertView;
        }
    }

    private void init() {
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        Editor editor = mSp.edit();
        for (int i = 0; i < itemString.length; i++) {
            editor.putString(getString(itemString[i]), AppDefine.FT_DEFAULT);
        }
        editor.putString(getString(R.string.headsethook_name), AppDefine.FT_DEFAULT);
        editor.apply();
    }

    private void SetColor(TextView s) {
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        for (int i = 0; i < itemString.length; i++) {
            if (getResources().getString(itemString[i]).equals(s.getText().toString())) {
                String name = mSp.getString(getString(itemString[i]), AppDefine.FT_DEFAULT);
//                if(getResources().getString(itemString[i]).equals(getResources().getString(R.string.device_info))) {
//                    name = FactoryModeFeatureOption.get("persist.sys.device_info", AppDefine.FT_DEFAULT);
//                }
                if (name.equals(AppDefine.FT_SUCCESS)) {
                    s.setTextColor(PASS_COLOR);
                } else if (name.equals(AppDefine.FT_DEFAULT)) {
                    s.setTextColor(getApplicationContext().getResources().getColor(R.color.black));
                } else if (name.equals(AppDefine.FT_FAILED)) {
                    s.setTextColor(getApplicationContext().getResources().getColor(R.color.Red));
                }
            }
        }
    }

    private List<String> getData() {
        List<String> items = new ArrayList<String>();
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
        for (int i = 0; i < itemString.length; i++) {
            if (pre.getBoolean(getString(itemString[i]), true)) {
                if (true) {
                    if (getString(R.string.rgb_led_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }


                if (!FactoryModeFeatureOption.CENON_HEADSET_FEATURE) {
                    if (getString(R.string.headset_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }

                if (FactoryModeFeatureOption.CENON_DOUBLE_MIC_FEATURE) {
                    if (getString(R.string.microphone_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                } else {
                    if (getString(R.string.double_microphone_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }

                if (!FactoryModeFeatureOption.CENON_EARPHONE_FEATURE) {
                    if (getString(R.string.earphone_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_FACTORY_BACKTOUCH) {
                    if (getString(R.string.backtouch_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_FACTORY_KPDLED) {
                    if (getString(R.string.kpd_light_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_MAGNETIC_FEATURE) {
                    if (getString(R.string.msensor_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_NFC) {
                    if (getString(R.string.nfc).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (FactoryModeFeatureOption.CENON_MT6735_DEFAULT_OPEN_FRONT_CAMERA || !FactoryModeFeatureOption.CENON_FRONT_CAMERA_FEATURE) {
                    if (getString(R.string.subcamera_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_VIBRATE_FEATURE) {
                    if (getString(R.string.vibrator_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_GRAVITY_FEATURE) {
                    if (getString(R.string.gsensor_name).equals(getString(itemString[i]))
                            || getString(R.string.gsensor_with_calibrate_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_LIGHT_FEATURE) {
                    if (getString(R.string.lsensor_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_DISTANCE_FEATURE) {
                    if (getString(R.string.psensor_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_GRYOS_FEATURE) {
                    if (getString(R.string.gysensor_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                //gps start
                if (!FactoryModeFeatureOption.CENON_GPS_FEATURE) { //no gps
                    if (getString(R.string.gps_name).equals(getString(itemString[i]))
                            || getString(R.string.gps2_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                } else {
                    if (FactoryModeFeatureOption.CENON_FACTORY_COMMON/*true*/) {
                        if (getString(R.string.gps_name).equals(getString(itemString[i]))) {
                            continue;
                        }
                    } else {
                        if (getString(R.string.gps1_name).equals(getString(itemString[i]))) {
                            continue;
                        }
                    }
                }

                if (!FactoryModeFeatureOption.CENON_GPS1_FEATURE) {
                    if (getString(R.string.gps1_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                //gps end

                if (!FactoryModeFeatureOption.CENON_FM_FEATURE) {
                    if (getString(R.string.fmradio_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_FLASH_LAMP_FEATURE) {
                    if (getString(R.string.flash_lamp).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_DEVICE_INFO) {
                    if (getString(R.string.device_info).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_SIMSDCARD_FEATURE) {
                    if (getString(R.string.sim_sdcard_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                } else {
                    if (getString(R.string.sdcard_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_GPIO_FEATURE) {
                    if (getString(R.string.gpio_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_INDICATOR_LIGHT_FEATURE) {
                    if (getString(R.string.indicatorlight_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_USB_OTG_FEATURE) {
                    if (getString(R.string.usb_otg_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_HDMI_FEATURE) {
                    if (getString(R.string.hdmi_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_MT6735_COLOR_LIGHT_SUPPORT) {
                    if (getString(R.string.color_light).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_TELEPHONE_FEATURE) {
                    if (getString(R.string.telephone_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (!FactoryModeFeatureOption.CENON_MT6735_FM_USBOTG) {
                    if (getString(R.string.otg_flashcard).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                if (FactoryModeFeatureOption.CENON_PROJECT_A868) {
                    if (getString(R.string.backlight_name).equals(getString(itemString[i]))) {
                        continue;
                    }
                }
                items.add(getString(itemString[i]));
            }
        }
        return items;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            String name = mListData.get(position);
            String classname = null;
            if (getString(R.string.speaker_name).equals(name)) {
                classname = "com.mediatek.factorymode.audio.AudioTest";
            } else if (getString(R.string.battery_name).equals(name)) {
                classname = "com.mediatek.factorymode.BatteryLog";
            } else if (getString(R.string.touchscreen_name).equals(name)) {
//                classname = "com.mediatek.factorymode.touchscreen.LineTest";
                classname = "com.mediatek.factorymode.touchscreen.NewTouchScreen";
            } else if (getString(R.string.camera_name).equals(name)) {
                classname = "com.mediatek.factorymode.camera.CameraTest";
            } else if (getString(R.string.wifi_name).equals(name)) {
//                classname = "com.mediatek.factorymode.wifi.WiFiTest";
                classname = "com.mediatek.factorymode.wifi.Wifi";
            } else if (getString(R.string.bluetooth_name).equals(name)) {
                classname = "com.mediatek.factorymode.bluetooth.Bluetooth";
            } else if (getString(R.string.headset_name).equals(name)) {
                classname = "com.mediatek.factorymode.headset.HeadSet";
            } else if (getString(R.string.earphone_name).equals(name)) {
                classname = "com.mediatek.factorymode.earphone.Earphone";
            } else if (getString(R.string.vibrator_name).equals(name)) {
                classname = "com.mediatek.factorymode.vibrator.Vibrator";
            } else if (getString(R.string.telephone_name).equals(name)) {
                classname = "com.mediatek.factorymode.signal.Signal";
            } else if (getString(R.string.gps_name).equals(name)) {
                classname = "com.mediatek.factorymode.gps.GPS";
            } else if (getString(R.string.gps1_name).equals(name)) {
                classname = "com.mediatek.factorymode.gps.GPS1";
            } else if (getString(R.string.gps2_name).equals(name)) {
                classname = "com.mediatek.factorymode.gps.GPS2";
            } else if (getString(R.string.otg_name).equals(name)) {
                classname = "com.mediatek.factorymode.otg.OtgState";
            } else if (getString(R.string.backlight_name).equals(name)) {
                classname = "com.mediatek.factorymode.backlight.BackLight";
            } else if (getString(R.string.memory_name).equals(name)) {
                classname = "com.mediatek.factorymode.memory.Memory";
            } else if (getString(R.string.double_microphone_name).equals(name)) {
                classname = "com.mediatek.factorymode.microphone.DoubleMicPhone";
            } else if (getString(R.string.microphone_name).equals(name)) {
                classname = "com.mediatek.factorymode.microphone.MicRecorder";
            } else if (getString(R.string.gsensor_name).equals(name)) {
                classname = "com.mediatek.factorymode.sensor.GSensor";
            } else if (getString(R.string.gsensor_with_calibrate_name).equals(name)) {
                classname = "com.mediatek.factorymode.sensor.GSensorWithCalibrate";
            } else if (getString(R.string.gysensor_name).equals(name)) {
                classname = "com.mediatek.factorymode.sensor.GYSensor";
            } else if (getString(R.string.msensor_name).equals(name)) {
                classname = "com.mediatek.factorymode.sensor.MSensor";
            } else if (getString(R.string.lsensor_name).equals(name)) {
                classname = "com.mediatek.factorymode.sensor.LSensor";
            } else if (getString(R.string.psensor_name).equals(name)) {
                classname = "com.mediatek.factorymode.sensor.PSensor";
            } else if (getString(R.string.sdcard_name).equals(name)) {
                classname = "com.mediatek.factorymode.sdcard.SDCard";
            } else if (getString(R.string.fmradio_name).equals(name)) {
                classname = "com.mediatek.factorymode.fmradio.FMRadio";
            } else if (getString(R.string.keyCode_name).equals(name)) {
                classname = "com.mediatek.factorymode.keycode.KeyCode";
            } else if (getString(R.string.lcd_name).equals(name)) {
                classname = "com.mediatek.factorymode.lcd.LCD";
            } else if (getString(R.string.sim_name).equals(name)) {
                classname = "com.mediatek.factorymode.simcard.SimCard";
            } else if (getString(R.string.sim_sdcard_name).equals(name)) {
                classname = "com.mediatek.factorymode.simsdcard.SimCardPre";
            } else if (getString(R.string.subcamera_name).equals(name)) {
                classname = "com.mediatek.factorymode.camera.SubCamera";
            } else if (getString(R.string.rgb_led_name).equals(name)) {
                classname = "com.mediatek.factorymode.rgb_led.Rgb_led";
            } else if (getString(R.string.kpd_light_name).equals(name)) {
                classname = "com.mediatek.factorymode.kpdLed.KpdLed";
            } else if (getString(R.string.flash_lamp).equals(name)) {
                classname = "com.mediatek.factorymode.flashlamp.FlashLamp";
                intent.putExtra("batterylow", batteryLow);
            } else if (getString(R.string.hall_name).equals(name)) {
                classname = "com.mediatek.factorymode.hall.Hall";
            } else if (getString(R.string.fingerprint_recognition).equals(name)) {
                classname = "com.mediatek.factorymode.util.Fingerprint";
            } else if (getString(R.string.backtouch_name).equals(name)) {
                classname = "com.mediatek.factorymode.backtouch.BackTouch";
            } else if (getString(R.string.nfc).equals(name)) {
                classname = "com.mediatek.factorymode.sensor.Nfc";
            } else if (getString(R.string.device_info).equals(name)) {
                classname = "com.mediatek.factorymode.DeviceInfo";
            } else if (getString(R.string.otg_flashcard).equals(name)) {
                classname = "com.mediatek.factorymode.otg.OtgTCard";
            } else if (getString(R.string.otg_speaker_name).equals(name)) {
                classname = "com.mediatek.factorymode.otg.OtgAudioTest";
            } else if (getString(R.string.gpio_name).equals(name)) {
                classname = "com.mediatek.factorymode.util.GPIO";
            } else if (getString(R.string.indicatorlight_name).equals(name)) {
                classname = "com.mediatek.factorymode.util.IndicatorLight";
            } else if (getString(R.string.usb_otg_name).equals(name)) {
                classname = "com.mediatek.factorymode.util.UsbOtgState";
            } else if (getString(R.string.hdmi_name).equals(name)) {
                classname = "com.mediatek.factorymode.util.Hdmi";
            }
            intent.setClassName(this, classname);
            this.startActivity(intent);

        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this , AlertDialog.THEME_TRADITIONAL);
            builder.setTitle(R.string.PackageIerror);
            builder.setMessage(R.string.Packageerror);
            builder.setPositiveButton("OK", null);
            builder.create().show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        System.gc();
        Intent intent = new Intent(FactoryMode.this, Report.class);
        startActivity(intent);
    }

   private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
                int level = (int) (100f
                        * intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                        / intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100));
                boolean charging = (status == BatteryManager.BATTERY_STATUS_CHARGING);
                if (level <= 15) {
                    batteryLow = true;
                } else {
                    batteryLow = false;
                }
            }
        }
    };

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(FactoryMode.this).setTitle(
                    R.string.alert_title).setMessage(
                    R.string.alert_content).setPositiveButton(
                    R.string.alert_dialog_ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            finish();
                        }
                    }).setNegativeButton(R.string.alert_dialog_cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {

                        }
                    }).create().show();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU
                || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_SEARCH
                || keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public boolean isServiceRunning(String servicename) {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> infos = am.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo info : infos) {
            if (servicename.equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
