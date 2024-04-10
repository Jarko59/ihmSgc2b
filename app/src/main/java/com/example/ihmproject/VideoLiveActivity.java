package com.example.ihmproject;

import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class VideoLiveActivity extends AppCompatActivity {

    private static final String VIDEO_URL = "http://192.168.0.100/live";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_live);

        // Obtention de la référence du VideoView
        VideoView videoView = findViewById(R.id.videoView);

        // Création d'un objet MediaController pour contrôler le lecteur vidéo
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Définition de l'URL du flux vidéo
        videoView.setVideoPath(VIDEO_URL);

        // Démarrage de la lecture du flux vidéo
        videoView.start();
    }
}
