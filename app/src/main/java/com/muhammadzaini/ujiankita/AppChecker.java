package com.muhammadzaini.ujiankita;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

public class AppChecker {

    public static void openApplications(Activity context, String packageNamesString) {
        // Split the comma-separated string into an array of package names
        if(packageNamesString == null){
            return;
        }
        String[] packageNames = packageNamesString.split(",");

        for (String packageName : packageNames) {
            try {
                String trimmedPackageName = packageName.trim();

                PackageManager packageManager = context.getPackageManager();
                Intent intent = packageManager.getLaunchIntentForPackage(trimmedPackageName);

                if (intent != null) {
                    // Start the application
                    context.startActivity(intent);
                    context.stopLockTask();
                    Toast.makeText(context, "Tolong hapus aplikasi yang terbuka ini sebelum melanjutkan ujian", Toast.LENGTH_SHORT).show();

                    break;
                } else {
                }
            } catch (Exception e) {
            }
        }
    }
}

