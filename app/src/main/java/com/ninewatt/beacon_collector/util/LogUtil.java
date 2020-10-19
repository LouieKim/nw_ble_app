package com.ninewatt.beacon_collector.util;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 데이터 로깅을 위한 클래스
 * Created by jhkim. 2020/03/xx
 */
public class LogUtil {
    private static final String TAG = "!!@@LogUtil";

    /**
     * 로깅
     *
     * @param log
     */
    public static void saveLog(String type, String tag, String log) {
        try {
            // 폴더가 존재하지 않으면 생성
            String dirMain = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "NineWattLog";
            File dir = new File(dirMain);
            if (!dir.exists() || !dir.isDirectory())
                Log.i(TAG, "saveLog - mkDir : " + dirMain + ", " + dir.mkdirs());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            Date timeInDate = new Date(System.currentTimeMillis());
            String fileName = "log_" + sdf.format(timeInDate) + ".txt";

            String path = dirMain + File.separator + fileName;
            File file = new File(path);
            Log.i(TAG, "saveLog - path : " + path + ", exist : " + file.exists());

            sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss.sss", Locale.getDefault());
            String msg = "[" + sdf.format(timeInDate) + "][" + type + "][" + tag + "] " + log;
            Log.i(TAG, "saveLog - msg : " + msg);

            // 파일이 존재하면 로깅
            if (file.exists())
                writeToFile(file, msg);
                // 파일이 존재하지 않으면 파일 생성
            else {
                boolean create = file.createNewFile();
                // 파일 생성에 성공하면 항목이름과 데이터 로깅
                if (create)
                    writeToFile(file, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "saveLog - Exception : " + e.getMessage());
        }
    }

    private synchronized static void writeToFile(File file, String text) {
        Log.d(TAG, "writeToFile : " + text);
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "writeToFile - Exception : " + e.getMessage());
        }
    }

    public static void showToast(final Context context, final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}