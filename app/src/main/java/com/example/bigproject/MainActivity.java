package com.example.bigproject;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bigproject.RunService.RunService;
import com.example.bigproject.LocationService.LocationService;
import com.example.bigproject.light.LightSensor;
import com.example.bigproject.orientation.OrientSensor;
import com.example.bigproject.LocationService.LocationUtils;
import com.example.bigproject.ui.home.HomeFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements OrientSensor.OrientCallBack, LocationService.LocationCallBack, LightSensor.LightCallback {

    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private LightSensor lightSensor;
    private OrientSensor orientSensor;
    private LocationService locationService;
    private HomeFragment homeFragment = new HomeFragment();
    // 获取权限的请求码，到请求权限都要对应输入这个请求码
    private static final int BAIDU_READ_PHONE_STATE = 100;
    private PowerManager powerManager;
    private KeyguardManager keyguardManager;
    private IntentFilter intentFilter;
    private Intent intent;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Log.i("====","初始化每日时间");
                    saveToFile(1,1,1,3);
                    break;
            }
        }
    };

    private float sleepTime=0;
    private float moveTime=0;
    private float darkTime =10;
    private long lastDarkTime =0;
    private long lastLocationTime=0;
    private long lastOrientTime=0;

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
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_description, R.id.nav_report, R.id.nav_setting, R.id.nav_team)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if (Build.VERSION.SDK_INT >= 23) {
            showContacts();
        }

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        intent = new Intent(this, RunService.class);//初始化后台服务
        if(Build.VERSION.SDK_INT>=26)//开启后台服务
            startForegroundService(intent);
        else startService(intent);

        //注册方向传感器监听
        orientSensor = new OrientSensor(this, this);
        orientSensor.registerOrient();
        //注册GPS监听
        locationService = new LocationService(this, this, this);
        locationService.start();
        //注册光线传感器监听
        lightSensor = new LightSensor(this, this);
        lightSensor.registerLightSensor();

        //在每天凌晨重置记录的数据
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(1);
            }
        };
        Timer timer = new Timer(true);
        Calendar calendar=Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        String mYear=String.valueOf(calendar.get(Calendar.YEAR));
        String mMonth=String.valueOf(calendar.get(Calendar.MONTH)+1);
        String mDay=String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String strDate=mYear+"-"+mMonth+"-"+mDay+" 23:59:59";
        timer.schedule(task,strToDateLong(strDate));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void getLocationTime(float time) {
        if (!isPhoneLocked() && isPowerOn()) {
            long currentTime=System.currentTimeMillis();
            if(currentTime-lastLocationTime>10*1000){
                float timeDifference = time-moveTime;
                moveTime=time;
                float[] times = readFile();
                saveToFile(times[0],timeDifference/60+times[1],times[2],timeDifference/60+times[3]);
                lastLocationTime=currentTime;
                Toast.makeText(this,"请不要在行走时使用手机",Toast.LENGTH_LONG).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void getLightTime(float time) {
        if (!isPhoneLocked() && isPowerOn()) {
            long currentTime=System.currentTimeMillis();
            if(currentTime- lastDarkTime >1*60*1000){
                float timeDifference = time-darkTime;
                darkTime=time;
                float[] times = readFile();
                saveToFile(times[0],times[1],timeDifference/60+times[2],timeDifference/60+times[3]);
                lastDarkTime =currentTime;
                Toast.makeText(this,"请不要在黑暗处使用手机",Toast.LENGTH_LONG).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void getOrientTime(float time) {
        if (!isPhoneLocked() && isPowerOn()) {
            long currentTime=System.currentTimeMillis();
            if(currentTime-lastOrientTime>1*60*1000){
                float timeDifference = time-sleepTime;
                sleepTime=time;
                if(timeDifference<0) return;
                float[] times = readFile();
                saveToFile(timeDifference/60+times[0],times[1],times[2],timeDifference/60+times[3]);
                lastOrientTime=currentTime;
                Toast.makeText(this,"请注意您使用手机的姿势",Toast.LENGTH_LONG).show();
            }
        }
    }

    public float[] readFile(){
        float num[] = new float[4];//用来存储从文件中读取出的数据
        String detail = "";
        FileHelper fHelper2 = new FileHelper(this);
        try    {
            String fname = "myFile.txt";
            detail = fHelper2.read(fname);
            String[] stringArray = detail.split(" ");
            for (int i = 0; i < stringArray.length; i++) {
                num[i] = Float.parseFloat(stringArray[i]);
            }
        } catch(IOException e){
            e.printStackTrace();
        }finally {
            return num;
        }
    }

    public void saveToFile(float sleepTime,float moveTime,float darkTime,float sumTime) {
        FileHelper fileHelper=new FileHelper(this);
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
        orientSensor.unregisterOrient();
        locationService.stop();
        stopService(intent);
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showContacts() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
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
            Toast.makeText(this, "请在设置中查看权限是否都开启以确保程序正常运行", Toast.LENGTH_SHORT).show();
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
                    Manifest.permission.CONTROL_LOCATION_UPDATES,
                    Manifest.permission.ACCESS_NOTIFICATION_POLICY
            }, BAIDU_READ_PHONE_STATE);
        } else {
            showLocation();
        }
    }

    private void showLocation() {
        Log.d(TAG, LocationUtils.getInstance().getLocations(this));
        //Toast.makeText(MainActivity.this, LocationUtils.getInstance().getLocations(MainActivity.this), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(this, "获取权限成功", Toast.LENGTH_SHORT).show();
                } else {
                    // 没有获取到权限，做特殊处理
                    Toast.makeText(this, "获取权限失败，请手动开启", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    // 判断是否熄屏 要求安卓在4.4W及以上
    // true=>亮屏 false=>熄屏
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

    /*private class ScreenBroadcastReceiver extends BroadcastReceiver {
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
    }*/

    @Override
    public boolean onKeyDown(int KeyCode, KeyEvent keyEvent){//按下返回键后，转入后台运行
        if(KeyCode==KeyEvent.KEYCODE_BACK){//返回键按下
            Toast.makeText(this,"应用转入后台运行",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }else return super.onKeyDown(KeyCode,keyEvent);
    }

    /**
     * string类型时间转换为date
     * @param strDate
     * @return
     */
    public static Date strToDateLong(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }
}
