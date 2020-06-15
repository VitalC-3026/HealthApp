package com.example.bigproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toast;

import com.example.bigproject.LocationService.LocationService;
import com.example.bigproject.light.LightSensor;

import com.example.bigproject.orientation.OrientSensor;
import com.example.bigproject.LocationService.LocationUtils;
import com.example.bigproject.light.LightSensor;
import com.example.bigproject.ui.home.HomeFragment;
import com.example.bigproject.ui.setting.SettingFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.annotation.RequiresApi;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements OrientSensor.OrientCallBack, LocationService.LocationCallBack, LightSensor.LightCallback {

    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private TextView lightValues;
    private LightSensor lightSensor;
    private HomeFragment homeFragment = new HomeFragment();
    // 获取权限的请求码，到请求权限都要对应输入这个请求码
    private static final int BAIDU_READ_PHONE_STATE = 100;
    private PowerManager powerManager;
    private KeyguardManager keyguardManager;
    private ScreenBroadcastReceiver screenReceiver;
    private IntentFilter intentFilter;


    private Context mContext;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        lightValues = (TextView) findViewById(R.id.text_light);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_description, R.id.nav_report, R.id.nav_setting, R.id.nav_team)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        lightSensor = new LightSensor(this, this);
        /*if (!lightSensor.registerLightSensor()) {
            Toast.makeText(this, "光线传感器不可用", Toast.LENGTH_SHORT).show();
        }*/
        if (Build.VERSION.SDK_INT >= 23) {
            showContacts();
        }

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        boolean isPowerOn = isPowerOn();
        boolean isUsing = isPhoneLocked();

        // 广播方式
        intentFilter = startScreenBroadcastReciver(this);
        screenReceiver = new ScreenBroadcastReceiver();
        registerReceiver(screenReceiver, intentFilter);
        // 获取屏幕状态 1=>屏幕亮 2=>屏幕熄灭 3=>屏幕正在使用
        int res = screenReceiver.getResult();

        float num[] = new float[4];//用来存储从文件中读取出的数据
        String detail = "";
        FileHelper fHelper2 = new FileHelper(getApplicationContext());
        try    {
        String fname = "myFile.txt";
        detail = fHelper2.read(fname);
        String stringArray[] = detail.split(" ");
        for (int i = 0; i < stringArray.length; i++) {
            num[i] = Float.parseFloat(stringArray[i]);
        }
//            Toast.makeText(getContext(), values[0]+" "+values[1]+" "+values[2]+" "+sum+" ", Toast.LENGTH_SHORT).show();
        } catch(IOException e){
        e.printStackTrace();
    }
    saveToFile(sleepTime/60+num[0], moveTime/60+num[1], darkTime/60+num[2], sumTime);//将更新的参数保存到文件中

    OrientSensor orientSensor = new OrientSensor(this, this);
        orientSensor.registerOrient();
    LocationService locationService = new LocationService(this, this, this);
        locationService.start();
//        LightSensor lightSensor=new LightSensor(this,this);
}

    private float sleepTime;
    private float moveTime;
    private float darkTime=10;
    private float sumTime=300;




    @Override
    public void getLocationTime(float time) {
        this.moveTime=time;
    }

    @Override
    public void getDarktime(float Darktime) {
//        this.darkTime=Darktime;//对应的传感器未传值
    }

    @Override
    public void getOrientTime(float time) {
        this.sleepTime=time;
    }

    public void saveToFile(float sleepTime,float moveTime,float darkTime,float sumTime)
    {
        FileHelper fileHelper=new FileHelper(mContext);
        String fileName="myFile.txt";
        String filedetail=sleepTime+" "+moveTime+" "+darkTime+" "+sumTime;

        try {
            fileHelper.save(fileName,filedetail);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onDestroy() {
        lightSensor.unregisterLightSensor();
        unregisterReceiver(screenReceiver);
        super.onDestroy();
    }

    @Override
    public void getLight(float[] values) {
        Toast.makeText(this, "光线传感器获取参数: " + values[0], Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showContacts() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.CONTROL_LOCATION_UPDATES)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "没有权限,请手动开启定位权限", Toast.LENGTH_SHORT).show();
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.BODY_SENSORS,
                    Manifest.permission.VIBRATE,
                    Manifest.permission.CONTROL_LOCATION_UPDATES,
                    Manifest.permission.ACCESS_NOTIFICATION_POLICY
            }, BAIDU_READ_PHONE_STATE);
        } else {
            showLocation();
        }
    }

    private void showLocation() {
        Log.d(TAG, LocationUtils.getInstance().getLocations(this));
        Toast.makeText(MainActivity.this, LocationUtils.getInstance().getLocations(MainActivity.this), Toast.LENGTH_LONG).show();
//                Toast.makeText(MainActivity.this, "ssssss", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case BAIDU_READ_PHONE_STATE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获取到权限，作相应处理（调用定位SDK应当确保相关权限均被授权，否则可能引起定位失败）
                    showLocation();
                } else {
                    // 没有获取到权限，做特殊处理
                    Toast.makeText(getApplicationContext(), "获取位置权限失败，请手动开启", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    // 判断是否熄屏 要求安卓在4.4W及以上
    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    public boolean isPowerOn() {
        return powerManager.isInteractive();
    }

    // 判断是否解锁 要求安卓在5.1及以上
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    // true=>手机处于锁屏状态， false=>手机处于解锁状态
    public boolean isPhoneLocked() {
        return keyguardManager.isDeviceLocked() || keyguardManager.isKeyguardLocked();
    }

    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private String action = null;
        private int result = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                result = 1;
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                result = 2;
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                result = 3;
            }
        }

        public int getResult() {
            return result;
        }
    }

    // 广播方式
    private IntentFilter startScreenBroadcastReciver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        context.registerReceiver(screenReceiver, filter);
        return filter;
    }

}

