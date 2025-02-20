package com.example.kidapp;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kidapp.log.FileLogger;
import com.example.kidapp.permission.AccessibilityPermissionHandler;
import com.example.kidapp.permission.UsagePermissionHandler;
import com.example.kidapp.services.AccessibilityKidService;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText codeField;
    private Button connectBtn;
    private ViewFlipper viewFlipper;
    private Button startSetUpBtn;
    private Button contNameBtn;
    private Button ageContBtn;
    private Button goSettingsUsageBtn;
    private boolean isFirstLaunch = true;
    private  UsagePermissionHandler usagePermissionHandler;
    private Button goSettingsAcessibilityBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.viewFlipper), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });




//        SharedPreferences prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
//        boolean wasPermissionGranted = prefs.getBoolean("accessibility_permission_granted", false);






        usagePermissionHandler = new UsagePermissionHandler(this);
FileLogger.init(this);
Log.w("GGGGGGGGGGGGG",FileLogger.getLogFilePath());
        viewFlipper= findViewById(R.id.viewFlipper);
        codeField=findViewById(R.id.codeField);
        connectBtn=findViewById(R.id.connectBtn);
        startSetUpBtn= findViewById(R.id.startSetUpBtn);
        contNameBtn=findViewById(R.id.contNameBtn);
        ageContBtn=findViewById(R.id.ageContBtn);
        goSettingsUsageBtn=findViewById(R.id.goSetStatBtn);
       connectBtn.setOnClickListener(v->{
            viewFlipper.showNext();
       });
        startSetUpBtn.setOnClickListener(v->{
            viewFlipper.showNext();
        });
        contNameBtn.setOnClickListener(v->{
            viewFlipper.showNext();
        });
        ageContBtn.setOnClickListener(v->{
            viewFlipper.showNext();
        });
goSettingsUsageBtn.setOnClickListener(v->{
    isFirstLaunch=false;
if (usagePermissionHandler.isPermissionGranted()){
    viewFlipper.showNext();
}else {
    usagePermissionHandler.requestPermission();
}
});

       goSettingsAcessibilityBtn=findViewById(R.id.enableAccessibilityBtn);
        AccessibilityPermissionHandler accessibilityPermissionHandler = new AccessibilityPermissionHandler(this);
        goSettingsAcessibilityBtn.setOnClickListener(v->{
            if (accessibilityPermissionHandler.isPermissionGranted()){
                System.out.println("GETTTTTTTTTTTTTTTTTTTTTTTTT"+ accessibilityPermissionHandler.isPermissionGranted());
                viewFlipper.showNext();
            }else {
//
//                Intent serviceIntent = new Intent(this, AccessibilityKidService.class);
//                startService(serviceIntent);

                System.out.println("SUCKKKKKING&*&*&&*&**&&*&*&*&*&*&*"+ accessibilityPermissionHandler.isPermissionGranted());
            }
         accessibilityPermissionHandler.requestPermission();
        });


    }//OnCreate
    @Override
    protected void onResume() {
        super.onResume();
//        if (!isFirstLaunch){
//            if (!usagePermissionHandler.isPermissionGranted()){
//                Toast.makeText(this, "РАЗРЕШЕНИЕ СЭР", Toast.LENGTH_LONG).show();
//                Log.e("-----------------","--------------------");
//            }else {
//                viewFlipper.showNext();
//            }
//        }else {
//            Log.w("-----------------","--------------------");
//        }
    }
    ////CONFIG FILE SERVISA LYBOGO
////CONFIG FILE SERVISA LYBOGO
    ////CONFIG FILE SERVISA LYBOGO
    //Фоновые процессы




}