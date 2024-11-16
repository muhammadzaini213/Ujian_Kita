package com.muhammadzaini.ujiankita.main;

import android.content.Context;
import android.provider.Settings;

public class TimeSettingsUtil {

    /**
     * Method to check if the user has enabled Automatic Date & Time (Network-provided time).
     *
     * @param context The context of the application.
     * @return true if Automatic Date & Time is enabled, false otherwise.
     */
    public static boolean isAutoDateTimeEnabled(Context context) {
        try {
            // Check the system setting for Automatic Date & Time (Network-provided time)
            int autoTime = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME);
            return autoTime == 1; // 1 means enabled, 0 means disabled
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false; // If the setting is not found, assume it's disabled.
        }
    }
}

