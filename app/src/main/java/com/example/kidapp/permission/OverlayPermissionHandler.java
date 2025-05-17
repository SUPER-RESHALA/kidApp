package com.example.kidapp.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class OverlayPermissionHandler {
    private final Context context;

    public OverlayPermissionHandler(Context context) {
        this.context = context;
    }

    // Проверка разрешения
    public boolean isPermissionGranted() {
        return Settings.canDrawOverlays(context);
    }

    // Запрос разрешения у пользователя
    public void requestPermission() {
        if (!isPermissionGranted()) {
            Log.w("OverlayPermission", "Requesting overlay permission...");
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            if (!(context instanceof Activity)) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        }
    }
}
