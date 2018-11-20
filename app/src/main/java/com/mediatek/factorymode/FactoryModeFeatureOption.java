package com.mediatek.factorymode;

import android.os.SystemProperties;
import android.util.Log;

public class FactoryModeFeatureOption {

    public static final boolean CENON_NFC = getBoolean("ro.cenon_nfc", false);
    public static final boolean CENON_MAGNETIC_FEATURE = getBoolean("ro.cenon_magnetic_feature", true); // true
    public static final boolean CENON_HALL_FEATURE = getBoolean("ro.cenon_hall_feature", false);
    public static final boolean CENON_FACTORY_BACKTOUCH = getBoolean("ro.cenon_factory_backtouch", false);
    public static final boolean CENON_FACTORY_KPDLED = getBoolean("ro.cenon_factory_kpdled", false);
    public static final boolean MTK_GEMINI_SUPPORT = get("ro.mtk_gemini_support").equals("1");
    public static final boolean CENON_MT6735_DEFAULT_OPEN_FRONT_CAMERA = getBoolean("ro.mt6735_only_front_camera",
            false);
    public static final boolean CENON_MT6735_COLOR_LIGHT_SUPPORT = getBoolean("ro.mtk_color_light_support", false);
    public static final boolean CENON_MT6735_FINGER_RECOGNITION_SUPPORT = getBoolean("ro.mtk_finger_support", false);
    public static final boolean CENON_MT6735_EX_BTPHONE = getBoolean("ro.cenon_mt6735_ex_btphone", false);
    public static final boolean CENON_MT6735_BARROMETTER = getBoolean("ro.cenon_mt6735_barrometter", false);
    public static final boolean CENON_MT6735_FM_TRANSMITTER = getBoolean("ro.cenon_mt6735_fm_transmitter", false);
    public static final boolean CENON_MT6735_FM_USBOTG = getBoolean("ro.cenon_mt6735_fm_usbotg", false);
    public static final boolean CENON_VIBRATE_FEATURE = getBoolean("ro.cenon_vibrate_feature", true);
    public static final boolean CENON_GRAVITY_FEATURE = getBoolean("ro.cenon_gravity_feature", true);
    public static final boolean CENON_LIGHT_FEATURE = getBoolean("ro.cenon_light_feature", true);
    public static final boolean CENON_DISTANCE_FEATURE = getBoolean("ro.cenon_distance_feature", true);
    public static final boolean CENON_GRYOS_FEATURE = getBoolean("ro.cenon_gryos_feature", true);
    public static final boolean CENON_GPS_FEATURE = getBoolean("ro.cenon_gps_feature", true);
    public static final boolean CENON_GPS1_FEATURE = getBoolean("ro.cenon_gps1_feature", true);
    public static final boolean CENON_FLASH_LAMP_FEATURE = getBoolean("ro.cenon_flashlamp_feature", true);
    public static final boolean CENON_DEVICE_INFO = getBoolean("ro.cenon_deviceinfo", true);
    public static final boolean CENON_GPIO_FEATURE = getBoolean("ro.cenon_gpio_feature", false);
    public static final boolean CENON_INDICATOR_LIGHT_FEATURE = getBoolean("ro.cenon_indicatorlight_feature", false);
    public static final boolean CENON_OTG_FEATURE = getBoolean("ro.cenon_otg_feature", false);
    public static final boolean CENON_USB_OTG_FEATURE = getBoolean("ro.cenon_usb_otg_feature", false);
    public static final boolean CENON_HDMI_FEATURE = getBoolean("ro.cenon_hdmi_feature", false);
    public static final boolean CENON_SCAN_FEATURE = getBoolean("ro.cenon_scan_feature", false);
    public static final boolean CENON_SIM_FEATURE = getBoolean("ro.cenon_sim_feature", false);
    public static final boolean CENON_SIMSDCARD_FEATURE = !getBoolean("ro.cenon_wifi_only", false);
    public static final boolean CENON_HEADSET_FEATURE = getBoolean("ro.cenon_headset_feature", true);
    public static final boolean CENON_EARPHONE_FEATURE = getBoolean("ro.cenon_earphone_feature", true);
    public static final boolean CENON_FRONT_CAMERA_FEATURE = getBoolean("ro.cenon_front_cam_feature", true);
    public static final boolean CENON_FM_FEATURE = getBoolean("ro.cenon_fm_feature", true);
    public static final boolean CENON_TORCH_FEATURE = getBoolean("ro.mtk_torch_support", false);
    public static final boolean CENON_TELEPHONE_FEATURE = !getBoolean("ro.cenon_wifi_only", false);
    public static final boolean CENON_DOUBLE_MIC_FEATURE = get("ro.mtk_dual_mic_support").equals("1");
    public static final boolean CENON_PROJECT_A868 = getBoolean("ro.cenon_project_a868", false);
    public static final boolean CENON_PROJECT_A865 = get("ro.cenon.project.a865").equals("1");
    public static final boolean CENON_PROJECT_A865_NORF_FACTORY = get("ro.cenon.project.a865_norf_f").equals("1");
    public static final boolean CENON_PROJECT_A878_CORETEST = get("ro.cenon_a878_coretest").equals("1");
    public static final boolean CENON_WIFI_ONLY_PROJECT = getBoolean("ro.cenon_wifi_only", false);
    public static final boolean CENON_C2K_SUPPORT = getBoolean("ro.mtk_c2k_support", false);
    public static final boolean CENON_FACTORY_COMMON = getBoolean("ro.cenon.factory.common", false);

    public static void set(String key, String val) {
        SystemProperties.set(key, val);
    }

    public static String get(String key) {
        return SystemProperties.get(key);
    }

    public static String get(String key, String def) {
        return SystemProperties.get(key, def);
    }

    public static int getInt(String key, int def) {
        return SystemProperties.getInt(key, def);
    }

    public static long getLong(String key, long def) {
        return SystemProperties.getLong(key, def);
    }

    public static boolean getBoolean(String key, boolean def) {
        return SystemProperties.getBoolean(key, def);
    }
}
