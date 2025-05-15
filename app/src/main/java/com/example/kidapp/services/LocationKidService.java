package com.example.kidapp.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.kidapp.MainActivity;
import com.example.kidapp.log.FileLogger;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocationKidService extends Service {
    public static final int id=1;
    protected int timerInMin=5;
    private FusedLocationProviderClient fusedLocationClient;
    public static final String nodeName="location";
    private LocationCallback locationCallback;
    ScheduledExecutorService scheduledExecutorService;
    FirebaseDatabase firebaseDatabase;
    public final String mapName="cords";
    Location location;
    FirebaseAuth auth;
    SharedPreferences prefs;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FileLogger.log("LocationKidService", "Start");
if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
if (scheduledExecutorService==null||scheduledExecutorService.isShutdown()){
    scheduledExecutorService= Executors.newSingleThreadScheduledExecutor();
}
    firebaseDatabase= FirebaseDatabase.getInstance();
auth=FirebaseAuth.getInstance();
prefs=getSharedPreferences(MainActivity.prefsName,MODE_PRIVATE);
fusedLocationClient= LocationServices.getFusedLocationProviderClient(this);
    startForeground(id,buildForegroundNotification());
startLocationUpdates();
startSendLoc();
}else {
    FileLogger.logError("LocationKidService","Permission denied");
}
        return START_STICKY;
    }

    private Notification buildForegroundNotification() {
        // Создание уведомления
        String channelId = "location_channel_id";
        NotificationChannel channel = new NotificationChannel(channelId, "Location Tracking", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Tracking Location")
                .setContentText("Tracking your location in the background")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build();
    }
    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                 location = locationResult.getLastLocation();
                    Log.d("LocationKidService", "Location: " + location.getLatitude() + ", " + location.getLongitude());
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e("LocationTrackingService", "Permission not granted for location updates.", e);
        }
    }
    private void sendLocationToFb(){
  FileLogger.log("LocationKidService", "sendLocationToFb");
        String parentUid=prefs.getString(MainActivity.pUidName,"") ;
        String childUid=auth.getCurrentUser().getUid();
        if (parentUid.isEmpty()){FileLogger.logError("LocationKidService"," ParentUid is empty");}
        if (childUid.isEmpty()){FileLogger.logError("LocationKidService"," ChildUid is empty");}
        DatabaseReference ref= firebaseDatabase.getReference()
                .child("users")
                .child(parentUid)
                .child("children")
                .child(childUid)
                .child(nodeName);
        FileLogger.log("LOCATION", location.toString());
        if (location!=null){
            ref.updateChildren(getLocationInMap());
        }else {FileLogger.logError("LocationKidService|sendLocationToFb","location is null");}
    }
protected void startSendLoc(){
        FileLogger.log("LocationKidService","startSendLoc");
        scheduledExecutorService.scheduleWithFixedDelay(this::sendLocationToFb,1,timerInMin, TimeUnit.MINUTES);
}
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Останавливаем обновления местоположения при завершении работы сервиса
        fusedLocationClient.removeLocationUpdates(locationCallback);
        stopForeground(true);  // Останавливаем foreground-сервис
        if (scheduledExecutorService!=null&&!scheduledExecutorService.isShutdown()){
            scheduledExecutorService.shutdown();
        }
    }
public Map<String,Object> getLocationInMap(){
        Map<String,Object> map = new HashMap<>();
        map.put(mapName,location);
        return map;
}
}
