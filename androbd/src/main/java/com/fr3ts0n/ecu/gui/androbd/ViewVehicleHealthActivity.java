package com.fr3ts0n.ecu.gui.androbd;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewVehicleHealthActivity extends AppCompatActivity {

    private static final Logger log = Logger.getLogger(ViewVehicleHealthActivity.class.getName());

    private RecyclerView recyclerView;
    private VehicleHealthAdapter adapter;
    private List<Vehicle> carList = new ArrayList<>();
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_vehicle_health);

        userId = getSharedPreferences("AppSession", MODE_PRIVATE).getInt("UserId", -1);

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
                    log.log(Level.SEVERE, "Error fetching vehicles", e);
                    return null;
                }
            }

            protected void onPostExecute(String result) {
                carList.clear();
                if (result != null) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        if (obj.has("vehicles")) {
                            JSONArray vehicles = obj.getJSONArray("vehicles");
                            for (int i = 0; i < vehicles.length(); i++) {
                                JSONObject v = vehicles.getJSONObject(i);
                                carList.add(new Vehicle(v.getInt("vehicle_id"), v.getString("vin")));
                            }
                            adapter.notifyDataSetChanged();
                        } else if (obj.has("message")) {
                            Toast.makeText(ViewVehicleHealthActivity.this, obj.getString("message"), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Error parsing vehicle data", e);
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
                    log.log(Level.SEVERE, "Error fetching vehicle health", e);
                    return null;
                }
            }

            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        // Log the raw JSON response
                        log.info("Vehicle Health Response: " + result);

                        // Parse the JSON array directly
                        JSONArray predictions = new JSONArray(result);

                        // Prepare data
                        List<Double> rulList = new ArrayList<>();
                        List<Float> carHealthList = new ArrayList<>();
                        List<Integer> dtcList = new ArrayList<>();
                        String mostRecentState = "";

                        for (int i = 0; i < predictions.length(); i++) {
                            JSONObject prediction = predictions.getJSONObject(i);

                            // Extract data
                            String carHealth = prediction.getString("car_health");
                            int predictedDtc = prediction.getInt("predicted_dtc");
                            double rul = (prediction.getDouble("remaining_useful_life"))/24;

                            // Log parsed values for debugging
                            log.info("Parsed Data - Car Health: " + carHealth + ", Predicted DTC: " + predictedDtc
                                    + ", RUL: " + rul);

                            // Map car health to float (1 = Normal, 0 = Issue)
                            float healthValue = carHealth.equalsIgnoreCase("Normal") ? 1f : 0f;

                            // Add to lists
                            rulList.add(rul);
                            carHealthList.add(healthValue);
                            dtcList.add(predictedDtc);

                            // Set most recent state
                            if (i == 0) {
                                mostRecentState = "Health: " + carHealth + ", DTC: " + mapDtc(predictedDtc) + ", RUL: "
                                        + rul;
                            }
                        }

                        // Log before launching the details activity
                        log.info("Most Recent State: " + mostRecentState);
                        log.info("RUL List: " + rulList);
                        log.info("Car Health List: " + carHealthList);
                        log.info("DTC List: " + dtcList);

                        log.info("Launching VehicleHealthDetailsActivity with Intent...");
                        Intent intent = new Intent(ViewVehicleHealthActivity.this, VehicleHealthDetailsActivity.class);
                        intent.putExtra("mostRecentState", mostRecentState);
                        intent.putExtra("rulList", new ArrayList<>(rulList));
                        intent.putExtra("carHealthList", new ArrayList<>(carHealthList));
                        intent.putExtra("dtcList", new ArrayList<>(dtcList));
                        intent.putExtra("vin", vin);
                        startActivity(intent);

                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Error parsing health data", e);
                        Toast.makeText(ViewVehicleHealthActivity.this, "Error parsing health data", Toast.LENGTH_SHORT)
                                .show();
                    }
                } else {
                    Toast.makeText(ViewVehicleHealthActivity.this,
                            "No readings have been taken for this vehicle. Take a reading to view the health of the vehicle",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private String mapDtc(int dtc) {
        switch (dtc) {
            case 0:
                return "P0133";
            case 1:
                return "C0300";
            case 2:
                return "P0079P2004P3000";
            case 3:
                return "P0078U1004P3000";
            case 4:
                return "P0079C1004P3000";
            case 5:
                return "P007EP2036P18F0";
            case 6:
                return "P007EP2036P18D0";
            case 7:
                return "P007FP2036P18D0";
            case 8:
                return "P0079P1004P3000";
            case 9:
                return "P007EP2036P18E0";
            case 10:
                return "P007FP2036P18E0";
            case 11:
                return "P0078B0004P3000";
            case 12:
                return "P007FP2036P18F0";
            case 13:
                return "Normal";
            default:
                return "Unknown";
        }
    }
}