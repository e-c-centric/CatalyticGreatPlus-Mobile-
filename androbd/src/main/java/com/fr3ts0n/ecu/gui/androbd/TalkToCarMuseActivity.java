package com.fr3ts0n.ecu.gui.androbd;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import io.noties.markwon.Markwon;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TalkToCarMuseActivity extends AppCompatActivity {
    private static final Logger log = Logger.getLogger(TalkToCarMuseActivity.class.getName());
    private TextView tvAnalysis;
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk_to_carmuse);

        tvAnalysis = findViewById(R.id.tvAnalysis);
        String vin = getIntent().getStringExtra("vin");
        String question = "Tell me about my vehicle with vin of " + vin;

        markwon = Markwon.builder(this).build();

        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... _voids) {
                try {
                    URL url = new URL("http://137.184.18.27/util.php/cai.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    String postData = "question=" + URLEncoder.encode(question, "UTF-8");
                    try (BufferedWriter w = new BufferedWriter(
                            new OutputStreamWriter(conn.getOutputStream(), "UTF-8"))) {
                        w.write(postData);
                    }

                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        for (String line; (line = r.readLine()) != null;)
                            sb.append(line);
                    }
                    return sb.toString();
                } catch (Exception x) {
                    log.log(Level.SEVERE, "fetch analysis", x);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String json) {
                if (json != null) {
                    try {
                        String markdown = new JSONObject(json).getString("analysis");
                        // Render markdown into tvAnalysis
                        markwon.setMarkdown(tvAnalysis, markdown);
                    } catch (Exception x) {
                        Toast.makeText(TalkToCarMuseActivity.this,
                                "Parse error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TalkToCarMuseActivity.this,
                            "Network error", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
}