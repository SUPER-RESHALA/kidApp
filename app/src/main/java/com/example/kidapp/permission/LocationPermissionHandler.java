package com.example.kidapp.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.kidapp.log.FileLogger;
import com.example.kidapp.services.LocationKidService;

public class LocationPermissionHandler implements  PermissionHandler{
    protected Context context;
    protected Activity activity;

    public LocationPermissionHandler(Context context, Activity activity) {
        this.context = context;
        this.activity=activity;
    }

    @Override
    public boolean isPermissionGranted() {
    return  ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public void requestPermission() {
        if (!isPermissionGranted()) {
            // Проверяем, нужно ли показать объяснение
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Показать объяснение (можно заменить на диалог)
                FileLogger.log("LocationPermissionHandler", "Showing rationale for location permission");
                new android.app.AlertDialog.Builder(activity)
                        .setMessage("Геолокация нужна для отслеживания местоположения вашего ребенка")
                        .setPositiveButton("OK", (dialog, which) -> {
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    PermissionCodes.LOCATION_REQUEST_CODE);
                        })
                        .setNegativeButton("Отмена", (dialog, which) -> onPermissionDenied())
                        .show();
            } else {
                // Запрашиваем разрешение напрямую
                FileLogger.log("LocationPermissionHandler", "Requesting location permission");
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PermissionCodes.LOCATION_REQUEST_CODE);
            }
        } else {
            // Разрешение уже есть
            FileLogger.log("LocationPermissionHandler", "Location permission already granted");
            onPermissionGranted();
        }
    }

    @Override
    public void onPermissionDenied() {
        FileLogger.log("LocationPermissionHandler", "Location permission denied");
        // Показываем сообщение пользователю или направляем в настройки
        new android.app.AlertDialog.Builder(activity)
                .setMessage("Без геолокации некоторые функции могут быть недоступны. Хотите включить доступ в настройках?")
                .setPositiveButton("Настройки", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    activity.startActivity(intent);
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void handlePermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==PermissionCodes.LOCATION_REQUEST_CODE){
if (grantResults.length>0&& grantResults[0]==PackageManager.PERMISSION_GRANTED){
    onPermissionGranted();
}else {
    onPermissionDenied();
}
        }
        // Заглушка: для обработки результата нужен доступ к onRequestPermissionsResult
        // Обычно этот метод должен вызываться из активности, передавая requestCode, permissions и grantResults
        FileLogger.log("LocationPermissionHandler", "handlePermissionResult called, but implementation requires requestCode, permissions, and grantResults");

    }
private  void startLocationService(){
        Intent intent= new Intent(activity, LocationKidService.class);
ContextCompat.startForegroundService(context.getApplicationContext(),intent);
}
    @Override
    public void onPermissionGranted() {
        FileLogger.log("LocationPermissionHandler", "Location permission granted");
    //  takeLocation()
        startLocationService();
    }
}