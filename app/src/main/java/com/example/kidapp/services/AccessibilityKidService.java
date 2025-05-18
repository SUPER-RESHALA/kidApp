package com.example.kidapp.services;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.kidapp.MainActivity;
import com.example.kidapp.R;
import com.example.kidapp.apps.AppLimit;
import com.example.kidapp.log.FileLogger;
import com.example.kidapp.utils.UsageStatsHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AccessibilityKidService extends android.accessibilityservice.AccessibilityService implements OnLimitsUpdatedListener {

    private static final String TAG = "AccessibilityKidService";
private static final String timeOut="Лимит времени истек";
ScheduledExecutorService scheduledExecutorService;
    private ArrayList<AppLimit> appLimits = new ArrayList<>();
    String lastApp=" ";
    UsageStatsHelper usageStatsHelper;
    int timerLimit=1;
    AtomicLong counter= new AtomicLong(0);
    private static final String dialogMsg="Сегодняшнее время на пользование данным приложением вышло, вы можете воспользоваться им завтра или попросить родителя сменить лимит";
    private static final List<String> blockedApps = Arrays.asList(
            "com.instagram.android",  // Пример: Instagram
            "com.whatsapp",           // WhatsApp
            "com.facebook.katana",// Facebook
            "com.google.android.youtube"
    );
//private void startCheckLimits(){
//    scheduledExecutorService.scheduleWithFixedDelay()
//}
private void scheduleChecking(String packageName){
    scheduledExecutorService.scheduleWithFixedDelay(()->checkApps(packageName),1,timerLimit, TimeUnit.MINUTES);
}
private void stopScheduleClearCounter(){
if(scheduledExecutorService!=null&&!scheduledExecutorService.isShutdown()){
    scheduledExecutorService.shutdown();//MAYBE shutdownNOW check
}
scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
counter.set(0);
}
    private OnLimitsUpdatedListener limitsUpdatedListener;

    // Сеттер для установки слушателя
    public void setLimitsListener(OnLimitsUpdatedListener listener) {
        this.limitsUpdatedListener = listener;
    }
private List<AppLimit> getLimits(){
    SharedPreferences prefs= getSharedPreferences(MainActivity.prefsName,MODE_PRIVATE);
    return AppLimit.getLimFromPrefs(prefs);
}
private void blockApp(){
        FileLogger.log("blockApp", "called");
    performGlobalAction(GLOBAL_ACTION_HOME);
}
    private long getUseTimeWithCounter(String packageName){
        if (usageStatsHelper==null){
            FileLogger.logError("getUseTimeWithCounter", "usageStatsHelperIsNull");
            return 0;}
if (counter.get()==0){ FileLogger.logError("getUseTimeWithCounter", "counter 0");  return 0;}
return counter.get()*60*1000+usageStatsHelper.getPreciseUsageTime(packageName);
    }
private void checkApps(String packageName){
    FileLogger.log("checkApp", "call");
    if (appLimits.isEmpty()){ FileLogger.log("EMPTY", "EMPTY APPLIMITS");}
    if (appLimits.isEmpty()|| !hasPackage(packageName)){ FileLogger.log("checkApp", "return");  return; }
    if (usageStatsHelper == null) return;
    Iterator<AppLimit> iterator= appLimits.iterator();
    while (iterator.hasNext()){
        AppLimit app =iterator.next();
        String pName=app.getPackageName();
        if (!Objects.equals(pName, packageName)){continue;}
        FileLogger.log("CheckAPPS NEW ",
                "usageStatsHelper.getPreciseUsageTime(packageName) " +  usageStatsHelper.getPreciseUsageTime(packageName)
                        +"      "+"app.getLimitMilliseconds() "+app.getLimitMilliseconds()+ "    "+ app.getLimitMinutes()
                        + "pName "+pName+"    "+ "packageName "+ packageName+ "TO STRING "+  app.toString());
        if(getUseTimeWithCounter(packageName)>=app.getLimitMilliseconds()){
            FileLogger.log("COUNTER","TIME: "+ getUseTimeWithCounter(packageName)+"    COUNT "+ counter.get());
            showNotification(packageName);
            blockApp();
            break;
        }}
    counter.incrementAndGet();
}
    private boolean checkApp(String packageName){
        boolean success=false;
        FileLogger.log("checkApp", "call");
        if (appLimits.isEmpty()){ FileLogger.log("EMPTY", "EMPTY APPLIMITS");}
        if (appLimits.isEmpty()|| !hasPackage(packageName)){ FileLogger.log("checkApp", "return");  return false; }
        if (usageStatsHelper == null) return false;
        Iterator<AppLimit> iterator= appLimits.iterator();
        while (iterator.hasNext()){
            AppLimit app =iterator.next();
            String pName=app.getPackageName();
            if (!Objects.equals(pName, packageName)){continue;}
            FileLogger.log("TIME ",
                    "usageStatsHelper.getPreciseUsageTime(packageName) " +  usageStatsHelper.getPreciseUsageTime(packageName)
                            +"      "+"app.getLimitMilliseconds() "+app.getLimitMilliseconds()+ "    "+ app.getLimitMinutes()
            + "pName "+pName+"    "+ "packageName "+ packageName+ "TO STRING "+  app.toString());
            if( usageStatsHelper.getPreciseUsageTime(packageName)>=app.getLimitMilliseconds()){
                showNotification(packageName);
                blockApp();
                success=true;
                break;
            }
        }
        return success;
    }

private void showNotification(String packageName){
    NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    String channelId="limit_channel";
    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
        NotificationChannel notificationChannel= new NotificationChannel(
                channelId,
                "App Limits",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        notificationManager.createNotificationChannel(notificationChannel);
    }
    Notification notification = new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(timeOut)
            .setContentText("Время для " + packageName + " закончилось!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build();
    // Отправка уведомления
    notificationManager.notify(packageName.hashCode(), notification);
}
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
            boolean success=checkApp(packageName);
            if (!packageName.equals(lastApp)){
                FileLogger.log(" if (!packageName.equals(lastApp)){", " if (!packageName.equals(lastApp)){");
                stopScheduleClearCounter();
                lastApp=packageName;
            }
           if (!success) {
               scheduleChecking(packageName);}

//           String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
//
//            // Проверка, заблокировано ли приложение
//            if (blockedApps.contains(packageName)) {
//                showToast("Это приложение заблокировано!");
//
//                // Пытаемся свернуть приложение, чтобы заблокировать доступ
//                performGlobalAction(GLOBAL_ACTION_HOME);
//            }
        }
    }

    // Метод для показа Toast-сообщения
    private void showToast(String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }



