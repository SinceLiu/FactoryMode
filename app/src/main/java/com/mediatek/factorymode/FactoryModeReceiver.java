package com.mediatek.factorymode;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

public class FactoryModeReceiver extends BroadcastReceiver {

    private final String TAG = "FACTORY/SECRET_CODE";
    // process *#66#
    private static final String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";
    private static final String SYSTEM_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    Uri engineerUri = Uri.parse("android_secret_code://66");
    Uri versionCodeUri = Uri.parse("android_secret_code://60");

    public static final String NAME = "name";
    public static final String RESULT = "result";

    public FactoryModeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equals(SECRET_CODE_ACTION)) {
                Uri uri = intent.getData();
                Log.i(TAG, "getIntent success in if");
                if (uri.equals(engineerUri)) {
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.setComponent(
                            new ComponentName("com.mediatek.factorymode", "com.mediatek.factorymode.FactoryMode"));
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Log.i(TAG, "Before start FACTORY activity");
                    context.startActivity(i);
                } else if (uri.equals(versionCodeUri)) {
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.setComponent(
                            new ComponentName("com.mediatek.factorymode", "com.mediatek.factorymode.VersionCodeInfo"));
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                } else {
                    Log.i(TAG, "No matched URI!");
                }
            } else if (intent.getAction().equals(SYSTEM_BOOT_COMPLETED)) {
                // Settings.System.putInt(context.getContentResolver(),
                // Settings.System.CENON_IGNORE_HOME_POWER, 0);
                if (FactoryModeFeatureOption.get("ro.cenon_factorymode_feature", "0").equals("1")) {
                    Intent i = new Intent();
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setClassName("com.mediatek.factorymode", "com.mediatek.factorymode.FactoryMode");
                    context.startActivity(i);
                }
                Log.i(TAG,
                        "after system boot up, must set CENON_IGNORE_HOME_POWER value is 0, because system shutdown expection");
            } else {
                Log.i(TAG, "Not SECRET_CODE_ACTION!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Package exception.");
        }
    }
}
