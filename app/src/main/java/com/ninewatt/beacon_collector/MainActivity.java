package com.ninewatt.beacon_collector;

import android.annotation.SuppressLint;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ninewatt.beacon_collector.beacon.BeaconData;
import com.ninewatt.beacon_collector.beacon.BeaconScanner;
import com.ninewatt.beacon_collector.util.LogUtil;
import com.ninewatt.beacon_collector.util.PrefUtil;

import static com.ninewatt.beacon_collector.BeaconReceiver.startAlarm;

public class MainActivity extends AppCompatActivity {
    private TextView mTextId;
    private BeaconScanner mScanner;

    private boolean mSending;

    private final String TAG = "!!@@MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        PrefUtil.setSiteId(this, "10000050");
        String siteId = PrefUtil.getSiteId(this);
        mTextId = findViewById(R.id.txt_main_id);
        mTextId.setText("ID : " + siteId);

        mScanner = new BeaconScanner(this, new BeaconScanner.OnBleListener() {
            @Override
            public void onRetry() {

            }

            @Override
            public void onInitFailed() {

            }

            @Override
            public void onAddScanResult(ScanResult result) {
                BeaconData data = new BeaconData();
                data.convertBeaconData(result);
                Log.d(TAG, "onAddScanResult - data : " + data.toString());
                LogUtil.saveLog("D", "!!@@MainActivity", "onAddScanResult : " + data.toString());
                TaskManager.getInstance().sendData(PrefUtil.getSiteId(MainActivity.this),
                        String.valueOf(data.getMinor()), new TaskManager.OnSendListener() {
                            @Override
                            public void onSuccess(String data) {
                                Log.d(TAG, "sendData - onSuccess : " + data);
                                LogUtil.saveLog("D", "!!@@MainActivity", "onSuccess : " + data);
                                LogUtil.showToast(MainActivity.this, "Send Data : " + data);
                                mSending = false;
                            }

                            @Override
                            public void onFailed() {
                                Log.d(TAG, "sendData - onFailed");
                                LogUtil.saveLog("D", "!!@@MainActivity", "onFailed");
                                mSending = false;
                            }
                        });
            }

            @Override
            public void onScanFailed() {
                LogUtil.showToast(MainActivity.this, "Scan Failed : " + PrefUtil.getSiteId(MainActivity.this));
            }
        });

        findViewById(R.id.txt_device).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(0.5f);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setAlpha(1.f);
                    startActivity(new Intent(MainActivity.this, BeaconActivity.class));
                }

                return true;
            }
        });

        findViewById(R.id.txt_history).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(0.5f);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setAlpha(1.f);
                    startActivity(new Intent(MainActivity.this, HistoryActivity.class));
//                    LogUtil.showToast(MainActivity.this, "업데이트 예정...");
                }

                return true;
            }
        });

        findViewById(R.id.txt_send).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(0.5f);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setAlpha(1.f);
                    if (mSending)
                        LogUtil.showToast(MainActivity.this, "데이터 전송 중...");
                    else {
                        mSending = true;
                        boolean exist = PrefUtil.hasDevice(MainActivity.this);
                        LogUtil.saveLog("D", "!!@@MainActivity", "txt_send - exist : " + exist);
                        if (exist) {
                            mScanner.startScan(PrefUtil.getDeviceAddress(MainActivity.this));
                        } else {
                            LogUtil.showToast(MainActivity.this, "등록된 디바이스가 없습니다.");
                        }
                    }
                }

                return true;
            }
        });

        findViewById(R.id.txt_option).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(0.5f);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setAlpha(1.f);
                    startActivity(new Intent(MainActivity.this, OptionActivity.class));
                }

                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTextId != null) {
            String siteId = PrefUtil.getSiteId(this);
            mTextId.setText("ID : " + siteId);
        }
    }

    @Override
    protected void onDestroy() {
        final long interval = 1000 * 60 * PrefUtil.getInterval(this);
        startAlarm(this, interval);

        super.onDestroy();
    }
}