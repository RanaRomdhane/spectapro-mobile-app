package com.example.spectapro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.spectapro.databinding.ActivityMainBinding;
import com.example.spectapro.model.Client;
import com.example.spectapro.network.ApiService;
import com.example.spectapro.network.RetrofitClient;
import com.example.spectapro.storage.SharedPrefManager;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getApiService();

        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            navigateToHome();
        }

        binding.buttonLogin.setOnClickListener(v -> loginUser());
        binding.textViewSignUpLink.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void loginUser() {
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();

        // Validation des entrées
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email et mot de passe requis", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Format d'email invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Désactiver le bouton pendant la requête
        binding.buttonLogin.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        // Appel API
        apiService.login(email, password).enqueue(new Callback<Client>() {
            @Override
            public void onResponse(Call<Client> call, Response<Client> response) {
                binding.buttonLogin.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Client client = response.body();
                    if (client != null) {
                        // Debug: Vérifier les données reçues
                        Log.d("Login", "Client reçu: " + client.getEmail() + ", " + client.getFullName());

                        // Sauvegarde du client
                        SharedPrefManager.getInstance(MainActivity.this).saveClient(client);
                        navigateToHome();
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Réponse serveur invalide", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String error = response.errorBody() != null ?
                                response.errorBody().string() : "Erreur inconnue";
                        Log.e("Login", "Erreur serveur: " + error);
                        Toast.makeText(MainActivity.this,
                                "Authentification échouée", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Client> call, Throwable t) {
                binding.buttonLogin.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                Log.e("Login", "Erreur réseau", t);
                Toast.makeText(MainActivity.this,
                        "Erreur de connexion: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}