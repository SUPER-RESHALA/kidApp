package com.example.kidapp.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.example.kidapp.log.FileLogger;

import java.util.Arrays;
import java.util.List;

public class AccessibilityKidService extends android.accessibilityservice.AccessibilityService {

    private static final String TAG = "AccessibilityKidService";




    private static final List<String> blockedApps = Arrays.asList(
            "com.instagram.android",  // Пример: Instagram
            "com.whatsapp",           // WhatsApp
            "com.facebook.katana" ,// Facebook
            "com.google.android.youtube"
    );


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";

            // Проверка, заблокировано ли приложение
            if (blockedApps.contains(packageName)) {
                showToast("Это приложение заблокировано!");

                // Пытаемся свернуть приложение, чтобы заблокировать доступ
                performGlobalAction(GLOBAL_ACTION_HOME);
            }
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
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // Этот метод вызывается при успешном подключении сервиса
        FileLogger.log(TAG,"Сервис доступности подключен");
//        // Сохраняем состояние разрешения
//        SharedPreferences prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putBoolean("accessibility_permission_granted", true);
//        editor.apply();
    }



//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d("AccessibilityKidService", "Сервис запущен с START_STICKY");
//        return START_STICKY;  // Сервис будет перезапускаться системой
//    }



}
