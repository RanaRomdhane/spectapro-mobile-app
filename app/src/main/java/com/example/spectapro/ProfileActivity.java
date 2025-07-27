package com.example.spectapro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.spectapro.model.Billet;
import com.example.spectapro.model.Client;
import com.example.spectapro.network.ApiService;
import com.example.spectapro.network.RetrofitClient;
import com.example.spectapro.storage.BilletManager;
import com.example.spectapro.storage.FavoriteManager;
import com.example.spectapro.storage.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private TextView profileName, profileEmail, profilePhone, memberSince;
    private TextView tvFavoritesCount, tvTicketsCount;
    private Client currentClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        checkUserAuthentication();
        displayProfileInfo();
        setupButtons();
        setupBottomNavigation();
        loadCounters();
    }

    private void initializeViews() {
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profilePhone = findViewById(R.id.profilePhone);
        memberSince = findViewById(R.id.memberSince);
        tvFavoritesCount = findViewById(R.id.favoritesCount);
        tvTicketsCount = findViewById(R.id.ticketsCount);
    }

    private void checkUserAuthentication() {
        currentClient = SharedPrefManager.getInstance(this).getClient();
        if (currentClient == null) {
            redirectToLogin();
        }
    }

    private void displayProfileInfo() {
        if (currentClient == null) return;

        profileName.setText(currentClient.getFullName() != null ?
                currentClient.getFullName() : "Non renseigné");
        profileEmail.setText(currentClient.getEmail() != null ?
                currentClient.getEmail() : "Non renseigné");
        profilePhone.setText(currentClient.getTel() != null ?
                currentClient.getTel() : "Non renseigné");
        memberSince.setText(currentClient.getDateInscription() != null ?
                currentClient.getDateInscription() : "2025");
    }

    private void loadCounters() {
        // Compteur des favoris (local)
        FavoriteManager favoriteManager = FavoriteManager.getInstance(this);
        int favoriteCount = favoriteManager.getFavorites().size();
        tvFavoritesCount.setText(String.valueOf(favoriteCount));

        // Afficher d'abord le compte local des billets
        updateLocalTicketCount();

        // Synchroniser avec l'API si connecté
        if (currentClient != null && currentClient.getIdclt() != null) {
            fetchTicketCountFromApi();
        }
    }

    private void updateLocalTicketCount() {
        int localCount = BilletManager.getInstance(this).getBilletCount();
        tvTicketsCount.setText(String.valueOf(localCount));
        Log.d(TAG, "Local tickets count: " + localCount);
    }

    private void fetchTicketCountFromApi() {
        Log.d(TAG, "Fetching ticket count from API");
        ApiService apiService = RetrofitClient.getApiService();
        Call<Integer> call = apiService.getTicketCount(currentClient.getIdclt());

        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int apiCount = response.body();
                    Log.d(TAG, "API tickets count: " + apiCount);

                    // Mettre à jour le stockage local si nécessaire
                    BilletManager billetManager = BilletManager.getInstance(ProfileActivity.this);
                    int localCount = billetManager.getBilletCount();

                    if (apiCount != localCount) {
                        Log.d(TAG, "Synchronizing tickets: API=" + apiCount + ", Local=" + localCount);
                        // Si l'API a plus de billets, on doit les récupérer
                        if (apiCount > localCount) {
                            fetchClientBilletsFromApi();
                        }
                        // Si l'API a moins de billets, on synchronise le local
                        else {
                            billetManager.setBilletCount(apiCount);
                            updateLocalTicketCount();
                        }
                    }
                } else {
                    Log.e(TAG, "API error: " + response.code());
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                // On garde l'affichage local en cas d'erreur réseau
            }
        });
    }

    private void fetchClientBilletsFromApi() {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Billet>> call = apiService.getTicketsByClient(currentClient.getIdclt());
        call.enqueue(new Callback<List<Billet>>() {
            @Override
            public void onResponse(Call<List<Billet>> call, Response<List<Billet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Billet> billets = response.body();
                    // Mettre à jour le stockage local
                    BilletManager.getInstance(ProfileActivity.this).syncBillets(billets);
                    // Mettre à jour l'affichage
                    updateLocalTicketCount();
                    Log.d(TAG, "Synced " + billets.size() + " tickets from API");
                } else {
                    Log.e(TAG, "Failed to fetch tickets from API");
                }
            }

            @Override
            public void onFailure(Call<List<Billet>> call, Throwable t) {
                Log.e(TAG, "Network error fetching tickets", t);
            }
        });
    }

    private void handleApiError(Response<Integer> response) {
        try {
            String error = response.errorBody() != null ?
                    response.errorBody().string() : "Unknown error";
            Log.e(TAG, "API error: " + response.code() + " - " + error);
        } catch (Exception e) {
            Log.e(TAG, "Error processing API error", e);
        }
    }

    private void setupButtons() {
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
        findViewById(R.id.btnHelp).setOnClickListener(v -> {
            startActivity(new Intent(this, HelpActivity.class));
        });
    }

    private void logout() {
        SharedPrefManager.getInstance(this).clear();
        BilletManager.getInstance(this).clear();
        redirectToLogin();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationUtils.setupBottomNavigation(this, bottomNav, R.id.nav_profile);
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCounters();
    }

    public static void refreshBillets(Context context, List<Billet> newBillets) {
        BilletManager manager = BilletManager.getInstance(context);
        manager.syncBillets(newBillets);
        Log.d("ProfileActivity", "Billets synchronisés: " + newBillets.size());
    }
}