package com.example.kidapp.apps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import com.example.kidapp.log.FileLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstalledAppsHelper {
    private Context context;

    public InstalledAppsHelper(Context context) {
        this.context = context;
    }

    public List<AppInfo> getInstalledUserApps() {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;
            String appName = pm.getApplicationLabel(resolveInfo.activityInfo.applicationInfo).toString();
            Drawable icon = pm.getApplicationIcon(resolveInfo.activityInfo.applicationInfo);

            appList.add(new AppInfo(appName, packageName, icon));
        }
        return appList;
    }

    public List<AppInfo> getInstalledAppsUsingApplications() {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> applications = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo appInfo : applications) {
            String packageName = appInfo.packageName;
            String appName = pm.getApplicationLabel(appInfo).toString();
            Drawable icon = pm.getApplicationIcon(appInfo);

            appList.add(new AppInfo(appName, packageName, icon));
        }
        return appList;
    }


    public List<AppInfo> getInstalledAppsUsingPackages() {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);

        for (PackageInfo packageInfo : packages) {
            String packageName = packageInfo.packageName;
            String appName = pm.getApplicationLabel(packageInfo.applicationInfo).toString();
            Drawable icon = pm.getApplicationIcon(packageInfo.applicationInfo);

            appList.add(new AppInfo(appName, packageName, icon));
        }
        return appList;
    }




    public List<AppInfo> getInstalledAppsWithFlag() {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            ApplicationInfo appInfo = resolveInfo.activityInfo.applicationInfo;

            // Исключаем системные приложения
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                String packageName = resolveInfo.activityInfo.packageName;
                String appName = pm.getApplicationLabel(appInfo).toString();
                Drawable icon = pm.getApplicationIcon(appInfo);
                appList.add(new AppInfo(appName, packageName, icon));
            }
        }
        return appList;
    }

    public Map<String, Object> convertAppsListToMap(List<AppInfo> allApps) {
        Map<String, Object> allAppsMap = new HashMap<>();
        for (AppInfo app : allApps) {
            String packageName = app.getPackageName();
            String savePackageName= packageName.replace(".","_");
            Map<String, Object> oneApp = new HashMap<>();
            oneApp.put("appName", app.getAppName());
            oneApp.put("packageName", packageName);
            allAppsMap.put(savePackageName, oneApp);
        }
        if (allAppsMap.isEmpty()){
            FileLogger.logError("convertAppsListToMap", "map is empty");
        }
        return allAppsMap;
    }


}
