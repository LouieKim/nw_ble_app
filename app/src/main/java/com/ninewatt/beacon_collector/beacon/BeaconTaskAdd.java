package com.ninewatt.beacon_collector.beacon;

import android.os.AsyncTask;
import android.util.Log;

import com.ninewatt.beacon_collector.util.LogUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jhkim. 2020/03/xx
 * 수집된 BLE 데이터를 측위서버에 보내고 위치데이터를 받기 위한 클래스
 */
public class BeaconTaskAdd extends AsyncTask<Void, Void, String> {
    private OnSendListener mListener;
    private String mUuid;

    private final String TAG = "!!@@BeaconTaskAdd";

    /**
     * 결과를 전달하기 위한 리스너
     */
    public interface OnSendListener {
        void onFinishedSending(String msg);
    }

    /**
     * 클래스 생성자
     *
     * @param uuid String
     * @param l
     */
    public BeaconTaskAdd(String uuid, OnSendListener l) {
        Log.i(TAG, "BeaconTaskAdd - uuid : " + uuid);
        mUuid = uuid;
        mListener = l;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("http://14.63.163.204:5000/site/add/" + mUuid);
                // URLConnection 생성
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(5000);

                // 전송할 데이터를 JSON 형식으로 변환
//                JSONObject body = new JSONObject();
//                body.put("mac_addr", Statics.INFO_MAC);
//                JSONArray jArray = new JSONArray();
//                BleData data;
//                JSONObject subObject;
//                for (int i = 0; i < mBles.size(); i++) {
//                    data = mBles.get(i);
//                    subObject = new JSONObject();
//                    subObject.put("uuid", data.getUuid());
//                    subObject.put("major", data.getMajor());
//                    subObject.put("minor", data.getMinor());
//                    subObject.put("accuracy", data.getAccuracy());
//                    jArray.put(subObject);
//                }
//
//                body.put("ble_list", jArray);
//                Log.i(TAG, "doInBackground - body : " + body.toString());
//
//                OutputStream os = urlConnection.getOutputStream();
//                os.write(body.toString().getBytes());
//                os.flush();
//                os.close();

                urlConnection.connect();
                Log.i(TAG, "doInBackground - response : " + urlConnection.getResponseCode());
                // Response Code 가 200인 경우 데이터 확인.
                if (urlConnection.getResponseCode() == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = br.readLine()) != null) {
                        response.append(inputLine);
                    }

                    br.close();
                    return response.toString();
                }
                else
                    return null;
            } catch (Exception e) {
                Log.e(TAG, "doInBackground - Exception 2 : " + e.getMessage());
                // If the code didn't successfully get the weather data, there's no point in attemping to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "doInBackground - Exception 1 : " + e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.i(TAG, "onPostExecute : " + result);
        mListener.onFinishedSending(result);
    }
}