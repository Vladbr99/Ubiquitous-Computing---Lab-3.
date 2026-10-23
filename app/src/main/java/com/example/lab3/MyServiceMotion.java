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
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Date;

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

        // Get the sensor manager and the device's accelerometer.
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Check that the device has an accelerometer.
        if (mSensor != null) {
            Log.d("MyServiceMotion", "Accelerometer found");

            // Start a separate thread for handling sensor readings.
            sensorThread = new HandlerThread("MotionSensorThread");
            sensorThread.start();

            // Create a handler connected to the sensor thread.
            Handler sensorHandler = new Handler(sensorThread.getLooper());

            // Register this service to receive sensor readings on that thread.
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
        // Read acceleration along the three axes.
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Save the first reading so we have something to compare against.
        if (firstReading) {
            previousX = x;
            previousY = y;
            previousZ = z;
            firstReading = false;
            return;
        }

        // Calculate how much each axis changed since the previous reading.
        float changeX = x - previousX;
        float changeY = y - previousY;
        float changeZ = z - previousZ;

        // Combine the changes into one overall acceleration-change value.
        double movement = Math.sqrt(
                changeX * changeX +
                        changeY * changeY +
                        changeZ * changeZ
        );

        // Save these values for the next comparison.
        previousX = x;
        previousY = y;
        previousZ = z;

        // Convert the sensor timestamp from nanoseconds to milliseconds.
        long now = event.timestamp / 1000000L;

        // Detect sufficient movement, allowing at least 3 seconds between alerts.
        if (movement > MOVEMENT_THRESHOLD &&
                (lastMotionTime == 0 || now - lastMotionTime >= 3000)) {

            // Record when this alert was triggered.
            lastMotionTime = now;

            Log.d("MyServiceMotion", "Movement detected");

            // Show a notification containing the motion time.
            showMotionNotification();
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

    private void showMotionNotification() {
        // Check notification permission on Android 13 or newer.
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyServiceMotion", "Notification permission not granted");
            return;
        }

        // Get Android's notification manager.
        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Create the notification channel required on Android 8 or newer.
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    "motion", "Motion alerts",
                    NotificationManager.IMPORTANCE_DEFAULT);

            manager.createNotificationChannel(channel);
        }

        // Include the date and time of the detected movement.
        String message = "Motion detected at " + new Date();

        // Send the motion message to the details activity.
        Intent intent = new Intent(this, MotionDetails.class);
        intent.putExtra("message", message);

        // Open the details activity when the notification is tapped.
        // Update its message when another movement is detected.
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set the notification's icon, title, message and tap action.
        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(this, "motion")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Motion detected")
                        .setContentText(message)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        // Show the notification, replacing any previous motion alert.
        manager.notify(1, notification.build());
    }
}