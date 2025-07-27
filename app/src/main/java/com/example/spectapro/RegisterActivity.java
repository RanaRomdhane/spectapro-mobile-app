package com.example.spectapro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.spectapro.databinding.ActivityRegisterBinding;
import com.example.spectapro.network.ApiService;
import com.example.spectapro.network.RetrofitClient;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int PHONE_NUMBER_LENGTH = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonRegister.setOnClickListener(v -> registerUser());
        binding.textViewLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String nom = binding.editTextNom.getText().toString().trim();
        String prenom = binding.editTextPrenom.getText().toString().trim();
        String telephone = binding.editTextTelephone.getText().toString().trim();
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();
        String confirmPassword = binding.editTextConfirmPassword.getText().toString().trim();

        if (!validateInputs(nom, prenom, telephone, email, password, confirmPassword)) {
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<ApiService.RegisterResponse> call = apiService.register(
                nom,
                prenom,
                telephone,
                email,
                password
        );

        call.enqueue(new Callback<ApiService.RegisterResponse>() {
            @Override
            public void onResponse(Call<ApiService.RegisterResponse> call, Response<ApiService.RegisterResponse> response) {
                try {
                    if (response.isSuccessful()) {
                        ApiService.RegisterResponse body = response.body();
                        if (body != null) {
                            if (body.getIdclt() != null || "success".equals(body.getStatus())) {
                                // Inscription réussie
                                showToast("Inscription réussie !");
                                startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                finish();
                            } else if (body.getError() != null) {
                                // Erreur spécifique du serveur
                                showToast(body.getError());
                            } else {
                                // Réponse inattendue mais réussie
                                Log.w("API_WARNING", "Réponse 200 mais structure inattendue: " + response.body());
                                showToast("Inscription réussie (réponse serveur inattendue)");
                                startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                finish();
                            }
                        } else {
                            // Réponse 200 mais body null
                            Log.d("API_DEBUG", "Réponse 200 avec body null - vérifiez la base de données");
                            showToast("Inscription semble réussie (vérifiez votre email)");
                            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                            finish();
                        }
                    } else {
                        // Erreur HTTP (4xx/5xx)
                        String errorMsg = response.errorBody() != null ?
                                response.errorBody().string() : "Erreur sans message";
                        showToast("Erreur serveur: " + errorMsg);
                        Log.e("API_ERROR", "Code: " + response.code() + " - " + errorMsg);
                    }
                } catch (IOException e) {
                    Log.e("API_EXCEPTION", "Erreur de traitement", e);
                    showToast("Erreur de traitement de la réponse");
                }
            }

            @Override
            public void onFailure(Call<ApiService.RegisterResponse> call, Throwable t) {
                showToast("Erreur réseau: " + t.getMessage());
                Log.e("NETWORK_ERROR", "Échec de la requête", t);
            }
        });
    }

    private boolean validateInputs(String nom, String prenom, String telephone,
                                   String email, String password, String confirmPassword) {
        if (nom.isEmpty()) {
            showToast("Le nom est requis");
            return false;
        }
        if (prenom.isEmpty()) {
            showToast("Le prénom est requis");
            return false;
        }
        if (telephone.length() != PHONE_NUMBER_LENGTH || !telephone.matches("\\d+")) {
            showToast("Téléphone invalide (10 chiffres requis)");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Email invalide");
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            showToast("Mot de passe trop court (min " + MIN_PASSWORD_LENGTH + " caractères)");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showToast("Les mots de passe ne correspondent pas");
            return false;
        }
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}