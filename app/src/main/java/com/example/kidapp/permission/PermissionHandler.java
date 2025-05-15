package com.example.kidapp.permission;

import androidx.annotation.NonNull;

public interface PermissionHandler {
  boolean isPermissionGranted();
    void requestPermission();
   void onPermissionDenied();
    void handlePermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
    void onPermissionGranted();
}
