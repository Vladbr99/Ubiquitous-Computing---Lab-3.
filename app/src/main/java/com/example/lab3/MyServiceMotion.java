package com.example.lab3;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Handler;
import android.os.HandlerThread;

public class MyServiceMotion extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private HandlerThread sensorThread;
    private float previousX, previousY, previousZ;
    private boolean firstReading = true;
    private long lastMotionTime = 0;

    private static final float MOVEMENT_THRESHOLD = 2.0f;
    public MyServiceMotion() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (mSensor != null) {
            Log.d("MyServiceMotion", "Accelerometer found");
            sensorThread = new HandlerThread("MotionSensorThread");
            sensorThread.start();

            Handler sensorHandler = new Handler(sensorThread.getLooper());

            mSensorManager.registerListener(
                    this,
                    mSensor,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    sensorHandler
            );
        } else {
            Log.d("MyServiceMotion", "No accelerometer available");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (firstReading) {
            previousX = x;
            previousY = y;
            previousZ = z;
            firstReading = false;
            return;
        }

        float changeX = x - previousX;
        float changeY = y - previousY;
        float changeZ = z - previousZ;

        double movement = Math.sqrt(
                changeX * changeX +
                        changeY * changeY +
                        changeZ * changeZ
        );

        previousX = x;
        previousY = y;
        previousZ = z;

        long now = event.timestamp / 1000000L;

        if (movement > MOVEMENT_THRESHOLD &&
                (lastMotionTime == 0 || now - lastMotionTime >= 3000)) {

            lastMotionTime = now;

            Log.d("MyServiceMotion", "Movement detected");
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No action needed for this lab.
    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(this);

        if (sensorThread != null) {
            sensorThread.quitSafely();
        }

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MyServiceMotion", "Motion service started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}