package com.mediatek.factorymode;

import java.util.List;

import com.mediatek.factorymode.wifi.WiFiTools;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AllTest extends TestActivity {
    WiFiTools mWifiTools;

    boolean mOtherOk = false;

    boolean mBlueResult = false;
    boolean mBlueFlag = false;
    boolean mBlueStatus = false;

    boolean mSdCardResult = false;

    Message msg = null;

    SharedPreferences mSp;

//    private BluetoothAdapter mAdapter = null;

    boolean isregisterReceiver = false;

//    HandlerThread mBlueThread = new HandlerThread("blueThread");

//    BlueHandler mBlueHandler;



    // GPSThread mGPS = null; //bob

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.alltest);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);

//        mBlueThread.start();
//        mBlueHandler = new BlueHandler(mBlueThread.getLooper());
//        mBlueHandler.post(bluerunnable);

        // mGPS = new GPSThread(this); //bob
        // mGPS.start();

        Intent intent = new Intent();
        String classname = null;


//        classname = "com.mediatek.factorymode.touchscreen.LineTest";
        classname = "com.mediatek.factorymode.touchscreen.NewTouchScreen";

        intent.setClassName(this, classname);
        this.startActivityForResult(intent, AppDefine.FT_TOUCHSCREENID);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent intent = new Intent();
        // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); //bob.chen disabled

        String classname = null;
        int requestid = AppDefine.FT_AllTest_END;

        switch (requestCode) {
            case AppDefine.FT_TOUCHSCREENID: {
                requestid = AppDefine.FT_LCDID;
                classname = "com.mediatek.factorymode.lcd.LCD";
                break;
            }
            case AppDefine.FT_LCDID: {
                requestid = AppDefine.FT_BATTERYID;
                classname = "com.mediatek.factorymode.BatteryLog";
                break;
            }
            case AppDefine.FT_BATTERYID: {
//                requestid = AppDefine.FT_KEYCODEID;
//                if(FactoryModeFeatureOption.CENON_PROJECT_A865){
//                    classname = "com.mediatek.factorymode.CustKeyCode";
//                }else{
//                    classname = "com.mediatek.factorymode.keycode.KeyCode";
//                }
//                break;
//            }
//            case AppDefine.FT_KEYCODEID:{
                requestid = AppDefine.FT_AUDIOID;
                classname = "com.mediatek.factorymode.audio.AudioTest";
                break;
            }
            case AppDefine.FT_AUDIOID: {
                if (FactoryModeFeatureOption.CENON_HEADSET_FEATURE) {
                    requestid = AppDefine.FT_HEADSETID;
                    classname = "com.mediatek.factorymode.headset.HeadSet";
                    break;
                }
            }
            case AppDefine.FT_HEADSETID: {
                requestid = AppDefine.FT_MICROPHONEID;
                classname = "com.mediatek.factorymode.microphone.MicRecorder";
                break;
            }
            case AppDefine.FT_MICROPHONEID: {
                if (FactoryModeFeatureOption.CENON_EARPHONE_FEATURE) {
//                  requestid = AppDefine.FT_EARPHONEID;
//                  classname = "com.mediatek.factorymode.earphone.Earphone";  //听筒
                    requestid = AppDefine.FT_WIFIID;
                    classname = "com.mediatek.factorymode.wifi.Wifi";
                    break;
                }
            }
            case AppDefine.FT_EARPHONEID: {
                if (FactoryModeFeatureOption.CENON_HALL_FEATURE) {
                    requestid = AppDefine.FT_HALLID;
                    classname = "com.mediatek.factorymode.hall.Hall";
                    break;
                }
            }
            case AppDefine.FT_HALLID: {
                requestid = AppDefine.FT_WIFIID;
                classname = "com.mediatek.factorymode.wifi.WiFiTest";
                break;
            }
            case AppDefine.FT_WIFIID: {
                requestid = AppDefine.FT_BLUETOOTHID;
                classname = "com.mediatek.factorymode.bluetooth.Bluetooth";
                break;
            }
            case AppDefine.FT_BLUETOOTHID: {
//                if(FactoryModeFeatureOption.CENON_VIBRATE_FEATURE){
//                    requestid = AppDefine.FT_VIBRATORID;
//                    classname = "com.mediatek.factorymode.vibrator.Vibrator";
//                    break;
//                }
                requestid = AppDefine.FT_BACKLIGHTID;
                classname = "com.mediatek.factorymode.backlight.BackLight";
                break;
            }
            case AppDefine.FT_VIBRATORID: {
                if (FactoryModeFeatureOption.CENON_TELEPHONE_FEATURE) {
                    requestid = AppDefine.FT_TELEPHONE;
                    classname = "com.mediatek.factorymode.signal.Signal";
                    break;
                }
            }
            case AppDefine.FT_TELEPHONE: {
                requestid = AppDefine.FT_BACKLIGHTID;
                classname = "com.mediatek.factorymode.backlight.BackLight";
                break;
            }
            case AppDefine.FT_BACKLIGHTID: {
                requestid = AppDefine.FT_CAMERAID;
                classname = "com.mediatek.factorymode.camera.CameraTest";
                break;
            }
            case AppDefine.FT_CAMERAID: {
                if (FactoryModeFeatureOption.CENON_FRONT_CAMERA_FEATURE || !FactoryModeFeatureOption.CENON_MT6735_DEFAULT_OPEN_FRONT_CAMERA) {
                    requestid = AppDefine.FT_SUBCAMERAID;
                    classname = "com.mediatek.factorymode.camera.SubCamera";
                    break;
                }
            }
            case AppDefine.FT_SUBCAMERAID: {
                if (FactoryModeFeatureOption.CENON_GRAVITY_FEATURE) {
                    requestid = AppDefine.FT_GSENSORID;
                    classname = "com.mediatek.factorymode.sensor.GSensorWithCalibrate";
                    break;
                }
            }
            case AppDefine.FT_GSENSORID: {
                requestid = AppDefine.FT_SDCARDID;
                classname = "com.mediatek.factorymode.sdcard.SDCard";
                break;
            }
//            case AppDefine.FT_NFCID: {
//                if (FactoryModeFeatureOption.CENON_LIGHT_FEATURE) {
//                    requestid = AppDefine.FT_LSENSORID;
//                    classname = "com.mediatek.factorymode.sensor.LSensor";
//                    break;
//                }
//            }
//            case AppDefine.FT_LSENSORID: {
//                if (FactoryModeFeatureOption.CENON_DISTANCE_FEATURE) {
//                    requestid = AppDefine.FT_PSENSORID;
//                    classname = "com.mediatek.factorymode.sensor.PSensor";
//                    break;
//                }
//            }
//            case AppDefine.FT_PSENSORID: {
//                if (FactoryModeFeatureOption.CENON_MAGNETIC_FEATURE) {
//                    requestid = AppDefine.FT_MSENSORID;
//                    classname = "com.mediatek.factorymode.sensor.MSensor";
//                    break;
//                }
//            }
//            case AppDefine.FT_MSENSORID: {
//                if (FactoryModeFeatureOption.CENON_GRYOS_FEATURE) {
//                    requestid = AppDefine.FT_GYSENSORID;
//                    classname = "com.mediatek.factorymode.sensor.GYSensor";
//                    break;
//                }
//            }
//            case AppDefine.FT_GYSENSORID: {
//                if (FactoryModeFeatureOption.CENON_TORCH_FEATURE) {
//                    requestid = AppDefine.FT_TORCHID;
//                    classname = "com.mediatek.factorymode.util.Torch";
//                    break;
//                }
//            }
//            case AppDefine.FT_TORCHID: {
//                requestid = AppDefine.FT_CAMERAID;
//                classname = "com.mediatek.factorymode.camera.CameraTest";
//                break;
//            }

//            case AppDefine.FT_FMRADIOID: {
//                if (FactoryModeFeatureOption.CENON_SCAN_FEATURE) {
//                    requestid = AppDefine.FT_SCANNERID;
//                    classname = "com.mediatek.factorymode.util.Scanner";
//                    break;
//                }
//            }
//            case AppDefine.FT_SCANNERID: {
//                if (false) {
//                    requestid = AppDefine.FT_RGBLEDID;
//                    classname = "com.mediatek.factorymode.rgb_led.Rgb_led";
//                    break;
//                }
//            }
//            case AppDefine.FT_RGBLEDID: {
//                if (FactoryModeFeatureOption.CENON_SIM_FEATURE) {
//                    requestid = AppDefine.FT_SIMCARDID;
//                    classname = "com.mediatek.factorymode.simcard.SimCard";
//                    break;
//                }
//            }
//            case AppDefine.FT_SIMCARDID: {
//                if (FactoryModeFeatureOption.CENON_SIMSDCARD_FEATURE) {
//                    requestid = AppDefine.FT_SIMSDCARDHOOKID;
//                    classname = "com.mediatek.factorymode.simsdcard.SimCardPre";
//                    break;
//                }
//            }
//            case AppDefine.FT_SIMSDCARDHOOKID: {
//                if (FactoryModeFeatureOption.CENON_GPS_FEATURE) {
//                    requestid = AppDefine.FT_GPSID;
//                    classname = "com.mediatek.factorymode.gps.GPS";
//                    break;
//                }
//            }
//            case AppDefine.FT_GPSID: {
//                if (FactoryModeFeatureOption.CENON_FLASH_LAMP_FEATURE) {
//                    requestid = AppDefine.FT_FLASHLAMP;
//
//                    classname = "com.mediatek.factorymode.flashlamp.FlashLamp";
//                    break;
//                }
//            }
//            case AppDefine.FT_FLASHLAMP: {
//                if (!FactoryModeFeatureOption.CENON_MT6735_DEFAULT_OPEN_FRONT_CAMERA || FactoryModeFeatureOption.CENON_FRONT_CAMERA_FEATURE) {
//                    requestid = AppDefine.FT_SUBCAMERAID;
//                    classname = "com.mediatek.factorymode.camera.SubCamera";
//                    break;
//                }
//            }

            case AppDefine.FT_SDCARDID: {
//                if (FactoryModeFeatureOption.CENON_OTG_FEATURE) {
                requestid = AppDefine.FT_OTG;
                classname = "com.mediatek.factorymode.otg.OtgState";
                break;
//                }
            }
            case AppDefine.FT_OTG: {
                if (FactoryModeFeatureOption.CENON_DEVICE_INFO) {
                    requestid = AppDefine.FT_DEVICEINFO;
                    classname = "com.mediatek.factorymode.DeviceInfo";
                    break;
                }
            }
//            case AppDefine.FT_USBOTGID: {
//                if(FactoryModeFeatureOption.CENON_HDMI_FEATURE){
//                    requestid = AppDefine.FT_HDMIID;
//                    classname = "com.mediatek.factorymode.util.Hdmi";
//                    break;
//                }
//
//            }
//            case AppDefine.FT_HDMIID: {
//                if (FactoryModeFeatureOption.CENON_GPIO_FEATURE) {
//                    requestid = AppDefine.FT_GPIOID;
//
//                    classname = "com.mediatek.factorymode.util.GPIO";
//                    break;
//                }
//            }
//            case AppDefine.FT_GPIOID: {
//                if (FactoryModeFeatureOption.CENON_INDICATOR_LIGHT_FEATURE) {
//                    requestid = AppDefine.FT_INDICATORLIGHTID;
//
//                    classname = "com.mediatek.factorymode.util.IndicatorLight";
//                    break;
//                }
//            }
//            case AppDefine.FT_INDICATORLIGHTID: {
//                if (FactoryModeFeatureOption.CENON_MT6735_FINGER_RECOGNITION_SUPPORT) {
//                    requestid = AppDefine.FT_FINGERPRINTID;
//
//                    classname = "com.mediatek.factorymode.util.Fingerprint";
//                    break;
//                }
//            }
//            case AppDefine.FT_FINGERPRINTID: {
//                if (FactoryModeFeatureOption.CENON_DEVICE_INFO) {
//                    requestid = AppDefine.FT_DEVICEINFO;
//                    classname = "com.mediatek.factorymode.DeviceInfo";
//                    break;
//                }
//            }
            case AppDefine.FT_DEVICEINFO: {

            }
            case AppDefine.FT_AllTest_END: {
                finish();
                return;
            }
            default: {
                return;
            }

        }

        if (null != classname) {
            intent.setClassName(this, classname);
            this.startActivityForResult(intent, requestid);
        }
    }

    public void onDestroy() {
        super.onDestroy();
//        BackstageDestroy();
    }

