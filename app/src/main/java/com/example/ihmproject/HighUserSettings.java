package com.example.ihmproject;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class HighUserSettings extends AppCompatActivity {

    private Switch switchRockingMode, switchAutomaticRocking;
    private Button btnChooseMusic, btnSendNotification;
    private TextView tvSpeed;
    private int speed = 50;

    private static final String CHANNEL_ID = "Test";
    private static final int NOTIFICATION_ID = 1;
    private static final int PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_user_settings);

        // Création du canal de notification
        createNotificationChannel();

        switchRockingMode = findViewById(R.id.switchRockingMode);
        switchAutomaticRocking = findViewById(R.id.switchAutomaticRocking);
        btnChooseMusic = findViewById(R.id.btnChooseMusic);
        btnSendNotification = findViewById(R.id.btnSendNotification);
        tvSpeed = findViewById(R.id.tvSpeed);

        btnSendNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification();
            }
        });

        btnChooseMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code pour choisir une musique de bercement
            }
        });

        updateSpeedTextView();
    }

    public void decreaseSpeed(View view) {
        speed -= 5;
        if (speed < 0) {
            speed = 0;
        }
        updateSpeedTextView();
    }

    public void increaseSpeed(View view) {
        speed += 5;
        if (speed > 100) {
            speed = 100;
        }
        updateSpeedTextView();
    }

    private void updateSpeedTextView() {
        tvSpeed.setText(speed + "%");
    }

    private void sendNotification() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        } else {
            // Création de l'intent pour ouvrir l'activité lors du clic sur la notification
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentTitle("Notification")
                    .setContentText("Message de notification")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    // Méthode pour créer le canal de notification
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Test Channel";
            String description = "Channel for Test Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Enregistrement du canal de notification dans le système
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
