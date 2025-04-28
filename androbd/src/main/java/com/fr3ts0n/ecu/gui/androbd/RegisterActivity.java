package com.fr3ts0n.ecu.gui.androbd;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (!username.isEmpty() && !password.isEmpty()) {
                registerUser(username, password);
            } else {
                Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser(String username, String password) {
        String urlString = "https://your-server.com/api/register.php"; // Your PHP register endpoint

        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                // Create POST data
                String postData = "username=" + username + "&password=" + password;

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(postData.getBytes());
                outputStream.flush();

                InputStream inputStream = connection.getInputStream();
                String response = new java.util.Scanner(inputStream).useDelimiter("\\A").next();

                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.getString("status").equals("success")) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
