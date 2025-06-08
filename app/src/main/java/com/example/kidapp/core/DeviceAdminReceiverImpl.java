package com.example.kidapp.core;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.widget.Toast;

public class DeviceAdminReceiverImpl extends DeviceAdminReceiver {
        public static final int BIND_DEVICE_ADMIN_CODE=10;
        private void showToast(Context context, String msg) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEnabled( Context context,  Intent intent) {
                super.onEnabled(context, intent);
                showToast(context, "Device admin enabled");
        }

}
