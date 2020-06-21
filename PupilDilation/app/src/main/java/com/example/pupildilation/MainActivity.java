package com.example.pupildilation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import static com.example.pupildilation.R.id.howtoButton;
import static com.example.pupildilation.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(activity_main);
        createDirectory();

        Button start = (Button) findViewById(R.id.startButton);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CameraNotice.class);
                startActivity(intent);
            }
        });


        Button howto = (Button)findViewById(howtoButton);
        howto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HowToPlay.class);
                startActivity(intent);
            }
        });
    }

    public void createDirectory(){
        File folder = new File(Environment.getExternalStorageDirectory() + "/Experiments/Experiment_files");
        if(!folder.exists()) folder.mkdirs();

    }
}
