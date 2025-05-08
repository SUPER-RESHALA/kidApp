package com.example.kidapp.apps;

import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AppInfo {
    private String appName;
    private String packageName;
    private Drawable appIcon;

    public AppInfo(String appName, String packageName, Drawable appIcon) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
    }

    public String getAppName() { return appName; }
    public String getPackageName() { return packageName; }
    public Drawable getAppIcon() { return appIcon; }
public Map<String, Object> appInfoMap(){
        Map<String, Object>map= new HashMap<>();
        map.put("appName", appName);
        map.put("packageName", packageName);
        return map;

    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", appIcon=" + appIcon +
                '}';
    }
}
