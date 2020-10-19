package com.ninewatt.beacon_collector;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import com.ninewatt.beacon_collector.beacon.BeaconData;
import com.ninewatt.beacon_collector.beacon.BeaconScanner;
import com.ninewatt.beacon_collector.util.LogUtil;
import com.ninewatt.beacon_collector.util.PrefUtil;

import static android.content.Context.ALARM_SERVICE;

/**
 * Alarm 으로 호출
 * Beacon 스캔 및 데이터 전송 작업 수행 후 Alarm 재등록
 */
public class BeaconReceiver extends BroadcastReceiver {
    private static final String TAG = "!!@@BeaconReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        boolean exist = PrefUtil.hasDevice(context);
        final long interval = 1000 * 60 * PrefUtil.getInterval(context);
        Log.d(TAG, "onReceive - Exist Device : " + exist + ", Interval : " + interval);
        LogUtil.saveLog("D", "!!@@BeaconReceiver", "onReceive - Exist Device : " + exist + ", Interval : " + interval);
        // 등록된 디바이스가 있으면 작업 수행
        if (exist) {
            BeaconScanner scanner = new BeaconScanner(context, new BeaconScanner.OnBleListener() {
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
                    LogUtil.saveLog("D", TAG, "onAddScanResult - data : " + data.toString());
                    TaskManager.getInstance().sendData(PrefUtil.getSiteId(context), String.valueOf(data.getMinor()),
                            new TaskManager.OnSendListener() {
                                @Override
                                public void onSuccess(String data) {
                                    Log.d(TAG, "sendData - onSuccess : " + data);
                                    LogUtil.saveLog("D", "!!@@BeaconReceiver", "sendData - onSuccess : " + data);
                                    startAlarm(context, interval);
                                }

                                @Override
                                public void onFailed() {
                                    Log.e(TAG, "sendData - onFailed");
                                    LogUtil.saveLog("D", "!!@@BeaconReceiver", "sendData - onFailed");
                                    startAlarm(context, interval);
                                }
                            });
                }

                @Override
                public void onScanFailed() {
                    Log.e(TAG, "sendData - onScanFailed");
                    LogUtil.saveLog("D", "!!@@BeaconReceiver", "sendData - onScanFailed");
                    startAlarm(context, interval);
                }
            });
            scanner.startScan(PrefUtil.getDeviceAddress(context));
        } else {
            Log.e(TAG, "등록된 디바이스가 없습니다.");
            LogUtil.saveLog("E", "!!@@BeaconReceiver", "등록된 디바이스가 없습니다.");
            startAlarm(context, interval);
        }
    }

    public static void startAlarm(Context context, long interval) {
        LogUtil.saveLog("E", "!!@@BeaconReceiver", "startAlarm : " + interval);
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            boolean isScreenOn = pm.isInteractive();
            boolean isIdle = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                isIdle = pm.isDeviceIdleMode();

            Log.i(TAG, "startAlarm - isScreenOn : " + isScreenOn + ", isIdle : " + isIdle);
            if (!isScreenOn || isIdle) {
//                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:notificationLock");
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:notificationLock");
//                PowerManager.WakeLock wl = pm.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, "myApp:notificationLock");
                wl.acquire(interval);
                Log.d(TAG, "startAlarm - WakeLock");
            }
        }

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, BeaconReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, 101, alarmIntent, 0);
        alarmMgr.cancel(alarmPendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "startAlarm - setExactAndAllowWhileIdle");
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, alarmPendingIntent);
        } else {
            Log.d(TAG, "startAlarm - setExact");
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, alarmPendingIntent);
        }
    }
}