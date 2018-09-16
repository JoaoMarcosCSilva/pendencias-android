package com.cardoso.joao.pendencies;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.User;
import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;



public class MainActivity extends AppCompatActivity {

    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";

    LinearLayout main_layout;
    LinearLayout initial_layout;
    Button pendentes_button, feitas_button;
    CalendarView calendar_picker;
    Switch switcher;
    GoogleAccountCredential mCredential;
    boolean pendentes;

    String timetext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         configureUI();


    }

    void configureUI() {
        initial_layout = findViewById(R.id.starting_layout);
        main_layout = findViewById(R.id.main_layout);
        pendentes_button = findViewById(R.id.pendentes);
        feitas_button = findViewById(R.id.feitas);

        pendentes_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pendentes = true;
                startProtocol();
            }
        });

        feitas_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pendentes = false;
                startProtocol();
            }
        });

    }

    void startProtocol() {

        initial_layout.setVisibility(View.GONE);

        try {

            Resultados results;
            new RequestTask().execute();

        }catch (Exception e){

        }

    }

    void publishResults(final Resultados resultados) {
        if (resultados == null)return;

        Set<Map.Entry<String, ArrayList<Integer>>> entry_set;
        if (pendentes) {
            entry_set = resultados.notDone.entrySet();
        } else {
            entry_set = resultados.done.entrySet();
        }

        for (Map.Entry<String, ArrayList<Integer>> entry : entry_set) {
            String result_string = "";

            for (int i : entry.getValue()) {
                result_string += resultados.activities.get(i).name;
                result_string += "\n";
            }


            LinearLayout layout = new LinearLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            layout.setLayoutParams(lp);
            layout.setOrientation(LinearLayout.VERTICAL);

            final TextView content = new TextView(this);
            content.setText(result_string);
            content.setLayoutParams(lp);
            content.setClickable(true);
            content.setVisibility(View.GONE);


            TextView title = new TextView(this);
            title.setText(entry.getKey());
            title.setLayoutParams(lp);
            title.setClickable(true);
            title.setTextSize(20);
            title.setPadding(0, 3, 0, 40);

            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));

            spacer.setBackgroundColor(Color.parseColor("#424242"));


            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (content.getVisibility() == View.VISIBLE) {
                        content.setVisibility(View.GONE);
                    } else {
                        content.setVisibility(View.VISIBLE);
                    }
                }
            };

            title.setOnClickListener(listener);
            content.setOnClickListener(listener);

            layout.addView(title);
            layout.addView(content);
            layout.addView(spacer);
            main_layout.addView(layout);
        }

    }

    void printLabel(String text) {
        TextView txt = new TextView(this);
        txt.setText(text);
        txt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        main_layout.addView(txt);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case 1002:
                if (resultCode != RESULT_OK) {
                    printLabel(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    startProtocol();
                }
                break;
            case 1000:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("accountName", accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        startProtocol();
                    }
                }
                break;
            case 1001:
                if (resultCode == RESULT_OK) {
                    startProtocol();
                }
                break;
        }
    }

    private class RequestTask extends AsyncTask<Void,Void,Resultados> {
        String data;
        boolean worked = false;
        RequestTask() {

        }

        protected String getCloudContent(){
            try {
                String data = "";


                URL url = new URL("http://serverdrive.pythonanywhere.com");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("GET");
                http.connect();
                int statusCode = http.getResponseCode();

                if (statusCode != 200) {
                    data = "ERROR: SERVER RETURNED " + statusCode;
                    return data;
                }

                InputStream is = url.openStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                String line;
                while((line = br.readLine()) != null){
                    data += line;
                }
                worked = true;
                return data;
            } catch (Exception e) {
                worked = false;
                return "ERROR: " + e.getMessage();
            }
        }

        @Override
        protected Resultados doInBackground(Void... params) {
            String json_data = getCloudContent();
            Gson gson = new Gson();
            if(worked){
                Resultados results;
                results = gson.fromJson(json_data, Resultados.class);
                return results;
            }else{
                printLabel(json_data);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Resultados results) {
            publishResults(results);
        }

    }
}

