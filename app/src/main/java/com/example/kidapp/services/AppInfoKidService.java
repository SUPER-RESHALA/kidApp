package com.example.kidapp.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.kidapp.MainActivity;
import com.example.kidapp.R;
import com.example.kidapp.apps.AppInfo;
import com.example.kidapp.apps.InstalledAppsHelper;
import com.example.kidapp.log.FileLogger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class AppInfoKidService extends Service {
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    SharedPreferences prefs;
    ScheduledExecutorService scheduledExecutorService;
    int delayInMin=2;
    public static final String nodeName="allApps";

    @Override
    public void onCreate(){
        super.onCreate();
        FileLogger.log("AppInfoKidService", "onCreate");
        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        prefs=getSharedPreferences(MainActivity.prefsName,MODE_PRIVATE);
        scheduledExecutorService= Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FileLogger.log("AppInfoKidService", "onStartCommand");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            startForeground(11,createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            //startForeground(SERVICE_ID, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK) // Выберите нужный тип
        } else {
            startForeground(11,createNotification());
        }
        sendDataFlow();
        return START_STICKY;
    }
private void sendDataToDb(){
        FileLogger.log("AppInfoKidService","sendDataToDb call");
    String parenUid= prefs.getString(MainActivity.pUidName,"");
    String childUid=auth.getCurrentUser().getUid();
    if (parenUid.isEmpty() ){
        FileLogger.logError("AppInfoKidService|OnStartCommand", "pUid is empty");
    }
    if (childUid.isEmpty() ){
        FileLogger.logError("AppInfoKidService|OnStartCommand", "ChildUid is empty");
    }
    DatabaseReference db=
            firebaseDatabase.getReference()
                    .child("users")
                    .child(parenUid)
                    .child("children")
                    .child(childUid)
                    .child(nodeName);
    InstalledAppsHelper installedAppsHelper= new InstalledAppsHelper(getApplicationContext());
   List<AppInfo> allApps= installedAppsHelper.getInstalledUserApps();
   Map<String, Object> allAppsMap=installedAppsHelper.convertAppsListToMap(allApps);
    db.setValue(allAppsMap);
}
private  void sendDataFlow(){
       // scheduledExecutorService.scheduleWithFixedDelay(this::sendDataToDb,0,10, TimeUnit.SECONDS);
        scheduledExecutorService.schedule(this::sendDataToDb,0, TimeUnit.MINUTES);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel("AppInfoKidService", "AppInfoKidService", NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, "AppInfoKidService")
                .setContentTitle("AppInfoKidService работает")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scheduledExecutorService!=null&& !scheduledExecutorService.isShutdown()){
            scheduledExecutorService.shutdown();
        }
    }
}
