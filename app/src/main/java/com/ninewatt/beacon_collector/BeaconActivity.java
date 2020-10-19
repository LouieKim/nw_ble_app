package com.ninewatt.beacon_collector;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.le.ScanResult;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninewatt.beacon_collector.beacon.BeaconData;
import com.ninewatt.beacon_collector.beacon.BeaconScanner;
import com.ninewatt.beacon_collector.util.LogUtil;
import com.ninewatt.beacon_collector.util.PrefUtil;

import java.util.ArrayList;
import java.util.List;

public class BeaconActivity extends AppCompatActivity {
    private TextView mTextScan, mTextSet, mTextInfo;
    private RecyclerView mRecycler;
    private BeaconAdapter mBeaconAdapter;
    private BeaconScanner mScanner;
    private Dialog mProgressDialog;

    private boolean mExistDevice;

    private final String TAG = "!!@@BeaconActivity";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

        mTextScan = findViewById(R.id.txt_beacon_scan);
        mTextScan.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(0.5f);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setAlpha(1.f);
                    if(mBeaconAdapter != null)
                        mBeaconAdapter.clear();

                    mScanner.startScan("");
                }

                return true;
            }
        });

        mExistDevice = PrefUtil.hasDevice(this);
        mTextSet = findViewById(R.id.txt_beacon_set);
        mTextSet.setText(mExistDevice ? getString(R.string.str_beacon_reset) : getString(R.string.str_beacon_set));
        mTextSet.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(0.5f);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setAlpha(1.f);
                    mScanner.stopScan();
                    int index = mBeaconAdapter.getSelectedIndex();
                    if (index == -1) {
                        LogUtil.showToast(BeaconActivity.this, "디바이스를 선택해 주세요.");
                    } else {
                        showProgress("디바이스 등록 중...");
                        final BeaconData data = mBeaconAdapter.getSelectedBeacon();
                        Log.i(TAG, "add device : " + data.toString());
                        // 등록된 디바이스가 있으면 등록해제 후 다시 등록
                        if(mExistDevice) {
                            String siteId = PrefUtil.getSiteId(BeaconActivity.this);
                            TaskManager.getInstance().deleteDevice(siteId, new TaskManager.OnDeleteListener() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "deleteDevice - onSuccess");
                                    LogUtil.showToast(BeaconActivity.this, "디바이스 삭제 성공");
                                    TaskManager.getInstance().addDevice(data.getUuid(), new TaskManager.OnAddListener() {
                                        @Override
                                        public void onSuccess(String siteId) {
                                            Log.d(TAG, "addDevice - onSuccess : " + siteId);
                                            dismissProgress();
                                            PrefUtil.setDeviceName(BeaconActivity.this, data.getName());
                                            PrefUtil.setDeviceAddress(BeaconActivity.this, data.getMac());
                                            PrefUtil.setDeviceUUID(BeaconActivity.this, data.getUuid());
                                            PrefUtil.setSiteId(BeaconActivity.this, siteId);
                                            LogUtil.showToast(BeaconActivity.this, "디바이스 추가 성공");
                                            mExistDevice = true;
                                            mTextSet.setText(getString(R.string.str_beacon_reset));
                                            String txt = "UUID : " + data.getUuid() + "\nSITE_ID : " + siteId;
                                            mTextInfo.setText(txt);
                                        }

                                        @Override
                                        public void onFailed() {
                                            Log.d(TAG, "addDevice - onFailed");
                                            dismissProgress();
                                            LogUtil.showToast(BeaconActivity.this, "디바이스 추가 실패");
                                        }
                                    });
                                }

                                @Override
                                public void onFailed() {
                                    Log.d(TAG, "deleteDevice - onFailed");
                                    dismissProgress();
                                    LogUtil.showToast(BeaconActivity.this, "디바이스 삭제 실패");
                                }
                            });
                        }
                        // 등록된 디바이스가 없으면 등록
                        else {
                            TaskManager.getInstance().addDevice(data.getUuid(), new TaskManager.OnAddListener() {
                                @Override
                                public void onSuccess(String siteId) {
                                    Log.d(TAG, "addDevice - onSuccess : " + siteId);
                                    dismissProgress();
                                    PrefUtil.setDeviceName(BeaconActivity.this, data.getName());
                                    PrefUtil.setDeviceAddress(BeaconActivity.this, data.getMac());
                                    PrefUtil.setDeviceUUID(BeaconActivity.this, data.getUuid());
                                    PrefUtil.setSiteId(BeaconActivity.this, siteId);
                                    LogUtil.showToast(BeaconActivity.this, "디바이스 추가 성공");
                                    mExistDevice = true;
                                    mTextSet.setText(getString(R.string.str_beacon_reset));
                                    String txt = "UUID : " + data.getUuid() + "\nSITE_ID : " + siteId;
                                    mTextInfo.setText(txt);
                                }

                                @Override
                                public void onFailed() {
                                    Log.d(TAG, "addDevice - onFailed");
                                    dismissProgress();
                                    LogUtil.showToast(BeaconActivity.this, "디바이스 추가 실패");
                                }
                            });
                        }
                    }
                }

                return true;
            }
        });

        mTextInfo = findViewById(R.id.txt_beacon_info);
        // 등록된 디바이스가 있으면 정보 표시
        if(mExistDevice) {
            String uuid = PrefUtil.getDeviceUUID(this);
            String id = PrefUtil.getSiteId(this);
            String txt = "UUID : " + uuid + "\nSITE_ID : " + id;
            mTextInfo.setText(txt);
        }
        else
            mTextInfo.setText("");

        mRecycler = findViewById(R.id.recycler_beacon);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mBeaconAdapter = new BeaconAdapter();
        mRecycler.setAdapter(mBeaconAdapter);

        mScanner = new BeaconScanner(this, new BeaconScanner.OnBleListener() {
            @Override
            public void onRetry() {
                Log.d(TAG, "OnBleListener - onRetry");
                mScanner.startScan("");
            }

            @Override
            public void onInitFailed() {
                Log.d(TAG, "OnBleListener - onInitFailed");
                Toast.makeText(BeaconActivity.this, "블루투스 준비 실패", Toast.LENGTH_SHORT).show();
                mTextScan.setEnabled(true);
            }

            @Override
            public void onAddScanResult(ScanResult result) {
                Log.d(TAG, "OnBleListener - onAddScanResult : " + result.toString());
                BeaconData data = new BeaconData();
                data.convertBeaconData(result);
                mBeaconAdapter.addItem(data);
            }

            @Override
            public void onScanFailed() {
                Log.e(TAG, "OnBleListener - onScanFailed");
                LogUtil.saveLog("D", TAG, "OnBleListener - onScanFailed");
            }
        });

        mProgressDialog = new Dialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    private void showProgress(String msg) {
        mProgressDialog.setContentView(R.layout.dialog_progress);
        TextView txt = mProgressDialog.findViewById(R.id.txt_dialog);
        txt.setText(msg);
        mProgressDialog.show();
    }

    private void dismissProgress() {
        if(mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    public class BeaconAdapter extends RecyclerView.Adapter<BeaconAdapter.MyViewHolder> {
        private List<BeaconData> mBeacons;

        private int mSelectedIndex = -1;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView textName, textAddress;
            public LinearLayout layout;

            public MyViewHolder(View v) {
                super(v);
                textName = v.findViewById(R.id.txt_beacon_name);
                textAddress = v.findViewById(R.id.txt_beacon_addr);
                layout = v.findViewById(R.id.ll_beacon);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public BeaconAdapter() {
            mBeacons = new ArrayList<>();
        }

        public int getSelectedIndex() {
            return mSelectedIndex;
        }

        public void addItem(BeaconData data) {
            // 중복 체크 - 필요 시 제거
            if (checkExist(data)) {
                Log.d(TAG, "addItem - exist : " + data.getName() + ", " + data.getMac());
            } else {
                Log.d(TAG, "addItem : " + data.getName() + ", " + data.getMac());
                mBeacons.add(data);
                notifyDataSetChanged();
            }
        }

        private boolean checkExist(BeaconData result) {
            boolean exist = false;
            BeaconData beacon;
            for (int i = 0; i < mBeacons.size(); i++) {
                beacon = mBeacons.get(i);
                if (beacon != null) {
                    if (beacon.getMac().equals(result.getMac())) {
                        exist = true;
                        break;
                    }
                }
            }

            return exist;
        }

        public void clear() {
            mBeacons.clear();
            notifyDataSetChanged();
        }

        // Create new views (invoked by the layout manager)
        @NonNull
        @Override
        public BeaconAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_beacon, parent, false);
            MyViewHolder vh = new MyViewHolder(v);

            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @SuppressLint("NewApi")
        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            String name = mBeacons.get(position).getName();
            holder.textName.setText(name == null || name.trim().length() == 0 ? "N / A" : name);
            holder.textAddress.setText(mBeacons.get(position).getMac());
            holder.layout.setBackgroundColor(mSelectedIndex == position ? Color.LTGRAY : Color.WHITE);
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "FileAdapter - onClick : " + position);
                    mSelectedIndex = position;
                    notifyDataSetChanged();
                }
            });
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mBeacons.size();
        }

        public BeaconData getSelectedBeacon() {
            return mBeacons.get(mSelectedIndex);
        }
    }
}