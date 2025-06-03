package com.example.kidapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
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
        Button blockButton = new Button(context);
        blockButton.setText("Ok");
        blockButton.setBackgroundColor(Color.RED);
        blockButton.setTextColor(Color.WHITE);
        blockButton.setOnClickListener(v -> {
            removeOverlay();
        });
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#888888"));
        int padding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());
        layout.setPadding(padding, padding, padding, padding);
        layout.addView(textView);
        layout.addView(blockButton);
        overlayView = layout;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;
        windowManager.addView(overlayView, params);
    }

    public static void removeOverlay() {
        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }
}

