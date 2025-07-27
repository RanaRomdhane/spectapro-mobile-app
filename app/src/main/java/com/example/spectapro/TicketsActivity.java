package com.example.spectapro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.spectapro.adapter.TicketAdapter;
import com.example.spectapro.model.Billet;
import com.example.spectapro.model.Client;
import com.example.spectapro.network.ApiService;
import com.example.spectapro.network.RetrofitClient;
import com.example.spectapro.storage.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketsActivity extends AppCompatActivity implements TicketAdapter.OnTicketClickListener {
    private static final String TAG = "TicketsActivity";
    private RecyclerView recyclerView;
    private TicketAdapter adapter;
    private TextView emptyView;
    private Client currentClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets);

        initializeViews();
        checkUserAuthentication();
        setupBottomNavigation();
        setupRecyclerView();
        loadUserTickets();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.ticketsRecyclerView);
        emptyView = findViewById(R.id.emptyView);
    }

    private void checkUserAuthentication() {
        currentClient = SharedPrefManager.getInstance(this).getClient();
        if (currentClient == null) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationUtils.setupBottomNavigation(this, bottomNav, R.id.nav_tickets);
    }

    private void setupRecyclerView() {
        adapter = new TicketAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadUserTickets() {
        if (currentClient == null || currentClient.getIdclt() == null) {
            Log.e(TAG, "Client ID is null, refreshing client data");
            refreshClientData();
            return;
        }

        Log.d(TAG, "Loading tickets for client ID: " + currentClient.getIdclt());
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Billet>> call = apiService.getTicketsByClient(currentClient.getIdclt());

        call.enqueue(new Callback<List<Billet>>() {
            @Override
            public void onResponse(Call<List<Billet>> call, Response<List<Billet>> response) {
                if (response.isSuccessful()) {
                    handleSuccessfulResponse(response);
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<List<Billet>> call, Throwable t) {
                handleFailure(t);
            }
        });
    }

    private void handleSuccessfulResponse(Response<List<Billet>> response) {
        List<Billet> tickets = response.body();
        if (tickets != null && !tickets.isEmpty()) {
            Log.d(TAG, "Tickets loaded: " + tickets.size());
            adapter.updateList(tickets);
            updateEmptyView(false);
        } else {
            Log.d(TAG, "No tickets found");
            updateEmptyView(true);
            Toast.makeText(TicketsActivity.this,
                    "Aucun billet trouvé", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleErrorResponse(Response<List<Billet>> response) {
        try {
            String error = response.errorBody() != null ?
                    response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Server error: " + response.code() + " - " + error);

            if (response.code() == 404) {
                Toast.makeText(TicketsActivity.this,
                        "Endpoint API introuvable. Contactez le support.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(TicketsActivity.this,
                        "Erreur serveur: " + response.code(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error response", e);
            Toast.makeText(TicketsActivity.this,
                    "Erreur de traitement", Toast.LENGTH_SHORT).show();
        }
        updateEmptyView(true);
    }

    private void handleFailure(Throwable t) {
        Log.e(TAG, "Network error: " + t.getMessage());
        Toast.makeText(TicketsActivity.this,
                "Erreur réseau: " + t.getMessage(), Toast.LENGTH_LONG).show();
        updateEmptyView(true);
    }

    private void refreshClientData() {
        Client cachedClient = SharedPrefManager.getInstance(this).getClient();
        if (cachedClient == null || cachedClient.getEmail() == null) {
            redirectToLogin();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<Client> call = apiService.getClientByEmail(cachedClient.getEmail());

        call.enqueue(new Callback<Client>() {
            @Override
            public void onResponse(Call<Client> call, Response<Client> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SharedPrefManager.getInstance(TicketsActivity.this).saveClient(response.body());
                    loadUserTickets();
                } else {
                    redirectToLogin();
                }
            }

            @Override
            public void onFailure(Call<Client> call, Throwable t) {
                redirectToLogin();
            }
        });
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Session expirée, veuillez vous reconnecter", Toast.LENGTH_SHORT).show();
        SharedPrefManager.getInstance(this).clear();
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    private void updateEmptyView(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onTicketClick(Billet billet) {
        if (billet != null && billet.getSpectacle() != null) {
            Intent intent = new Intent(this, TicketDetailActivity.class);
            intent.putExtra("billet", billet);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Détails du billet indisponibles", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les billets quand l'activité reprend au premier plan
        loadUserTickets();
    }
}