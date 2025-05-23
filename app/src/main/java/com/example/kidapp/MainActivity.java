package com.example.kidapp;

import android.app.AlertDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.Manifest;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kidapp.apps.AppInfo;
import com.example.kidapp.apps.InstalledAppsHelper;
import com.example.kidapp.database.FirebaseManager;
import com.example.kidapp.log.FileLogger;
import com.example.kidapp.permission.AccessibilityPermissionHandler;
import com.example.kidapp.permission.LocationPermissionHandler;
import com.example.kidapp.permission.OverlayPermissionHandler;
import com.example.kidapp.permission.UsagePermissionHandler;
import com.example.kidapp.services.AccessibilityKidService;
import com.example.kidapp.services.AppInfoKidService;
import com.example.kidapp.services.AppLimitKidService;
import com.example.kidapp.services.UsageKidService;
import com.example.kidapp.utils.OverlayBlocker;
import com.example.kidapp.utils.UsageStatsHelper;
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
    public static final String  prefsName="AppPrefs";
    public static final String pUidName="parent_uid";
    private LocationPermissionHandler locationPermissionHandler;
//TODO
// 1. посылать координаты
// 2. Допилить ограничение приложений(если нет смены экрана)
// 3.Сделать оверлей
// 4.Обязательно защита от удаления(администратор)
// 5. Запрет в настройки с паролем
// 6.Защита от аварийного завершения
// 7.Оптимизация батареи
// 8. Сделать как сервис чтобы все приложение работало
// 9. Удалять данные об использовании при загрузке на сервер(usage data)
// 10. ОТДЕЛЬНЫЙкласс для уведов
// 11. Запускать сервисы можно не только из mainActivit
// 12. Меньше информации о локации и разные источники wifi моб интернет
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
         prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        // prefs.edit().clear().apply();//ATTENTI
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
locationPermissionHandler=new LocationPermissionHandler(this,this);
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
            FileLogger.log("AppInfoKidService", "start service");
            Intent intent= new Intent(this, AppInfoKidService.class);
            startService(intent);
            viewFlipper.showNext();
        });
        contNameBtn.setOnClickListener(v->{
            locationPermissionHandler.requestPermission();
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
                Intent intent= new Intent(this, AppLimitKidService.class);
                startService(intent);
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
        List<AppInfo> getInstalledAppsUsingWithFlag=  installedAppsHelper.getInstalledAppsWithFlag();
        OverlayPermissionHandler overlayPermissionHandler= new OverlayPermissionHandler(this);
        if (overlayPermissionHandler.isPermissionGranted()){
            Log.e("OVERLAY" , "NOt granted");
            overlayPermissionHandler.requestPermission();
        }else {
            Log.e("OVERLAY HERE SUKI", "TUN TUN TUN TUN SAHUR");
        }
        overlayPermissionHandler.requestPermission();
        Handler handler= new Handler(getMainLooper());
        Handler handler2= new Handler(getMainLooper());
        viewApps.setOnClickListener(v->{
           handler.postDelayed(()-> OverlayBlocker.showOverlay(this,"TRALARELO TRALALA"),20000);
            handler2.postDelayed(OverlayBlocker::removeOverlay,30000);


//            int counter=0;
//            int counter2=0;
//            int counter3=0;
//            int counter4=0;
////            Intent serviceIntent = new Intent(this, UsageKidService.class);
////            startService(serviceIntent);
//            System.out.println("getInstalledUserApps//////////////////////////////////////////////");
//            for (AppInfo appInfo:getInstalledUserApps){
//counter3++;
//                System.out.println(appInfo.toString());
//            }
//            System.out.println("getInstalledAppsUsingApplications<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><");
//
//            for (AppInfo appInfo:getInstalledAppsUsingApplications){
//                System.out.println(appInfo.toString());
//                counter2++;
//            }
//            System.out.println("getInstalledAppsUsingPackages  <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><");
//            for (AppInfo appInfo:getInstalledAppsUsingPackages){
//                counter++;
//                System.err.println(appInfo.toString());
//            }
//            System.err.println("\\n//////////////////////////////////////////////"+ counter+"   "+counter2+ "     "+counter3+ "  flag "+counter4);
//            for (AppInfo appInfo:getInstalledAppsUsingWithFlag){
//                counter4++;
//                System.err.println(appInfo.toString());
//            }
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionHandler.handlePermissionResult(requestCode, permissions, grantResults);
    }
}