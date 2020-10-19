package com.ninewatt.beacon_collector.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtil {
    private static final String KEY_NAME = "ninewatt";
    private static final String KEY_DEVICE_NAME = "key_device_name";            // 등록된 디바이스 Name
    private static final String KEY_DEVICE_ADDRESS = "key_device_mac";          // 등록된 디바이스 Mac Address
    private static final String KEY_DEVICE_UUID = "key_device_uuid";            // 등록된 디바이스 UUID
    private static final String KEY_SITE_ID = "key_site_id";                    // 등록된 Site ID
    private static final String KEY_OPTION_INTERVAL = "key_option_interval";    // 설정 - 알람 주기
    private static final String KEY_OPTION_SCAN = "key_option_scan";            // 설정 - 스캔 시간

    public static final String DEFAULT_SITE_ID = "00000000";

    public static void setDeviceName(Context context, String name) {
        SharedPreferences.Editor edit = context.getSharedPreferences(KEY_NAME, 0).edit();
        edit.putString(KEY_DEVICE_NAME, name);
        edit.apply();
    }

    public static String getDeviceName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(KEY_NAME, 0);
        return pref.getString(KEY_DEVICE_NAME, "");
    }

    public static void setDeviceAddress(Context context, String addr) {
        SharedPreferences.Editor edit = context.getSharedPreferences(KEY_NAME, 0).edit();
        edit.putString(KEY_DEVICE_ADDRESS, addr);
        edit.apply();
    }

    public static String getDeviceAddress(Context context) {
        SharedPreferences pref = context.getSharedPreferences(KEY_NAME, 0);
        return pref.getString(KEY_DEVICE_ADDRESS, "");
    }

    public static void setDeviceUUID(Context context, String uuid) {
        SharedPreferences.Editor edit = context.getSharedPreferences(KEY_NAME, 0).edit();
        edit.putString(KEY_DEVICE_UUID, uuid);
        edit.apply();
    }

    public static String getDeviceUUID(Context context) {
        SharedPreferences pref = context.getSharedPreferences(KEY_NAME, 0);
        return pref.getString(KEY_DEVICE_UUID, "");
    }

    public static void setSiteId(Context context, String id) {
        SharedPreferences.Editor edit = context.getSharedPreferences(KEY_NAME, 0).edit();
        edit.putString(KEY_SITE_ID, id);
        edit.apply();
    }

    public static String getSiteId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(KEY_NAME, 0);
        return pref.getString(KEY_SITE_ID, DEFAULT_SITE_ID);
    }

    public static boolean hasDevice(Context context) {
        String uuid = getDeviceUUID(context);
        return (uuid.trim().length() > 0 && !uuid.equals(DEFAULT_SITE_ID));
    }

    public static void setInterval(Context context, int interval) {
        SharedPreferences.Editor edit = context.getSharedPreferences(KEY_NAME, 0).edit();
        edit.putInt(KEY_OPTION_INTERVAL, interval);
        edit.apply();
    }

    public static int getInterval(Context context) {
        SharedPreferences pref = context.getSharedPreferences(KEY_NAME, 0);
        return pref.getInt(KEY_OPTION_INTERVAL, 60);
    }

    public static void setScanTime(Context context, int time) {
        SharedPreferences.Editor edit = context.getSharedPreferences(KEY_NAME, 0).edit();
        edit.putInt(KEY_OPTION_SCAN, time);
        edit.apply();
    }

    public static int getScanTime(Context context) {
        SharedPreferences pref = context.getSharedPreferences(KEY_NAME, 0);
        return pref.getInt(KEY_OPTION_SCAN, 20);
    }
}