package com.ninewatt.beacon_collector;

import android.util.Log;

import com.ninewatt.beacon_collector.beacon.BeaconTaskAdd;
import com.ninewatt.beacon_collector.beacon.BeaconTaskDay;
import com.ninewatt.beacon_collector.beacon.BeaconTaskDelete;
import com.ninewatt.beacon_collector.beacon.BeaconTaskMonth;
import com.ninewatt.beacon_collector.beacon.BeaconTaskSend;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private static TaskManager mInstance;

    public static TaskManager getInstance() {
        return (mInstance != null ? mInstance : (mInstance = new TaskManager()));
    }

    private BeaconTaskAdd mAddTask;
    private BeaconTaskDelete mDelTask;
    private BeaconTaskSend mSendTask;
    private BeaconTaskMonth mMonthDataTask;
    private BeaconTaskDay mDayDataTask;

    private TaskManager() {

    }

    public interface OnAddListener {
        void onSuccess(String siteId);
        void onFailed();
    }
    public void addDevice(String uuid, final OnAddListener l) {
        if(mAddTask != null) {
            mAddTask.cancel(true);
            mAddTask = null;
        }

        mAddTask = new BeaconTaskAdd(uuid.toUpperCase(), new BeaconTaskAdd.OnSendListener() {
            @Override
            public void onFinishedSending(String msg) {
                if(msg == null)
                    l.onFailed();
                else {
                    try {
                        JSONObject obj = new JSONObject(msg);
                        String id = obj.getString("site_id");
                        l.onSuccess(id);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                mAddTask = null;
            }
        });
        mAddTask.execute();
    }

    public interface OnDeleteListener {
        void onSuccess();
        void onFailed();
    }
    public void deleteDevice(String siteId, final OnDeleteListener l) {
        if(mDelTask != null) {
            mDelTask.cancel(true);
            mDelTask = null;
        }

        mDelTask = new BeaconTaskDelete(siteId, new BeaconTaskDelete.OnSendListener() {
            @Override
            public void onFinishedSending(String msg) {
                if(msg == null)
                    l.onFailed();
                else
                    l.onSuccess();

                mDelTask = null;
            }
        });
        mDelTask.execute();
    }

    public interface OnSendListener {
        void onSuccess(String data);
        void onFailed();
    }
    public void sendData(String siteId, String data, final OnSendListener l) {
        if(mSendTask != null) {
            mSendTask.cancel(true);
            mSendTask = null;
        }

        mSendTask = new BeaconTaskSend(siteId, data, new BeaconTaskSend.OnSendListener() {
            @Override
            public void onFinishedSending(String msg) {
                if(msg == null)
                    l.onFailed();
                else {
                    try {
                        JSONObject obj = new JSONObject(msg);
                        String data = obj.getString("success");
                        l.onSuccess(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                mSendTask = null;
            }
        });
        mSendTask.execute();
    }

    public interface OnDataListener {
        void onSuccess(List<String[]> data);
        void onFailed();
    }
    public void getMonthData(String siteId, String date, final OnDataListener l) {
        if(mMonthDataTask != null) {
            mMonthDataTask.cancel(true);
            mMonthDataTask = null;
        }

        mMonthDataTask = new BeaconTaskMonth(siteId, date, new BeaconTaskMonth.OnSendListener() {
            @Override
            public void onFinishedSending(String msg) {
                if(msg == null)
                    l.onFailed();
                else {
                    try {
                        JSONObject obj = new JSONObject(msg);
                        JSONArray arr = obj.getJSONArray("month_history");
                        JSONArray arr1;
                        List<String[]> datas = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            arr1 = arr.getJSONArray(i);
                            Log.i("!!@@TaskManager", "BeaconTaskMonth - arr1 : " + arr1.get(0) + ", " + arr1.get(1));
                            datas.add(new String[]{getMonth(arr1.get(0).toString()), arr1.get(1).toString()});
                        }
                        l.onSuccess(datas);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                mMonthDataTask = null;
            }
        });
        mMonthDataTask.execute();
    }
    public void getDayData(String siteId, String date, final OnDataListener l) {
        if(mDayDataTask != null) {
            mDayDataTask.cancel(true);
            mDayDataTask = null;
        }

        mDayDataTask = new BeaconTaskDay(siteId, date, new BeaconTaskDay.OnSendListener() {
            @Override
            public void onFinishedSending(String msg) {
                if(msg == null)
                    l.onFailed();
                else {
                    try {
                        JSONObject obj = new JSONObject(msg);
                        JSONArray arr = obj.getJSONArray("day_history");
                        JSONArray arr1;
                        List<String[]> datas = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            arr1 = arr.getJSONArray(i);
                            Log.i("!!@@TaskManager", "BeaconTaskDay - arr1 : " + arr1.get(0) + ", " + arr1.get(1));
                            datas.add(new String[]{getDay(arr1.get(0).toString()), arr1.get(1).toString()});
                        }
                        l.onSuccess(datas);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                mDayDataTask = null;
            }
        });
        mDayDataTask.execute();
    }
    private String getMonth(String time) {
        //2020-07-01 00:00:00
        String tmp = time.split(" ")[0];
        String year = tmp.split("-")[0];
        String month = tmp.split("-")[1];
        String day = tmp.split("-")[2];
        return (year.substring(2, 4) + "." + month);
    }
    private String getDay(String time) {
        String tmp = time.split(" ")[0];
        String year = tmp.split("-")[0];
        String month = tmp.split("-")[1];
        String day = tmp.split("-")[2];
        return day;
    }
}