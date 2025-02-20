package com.example.kidapp.services;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.IBinder;
import android.provider.Settings;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.util.Log;

import com.example.kidapp.log.FileLogger;
import com.example.kidapp.permission.UsagePermissionHandler;

import java.util.List;

public class UsageKidService extends Service {

    private UsageStatsManager usageStatsManager;
    private static final String TAG = "UsageKidService";
private UsagePermissionHandler usagePermissionHandler;
    @Override
    public void onCreate() {
        super.onCreate();
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (usagePermissionHandler.isPermissionGranted()) {
            FileLogger.log(TAG, "Получаем статистику об использовании");
            // Получаем статистику об использовании приложений
            getAppUsageStats();
        } else {
            FileLogger.logError(TAG,"Разрешение статистики отсутствует требуется запрос");
            // Запрашиваем разрешение на использование
            Intent settingsIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(settingsIntent);
        }
        return START_STICKY;
    }

    private String getAppName(String packageName) {
        try {
            return getPackageManager().getApplicationLabel(
                    getPackageManager().getApplicationInfo(packageName, 0)).toString();
        } catch (Exception e) {
            FileLogger.logError(TAG, "Имя приложения не найдено, пакет: "+ packageName);
            return packageName;  // Если имя не найдено, выводим название пакета
        }
    }


    private void getAppUsageStats() {
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - 1000 * 60 * 60 * 24;  // 24 часа назад

        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, currentTime);

        if (usageStatsList != null && !usageStatsList.isEmpty()) {
            for (UsageStats usageStats : usageStatsList) {
                long totalTimeForeground = usageStats.getTotalTimeInForeground();
                if (totalTimeForeground > 0) {  // **Фильтрация приложений, которые были использованы**
                    String appName = getAppName(usageStats.getPackageName());  // **Преобразование packageName в название приложения**
                  FileLogger.log(TAG,"Приложение: " + appName +
                          " | Время на переднем плане: " + (totalTimeForeground / 1000) + " секунд");
                }
            }
        } else {
            FileLogger.log(TAG,"Нет данных об использовании приложений.");

        }
    }



    private long getAppUsageTime(String targetPackageName) {
        long endTime = System.currentTimeMillis();
        long startTime = endTime - 1000 * 60 * 60 * 24;  // 24 часа назад

        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        for (UsageStats usageStats : usageStatsList) {
            if (usageStats.getPackageName().equals(targetPackageName)) {
                return usageStats.getTotalTimeInForeground();  // Время в мс
            }
        }
        return 0;  // Если приложение не использовалось
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        FileLogger.log(TAG, "Вызвался onDestroy");
        super.onDestroy();
    }
}