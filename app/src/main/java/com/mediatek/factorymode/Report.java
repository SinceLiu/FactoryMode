
package com.mediatek.factorymode;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;


public class Report extends TestActivity {
    private SharedPreferences mSp;
    private TextView mSuccessTitle;
    private TextView mFailedTitle;
    private TextView mDefaultTitle;
    private TextView mSuccess;
    private TextView mFailed;
    private TextView mDefault;

    private List<String> mOkList;
    private List<String> mFailedList;
    private List<String> mDefaultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);
        int itemString[] = FactoryMode.itemString;

        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mSuccessTitle = (TextView) findViewById(R.id.title_report_success);
        mFailedTitle = (TextView) findViewById(R.id.title_report_failed);
        mDefaultTitle = (TextView) findViewById(R.id.title_report_default);
        mSuccess = (TextView) findViewById(R.id.report_success);
        mFailed = (TextView) findViewById(R.id.report_failed);
        mDefault = (TextView) findViewById(R.id.report_default);
        mOkList = new ArrayList<String>();
        mFailedList = new ArrayList<String>();
        mDefaultList = new ArrayList<String>();

        for (int i = 0; i < itemString.length; i++) {
            if(true){
                if(getString(R.string.rgb_led_name)  .equals(getString(itemString[i]))){
                     continue;
                }
            }
            if(!FactoryModeFeatureOption.CENON_HEADSET_FEATURE) {
                if(getString(R.string.headset_name)  .equals(getString(itemString[i]))){
                    continue;
                }
            }
            if(!FactoryModeFeatureOption.CENON_EARPHONE_FEATURE) {
                if(getString(R.string.earphone_name)  .equals(getString(itemString[i]))){
                    continue;
                }
            }
            if(!FactoryModeFeatureOption.CENON_FACTORY_BACKTOUCH){
                if(getString(R.string.backtouch_name)  .equals(getString(itemString[i]))){
                    continue;
                }
            }
            if(!FactoryModeFeatureOption.CENON_FACTORY_KPDLED){
                if(getString(R.string.kpd_light_name)  .equals(getString(itemString[i]))){
                    continue;
                }
            }
            if(!FactoryModeFeatureOption.CENON_MAGNETIC_FEATURE){
                if(getString(R.string.msensor_name)  .equals(getString(itemString[i]))){
                    continue;
                }
            }
            if(!FactoryModeFeatureOption.CENON_NFC){
                if(getString(R.string.nfc)  .equals(getString(itemString[i]))){
                    continue;
                }
            }
            if(!FactoryModeFeatureOption.CENON_HALL_FEATURE){
                if(getString(R.string.hall_name) .equals(getString(itemString[i]))){
                    continue;
                }
            }
            if(FactoryModeFeatureOption.CENON_MT6735_DEFAULT_OPEN_FRONT_CAMERA || !FactoryModeFeatureOption.CENON_FRONT_CAMERA_FEATURE){
               if(getString(R.string.subcamera_name) .equals(getString(itemString[i]))){
                   continue;
               }
            }
            if(!FactoryModeFeatureOption.CENON_VIBRATE_FEATURE){
               if(getString(R.string.vibrator_name) .equals(getString(itemString[i]))){
                   continue;
               }
            }
            if(!FactoryModeFeatureOption.CENON_GRAVITY_FEATURE){
               if(getString(R.string.gsensor_name) .equals(getString(itemString[i]))){
                   continue;
               }
            }
            if(!FactoryModeFeatureOption.CENON_LIGHT_FEATURE){
               if(getString(R.string.lsensor_name) .equals(getString(itemString[i]))){
                   continue;
               }
            }
            if(!FactoryModeFeatureOption.CENON_DISTANCE_FEATURE){
               if(getString(R.string.psensor_name) .equals(getString(itemString[i]))){
                   continue;
               }
            }
            if(!FactoryModeFeatureOption.CENON_GRYOS_FEATURE){
               if(getString(R.string.gysensor_name) .equals(getString(itemString[i]))){
                   continue;
               }
            }
            if(!FactoryModeFeatureOption.CENON_TORCH_FEATURE){
               if(getString(R.string.torch_name) .equals(getString(itemString[i]))){
                   continue;
               }
            }
            if(!FactoryModeFeatureOption.CENON_GPS_FEATURE){
               if(getString(R.string.gps_name) .equals(getString(itemString[i]))){
                   continue;
               }
            }
            if(!FactoryModeFeatureOption.CENON_FM_FEATURE){
                if(getString(R.string.fmradio_name) .equals(getString(itemString[i]))){
                    continue;
                }
            }
            if(!FactoryModeFeatureOption.CENON_FLASH_LAMP_FEATURE){
               if(getString(R.string.flash_lamp) .equals(getString(itemString[i]))){
                   continue;
               }
            }
            if(!FactoryModeFeatureOption.CENON_DEVICE_INFO){
               if(getString(R.string.device_info) .equals(getString(itemString[i]))){
                   continue;
               }
            }
            if(!FactoryModeFeatureOption.CENON_GPIO_FEATURE){
               if(getString(R.string.gpio_name) .equals(getString(itemString[i]))){
                   continue;
               }
            }
            if(!FactoryModeFeatureOption.CENON_INDICATOR_LIGHT_FEATURE){
               if(getString(R.string.indicatorlight_name) .equals(getString(itemString[i]))){
                   continue;
               }
            }
//            if(!FactoryModeFeatureOption.CENON_OTG_FEATURE){
//               if(getString(R.string.otg_name) .equals(getString(itemString[i]))){
//                   continue;
//               }
//            }
            if(!FactoryModeFeatureOption.CENON_USB_OTG_FEATURE){
               if(getString(R.string.usb_otg_name) .equals(getString(itemString[i]))){
                   continue;
               }
            }
            if(!FactoryModeFeatureOption.CENON_HDMI_FEATURE){
               if(getString(R.string.hdmi_name) .equals(getString(itemString[i]))){
                   continue;
               }
            }
            if(!FactoryModeFeatureOption.CENON_SCAN_FEATURE){
               if(getString(R.string.scan_test_apk) .equals(getString(itemString[i]))){
                   continue;
               }
            }
            if(!FactoryModeFeatureOption.CENON_SIM_FEATURE){
               if(getString(R.string.sim_name) .equals(getString(itemString[i]))){
                   continue;
               }
            }
            if(!FactoryModeFeatureOption.CENON_SIMSDCARD_FEATURE){
               if(getString(R.string.sim_sdcard_name) .equals(getString(itemString[i]))){
                   continue;
               }
            }
			if(!FactoryModeFeatureOption.CENON_TELEPHONE_FEATURE){
               if(getString(R.string.telephone_name) .equals(getString(itemString[i]))){
                   continue;
               }
            }
            if(!FactoryModeFeatureOption.CENON_MT6735_COLOR_LIGHT_SUPPORT){
                if(getString(R.string.color_light)  .equals(getString(itemString[i]))){
                    continue;
                }
            }
            if(!FactoryModeFeatureOption.CENON_MT6735_FINGER_RECOGNITION_SUPPORT){
                if(getString(R.string.fingerprint_recognition)  .equals(getString(itemString[i]))){
                    continue;
                }
            }
            if(!FactoryModeFeatureOption.CENON_MT6735_FM_USBOTG){
                if(getString(R.string.otg_flashcard) .equals(getString(itemString[i]))){
                    continue;
                }
            }

            if (mSp.getString(getString(itemString[i]), AppDefine.FT_DEFAULT).equals(AppDefine.FT_SUCCESS)) {
                mOkList.add(getString(itemString[i]));
            } else if (mSp.getString(getString(itemString[i]), AppDefine.FT_DEFAULT).equals(AppDefine.FT_FAILED)) {
                mFailedList.add(getString(itemString[i]));
            } else {
                mDefaultList.add(getString(itemString[i]));
            }
        }
        ShowInfo();
    }

    protected void ShowInfo() {
        String okItem = "";
        for (int i = 0; i < mOkList.size(); i++) {
            okItem += mOkList.get(i) + " | ";
        }
        mSuccess.setText(okItem);

        String failedItem = "";
        for (int j = 0; j < mFailedList.size(); j++) {
            failedItem += mFailedList.get(j) + " | ";
        }
        mFailed.setText(failedItem);

        String defaultItem = "";
        for (int k = 0; k < mDefaultList.size(); k++) {
            defaultItem += mDefaultList.get(k) + " | ";
        }
        if(mDefaultList.size()>0){
            mDefaultTitle.setVisibility(View.VISIBLE);
            mDefault.setVisibility(View.VISIBLE);
            mDefault.setText(defaultItem);
        }else {
            mDefaultTitle.setVisibility(View.INVISIBLE);
            mDefault.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
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
}
