package com.fr3ts0n.ecu.gui.androbd;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;  // ← add this
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class VehicleHealthDetailsActivity extends AppCompatActivity {

    private static final Logger log = Logger.getLogger(VehicleHealthDetailsActivity.class.getName());

    private TextView tvMostRecentState;
    private LineChart chartRUL, chartDTC;
    private BarChart chartCarHealth;
    private Button btnTalkToCarMuse;   // ← add this field

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_health_details);

        try {
            // Initialize views
            tvMostRecentState = findViewById(R.id.tvMostRecentState);
            chartRUL = findViewById(R.id.chartRUL);
            chartCarHealth = findViewById(R.id.chartCarHealth);
            chartDTC = findViewById(R.id.chartDTC);

            // Get data from intent
            String mostRecentState = getIntent().getStringExtra("mostRecentState");
            List<Double> rulList = (List<Double>) getIntent().getSerializableExtra("rulList");
            List<Float> carHealthList = (List<Float>) getIntent().getSerializableExtra("carHealthList");
            List<Integer> dtcList = (List<Integer>) getIntent().getSerializableExtra("dtcList");

            // Log the received data for debugging
            log.info("Most Recent State: " + mostRecentState);
            log.info("RUL List: " + rulList);
            log.info("Car Health List: " + carHealthList);
            log.info("DTC List: " + dtcList);

            final String vin = getIntent().getStringExtra("vin");
            btnTalkToCarMuse = findViewById(R.id.btnTalkToCarMuse);
            btnTalkToCarMuse.setOnClickListener(v -> {
                Intent i = new Intent(this, TalkToCarMuseActivity.class);
                i.putExtra("vin", vin);
                startActivity(i);
            });

            // Display most recent state
            if (mostRecentState != null) {
                String[] stateParts = mostRecentState.split(", ");
                StringBuilder formattedState = new StringBuilder();
                for (String part : stateParts) {
                    if (part.contains("Normal")) {
                        formattedState.append("<font color='green'>").append(part).append("</font><br>");
                    } else {
                        formattedState.append("<font color='red'>").append(part).append("</font><br>");
                    }
                }
                tvMostRecentState.setText(android.text.Html.fromHtml(formattedState.toString()));
            } else {
                tvMostRecentState.setText("No data available");
            }

            // Populate charts
            if (rulList != null) {
                populateRULChart(chartRUL, rulList, "Remaining Useful Life", "RUL (Days)");
            }
            if (carHealthList != null) {
                populateCarHealthBarChart(chartCarHealth, carHealthList, "Car Health (1=Normal, 0=Issue)");
            }
            if (dtcList != null) {
                populateDTCLineChart(chartDTC, dtcList, "Predicted DTC");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in VehicleHealthDetailsActivity onCreate", e);
        }
    }

    private void populateCarHealthBarChart(BarChart chart, List<Float> dataList, String label) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            entries.add(new BarEntry(i, dataList.get(dataList.size() - 1 - i))); // Reverse the data
        }

        BarDataSet dataSet = new BarDataSet(entries, label);

        // Dynamically set colors based on value
        List<Integer> colors = new ArrayList<>();
        for (BarEntry entry : entries) {
            if (entry.getY() == 1f) {
                colors.add(getResources().getColor(android.R.color.holo_green_dark)); // Green for 1
            } else {
                colors.add(getResources().getColor(android.R.color.holo_red_dark)); // Red for 0
            }
        }
        dataSet.setColors(colors);

        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setDrawValues(false); // Disable value labels for cleaner display
        chart.setData(barData);

        // Customize chart appearance
        chart.getDescription().setEnabled(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setGranularity(1f);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(1f);
        chart.getAxisLeft().setGranularity(1f);
        chart.getAxisLeft().setLabelCount(2, true);
        chart.getAxisRight().setEnabled(false);

        chart.invalidate(); // Refresh chart
    }

    private void populateDTCLineChart(LineChart chart, List<Integer> dataList, String label) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            entries.add(new Entry(i, dataList.get(dataList.size() - 1 - i))); // Reverse the data
        }

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(getResources().getColor(android.R.color.holo_orange_dark));
        dataSet.setCircleColor(getResources().getColor(android.R.color.holo_orange_dark));
        dataSet.setCircleRadius(4f);
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Customize chart appearance
        chart.getDescription().setEnabled(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setGranularity(1f);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(13f);
        chart.getAxisLeft().setGranularity(1f);
        chart.getAxisLeft().setLabelCount(14, true);
        chart.getAxisRight().setEnabled(false);
        chart.invalidate(); // Refresh chart
    }

    private void populateRULChart(LineChart chart, List<Double> dataList, String label, String description) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            entries.add(new Entry(i, dataList.get(dataList.size() - 1 - i).floatValue())); // Reverse the data
        }

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        dataSet.setCircleColor(getResources().getColor(android.R.color.holo_blue_dark));
        dataSet.setCircleRadius(4f);
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Customize chart appearance
        chart.getDescription().setText(description);
        chart.getDescription().setTextSize(12f);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false);
        chart.invalidate(); // Refresh chart
    }

    private void populateChart(LineChart chart, List<?> dataList, String label, String description, int colorResId,
            boolean isDiscrete) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            float value;
            if (dataList.get(i) instanceof Double) {
                value = ((Double) dataList.get(i)).floatValue();
            } else if (dataList.get(i) instanceof Integer) {
                value = ((Integer) dataList.get(i)).floatValue();
            } else if (dataList.get(i) instanceof Float) {
                value = (Float) dataList.get(i);
            } else {
                throw new IllegalArgumentException(
                        "Unsupported data type in dataList: " + dataList.get(i).getClass().getName());
            }
            entries.add(new Entry(i, value));
        }

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(getResources().getColor(colorResId));
        dataSet.setCircleColor(getResources().getColor(colorResId));
        dataSet.setCircleRadius(4f);
        dataSet.setLineWidth(2f);

        // Remove value labels for cleaner charts
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Customize chart appearance
        chart.getDescription().setText(description);
        chart.getDescription().setTextSize(12f);
        chart.getXAxis().setDrawGridLines(true);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false);
        chart.setDrawBorders(true);
        chart.setBorderColor(getResources().getColor(android.R.color.darker_gray));
        chart.setBorderWidth(1f);

        // Customize y-axis for discrete values
        if (isDiscrete) {
            chart.getAxisLeft().setGranularity(1f); // Force integer steps
            chart.getAxisLeft().setGranularityEnabled(true);
            chart.getAxisLeft().setLabelCount(14, true); // For DTC (0-13)
        }

        chart.invalidate(); // Refresh chart
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources if needed
    }
}