package com.fr3ts0n.ecu.gui.androbd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.view.MenuItem;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private TextView registerLink;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);

        // Initialize SessionManager
        session = new SessionManager(getApplicationContext());

        // Handle login button click
        loginButton.setOnClickListener(v -> {
            String email = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        // Handle register link click
        registerLink.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://137.184.18.27"));
            startActivity(browserIntent);
        });
    }

    private void loginUser(String email, String password) {
        String urlString = "http://137.184.18.27/api.php/auth.php?action=login";

        new Thread(() -> {
            try {
                // Create URL and open connection
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Create POST data
                String postData = "email=" + email + "&password=" + password;

                // Write POST data to output stream
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(postData.getBytes());
                outputStream.flush();
                outputStream.close();

                // Read response
                InputStream inputStream = connection.getInputStream();
                String response = new java.util.Scanner(inputStream).useDelimiter("\\A").next();
                inputStream.close();

                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(response);
                String status = jsonResponse.getString("status");

                if (status.equals("success")) {
                    // Extract user data
                    int userId = jsonResponse.getInt("user_id");
                    String role = jsonResponse.getString("role");

                    // Store in session
                    session.createLoginSession(email, userId, role);

                    // Navigate to MainActivity
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    // Handle error response
                    String message = jsonResponse.getString("message");
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "An error occurred", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
