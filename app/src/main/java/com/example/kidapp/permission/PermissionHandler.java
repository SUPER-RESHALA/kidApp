package com.example.kidapp.permission;

public interface PermissionHandler {
  boolean isPermissionGranted();
    void requestPermission();
   void onPermissionDenied();
    void handlePermissionResult();
    void onPermissionGranted();
}
