package com.ninewatt.beacon_collector;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.ninewatt.beacon_collector.util.MyAxisValueFormatter;
import com.ninewatt.beacon_collector.util.PrefUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    private Spinner mSpinnerYear, mSpinnerMonth;
    private TextView mTextMonth, mTextDay;
    private RecyclerView mRecycler;
    private Dialog mProgressDialog;
    private HistoryAdapter mHistoryAdapter;

    private String[] mYears;
    private final String[] mMonths = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

    private String mYear, mMonth, mTab1, mTab2;

    private final String TAG = "!!@@HistoryActivity";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mYears = getYears();

        mSpinnerYear = findViewById(R.id.spinner_year);
        mSpinnerYear.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, mYears));
        mSpinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object obj = mSpinnerYear.getSelectedItem();
                try {
                    if (obj != null) {
                        mYear = obj.toString();
                        Log.i(TAG, "mSpinnerMonth - year : " + mYear);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpinnerMonth = findViewById(R.id.spinner_month);
        mSpinnerMonth.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, mMonths));
        mSpinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object obj = mSpinnerMonth.getSelectedItem();
                try {
                    if (obj != null) {
                        mMonth = obj.toString();
                        Log.i(TAG, "mSpinnerMonth - month : " + mMonth);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mTextMonth = findViewById(R.id.txt_history_month);
        mTextMonth.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(0.5f);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setAlpha(1.f);
                    mTab1 = "월"; // recycler title 1
                    showMonthlyData();
                }

                return true;
            }
        });

        mTextDay = findViewById(R.id.txt_history_day);
        mTextDay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(0.5f);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setAlpha(1.f);
                    mTab1 = "일"; // recycler title 1
                    showDailyData();
                }

                return true;
            }
        });

        mRecycler = findViewById(R.id.recycler_history);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mHistoryAdapter = new HistoryAdapter();
        mRecycler.setAdapter(mHistoryAdapter);

        mProgressDialog = new Dialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        mTab2 = "사용량(ton)"; // recycler title 2
        initChart();
    }

    // 현재 날짜를 기준으로 2000년도까지
    private String[] getYears() {
        List<String> years = new ArrayList<>();
        int currYear = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date()));
        Log.i(TAG, "getYears - curr : " + currYear);
        for (int i = currYear; i > 1999; i--) {
            years.add(String.valueOf(i));
        }

        String[] result = new String[years.size()];
        years.toArray(result);
        Log.i(TAG, "getYears : " + Arrays.toString(result));

        return result;
    }

    public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {
        private List<String[]> mDatas;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView textTime1, textValue1, textTime2, textValue2;
            public LinearLayout ll;

            public MyViewHolder(View v) {
                super(v);
                textTime1 = v.findViewById(R.id.txt_history_time1);
                textValue1 = v.findViewById(R.id.txt_history_value1);
                textTime2 = v.findViewById(R.id.txt_history_time2);
                textValue2 = v.findViewById(R.id.txt_history_value2);
                ll = v.findViewById(R.id.ll_history);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public HistoryAdapter() {
            mDatas = new ArrayList<>();
        }

        public void setItem(List<String[]> datas) {
            Log.d(TAG, "setItem : " + datas.size());
            mDatas.clear();
            mDatas.addAll(datas);
            notifyDataSetChanged();
        }

        public void clear() {
            mDatas.clear();
            notifyDataSetChanged();
        }

        // Create new views (invoked by the layout manager)
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_history, parent, false);
            MyViewHolder vh = new MyViewHolder(v);

            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @SuppressLint("NewApi")
        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            if (position == 0) {
                holder.ll.setBackgroundColor(Color.TRANSPARENT);
                holder.textTime1.setText(mTab1);
                holder.textTime1.setTypeface(null, Typeface.BOLD);
                holder.textValue1.setText(mTab2);
                holder.textValue1.setTypeface(null, Typeface.BOLD);
                holder.textTime2.setText(mTab1);
                holder.textTime2.setTypeface(null, Typeface.BOLD);
                holder.textValue2.setText(mTab2);
                holder.textValue2.setTypeface(null, Typeface.BOLD);
            } else {
                String time1 = mDatas.get(position - 1)[0];
                String val1 = mDatas.get(position - 1)[1];
                int index = mDatas.size() / 2 + mDatas.size() % 2 + position - 1; // 리스트를 2열로 표시하기 위해
                String time2 = index < mDatas.size() ? mDatas.get(index)[0] : ""; // 항목이 홀수인 경우 나머지 값은 공백
                String val2 = index < mDatas.size() ? mDatas.get(index)[1] : ""; // 항목이 홀수인 경우 나머지 값은 공백
                Log.i(TAG, "HistoryAdapter - " + position + " : " + time1 + ", " + val1 + ", " + time2 + ", " + val2);

                holder.ll.setBackgroundColor(position % 2 == 0 ? Color.TRANSPARENT : Color.LTGRAY);

                holder.textTime1.setText(time1);
                holder.textTime1.setTypeface(null, Typeface.NORMAL);
                holder.textValue1.setText(val1);
                holder.textValue1.setTypeface(null, Typeface.NORMAL);
                holder.textTime2.setText(time2);
                holder.textTime2.setTypeface(null, Typeface.NORMAL);
                holder.textValue2.setText(val2);
                holder.textValue2.setTypeface(null, Typeface.NORMAL);
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDatas.size() / 2 + mDatas.size() % 2 + 1;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Chart ////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    private BarChart chart;
    private static List<String> mBarEntryLabels = new ArrayList<>();
    // chart 초기화 - MPChart Git 참조 (https://github.com/PhilJay/MPAndroidChart)
    private void initChart() {
        chart = findViewById(R.id.barchart_history);
        chart.setOnChartValueSelectedListener(null);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.getDescription().setEnabled(false);
        // if more than 60 entries are displayed in the chart, no values will be drawn
        chart.setMaxVisibleValueCount(31);
        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);
        // chart.setDrawYLabels(false);
        chart.setNoDataTextColor(Color.RED);

        IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(chart);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setTypeface(tfLight);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(7f);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setValueFormatter(xAxisFormatter);

        IAxisValueFormatter custom = new MyAxisValueFormatter();

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawLabels(false);
//        leftAxis.setTypeface(tfLight);
//        leftAxis.setLabelCount(8, false);
//        leftAxis.setValueFormatter(custom);
//        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
//        leftAxis.setSpaceTop(15f);
//        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
//        rightAxis.setTypeface(tfLight);
        rightAxis.setLabelCount(8, false);
        rightAxis.setValueFormatter(custom);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.EMPTY);
        l.setFormSize(9f);
        l.setTextSize(7f);
        l.setXEntrySpace(4f);
    }

    // chart 데이터 설정
    @SuppressLint("UseCompatLoadingForDrawables")
    private void setData(List<String[]> data) {
        ArrayList<BarEntry> values = new ArrayList<>();
        String[] tmp;
        mBarEntryLabels.clear();
        for (int i = 0; i < data.size(); i++) {
            tmp = data.get(i);
            Log.i(TAG, "setData - " + i + " : " + Arrays.toString(tmp));
            mBarEntryLabels.add(tmp[0]);
            values.add(new BarEntry(i, Float.parseFloat(tmp[1]), getResources().getDrawable(R.mipmap.ic_launcher_round)));
        }

        BarDataSet set1;
        set1 = new BarDataSet(values, "");
        set1.setDrawIcons(false);
        set1.setColor(ContextCompat.getColor(this, android.R.color.holo_blue_light));

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData bar = new BarData(dataSets);
        bar.setValueTextSize(10f);
        bar.setBarWidth(0.9f);
        chart.setData(bar);

        chart.getXAxis().setLabelCount(data.size());
        chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    // chart X축 label 설정
    private static class DayAxisValueFormatter implements IAxisValueFormatter {
        private final BarLineChartBase<?> chart;

        public DayAxisValueFormatter(BarLineChartBase<?> chart) {
            this.chart = chart;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            Log.i("!!@@HA", "getFormattedValue : " + (int) value);
            int index = (int) value;// / 2 + (int)value % 2;
            if (index < mBarEntryLabels.size())
                return (index % 2 == 1 ? "\n" : "") +
                        mBarEntryLabels.get((int) ((BarDataSet) chart.getData().getDataSetByIndex(0)).getEntryForIndex(index).getX());
            else
                return "";
        }
    }

    // 일간 차트 표시
    private void showDailyData() {
        showProgress("데이터 확인 중...");
        String siteId = PrefUtil.getSiteId(this);
        TaskManager.getInstance().getDayData(siteId, makeDate(), new TaskManager.OnDataListener() {
            @Override
            public void onSuccess(final List<String[]> data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mHistoryAdapter.setItem(data);
                        mTextDay.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                        mTextMonth.setBackgroundColor(Color.DKGRAY);
                        setData(data);
                        dismissProgress();
                    }
                });
            }

            @Override
            public void onFailed() {
                dismissProgress();
            }
        });
    }

    // 월간 차트 표시
    private void showMonthlyData() {
        showProgress("데이터 확인 중...");
        String siteId = PrefUtil.getSiteId(this);
        TaskManager.getInstance().getMonthData(siteId, makeDate(), new TaskManager.OnDataListener() {
            @Override
            public void onSuccess(final List<String[]> data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mHistoryAdapter.setItem(data);
                        mTextDay.setBackgroundColor(Color.DKGRAY);
                        mTextMonth.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                        setData(data);
                        dismissProgress();
                    }
                });
            }

            @Override
            public void onFailed() {
                dismissProgress();
            }
        });
    }

    // 월간차트 날짜 생성
    private String makeDate() {
        String year = mYear.substring(mYear.length() - 2, mYear.length());
        String month = (mMonth.length() == 1 ? "0" + mMonth : mMonth);
        Log.i(TAG, "makeDate - year : " + year + ", month : " + month);

        return (year + month);
    }

    private void showProgress(String msg) {
        mProgressDialog.setContentView(R.layout.dialog_progress);
        TextView txt = mProgressDialog.findViewById(R.id.txt_dialog);
        txt.setText(msg);
        mProgressDialog.show();
    }

    private void dismissProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}