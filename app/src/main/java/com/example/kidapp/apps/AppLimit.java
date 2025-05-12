package com.example.kidapp.apps;

import android.content.SharedPreferences;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AppLimit {
    private String packageName;
    private int limitMinutes;
    public static final String limitPrefix="limit_";

    public AppLimit(String packageName, int limitMinutes) {
        this.packageName = packageName;
        this.limitMinutes = limitMinutes;
    }

    public String getPackageName() { return packageName; }
    public int getLimitMinutes() { return limitMinutes; }
    public static void saveToPrefs(AppLimit appLimit,SharedPreferences prefs){
       prefs.edit().putInt(limitPrefix+appLimit.getPackageName(),appLimit.getLimitMinutes()).apply();
    }
    public static void removeUselessFromPrefs(List<AppLimit> usefulAppLimits,SharedPreferences prefs){
        Set<String> validPackages= usefulAppLimits.stream()
                .map(AppLimit::getPackageName)
                        .collect(Collectors.toSet());
        SharedPreferences.Editor editor = prefs.edit();
        prefs.getAll().keySet().stream()
                .filter(key -> key.startsWith(limitPrefix)) // или contains, если так нужно
                .filter(key -> {
                    String packageName = key.substring(limitPrefix.length()); // извлекаем имя пакета
                    return !validPackages.contains(packageName);
                })
                .forEach(editor::remove);
        editor.apply();
    }
    public static void saveListToPrefs(List<AppLimit> appLimit, SharedPreferences prefs){
        for (AppLimit app:
             appLimit) {
            saveToPrefs(app,prefs);
        }
    }
}
