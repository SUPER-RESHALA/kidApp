package com.example.kidapp.data;

import static android.content.Context.BATTERY_SERVICE;

import android.content.Context;
import android.os.BatteryManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.ScheduledExecutorService;

public class Battery {
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    Context context;
    ScheduledExecutorService scheduledExecutorService;

    public Battery(Context context) {
        this.context = context;
    }

    public  int getBatteryLvl(){
        BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }
}
