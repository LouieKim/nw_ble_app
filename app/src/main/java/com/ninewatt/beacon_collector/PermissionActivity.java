package com.ninewatt.beacon_collector;

import android.Manifest;
import android.animation.Animator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

/*
디바이스 등록/삭제, 데이터 전송 - 10/6(화)
백그라운드 실행 - 10/9(금)
그래프 표현 - 10/13(화)
 */

/**
 * 퍼미션 확인을 위한 인트로 액티비티
 */
public class PermissionActivity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        TedPermission.with(this)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        Toast.makeText(PermissionActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                        startApp();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        Toast.makeText(PermissionActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setPermissions(
                        Manifest.permission.INTERNET,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .check();
    }

    private void startApp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.img_start).animate().alpha(0.f).setDuration(2000).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startActivity(new Intent(PermissionActivity.this, MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
            }
        });
    }
}