package com.example.kidapp.apps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class InstalledAppsHelper {
    private Context context;

    public InstalledAppsHelper(Context context) {
        this.context = context;
    }

    public List<AppInfo> getInstalledApps() {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            // Фильтруем системные приложения
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                String appName = pm.getApplicationLabel(packageInfo).toString();
                String packageName = packageInfo.packageName;
                Drawable icon = pm.getApplicationIcon(packageInfo);

                appList.add(new AppInfo(appName, packageName, icon));
            }
        }
        return appList;
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
}
