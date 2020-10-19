package com.ninewatt.beacon_collector;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.ninewatt.beacon_collector.util.LogUtil;
import com.ninewatt.beacon_collector.util.PrefUtil;

public class OptionActivity extends AppCompatActivity {
    private EditText mEditInterval, mEditScanTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        int interval = PrefUtil.getInterval(this);
        int time = PrefUtil.getScanTime(this);

        mEditInterval = ((EditText) findViewById(R.id.edit_interval));
        mEditInterval.setText(String.valueOf(interval));
        mEditScanTime = ((EditText) findViewById(R.id.edit_scantime));
        mEditScanTime.setText(String.valueOf(time));
    }

    @Override
    public void onBackPressed() {
        if (mEditInterval.getText().toString().trim().length() == 0 || mEditScanTime.getText().toString().trim().length() == 0) {
            LogUtil.showToast(this, "설정값을 입력해 주세요.");
        } else {
            try {
                int interval = Integer.parseInt(mEditInterval.getText().toString().trim());
                PrefUtil.setInterval(this, interval);
                int time = Integer.parseInt(mEditScanTime.getText().toString().trim());
                PrefUtil.setScanTime(this, time);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.showToast(this, "설정값 저장 실패.");
            } finally {
                super.onBackPressed();
            }
        }
    }
}