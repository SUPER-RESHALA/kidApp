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

import com.example.kidapp.apps.AppInfo;
import com.example.kidapp.apps.InstalledAppsHelper;
import com.example.kidapp.database.FirebaseManager;
import com.example.kidapp.log.FileLogger;
import com.example.kidapp.permission.AccessibilityPermissionHandler;
import com.example.kidapp.permission.UsagePermissionHandler;
import com.example.kidapp.services.AccessibilityKidService;
import com.example.kidapp.services.UsageKidService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

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
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String childUid;
    SharedPreferences prefs;
    FirebaseManager firebaseManager;

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

         prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
//         prefs.edit().clear().apply();//ATTENTION
//        boolean wasPermissionGranted = prefs.getBoolean("accessibility_permission_granted", false);
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference();
        usagePermissionHandler = new UsagePermissionHandler(this);
FileLogger.init(this);
Log.w("GGGGGGGGGGGGG",FileLogger.getLogFilePath());

// Write a message to the database
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("message");
//        myRef.setValue("Hello, People!");
        firebaseManager=FirebaseManager.getInstance();
        viewFlipper= findViewById(R.id.viewFlipper);
        codeField=findViewById(R.id.codeField);
        connectBtn=findViewById(R.id.connectBtn);
        startSetUpBtn= findViewById(R.id.startSetUpBtn);
        contNameBtn=findViewById(R.id.contNameBtn);
        ageContBtn=findViewById(R.id.ageContBtn);
        goSettingsUsageBtn=findViewById(R.id.goSetStatBtn);
        firebaseManager.signIn();
        connectBtn.setOnClickListener(v -> firebaseManager.linkWithParent(connectBtn,codeField,viewFlipper,this,prefs));

        // Проверяем, связан ли ребенок
        String parentUid = prefs.getString("parent_uid", null);
        if (parentUid != null) {
            codeField.setEnabled(false);
            connectBtn.setEnabled(false);
//            statusTextView.setText("Linked with parent");
            firebaseManager.listenForRequests(prefs);
            viewFlipper.showNext();
        }

//        connectBtn.setOnClickListener(v->{
//            viewFlipper.showNext();
//       });
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
    Intent intent= new Intent(this, UsageKidService.class);
    startService(intent);

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



        Button viewApps= findViewById(R.id.viewApps);
        InstalledAppsHelper installedAppsHelper= new InstalledAppsHelper(this);
       List<AppInfo> getInstalledUserApps=  installedAppsHelper.getInstalledUserApps();
        List<AppInfo> getInstalledAppsUsingApplications=  installedAppsHelper.getInstalledAppsUsingApplications();
        List<AppInfo> getInstalledAppsUsingPackages=  installedAppsHelper.getInstalledAppsUsingPackages();
        viewApps.setOnClickListener(v->{
            int counter=0;
            int counter2=0;
            int counter3=0;
//            Intent serviceIntent = new Intent(this, UsageKidService.class);
//            startService(serviceIntent);
            System.out.println("getInstalledUserApps//////////////////////////////////////////////");
            for (AppInfo appInfo:getInstalledUserApps){
counter3++;
                System.out.println(appInfo.toString());
            }
            System.out.println("getInstalledAppsUsingApplications<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><");

            for (AppInfo appInfo:getInstalledAppsUsingApplications){
                System.out.println(appInfo.toString());
                counter2++;
            }
            System.out.println("getInstalledAppsUsingPackages  <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><");
            for (AppInfo appInfo:getInstalledAppsUsingPackages){
                counter++;
                System.err.println(appInfo.toString());
            }
            System.err.println("\\n//////////////////////////////////////////////"+ counter+"   "+counter2+ "     "+counter3);
        });

Button viewAllApps = findViewById(R.id.viewAllAps1);
        viewAllApps.setOnClickListener(v->{

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