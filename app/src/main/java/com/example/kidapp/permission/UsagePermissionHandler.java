package com.example.kidapp.permission;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.app.AlertDialog;

import androidx.annotation.NonNull;

public class UsagePermissionHandler implements PermissionHandler {
    private final Activity activity;

    public UsagePermissionHandler(Activity activity) {
        this.activity = activity;
    }

    @Override
    public boolean isPermissionGranted() {
        AppOpsManager appOps = (AppOpsManager) activity.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), activity.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @Override
    public void requestPermission() {
        showPermissionDialog();
    }

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Необходим доступ");
        builder.setMessage("Для работы приложения необходимо разрешение на просмотр истории использования. Включите его в настройках.");
        builder.setPositiveButton("Перейти в настройки", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            activity.startActivity(intent);
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void handlePermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (isPermissionGranted()) {
            onPermissionGranted();
        } else {
            onPermissionDenied();
        }
    }

    @Override
    public void onPermissionGranted() {
        // Действия при успешном получении разрешения
    }

    @Override
    public void onPermissionDenied() {
        // Действия при отказе
    }
}
