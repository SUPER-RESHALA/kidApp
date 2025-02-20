package com.example.kidapp.permission;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import androidx.appcompat.app.AlertDialog;

import com.example.kidapp.log.FileLogger;

import java.util.List;

public class AccessibilityPermissionHandler implements PermissionHandler {
    private final Context context;
    private static final String ACCESSIBILITY_SERVICE_ID = "com.example.kidapp/.services.AccessibilityKidService";

      // private static final String ACCESSIBILITY_SERVICE_ID = "com.example.kidapp.services.AccessibilityKidService";

    public AccessibilityPermissionHandler(Context context) {
        this.context = context;
    }

//    @Override
//    public boolean isPermissionGranted() {
//        String enabledServices = Settings.Secure.getString(
//                context.getContentResolver(),
//                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
//        );
//
//        if (enabledServices != null && enabledServices.contains(context.getPackageName())) {
//            return true;
//        }
//        return false;
//    }



    @Override
    public boolean isPermissionGranted() {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo service : enabledServices) {
            FileLogger.log("PermissionCheck","Enabled Service: " + service.getId());
            FileLogger.log("PermissionCheck","Service ID: " + service.getId());
            FileLogger.log("PermissionCheck", "Expected ID: " + ACCESSIBILITY_SERVICE_ID);
//            if (service.getId().contains("AccessibilityKidService")) {
//                return true;
//            }
            if (service.getId().equals(ACCESSIBILITY_SERVICE_ID)) {
                return true;
            }
        }
        return false;
    }



//    @Override
//    public boolean isPermissionGranted() {
//        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
//        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
//
//        for (AccessibilityServiceInfo service : enabledServices) {
//            if (service.getId().equals(ACCESSIBILITY_SERVICE_ID)) {
//                return true;  // Разрешение предоставлено
//            }
//        }
//        return false;  // Разрешение не предоставлено
//    }

    @Override
    public void requestPermission() {
        if (!isPermissionGranted()) {
            showPermissionDialog();
        }
    }

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Необходим доступ к специальным возможностям");
        builder.setMessage("Для работы приложения необходимо разрешение на специальные возможности. Включите его в настройках.");
        builder.setPositiveButton("Перейти в настройки", (dialog, which) -> openAccessibilitySettings());
        builder.setNegativeButton("Отмена", (dialog, which) -> onPermissionDenied());
        builder.setCancelable(false);
        builder.show();
    }

    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        context.startActivity(intent);
    }

    @Override
    public void onPermissionDenied() {
        // Действия при отказе пользователя
    }

    @Override
    public void handlePermissionResult() {
        if (isPermissionGranted()) {
            onPermissionGranted();
        } else {
            onPermissionDenied();
        }
    }

    @Override
    public void onPermissionGranted() {
        // Логика при успешном получении разрешения
    }
}