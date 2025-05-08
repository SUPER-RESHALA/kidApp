package com.example.kidapp.services;
import android.app.AppOpsManager;
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
import com.example.kidapp.utils.DateUtils;
import com.google.android.gms.common.util.DataUtils;
import com.google.firebase.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UsageKidService extends Service {

    private UsageStatsManager usageStatsManager;
    private static final String TAG = "UsageKidService";
    FirebaseDatabase db;
    @Override
    public void onCreate() {
        super.onCreate();
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        db=FirebaseDatabase.getInstance();
        Log.i(TAG, "ONCREATE CALL");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand call");
        if (isUsagePermissionGranted()) {
            FileLogger.log(TAG, "Получаем статистику об использовании");
            // Получаем статистику об использовании приложений
            sendDataToFirebase();
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


    private void getAppUsageStatsPRINT() {
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
    private List<UsageStats> getAppsUsageStats() {
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - 1000 * 60 * 60 * 24;  // 24 часа назад
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, currentTime);
        if (usageStatsList == null) return Collections.emptyList();
        return usageStatsList.stream()
                    .filter(usageStats -> usageStats.getTotalTimeInForeground()>0)
                    .collect(Collectors.toList());
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

    private boolean isUsagePermissionGranted() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public void sendDataToFirebase() {
        Log.i(TAG, "sendDataToFirebase call");
       // getAppUsageStatsPRINT();
        List<UsageStats> usageStats = getAppsUsageStats();
        if (usageStats.isEmpty()) {Log.e("sendDataToFireBase", "usageStats isEmpty"); return;}

        DatabaseReference ref = db.getReference("kid")
                .child("kid1")
                .child("usageStats")
                .child(DateUtils.dateToString());

        Map<String, Object> allStats = new HashMap<>();
        for (UsageStats stats : usageStats) {
            Map<String, Object> appMap = new HashMap<>();
            appMap.put("timeInForeground", stats.getTotalTimeInForeground());
            String appName = getAppName(stats.getPackageName());
            // Можно добавить ещё данные, если нужно
            FileLogger.log(TAG,"Приложение: " +  appName+
                     " | Время на переднем плане: " + (stats.getTotalTimeInForeground() / 1000) + " секунд");
            String safePackageName = stats.getPackageName().replace(".", "_");
            allStats.put(safePackageName, appMap);
        }

        ref.setValue(allStats); // Загружаем всю карту за раз
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