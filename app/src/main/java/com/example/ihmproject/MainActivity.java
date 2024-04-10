package com.example.ihmproject;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Button btnSettings;
    private ImageButton btnFullscreenVideo;
    private TextView tvTemperature, tvHumidity;
    private VideoView videoView;

    private static final int REQUEST_INTERNET_PERMISSION = 1;
    private static final long UPDATE_INTERVAL = 30000; // Mettre à jour toutes les 30 secondes

    private Handler handler = new Handler();
    private Runnable updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HighUserSettings.class);
                startActivity(intent);
            }
        });

        btnFullscreenVideo = findViewById(R.id.btnFullscreenVideo);
        btnFullscreenVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VideoViewerActivity.class);
                startActivity(intent);
            }
        });

        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidity = findViewById(R.id.tvHumidity);

        videoView = findViewById(R.id.videoView);

        // Initialiser le Runnable pour la mise à jour périodique des données
        updater = new Runnable() {
            @Override
            public void run() {
                // Appeler la méthode pour mettre à jour les données
                updateData();
                // Planifier la prochaine mise à jour
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };

        // Démarrer la mise à jour périodique des données
        handler.post(updater);

        // Créer le canal de notification pour les appareils Android version 8 et supérieure
        createNotificationChannel();
    }

    // Méthode pour mettre à jour les données de l'interface utilisateur
    private void updateData() {
        // Vérifier les autorisations INTERNET
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_GRANTED) {
            // Accéder à la base de données via l'API
            new RetrieveDataFromDatabase().execute();
        }
    }

    private class RetrieveDataFromDatabase extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... voids) {
            String[] data = new String[2]; // Seulement 2 éléments pour la température et l'humidité
            try {
                // Utilisation de l'URL de votre API PHP
                URL url = new URL("https://sgc2b-projet.000webhostapp.com/api.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    String response = stringBuilder.toString();

                    // Parsez la réponse JSON de votre API pour extraire les données nécessaires
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    data[0] = jsonObject.getString("temperature");
                    data[1] = jsonObject.getString("humidite");
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                // message d'erreur connexion à l'API a échoué
                e.printStackTrace();
            } catch (JSONException e) {
                // message d'erreur format JSON incorrect
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String[] result) {
            // Affichage de température
            if (result[0] != null) {
                double temperature = Double.parseDouble(result[0]);
                tvTemperature.setText("Température : " + temperature + " °C");
                // Vérifier si la température est dangereuse pour un nourrisson
                if (temperature > 40 || temperature < 10) {
                    sendNotification("Température dangereuse", "La température est hors des limites normales pour un nourrisson : " + temperature + " °C");
                }
            } else {
                tvTemperature.setText("Température non disponible");
            }

            // Affichage de l'humidité
            if (result[1] != null) {
                int humidite = Integer.parseInt(result[1]);
                tvHumidity.setText("Humidité : " + humidite + " %");
                // Vérifier si l'humidité est dangereuse pour un nourrisson
                if (humidite > 55 || humidite < 45) {
                    sendNotification("Humidité dangereuse", "L'humidité est hors des limites normales pour un nourrisson : " + humidite + " %");
                }
            } else {
                tvHumidity.setText("Humidité non disponible");
            }

            // Charger le flux vidéo dans le VideoView
            startVideoStream();
        }
    }

    // Nouvelle méthode pour démarrer le flux vidéo de la caméra ESP32
    private void startVideoStream() {
        // Remplacez "http://192.168.1.39/" par l'URL réelle de votre flux vidéo ESP32
        String streamUrl = "http://192.168.1.39/";
        Uri videoUri = Uri.parse(streamUrl);

        videoView.setVideoURI(videoUri);
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // Gérer l'erreur de lecture vidéo ici
                // Vous pouvez afficher un message à l'utilisateur ou effectuer d'autres actions
                return true; // Indiquer que l'erreur a été traitée
            }
        });
        videoView.start();
    }

    // Méthode pour envoyer une notification
    private void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    // Créer un canal de notification pour les appareils Android version 8 et supérieure
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel name";
            String description = "Channel description";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("channel_id", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}