package com.example.gpstracking;


import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {
    EditText UsernameEt, PasswordEt;
    Button Login;
    SwitchCompat active;
    ProgressBar loginProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);

        UsernameEt = findViewById(R.id.username);
        PasswordEt = findViewById(R.id.password);
        Login = (Button) findViewById(R.id.login);
        active = (SwitchCompat) findViewById(R.id.active);
        loginProgressBar = (ProgressBar) findViewById(R.id.progressBar_login);

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = UsernameEt.getText().toString().trim();
                String password = PasswordEt.getText().toString().trim();

                if (validateInput(username, password)) {
                    new LoginTask(LoginActivity.this).execute(username, password);
                }
            }
        });
    }



    private boolean validateInput(String username, String password) {
        if (username.isEmpty()) {
            UsernameEt.setError("Username is required");
            loginProgressBar.setVisibility(View.GONE);
            Login.setVisibility(View.VISIBLE);
            UsernameEt.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            PasswordEt.setError("Password is required");
            loginProgressBar.setVisibility(View.GONE);
            Login.setVisibility(View.VISIBLE);
            PasswordEt.requestFocus();
            return false;
        }
        return true;
    }

    // Inner AsyncTask class for performing login operation
    private class LoginTask extends AsyncTask<String, Void, String> {
        private final Context context;
        private String user_name, password;

        LoginTask(Context ctx) {
            this.context = ctx;
            // Toast.makeText(context, "Welcome", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
            //  Toast.makeText(context, "Login Status", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(@NonNull String... params) {
            user_name = params[0];
            password = params[1];

            //  String serverUrl = "http://192.168.11.78/SwitchBoardApp/login.php";
           // String serverUrl = "http://apps.technicalhub.io:5002/aapp/logintest.php";
            String serverUrl = "http://apps.technicalhub.io:5002/tracking/app/login.php";

            try {
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Send data to the server
                String postData = "user_name=" + URLEncoder.encode(user_name, "UTF-8") +
                        "&password=" + URLEncoder.encode(password, "UTF-8");
                OutputStream os = connection.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                // Get the server response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Return the server response
                return response.toString();

            } catch (IOException e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }


        @Override
        protected void onPostExecute(String result) {
            switch (result) {
                case "login_success_user":
                    // Save the login status and role for user
                    saveLoginData("user", user_name);
                    startActivity(new Intent(LoginActivity.this, UserActivity.class));
                    break;
                case "login_success_admin":
                    // Save the login status and role for admin
                    saveLoginData("admin", user_name);
                    startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                    break;
                case "login failed":
                    Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        private void saveLoginData(String role, String user_name) {
            if (active.isChecked()) {
                // Save login status
                preferences.setDataLogin(LoginActivity.this, true);
            } else {
                // Clear login status
                preferences.clearData(LoginActivity.this);
            }

            // Save the role and username in SharedPreferences
            SharedPreferences user_sp = getSharedPreferences("user_sp", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor_user_sp = user_sp.edit();
            editor_user_sp.putString("ROLE", role);  // Save the role
            editor_user_sp.putString("USER", user_name);
            editor_user_sp.apply();

            // Save additional user data if needed (optional)
            SharedPreferences name_sp = getSharedPreferences("name_sp", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor_name_sp = name_sp.edit();
            editor_name_sp.putString("NAME", user_name);
            editor_name_sp.apply();

            SharedPreferences mail_sp = getSharedPreferences("mail_sp", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor_mail_sp = mail_sp.edit();
            editor_mail_sp.putString("MAIL", user_name);
            editor_mail_sp.apply();
        }
    }

    private void moreMethod(String username) {
        if (active.isChecked()) {
            // Save login status when the switch is active
            preferences.setDataLogin(LoginActivity.this, true);
        } else {
            // Clear login status if the switch is not active
            preferences.clearData(LoginActivity.this);
        }


        SharedPreferences user_sp = getSharedPreferences("user_sp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor_user_sp = user_sp.edit();
        editor_user_sp.putString("USER", username);
        editor_user_sp.apply();

        SharedPreferences name_sp = getSharedPreferences("name_sp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor_name_sp = name_sp.edit();

        editor_name_sp.putString("NAME", username);
        editor_name_sp.apply();

        SharedPreferences mail_sp = getSharedPreferences("mail_sp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor_mail_sp = mail_sp.edit();
        editor_mail_sp.putString("MAIL", username);
        editor_mail_sp.apply();

        finish();  // Call finish() once after saving all data
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (preferences.getDataLogin(this)) {
            // Retrieve the role from SharedPreferences
            SharedPreferences user_sp = getSharedPreferences("user_sp", Context.MODE_PRIVATE);
            String role = user_sp.getString("ROLE", "user");  // Default to "user" if role is not found

            // Navigate based on the role
            if ("admin".equals(role)) {
                // Navigate to the Admin activity
                startActivity(new Intent(this, AdminActivity.class));
            } else {
                // Navigate to the User activity
                startActivity(new Intent(this, UserActivity.class));
            }

            finish();  // Close the current activity
        }
    }


}