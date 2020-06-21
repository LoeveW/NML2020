package com.example.pupildilation;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CameraNotice extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{
    private static final String[] requiredPermissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera_notice);
        Button start = (Button)findViewById(R.id.startButton);
        start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(CameraNotice.this, Game1Activity.class);
                intent.putExtra("trial", 1);
                startActivityForResult(intent, 1);
            }
        });
        checkPermissions();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_CODE: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkPermissions();
                }
            }
        }
    }

    /**
     * checking  permissions at Runtime.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        System.out.println("Checking for permissions...");
        final List<String> neededPermissions = new ArrayList<>();
        for (final String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    permission) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(permission);
            }
        }
        if (!neededPermissions.isEmpty()) {
            System.out.println("Need new permission....");
            String[] arr = neededPermissions.toArray(new String[]{});
            System.out.println(Arrays.toString(arr));
            requestPermissions(arr, MY_PERMISSIONS_REQUEST_ACCESS_CODE);
        }
    }
}
