package com.example.tianyi.dashcam;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";

    private final String[] mPermissionsRequired = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private final int PERMISSIONS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length == 0) {
                    Log.i(LOG_TAG, "grantResults empty");
                    openCamera(null);
                    return;
                }

                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        openCamera(null);
                        Log.i(LOG_TAG, permissions[i] + " denied");
                        return;
                    }
                }

                Intent intent = new Intent(this, CameraActivity.class);
                startActivity(intent);
            default:
                break;
        }
    }

    public void openCamera(View view) {
        String[] permissionsToRequest = getPermissionsToRequest();

        for (int i = 0; i < permissionsToRequest.length; i++) {
            Log.i(LOG_TAG, permissionsToRequest[i]);
        }

        if (permissionsToRequest.length > 0) {
            ActivityCompat.requestPermissions(MainActivity.this, permissionsToRequest, PERMISSIONS_REQUEST_CODE);
        } else {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        }
    }

    private String[] getPermissionsToRequest() {
        ArrayList<String> permissionsToRequest = new ArrayList<>();

        for (int i = 0; i < mPermissionsRequired.length; i++) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, mPermissionsRequired[i]) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(mPermissionsRequired[i]);
            }
        }

        return permissionsToRequest.toArray(new String[0]);
    }
}
