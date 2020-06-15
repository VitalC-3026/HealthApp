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
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements LightSensor.LightCallback {

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
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

    public void showContacts() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE)
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
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.BODY_SENSORS,
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
