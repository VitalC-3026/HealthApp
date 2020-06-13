package com.example.bigproject.orientation;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientSensor implements SensorEventListener {
    private float totalTime=0;
    private long lastTime;
    private float[] values, r, accelerometer, magnetic;
    private SensorManager sensorManager;
    private Configuration configuration;
    private OrientCallBack orientCallBack;
    private Context context;

    public OrientSensor(Context context,OrientCallBack orientCallBack){
        this.context=context;
        this.orientCallBack=orientCallBack;
        this.configuration=context.getResources().getConfiguration();
        values=new float[3];
        r=new float[9];
        accelerometer=new float[3];
        magnetic=new float[3];
    }

    public interface OrientCallBack{
        void getOrientTime(float time);
    }

     /**
      * 注册加速度传感器和地磁传感器
      */
    public boolean registerOrient(){
        sensorManager= (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        boolean isAvailable = true;
        if(!sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME))
            isAvailable=false;
        if(!sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_GAME))
            isAvailable=false;
        return  isAvailable;
    }

    /**
     *注销传感器
     */
    public void  unregisterOrient(){
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long currentTime=System.currentTimeMillis();
        if(currentTime-lastTime<100) return;
        lastTime=currentTime;
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER) accelerometer = event.values.clone();
        if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD) magnetic = event.values.clone();

        SensorManager.getRotationMatrix(r,null,accelerometer,magnetic);
        SensorManager.getOrientation(r,values);
        float pitch = (float) Math.toDegrees(values[1]);
        float roll = (float) Math.toDegrees(values[2]);

        boolean flag=false;
        if(configuration.orientation==Configuration.ORIENTATION_LANDSCAPE) {
            if (pitch > -30 && pitch < 30 && roll < 80 && roll > -80)
                flag = true;
        }else {
            if(pitch > -90 && pitch < 5 && roll > -30 && roll < 30)
                flag=true;
        }
        if(!flag){
            totalTime+=0.1;
            orientCallBack.getOrientTime(totalTime);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
