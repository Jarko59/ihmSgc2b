package com.example.ihmproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPhone, etName, etPassword, etConfirmPassword;
    private Button btnRegister;

    private static final String API_URL = "https://sgc2b-projet.000webhostapp.com/inscriptionA.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String phone = etPhone.getText().toString();
                String name = etName.getText().toString();
                String password = etPassword.getText().toString();
                String confirmPassword = etConfirmPassword.getText().toString();

                // Vérifier si les champs sont vides
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) ||
                        TextUtils.isEmpty(name) || TextUtils.isEmpty(password) ||
                        TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Vérifier si les mots de passe correspondent
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Vérifier si l'adresse e-mail est valide
                if (!isValidEmail(email)) {
                    Toast.makeText(RegisterActivity.this, "Adresse e-mail invalide", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Effectuer l'inscription
                performRegistration(email, phone, name, password, confirmPassword);
            }
        });
    }

    private void performRegistration(String email, String phone, String name, String password, String confirmPassword) {
        new RegistrationTask().execute(email, phone, name, password, confirmPassword);
    }

    private class RegistrationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            String phone = params[1];
            String name = params[2];
            String password = params[3];
            String confirmPassword = params[4];

            try {
                // Construction des paramètres x-www-form-urlencoded
                String postData = "email=" + URLEncoder.encode(email, "UTF-8") +
                        "&phone=" + URLEncoder.encode(phone, "UTF-8") +
                        "&name=" + URLEncoder.encode(name, "UTF-8") +
                        "&password=" + URLEncoder.encode(password, "UTF-8") +
                        "&confirmPassword=" + URLEncoder.encode(confirmPassword, "UTF-8");

                // Création de la connexion HTTP
                URL url = new URL(API_URL);
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
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Fermeture de la connexion
                urlConnection.disconnect();

                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        // Inscription réussie, afficher un message et rediriger vers LoginActivity
                        Toast.makeText(RegisterActivity.this, "Inscription réussie", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish(); // Fermer l'activité actuelle pour empêcher de revenir à cette page avec le bouton retour
                    } else {
                        // L'inscription a échoué, afficher un message d'erreur
                        String errorMessage = jsonResponse.getString("error_message");
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                // Erreur de connexion ou réponse vide
                Toast.makeText(RegisterActivity.this, "Une erreur s'est produite", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
