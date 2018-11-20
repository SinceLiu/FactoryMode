package com.mediatek.factorymode;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class VersionCodeInfo extends TestActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.version_code);

        TextView tvSwVersion = (TextView) findViewById(R.id.tv_sw_version);
        TextView tvHwVersion = (TextView) findViewById(R.id.tv_hw_version);

        tvSwVersion.setText(FactoryModeFeatureOption.get("ro.custom.build.version", "Unknown"));
        tvHwVersion.setText(FactoryModeFeatureOption.get("ro.custom.hw.version", "Unknown"));
    }
}
