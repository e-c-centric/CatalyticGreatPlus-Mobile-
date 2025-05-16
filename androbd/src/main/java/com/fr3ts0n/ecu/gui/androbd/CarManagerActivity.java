package com.fr3ts0n.ecu.gui.androbd;

import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.fr3ts0n.ecu.gui.androbd.SessionManager;
import com.fr3ts0n.ecu.gui.androbd.Vehicle;

public class CarManagerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private List<Vehicle> carList = new ArrayList<>();
    private SharedPreferences prefs;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_manager);

        prefs = getSharedPreferences("AppSession", MODE_PRIVATE);
        userId = prefs.getInt("UserId", -1);

        recyclerView = findViewById(R.id.recyclerViewCars);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        carAdapter = new CarAdapter(carList, new CarAdapter.OnCarActionListener() {
            @Override
            public void onUseVehicle(int position) {
                Vehicle v = carList.get(position);
                prefs.edit()
                        .putInt("CurrentVehicleId", v.vehicle_id)
                        .putString("CurrentVehicleVin", v.vin)
                        .apply();
                Toast.makeText(CarManagerActivity.this, "Selected: " + v.vin, Toast.LENGTH_SHORT).show();

                // Navigate to MainActivity
                Intent intent = new Intent(CarManagerActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onRemoveCar(int position) {
                // TODO: Implement remove endpoint if available
                Toast.makeText(CarManagerActivity.this, "Feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(carAdapter);

        FloatingActionButton fab = findViewById(R.id.fabAddCar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCarDialog();
            }
        });

        fetchVehicles();
    }

    private void fetchVehicles() {
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL("http://137.184.18.27/api.php/fetch.php");
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
                            JSONArray arr = obj.getJSONArray("vehicles");
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject v = arr.getJSONObject(i);
                                carList.add(new Vehicle(v.getInt("vehicle_id"), v.getString("vin")));
                            }
                            carAdapter.notifyDataSetChanged();
                        } else if (obj.has("message")) {
                            Toast.makeText(CarManagerActivity.this, obj.getString("message"), Toast.LENGTH_SHORT)
                                    .show();
                            carAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        Toast.makeText(CarManagerActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CarManagerActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void showAddCarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter License Plate");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String licensePlate = input.getText().toString().trim();
                if (!licensePlate.isEmpty()) {
                    addVehicle(licensePlate);
                } else {
                    Toast.makeText(CarManagerActivity.this, "License plate cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void addVehicle(final String vin) {
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL("http://137.184.18.27/api.php/add.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    String postData = "user_id=" + URLEncoder.encode(String.valueOf(userId), "UTF-8")
                            + "&vin=" + URLEncoder.encode(vin, "UTF-8");
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
                Toast.makeText(CarManagerActivity.this, "UserId: " + userId, Toast.LENGTH_LONG).show();
                if (result != null) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        Toast.makeText(CarManagerActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        fetchVehicles();
                    } catch (Exception e) {
                        Toast.makeText(CarManagerActivity.this, "Error parsing add response", Toast.LENGTH_SHORT)
                                .show();
                    }
                } else {
                    Toast.makeText(CarManagerActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
}