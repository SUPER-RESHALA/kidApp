package com.example.kidapp.apps;

import android.content.SharedPreferences;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AppLimit implements Serializable {
    private String packageName;
    private int limitMinutes;
    public static final String limitPrefix="limit_";

    public AppLimit(String packageName, int limitMinutes) {
        this.packageName = packageName;
        this.limitMinutes = limitMinutes;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setLimitMinutes(int limitMinutes) {
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
    public static List<AppLimit> getLimFromPrefs(SharedPreferences prefs) {
        List<AppLimit> appLimits = new ArrayList<>();
        Map<String, ?> allPrefs = prefs.getAll();

        allPrefs.keySet().stream()
                .filter(key -> key.startsWith(limitPrefix))
                .forEach(key -> {
                    String packageName = key.substring(limitPrefix.length());
                    Object value = allPrefs.get(key);
                    if (value instanceof Integer) {
                        int limitMinutes = (Integer) value;
                        if (limitMinutes > 0) {
                            appLimits.add(new AppLimit(packageName, limitMinutes));
                        }
                    }
                });

        return appLimits;
    }
    public long getLimitMilliseconds() {
        return limitMinutes * 60 * 1000L; // Минуты * секунды * миллисекунды
    }

    @Override
    public String toString() {
        return "AppLimit{" +
                "packageName='" + packageName + '\'' +
                ", limitMinutes=" + limitMinutes +
                '}';
    }
}
