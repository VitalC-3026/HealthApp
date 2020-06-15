package com.example.bigproject.LocationService;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class LocationService {
    private float totalTime=0;
    private Location lastLocation;
    private LocationManager locationManager;
    private Criteria criteria;
    private String provider;
    private Context context;
    private Activity activity;
    private LocationCallBack locationCallBack;
    private final float accuracy = 5;

    public LocationService(Context context, Activity activity, LocationCallBack locationCallBack) {
        this.context = context;
        this.activity = activity;
        this.locationCallBack = locationCallBack;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        initCriteria();
        /* 从可用的位置提供器中，匹配以上标准的最佳提供器 */
        provider = locationManager.getBestProvider(criteria, true);
    }

    public interface LocationCallBack {
        void getLocationTime(float time);
    }

    /**
     * 初始化GPS参数
     */
    private void initCriteria() {
        if (criteria == null) {
            criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
            criteria.setAltitudeRequired(false); // 不要求海拔
            criteria.setBearingRequired(false); // 不要求方位
            criteria.setCostAllowed(true); // 允许有花费
            criteria.setPowerRequirement(Criteria.POWER_LOW);// 设置低功耗模式
        }
    }

    /**
     * @return 获取criteria
     */
    public Criteria getCriteria() {
        initCriteria();
        return criteria;
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean start() {
        try {
            if (!isGPSAble(locationManager)) {
                openGPS2();
                Thread.sleep(2000);
            }
            // 注册监听函数
            if (checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            provider = locationManager.getBestProvider(criteria, true);
            if (checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                //动态获取权限
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            locationManager.requestLocationUpdates(provider, 1000 * 10, accuracy, locationListener);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 停止GPS定位
     *
     *
     */
    public boolean stop() {
        try {
            locationManager.removeUpdates(locationListener);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 位置监听器
     */
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
        // 当位置变化时触发
        @Override
        public void onLocationChanged(android.location.Location location) {
            if (location != null) {
                if (location.hasAccuracy() && location.getAccuracy() <= accuracy) {
                    totalTime+=10;
                    locationCallBack.getLocationTime(totalTime);
                }
            }
        }
    };

    private boolean isGPSAble(LocationManager locationManager){
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void openGPS2(){
        Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        activity.startActivityForResult(intent,0);
    }
}
