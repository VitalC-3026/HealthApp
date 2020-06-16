package com.example.bigproject.light;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class LightSensor implements SensorEventListener {
    private Context context;
    private LightCallback callback;
    public SensorManager sensorManager;
    public Sensor lightSensor;

    private float lastDarkTime=0;
    private long lastTime=0;
    private boolean lastDark=false;
    private float darkTimeDiff=0;

    public LightSensor(Context context, LightCallback callback) {
        this.callback = callback;
        this.context = context;
    }

    // 回调函数接口
    public interface LightCallback {
        void getLightTime(float time);
    }

    /**
     * 接收传感器感知到的变化的亮度
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        long currentTime=System.currentTimeMillis();
        if(currentTime-lastTime<100) return;
        lastTime=currentTime;
        int sensorType = event.sensor.getType();
        if (sensorType == Sensor.TYPE_LIGHT) {
            boolean dark;
            if(event.values[0]<5) dark=true;
            else dark=false;
            if(lastDark) {
                darkTimeDiff += 0.1;
                callback.getLightTime(darkTimeDiff);
            }
            lastDark=dark;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 注册光线传感器
     *
     * @return boolean 是否支持注册光线传感器
     */
    public boolean registerLightSensor() {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        boolean isAvailable = sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
        return isAvailable;
    }

    /**
     * 注销光线传感器
     */
    public void unregisterLightSensor() {
        sensorManager.unregisterListener(this);
    }

}
