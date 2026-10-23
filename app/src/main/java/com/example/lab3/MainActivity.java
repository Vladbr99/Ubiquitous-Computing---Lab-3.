package com.example.lab3;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.view.View;
// new imports
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, MyServiceMotion.class);
        startService(intent);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void testMotionNotification(View view) {
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            return;
        }

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    "motion", "Motion alerts",
                    NotificationManager.IMPORTANCE_DEFAULT);

            manager.createNotificationChannel(channel);
        }

        String message = "Test motion detected at " + new Date();

        Intent intent = new Intent(this, MotionDetails.class);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(this, "motion")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Motion detected - test")
                        .setContentText(message)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        manager.notify(1, notification.build());
    }
}