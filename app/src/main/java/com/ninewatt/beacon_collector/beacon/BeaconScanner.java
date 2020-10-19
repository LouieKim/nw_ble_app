package com.ninewatt.beacon_collector.beacon;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.ninewatt.beacon_collector.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class BeaconScanner {
    private Context mContext;
    private OnBleListener mListener;
    private BluetoothLeScanner mBleScanner;
    private BLEScanCallback mBleCallback;
    private ScanSettings mSettings;

    private List<ScanFilter> mFilters;

    private String mDeviceAddress = "";
    private boolean mFinishedScan;

    private final String TAG = "!!@@BeaconScanner";

    /**
     * 블루투스 관련된 상태를 전달하기 위한 리스너.
     */
    public interface OnBleListener {
        void onRetry();

        void onInitFailed();

        void onAddScanResult(ScanResult result);

        void onScanFailed();
    }

    /**
     * 클래스 생성자
     *
     * @param context
     * @param l       OnBtListener
     */
    public BeaconScanner(Context context, OnBleListener l) {
        mContext = context;
        mListener = l;

        BluetoothManager mng = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mng != null) {
            BluetoothAdapter adpater = mng.getAdapter();
            // 블루투스 기능이 꺼져있으면 설정화면으로 이동
            if (adpater == null || !adpater.isEnabled()) {
                Log.e(TAG, "BluetoothAdapter is null or not enable");
                mContext.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } else {
                mBleScanner = adpater.getBluetoothLeScanner();
                mFilters = new ArrayList<>();
//                ScanFilter filter = new ScanFilter.Builder()
//                        .setManufacturerData(0x004C, new byte[]{}, new byte[]{})
//                        .build();
//                mFilters.add(filter);
                mSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
            }
        }
    }

    /**
     * 블루투스 스캔을 시작한다.
     */
    public void startScan(String address) {
        Log.d(TAG, "startScan : " + address);
        mDeviceAddress = address;
        BluetoothManager mng = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mng != null) {
            BluetoothAdapter adpater = mng.getAdapter();
            if (adpater != null) {
                if (adpater.isEnabled()) {
                    mBleScanner = adpater.getBluetoothLeScanner();
                    if (mBleScanner != null) {
                        Log.d(TAG, "startScan - done");
                        mBleCallback = new BLEScanCallback();
                        mBleScanner.startScan(mFilters, mSettings, mBleCallback);
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 특정한 디바이스를 스캔하는 경우
                                if(mDeviceAddress.trim().length() > 0) {
                                    // 딜레이된 시간 동안 특정한 디바이스를 스캔하지 못했으면 실패로 간주
                                    if(!mFinishedScan) {
                                        mListener.onScanFailed();
                                        LogUtil.showToast(mContext, "Scan Failed : " + mDeviceAddress);
                                        Log.d(TAG, "Scan Failed : " + mDeviceAddress);
                                    }
                                }
                                // 모든 디바이스를 스캔하는 경우
                                else {
                                    // 딜레이된 시간 동안 종료되지 않았으면 종료
                                    if(!mFinishedScan) {
                                        mBleScanner.stopScan(mBleCallback);
                                        LogUtil.showToast(mContext, "Finished BLE Scanning.");
                                        Log.d(TAG, "Finished BLE Scanning.");
                                    }
                                }

                                mFinishedScan = false;
                            }
                        }, mDeviceAddress.trim().length() == 0 ? 10000 : 20000);
                    } else {
                        Log.e(TAG, "startScan - BluetoothLeScanner is null");
                        mListener.onInitFailed();
                    }
                } else {
                    adpater.enable();
                    mListener.onRetry();
                }
            } else
                Log.e(TAG, "startScan - BluetoothAdapter is null");
        } else {
            Log.e(TAG, "startScan - BluetoothManager is null");
            mListener.onInitFailed();
        }
    }

    public void stopScan() {
        mFinishedScan = true;
        if (mBleScanner != null)
            mBleScanner.stopScan(mBleCallback);
    }

    /**
     * 블루투스 스캔을 종료한다.
     */
    public void destroy() {
        Log.d(TAG, "destroy");
        if (mBleScanner != null) {
            mBleScanner.stopScan(mBleCallback);
            mBleCallback = null;
        }
    }

    /**
     * BLE 스캔 콜백 함수
     */
    private class BLEScanCallback extends ScanCallback {
        BLEScanCallback() {
            Log.d(TAG, "BLEScanCallback - constructor");
        }

        @Override
        public void onScanResult(int _callback_type, ScanResult _result) {
            Log.d(TAG, "BLEScanCallback - onScanResult : " + _callback_type + ", " + _result.toString());
            // 모든 디바이스를 스캔하는 경우
            if (mDeviceAddress.trim().length() == 0)
                mListener.onAddScanResult(_result);
            else {
                // 특정한 디바이스를 스캔하는 경우
                if (mDeviceAddress.equals(_result.getDevice().getAddress())) {
                    mFinishedScan = true;
                    mListener.onAddScanResult(_result);
                    mBleScanner.stopScan(mBleCallback);
                    LogUtil.showToast(mContext, "Finished BLE Scanning.");
                    Log.d(TAG, "Finished BLE Scanning.");
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> _results) {
//            Log.d(TAG, "BLEScanCallback - onBatchScanResults : " + _results.size());
        }

        @Override
        public void onScanFailed(int _error) {
//            Log.d(TAG, "BLEScanCallback - onScanFailed : " + _error);
        }
    }
}