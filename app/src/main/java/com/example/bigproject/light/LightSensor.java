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
    public float[] lux;

    public LightSensor(Context context, LightCallback callback) {
        this.callback = callback;
        this.context = context;
    }

    /**
     * 接收传感器感知到的变化的亮度
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        lux = new float[2];
        int sensorType = event.sensor.getType();
        if (sensorType == Sensor.TYPE_LIGHT) {
            // 初始数据
            lux[0] = values[0];
            // 计算屏幕的亮度
            lux[1] = values[0] * (1f / 255f);
        }
        callback.getLight(lux);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface LightCallback {
        void getLight(float[] values);
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
