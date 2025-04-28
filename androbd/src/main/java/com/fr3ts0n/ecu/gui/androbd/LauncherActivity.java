package com.fr3ts0n.ecu.gui.androbd;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SessionManager
        SessionManager session = new SessionManager(this);

        // Check if the user is logged in
        if (session.isLoggedIn()) {
            // Navigate to MainActivity
            Intent intent = new Intent(this, CarManagerActivity.class);
            startActivity(intent);
        } else {
            // Navigate to LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        finish();
    }
}