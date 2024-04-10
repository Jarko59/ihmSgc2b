package com.example.ihmproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class MainPageLowUser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page_low_user);

        ImageButton btnFullscreenVideo = findViewById(R.id.btnFullScreen);

        btnFullscreenVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainPageLowUser.this, VideoViewerActivity.class));
            }
        });
    }
}
