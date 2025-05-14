package com.example.kidapp.utils;

import android.app.usage.UsageEvents;
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
    public long getPreciseUsageTime(String packageName) {
        long usageTime = 0;
        long startTime = System.currentTimeMillis() - 1000 * 60 * 60 * 24;
        long endTime = System.currentTimeMillis();

        UsageEvents events = usageStatsManager.queryEvents(startTime, endTime);
        UsageEvents.Event event = new UsageEvents.Event();
        long lastForegroundTime = 0;

        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            if (event.getPackageName().equals(packageName)) {
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    lastForegroundTime = event.getTimeStamp();
                } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND && lastForegroundTime > 0) {
                    usageTime += event.getTimeStamp() - lastForegroundTime;
                    lastForegroundTime = 0;
                }
            }
        }
        return usageTime; // уже в миллисекундах
    }

}