//    public void BackstageDestroy() {
//        mBlueHandler.removeCallbacks(bluerunnable);
//        mWifiHandler.removeCallbacks(wifirunnable);
//        if (isregisterReceiver) {
//            unregisterReceiver(mReceiver);
//        }
//        mAdapter.disable();
        // mGPS.closeLocation(); //bob
//    }

    public void SdCardInit() {
        String sDcString = Environment.getExternalStorageState();
        if (sDcString.equals(Environment.MEDIA_MOUNTED)) {
            mSdCardResult = true;
        }
    }

//    public void BlueInit() {
//        mAdapter = BluetoothAdapter.getDefaultAdapter();
//        mAdapter.enable();
//        if (mAdapter.isEnabled()) {
//            StartReciver();
//            while (!mAdapter.startDiscovery()) {
//                mAdapter.startDiscovery();
//            }
//        } else {
//            mBlueHandler.postDelayed(bluerunnable, 3000);
//        }
//    }

//    public void StartReciver() {
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(mReceiver, filter);
//        isregisterReceiver = true;
//    }

//    BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
//                    mBlueResult = true;
//                    if (isregisterReceiver) {
//                        unregisterReceiver(mReceiver);
//                        isregisterReceiver = false;
//                    }
//                    mAdapter.disable();
//                }
//            }
//        }
//    };
//
//    Runnable bluerunnable = new Runnable() {
//        @Override
//        public void run() {
//            BlueInit();
//        }
//    };
//
//    class BlueHandler extends Handler {
//        public BlueHandler() {
//        }
//
//        public BlueHandler(Looper looper) {
//            super(looper);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//        }
//    }

}
