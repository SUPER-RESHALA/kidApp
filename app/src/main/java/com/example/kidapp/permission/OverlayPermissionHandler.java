package com.example.kidapp.permission;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

public class OverlayPermissionHandler {
    private final Activity activity;

    public OverlayPermissionHandler(Activity activity) {
        this.activity = activity;
    }

    // Проверяем, есть ли разрешение на наложение окон
    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(activity);
        }
        return true; // На старых версиях Android разрешение не требуется
    }

    // Запрос разрешения у пользователя
    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(intent);
               // Toast.makeText(activity, "Разрешите показ поверх других приложений", Toast.LENGTH_LONG).show();
            }
        }
    }

}