//    @Override
//    public void onAccessibilityEvent(AccessibilityEvent event) {
//        // Этот метод вызывается при каждом событии доступности
//
//        int eventType = event.getEventType();
//        String eventText = "";
//
//        switch (eventType) {
//            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
//                eventText = "Окно изменено: " + event.getPackageName();
//                String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
//                if (blockedApps.contains(packageName)) {
//                    showToast("Это приложение заблокировано!");
//
//                    // Пытаемся свернуть приложение, чтобы заблокировать доступ
//                    performGlobalAction(GLOBAL_ACTION_HOME);
//
//                }
//
//
//                break;
//            case AccessibilityEvent.TYPE_VIEW_CLICKED:
//                eventText = "Элемент нажат: " + event.getText();
//                break;
//            default:
//                eventText = "Другое событие: " + event.getEventType();
//        }
//
//        // Логирование события для отладки
//        FileLogger.log(TAG,eventText);
//    }



    @Override
    public void onInterrupt() {
        // Этот метод вызывается, если сервис нужно прервать (например, при отключении специальных возможностей)
        FileLogger.logError(TAG,"Сервис был прерван");
        if (  scheduledExecutorService!=null&& !scheduledExecutorService.isShutdown()){
            scheduledExecutorService.shutdown();
        }
        unregisterReceiver(limitReceiver);
    }
    //TODO Не забудь в манифест зайти для limitService
    private BroadcastReceiver limitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FileLogger.log("Broadcast Accessibility", "broadcast called");
            if ("com.yourapp.UPDATE_LIMITS".equals(intent.getAction())) {
                ArrayList<AppLimit> limits = (ArrayList<AppLimit>) intent.getSerializableExtra("limits");
                if (limits != null) {
                    // Обновляем список
                    FileLogger.log("Receiver", "Получены лимиты: " + limits.size());
                    // Обнови здесь свои переменные, если нужно
                    appLimits=limits;
//                    if (limitsUpdatedListener != null) {
//                        limitsUpdatedListener.onLimitsUpdated(limits);
//                    }
                }
            }
        }
    };
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // Этот метод вызывается при успешном подключении сервиса
        FileLogger.log(TAG,"Сервис доступности подключен");
        scheduledExecutorService= Executors.newSingleThreadScheduledExecutor();
        IntentFilter filter = new IntentFilter("com.yourapp.UPDATE_LIMITS");
        ContextCompat.registerReceiver(
                this,
                limitReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
usageStatsHelper=new UsageStatsHelper(getApplicationContext());

//        FileLogger.log(TAG, "Сервис доступности подключен");
//        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
//        // Сохраняем состояние разрешения
//        SharedPreferences prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putBoolean("accessibility_permission_granted", true);
//        editor.apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (  scheduledExecutorService!=null&& !scheduledExecutorService.isShutdown()){
            scheduledExecutorService.shutdown();
        }
        unregisterReceiver(limitReceiver);
    }

    @Override
    public void onLimitsUpdated(ArrayList<AppLimit> updatedLimits) {

    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d("AccessibilityKidService", "Сервис запущен с START_STICKY");
//        return START_STICKY;  // Сервис будет перезапускаться системой
//    }

    public boolean hasPackage(String targetPackageName) {
        FileLogger.log("hasPackage", "called");
        if (appLimits == null || appLimits.isEmpty()) return false;
        Iterator<AppLimit> iterator = appLimits.iterator();
        while (iterator.hasNext()) {
            AppLimit app = iterator.next();
            if (app.getPackageName().equals(targetPackageName)) {
                return true;
            }
        }
        return false;
    }

}
