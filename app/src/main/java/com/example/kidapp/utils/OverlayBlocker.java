package com.example.kidapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class OverlayBlocker {

    private static View overlayView;
    private static WindowManager windowManager;

    public static void showOverlay(Context context, String message) {
        if (!Settings.canDrawOverlays(context)) {
            Log.e("OverlayBlocker", "Нет разрешения на SYSTEM_ALERT_WINDOW");
            return;
        }

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        if (overlayView != null) return; // УЖЕ показан

        TextView textView = new TextView(context);
        textView.setText(message);
        textView.setTextSize(22);
        textView.setBackgroundColor(Color.parseColor("#AA000000")); // полупрозрачный чёрный
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(40, 40, 40, 40);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.CENTER;

        overlayView = textView;
        windowManager.addView(overlayView, params);
    }

    public static void removeOverlay() {
        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }
}

