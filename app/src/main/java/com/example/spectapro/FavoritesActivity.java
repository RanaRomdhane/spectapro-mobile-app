package com.example.spectapro;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.spectapro.adapter.FavoritesAdapter;
import com.example.spectapro.model.Spectacle;
import com.example.spectapro.storage.FavoriteManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FavoritesActivity extends AppCompatActivity implements FavoritesAdapter.OnItemClickListener {
    private RecyclerView favoritesRecyclerView;
    private TextView emptyView;
    private FavoritesAdapter adapter;
    private FavoriteManager favoriteManager;
    private SearchView searchView;
    private BottomNavigationView bottomNavigationView;
    private String currentSearchQuery = "";
    private String currentDateFilter = "";
    private String currentLocationFilter = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        favoriteManager = FavoriteManager.getInstance(this);
        initViews();
        setupNavigation();
        setupRecyclerView();
        setupSearchView();
        setupFilterButtons();
        loadFavorites();
    }

    private void initViews() {
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        emptyView = findViewById(R.id.emptyView);
        searchView = findViewById(R.id.searchBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupNavigation() {
        NavigationUtils.setupBottomNavigation(this, bottomNavigationView, R.id.nav_favorites);
    }

    private void setupRecyclerView() {
        adapter = new FavoritesAdapter(this, this);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoritesRecyclerView.setAdapter(adapter);
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

    private void resetFilters() {
        currentSearchQuery = "";
        currentDateFilter = "";
        currentLocationFilter = "";
        searchView.setQuery("", false);
        applyFilters();
    }

    private void loadFavorites() {
        List<Spectacle> favorites = favoriteManager.getFavorites();
        adapter.setFavorites(favorites);
        applyFilters();
        updateEmptyView();
    }

    private void applyFilters() {
        List<Spectacle> favorites = favoriteManager.getFavorites();
        List<Spectacle> filteredList = new ArrayList<>();

        for (Spectacle spectacle : favorites) {
            boolean matches = true;

            if (!currentSearchQuery.isEmpty()) {
                matches = spectacle.getTitre() != null &&
                        spectacle.getTitre().toLowerCase().contains(currentSearchQuery.toLowerCase());
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

            if (matches) {
                filteredList.add(spectacle);
            }
        }

        adapter.setFavorites(filteredList);
        updateEmptyView();
    }

    public void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            favoritesRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            favoritesRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }
}