package com.example.ihmproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton, registerButton;

    private static final String LOGIN_URL = "https://sgc2b-projet.000webhostapp.com/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.email_input);
        loginButton = findViewById(R.id.login_btn);
        registerButton = findViewById(R.id.register_btn);
        passwordInput = findViewById(R.id.password_input);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    loginUser(email, password);
                } else {
                    Toast.makeText(LoginActivity.this, "Veuillez saisir un email et un mot de passe", Toast.LENGTH_SHORT).show();
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loginUser(String email, String password) {
        // Construction des paramètres x-www-form-urlencoded
        String postData = "email=" + email +
                "&password=" + password;

        // Exécution de la tâche de connexion
        new LoginTask().execute(LOGIN_URL, postData);
    }

    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String apiUrl = params[0];
            String postData = params[1];
            String response = "";

            try {
                // Création de la connexion HTTP
                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setDoOutput(true);

                // Écriture des données dans le flux de sortie
                OutputStream os = urlConnection.getOutputStream();
                os.write(postData.getBytes("UTF-8"));
                os.flush();
                os.close();

                // Lecture de la réponse de l'API
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                response = stringBuilder.toString();

                // Fermeture de la connexion
                urlConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        // Connexion réussie, rediriger vers MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Fermer l'activité actuelle pour empêcher de revenir à cette page avec le bouton retour
                    } else {
                        // La connexion a échoué, afficher un message d'erreur
                        String errorMessage = jsonResponse.getString("error_message");
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                // Erreur de connexion/réponse vide
                Toast.makeText(LoginActivity.this, "Une erreur s'est produite", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
