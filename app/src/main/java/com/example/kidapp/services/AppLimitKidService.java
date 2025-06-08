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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.kidapp.MainActivity;
import com.example.kidapp.R;
import com.example.kidapp.apps.AppLimit;
import com.example.kidapp.log.FileLogger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppLimitKidService extends Service {
    public static final String nodeName="appLimits";
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    SharedPreferences prefs;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        FileLogger.log("AppLimiKidService", "Started onCreate");
        firebaseDatabase=FirebaseDatabase.getInstance();
     auth= FirebaseAuth.getInstance();
     prefs= getSharedPreferences(MainActivity.prefsName,MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            startForeground(2,createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            //startForeground(SERVICE_ID, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK) // Выберите нужный тип
        } else {
            startForeground(2,createNotification());
        }
            takeLimits();
        return START_STICKY;
    }
    private  void takeLimits(){
        FileLogger.log("AppLimitKidService", "take Limit call");
        String parentUid= prefs.getString(MainActivity.pUidName,"");
        String childUid= auth.getCurrentUser().getUid();
        DatabaseReference db= firebaseDatabase.getReference("users")
                .child(parentUid)
                .child("children")
                .child(childUid)
                .child(nodeName);
        List<AppLimit> apps= new ArrayList<>();
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                apps.clear();
                for (DataSnapshot appSnapshot:
                        snapshot.getChildren()) {
                    String packageName=appSnapshot.getKey().replace("_",".");
                    Integer appLimit= appSnapshot.child("limitMinutes").getValue(Integer.class);
                    if (appLimit != null) {
                        apps.add(new AppLimit(packageName, appLimit));
                    }
                }
                AppLimit.removeUselessFromPrefs(apps,prefs);
                AppLimit.saveListToPrefs(apps,prefs);
                Intent intent = new Intent("com.yourapp.UPDATE_LIMITS");
                intent.putExtra("limits", new ArrayList<>(apps));
                FileLogger.log("AppLimitKidService", "broadcastSend");
                intent.setPackage(getPackageName());
                sendBroadcast(intent);
                FileLogger.log("AppLimitsSize in AppLimiKidService", "size "+ apps.size());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FileLogger.logError("takeLimits", "Error");
            }
        });
    }
    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel("AppLimiKidService", "AppLimiKidService", NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, "AppLimiKidService")
                .setContentTitle("AppLimiKidService работает")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }
    @Override
   public void onDestroy(){
        super.onDestroy();

    }
}