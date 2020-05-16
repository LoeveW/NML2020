package com.example.pupildilation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import static com.example.pupildilation.R.layout.activity_main;

public class CameraNotice extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_camera_notice);

        Button start = (Button)findViewById(R.id.startButton);
        start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CameraNotice.this, Game1Activity.class);
                startActivity(intent);
            }
        });
    }
}
