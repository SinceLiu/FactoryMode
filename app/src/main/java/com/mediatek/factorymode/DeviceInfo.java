package com.mediatek.factorymode;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.BidiFormatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceInfo extends TestActivity implements OnClickListener {
    public static final String TAG = "DeviceInfoCust";

    private TextView mStatus;
    private TextView mVersion;
    private TextView mMeid;
    private TextView mImei;
    private TextView mSn;
    private TextView mWifiMac;
    private TextView mRfInfo;
    private LinearLayout mC2kLayout;
    private TextView mC2kInfo;

    private LinearLayout mMEID_number;
    private LinearLayout mIMEI_number;
    private LinearLayout mRF_Info;

    private Phone mPhone = null;
    private Phone mCdmaPhone = null;
    private static final String SUPPORTED = "1";
    // Calibration字段代表了校准的状态，返回为0表示校准，其他值表示没有校准
    private static final String CALIBRATION_SUCCESS = "0";

    private static final String NAME = "name";
    private static final String RESULT = "result";

    public static final String FK_MTK_C2K_SUPPORT = "ro.mtk_c2k_support";
    private static final int MSG_NW_INFO = 1;

    // Get/Set band info
    private LinearLayout mC2kBandLayout;
    private TextView mBandInfo;
    private Button mBtSetBand;
    private static final int MSG_GET_BAND_INFO = 2;
    private static final int MSG_SET_BAND_INFO = 3;

    private TextView mToEM;
    private Button mBtOK;
    private Button mBtFailed;
    private EditText mEtSecureText;
    private Button mBtnSubmit;

    private TelephonyManager telephony;
    private WifiManager mWifi;
    private WifiInfo mWifiInfo;

    private SharedPreferences mSp;
    private String meid;
    private String imei1;
    private String imei2;
    private String snCheck;
    private String MotherBoader;
    private String snStr;
    private String meidStr = null;
    private String imeiStr = null;
    private String wifiMacStr = null;
    private MyHandler mATCmdHander;

    private static class MyHandler extends Handler {
        private WeakReference<DeviceInfo> reference;

        public MyHandler(DeviceInfo activity) {
            reference = new WeakReference<DeviceInfo>(activity);//这里传入activity的上下文
        }

        public void handleMessage(Message msg) {
            DeviceInfo activity = reference.get();
            AsyncResult ar;
            switch (msg.what) {
                case MSG_NW_INFO:
                    ar = (AsyncResult) msg.obj;
                    Log.d(TAG, "ar.exception: " + ar.exception);
                    if (ar.exception == null) {
                        String[] getResult = (String[]) (ar.result);
                        StringBuilder sb = new StringBuilder();

                        for (int index = 0; index < getResult.length; index++) {
                            Log.d(TAG, "getResult[" + index + "]=" + getResult[index]);
                            sb.append(getResult[index]);
                        }

                        String calibrationStr = sb.toString();
                        Log.d(TAG, "calibrationStr: " + calibrationStr);
                        int startindex = calibrationStr.indexOf(",", calibrationStr.indexOf(",") + 1) + 1;
                        String c2Kcalibration = calibrationStr.substring(startindex,
                                calibrationStr.indexOf(",", startindex));

                        if (CALIBRATION_SUCCESS.equals(c2Kcalibration)) {
                            activity.mC2kInfo.setText(R.string.Success);
                        } else {
                            activity.mC2kInfo.setText(R.string.Failed);
                        }
                    }
                    break;
                case MSG_GET_BAND_INFO:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        String[] bandInfo = (String[]) (ar.result);
                        StringBuilder sb = new StringBuilder();

                        for (String ss : bandInfo) {
                            sb.append(ss);
                        }
                        activity.mBandInfo.setText(sb.toString());
                    }
                    break;
                case MSG_SET_BAND_INFO:
                    String[] atGetBand = {"AT+EPBSE?", "+EPBSE:"};
                    activity.sendATCommand(atGetBand, activity.MSG_GET_BAND_INFO);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_info);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        mWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mATCmdHander = new MyHandler(this);

        imei1 = telephony.getDeviceId(0);
        imei2 = telephony.getDeviceId(1);
        System.out.println("imei=[" + imei1 + ", " + imei2 + "]");
        snStr = Build.SERIAL; //FactoryModeFeatureOption.get("ro.boot.serialno");
        System.out.println("snStr=[" + snStr + "]");
        snCheck = FactoryModeFeatureOption.get("gsm.serial");
        System.out.println("snCheck=[" + snCheck.trim() + "]");
        String s1 = null, s2 = null;
        try {
            s1 = snCheck.substring(60, 61);
            s2 = snCheck.substring(61, 62);
            int i1 = Integer.parseInt(s1);
            int i2 = Integer.parseInt(s2);
            if (i1 * 10 + i2 == 10) {
                snCheck = "success 10P";// "success 10P";
            } else {
                snCheck = "fail";// "fail";
            }
        } catch (Exception e) {
            snCheck = "fail";// "fail";
        }

        MotherBoader = Build.BOARD;
        if (FactoryModeFeatureOption.getBoolean("ro.project.a877", false)) {
            MotherBoader = "A877";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FactoryModeFeatureOption.CENON_WIFI_ONLY_PROJECT) {
            // Wifi Only
        } else {
            if (TelephonyManager.getDefault().getPhoneCount() > 1) {
                Log.v("@M_" + TAG, "Gemini");
                mPhone = PhoneFactory.getPhone(PhoneConstants.SIM_ID_1); // TODO
            } else {
                Log.v("@M_" + TAG, "Single");
                mPhone = PhoneFactory.getDefaultPhone();
            }

            if (FactoryModeFeatureOption.CENON_C2K_SUPPORT) {
                mCdmaPhone = PhoneFactory.getPhone(PhoneConstants.SIM_ID_1);
//            if (mCdmaPhone instanceof LteDcPhoneProxy) {
//                mCdmaPhone = ((LteDcPhoneProxy) mCdmaPhone).getNLtePhone();
//            }
//            if ((FactoryModeFeatureOption.getBoolean("ro.mtk_svlte_support", false)
//                 || FactoryModeFeatureOption.getBoolean("ro.mtk_srlte_support", false))
//                    && mPhone instanceof LteDcPhoneProxy) {
//                mPhone = ((LteDcPhoneProxy) mPhone).getLtePhone();
//            }
            }
        }

        mStatus = (TextView) findViewById(R.id.status);
        mVersion = (TextView) findViewById(R.id.version_number);
        mMeid = (TextView) findViewById(R.id.meid);
        mMEID_number = (LinearLayout) findViewById(R.id.meid_number);
        mIMEI_number = (LinearLayout) findViewById(R.id.imei_number);
        mRF_Info = (LinearLayout) findViewById(R.id.rf_number);

        if (FactoryModeFeatureOption.CENON_WIFI_ONLY_PROJECT) {
            mMEID_number.setVisibility(View.GONE);
            mIMEI_number.setVisibility(View.GONE);
            mRF_Info.setVisibility(View.GONE);
        }

        if (!FactoryModeFeatureOption.CENON_C2K_SUPPORT) {
            mMeid.setVisibility(View.GONE);
        }

        if (FactoryModeFeatureOption.get("ro.cenon_disable_meid", "0").equals("1")) {
            mMEID_number.setVisibility(View.GONE);
        }

        mImei = (TextView) findViewById(R.id.imei);
        if (FactoryModeFeatureOption.CENON_WIFI_ONLY_PROJECT) {
            mImei.setVisibility(View.GONE);
            mMeid.setVisibility(View.GONE);
        }
        mSn = (TextView) findViewById(R.id.sn);
        mWifiMac = (TextView) findViewById(R.id.wifi_mac);
        mRfInfo = (TextView) findViewById(R.id.rf_info);
        mC2kLayout = (LinearLayout) findViewById(R.id.c2k_layout);
        mC2kInfo = (TextView) findViewById(R.id.c2k_info);
        if (!FactoryModeFeatureOption.CENON_C2K_SUPPORT) {
            mC2kLayout.setVisibility(View.GONE);
        } else if (FactoryModeFeatureOption.CENON_WIFI_ONLY_PROJECT) {
            mC2kLayout.setVisibility(View.GONE);
            mC2kInfo.setVisibility(View.GONE);
        }
        mToEM = (TextView) findViewById(R.id.to_engineer_mode);
        if (FactoryModeFeatureOption.CENON_C2K_SUPPORT) {
            meid = FactoryModeFeatureOption.get("gsm.mtk.meid");
            if (FactoryModeFeatureOption.MTK_GEMINI_SUPPORT) {
                imei1 = FactoryModeFeatureOption.get("gsm.mtk.imei1");
                imei2 = FactoryModeFeatureOption.get("gsm.mtk.imei2");
            } else {
                imei1 = FactoryModeFeatureOption.get("gsm.mtk.imei1");
            }
        }
        meidStr = meid;
        imeiStr = (FactoryModeFeatureOption.MTK_GEMINI_SUPPORT) ? (imei1 + "\n" + imei2) : (imei1);

        mStatus.setText(MotherBoader);
//        mVersion.setText(FactoryModeFeatureOption.get("ro.custom.build.version", "Unknown"));
        mVersion.setText(BidiFormatter.getInstance().unicodeWrap(Build.DISPLAY));
        if (!FactoryModeFeatureOption.CENON_WIFI_ONLY_PROJECT) {
            mMeid.setText(meidStr);
            mImei.setText(imeiStr);
        }
        mSn.setText(snStr);
        updateWifiAddress();
        if (!FactoryModeFeatureOption.CENON_WIFI_ONLY_PROJECT) {
            mRfInfo.setText(snCheck);
        }
        mBtOK = (Button) findViewById(R.id.deviceinfo_bt_ok);
        mBtOK.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.deviceinfo_bt_failed);
        mBtFailed.setOnClickListener(this);

        mToEM.setOnClickListener(this);

        mEtSecureText = (EditText) findViewById(R.id.et_secure_code);
        mBtnSubmit = (Button) findViewById(R.id.btn_secure_code);
        mBtnSubmit.setOnClickListener(this);

        // Get/Set Band info
        mC2kBandLayout = (LinearLayout) findViewById(R.id.c2k_band_layout);
        mBandInfo = (TextView) findViewById(R.id.c2k_band_info);
        mBtSetBand = (Button) findViewById(R.id.device_c2k_band_btn);
        if (FactoryModeFeatureOption.CENON_WIFI_ONLY_PROJECT) {
            mC2kBandLayout.setVisibility(View.GONE);
            mBandInfo.setVisibility(View.GONE);
            mBtSetBand.setVisibility(View.GONE);
        }

        if (FactoryModeFeatureOption.CENON_WIFI_ONLY_PROJECT) {
            // ignore flowing
            return;
        }

        mBtSetBand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] mBandInfoStr = mBandInfo.getText().toString().split("EPBSE: ");
                String finalBand = null;
                if (mBandInfoStr.length >= 2) {
                    String bandValue = mBandInfoStr[1];
                    if (bandValue != null) {
                        String[] value = bandValue.split(",");
                        if (value.length >= 4) {
                            String gsmBand = value[0];
                            String utmsBand = value[1];
                            String lteBand1 = "16";
                            String lteBand2 = "0";
                            finalBand = gsmBand + "," + utmsBand + "," + lteBand1 + "," + lteBand2;

                            String[] atSetBand = {"AT+EPBSE=" + finalBand, ""};
                            sendATCommand(atSetBand, MSG_SET_BAND_INFO);
                        }
                    }
                }
            }
        });
        String[] atGetBand = {"AT+EPBSE?", "+EPBSE:"};
        sendATCommand(atGetBand, MSG_GET_BAND_INFO);

        // String[] atCommand = { "AT+ECENGINFO=1,2", "+ECENGINFO" }; // L及以下版本
        String[] atCommand = {"AT+ECENGINFO=1,2", "+ECENGINFO", "DESTRILD:C2K"}; // M及以上版本
        sendATCommandCdma(atCommand, MSG_NW_INFO);
    }

    private void sendATCommand(String[] atCommand, int msg) {
        if (mPhone != null) {
            // String[] atCommand = {"AT+EPBSE?", "+EPBSE:"};
            mPhone.invokeOemRilRequestStrings(atCommand, mATCmdHander.obtainMessage(msg));
        }
    }

    private void sendATCommandCdma(String[] atCommand, int msg) {
        if (mCdmaPhone != null) {
            // String[] atCommand = {"AT+ECENGINFO=1,2", "+ECENGINFO"};
            mCdmaPhone.invokeOemRilRequestStrings(atCommand, mATCmdHander.obtainMessage(msg));
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.to_engineer_mode) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName("com.mediatek.engineermode", "com.mediatek.engineermode.EngineerMode");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_secure_code) {
            String secureText = mEtSecureText.getText().toString();
            if (secureText != null) {
                if (secureText.startsWith("*#") && secureText.endsWith("#*")) {
                    secureText = secureText.substring(2, secureText.length() - 2);
                    Calendar calendar = Calendar.getInstance();
                    int month = calendar.get(Calendar.MONTH) + 1;
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int temp = month * 1102 + day;
                    temp = temp % 10000;
                    String tempValue = "0000";
                    if (temp / 1000 > 1) {
                        tempValue = "" + temp;
                    } else if (temp / 100 > 1) {
                        tempValue = "0" + temp;
                    } else if (temp / 10 > 1) {
                        tempValue = "00" + temp;
                    } else if (temp / 1 > 1) {
                        tempValue = "000" + temp;
                    }
                    if (tempValue.equals(secureText)) {
                        Intent intent = new Intent("android.settings.SETTINGS");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Error security Code!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Error security Code format!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Enter security Code!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // run in phone process
            Log.e("deviceInfo",v.getId()+"_______________"+"okId:"+mBtOK.getId()+"         "+"notId:"+mBtFailed.getId());
            Utils.SetPreferences(this, mSp, R.string.device_info,
                    (v.getId() == mBtOK.getId()) ? AppDefine.FT_SUCCESS : AppDefine.FT_FAILED);
//            if(v.getId() == mBtOK.getId()) {
//                FactoryModeFeatureOption.set("persist.sys.device_info", AppDefine.FT_SUCCESS);
//            } else {
//                FactoryModeFeatureOption.set("persist.sys.device_info", AppDefine.FT_FAILED);
//            }
            finish();
        }
    }

    private void updateWifiAddress() {
        mWifiInfo = mWifi.getConnectionInfo();
        wifiMacStr = mWifiInfo.getMacAddress();
        if (!mWifi.isWifiEnabled() && wifiMacStr.startsWith("02")) {
            mWifi.setWifiEnabled(true);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mWifiInfo = mWifi.getConnectionInfo();
                    wifiMacStr = mWifiInfo.getMacAddress();
                    mWifiMac.setText(wifiMacStr);
                    mWifi.setWifiEnabled(false);
                }
            }, 5000);
        } else {
            mWifiMac.setText(wifiMacStr);
        }
    }
}
