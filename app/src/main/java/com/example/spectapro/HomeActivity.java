package com.example.spectapro;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.spectapro.adapter.EventAdapter;
import com.example.spectapro.model.Spectacle;
import com.example.spectapro.network.ApiService;
import com.example.spectapro.network.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements EventAdapter.OnItemClickListener {
    private RecyclerView eventsRecyclerView;
    private EventAdapter eventAdapter;
    private List<Spectacle> spectacleList = new ArrayList<>();
    private SearchView searchView;
    private BottomNavigationView bottomNavigationView;
    private String currentSearchQuery = "";
    private String currentDateFilter = "";
    private String currentLocationFilter = "";
    private String currentCategoryFilter = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationUtils.setupBottomNavigation(this, bottomNav, R.id.nav_home);
        setupRecyclerView();
        loadAllSpectacles();
        setupSearchView();
        setupFilterButtons();
    }

    private void initViews() {
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        searchView = findViewById(R.id.searchBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(spectacleList, this, this);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsRecyclerView.setAdapter(eventAdapter);
    }

    @Override
    public void onItemClick(Spectacle spectacle) {
        if (spectacle == null) {
            Toast.makeText(this, "Spectacle non disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("spectacle", (Parcelable) spectacle);
        startActivity(intent);
    }

    @Override
    public void onReservationClick(Spectacle spectacle) {
        // Cette méthode n'est plus utilisée car nous avons remplacé le bouton Réserver
        // Mais nous la gardons pour la compatibilité
        onItemClick(spectacle);
    }

    private void loadAllSpectacles() {
        Call<List<Spectacle>> call = RetrofitClient.getApiService().getAllSpectacles();

        call.enqueue(new Callback<List<Spectacle>>() {
            @Override
            public void onResponse(Call<List<Spectacle>> call, Response<List<Spectacle>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    spectacleList.clear();
                    spectacleList.addAll(response.body());
                    eventAdapter.updateList(spectacleList);
                    Log.d("API_RESPONSE", "Données reçues: " + response.body().size() + " éléments");
                } else {
                    Log.e("API_ERROR", "Code: " + response.code() + " - " + response.message());
                    Toast.makeText(HomeActivity.this, "Erreur lors du chargement des spectacles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Spectacle>> call, Throwable t) {
                Log.e("API_FAILURE", "Erreur: " + t.getMessage());
                Toast.makeText(HomeActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query;
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                applyFilters();
                return true;
            }
        });
    }

    private void setupFilterButtons() {
        findViewById(R.id.btnDate).setOnClickListener(v -> showDatePickerDialog());
        findViewById(R.id.btnLocation).setOnClickListener(v -> showLocationDialog());
        findViewById(R.id.btnCategory).setOnClickListener(v -> showCategoryDialog());
        findViewById(R.id.btnReset).setOnClickListener(v -> resetFilters());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            currentDateFilter = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day);
            applyFilters();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showLocationDialog() {
        String[] cities = {"Tunis", "Nabeul", "Sousse", "Monastir"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Choisir une ville")
                .setItems(cities, (dialog, which) -> {
                    currentLocationFilter = cities[which];
                    applyFilters();
                })
                .show();
    }

    private void showCategoryDialog() {
        String[] categories = {"Concert", "Théâtre", "Magic"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Choisir une catégorie")
                .setItems(categories, (dialog, which) -> {
                    currentCategoryFilter = categories[which];
                    applyFilters();
                })
                .show();
    }

    private void resetFilters() {
        currentSearchQuery = "";
        currentDateFilter = "";
        currentLocationFilter = "";
        currentCategoryFilter = "";
        searchView.setQuery("", false);
        applyFilters();
    }

    private void applyFilters() {
        List<Spectacle> filteredList = new ArrayList<>();

        for (Spectacle spectacle : spectacleList) {
            boolean matches = true;

            if (!currentSearchQuery.isEmpty()) {
                String title = spectacle.getTitre() != null ? spectacle.getTitre().toLowerCase() : "";
                matches = title.contains(currentSearchQuery.toLowerCase());
            }

            if (matches && !currentDateFilter.isEmpty() && spectacle.getDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String spectacleDate = sdf.format(spectacle.getDate());
                matches = currentDateFilter.equals(spectacleDate);
            }

            if (matches && !currentLocationFilter.isEmpty()) {
                matches = spectacle.getLieu() != null &&
                        spectacle.getLieu().getVille() != null &&
                        currentLocationFilter.equalsIgnoreCase(spectacle.getLieu().getVille());
            }

            if (matches && !currentCategoryFilter.isEmpty()) {
                // Implémentez la logique de catégorie si nécessaire
            }

            if (matches) {
                filteredList.add(spectacle);
            }
        }

        eventAdapter.updateList(filteredList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllSpectacles();
    }
}