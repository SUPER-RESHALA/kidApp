package com.example.kidapp.utils;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import java.util.List;

public class UsageStatsHelper {
    private UsageStatsManager usageStatsManager;
    private Context context;

    public UsageStatsHelper(Context context) {
        this.context = context;
        usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    public long getUsageTime(String packageName) {
        long endTime = System.currentTimeMillis();
        long startTime = endTime - 1000 * 60 * 60 * 24;  // За последние 24 часа

        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        for (UsageStats usageStats : usageStatsList) {
            if (usageStats.getPackageName().equals(packageName)) {
                return usageStats.getTotalTimeInForeground();  // Время в мс
            }
        }
        return 0;  // Если приложение не использовалось
    }
}
