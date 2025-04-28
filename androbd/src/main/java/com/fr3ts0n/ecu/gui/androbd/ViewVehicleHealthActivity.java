package com.fr3ts0n.ecu.gui.androbd;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity to view vehicle health information
 * 
 * @author Elikem and Pascal
 */
public class ViewVehicleHealthActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VehicleHealthAdapter adapter;
    private List<Vehicle> carList = new ArrayList<>();
    private SharedPreferences prefs;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_vehicle_health);

        prefs = getSharedPreferences("AppSession", MODE_PRIVATE);
        userId = prefs.getInt("UserId", -1);

        Toast.makeText(this, "UserId: " + userId, Toast.LENGTH_LONG).show();

        recyclerView = findViewById(R.id.recyclerViewVehicleHealth);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VehicleHealthAdapter(carList, new VehicleHealthAdapter.OnHealthClickListener() {
            @Override
            public void onViewHealth(int position) {
                Vehicle v = carList.get(position);
                fetchVehicleHealth(v.vehicle_id, v.vin);
            }
        });
        recyclerView.setAdapter(adapter);

        fetchVehicles();
    }

    private void fetchVehicles() {
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL("http://137.184.208.235/api.php/fetch.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    String postData = "user_id=" + URLEncoder.encode(String.valueOf(userId), "UTF-8");
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(postData);
                    writer.flush();
                    writer.close();
                    os.close();

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();
                    return response.toString();
                } catch (Exception e) {
                    return null;
                }
            }

            protected void onPostExecute(String result) {
                carList.clear();
                if (result != null) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        if (obj.has("vehicles")) {
                            for (int i = 0; i < obj.getJSONArray("vehicles").length(); i++) {
                                JSONObject v = obj.getJSONArray("vehicles").getJSONObject(i);
                                carList.add(new Vehicle(v.getInt("vehicle_id"), v.getString("vin")));
                            }
                            adapter.notifyDataSetChanged();
                        } else if (obj.has("message")) {
                            Toast.makeText(ViewVehicleHealthActivity.this, obj.getString("message"), Toast.LENGTH_SHORT)
                                    .show();
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        Toast.makeText(ViewVehicleHealthActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ViewVehicleHealthActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void fetchVehicleHealth(final int vehicleId, final String vin) {
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... voids) {
                try {
                    // Replace with your actual endpoint for ML prediction
                    URL url = new URL("http://137.184.208.235/api.php/vehicle_health.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    String postData = "vehicle_id=" + URLEncoder.encode(String.valueOf(vehicleId), "UTF-8");
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(postData);
                    writer.flush();
                    writer.close();
                    os.close();

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();
                    return response.toString();
                } catch (Exception e) {
                    return null;
                }
            }

            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        String health = obj.optString("car_health", "N/A");
                        String dtc = obj.optString("predicted_dtc", "N/A");
                        String rul = obj.optString("remaining_useful_life", "N/A");

                        String msg = "Car Health: " + health +
                                "\nPredicted DTC: " + dtc +
                                "\nRemaining Useful Life: " + rul;

                        new AlertDialog.Builder(ViewVehicleHealthActivity.this)
                                .setTitle("Vehicle Health for " + vin)
                                .setMessage(msg)
                                .setPositiveButton("OK", null)
                                .show();
                    } catch (Exception e) {
                        Toast.makeText(ViewVehicleHealthActivity.this, "Error parsing health data", Toast.LENGTH_SHORT)
                                .show();
                    }
                } else {
                    Toast.makeText(ViewVehicleHealthActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
}