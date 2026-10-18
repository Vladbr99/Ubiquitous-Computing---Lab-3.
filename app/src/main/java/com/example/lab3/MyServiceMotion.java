package com.example.lab3;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MyServiceMotion extends Service {
    public MyServiceMotion() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Thread backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("MyServiceMotion", "Background thread started");
            }
        });

        backgroundThread.start();
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