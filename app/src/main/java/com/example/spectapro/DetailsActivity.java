package com.example.spectapro;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.spectapro.model.Lieu;
import com.example.spectapro.model.Spectacle;
import com.example.spectapro.network.RetrofitClient;
import com.example.spectapro.storage.FavoriteManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity {
    private Spectacle spectacle;
    private FavoriteManager favoriteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        favoriteManager = FavoriteManager.getInstance(this);
        spectacle = getIntent().getParcelableExtra("spectacle");

        if (spectacle == null) {
            Toast.makeText(this, "Spectacle non disponible", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupFavoriteButton();
        setupOtherDates();
    }

    private void initViews() {
        // Image
        ImageView imageView = findViewById(R.id.detailImage);
        if (spectacle.getLieu() != null && spectacle.getLieu().getPhotoUrl() != null && !spectacle.getLieu().getPhotoUrl().isEmpty()) {
            Glide.with(this)
                    .load(spectacle.getLieu().getPhotoUrl())
                    .placeholder(R.drawable.concert_placeholder)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.concert_placeholder);
        }

        // Titre
        TextView titleView = findViewById(R.id.detailTitle);
        titleView.setText(spectacle.getTitre() != null ? spectacle.getTitre() : "");

        // Lieu
        TextView locationView = findViewById(R.id.detailLocation);
        if (spectacle.getLieu() != null) {
            String locationText = spectacle.getLieu().getNomLieu() != null ? spectacle.getLieu().getNomLieu() : "";
            if (spectacle.getLieu().getVille() != null) {
                locationText += ", " + spectacle.getLieu().getVille();
            }
            locationView.setText(locationText);
        } else {
            locationView.setText("Lieu non spécifié");
        }

        // Date et heure
        TextView dateView = findViewById(R.id.detailDate);
        if (spectacle.getDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM yyyy - HH:mm", Locale.getDefault());
            String dateText = dateFormat.format(spectacle.getDate());
            if (spectacle.getHeureDebut() != null) {
                dateText += " " + spectacle.getHeureDebut();
            }
            dateView.setText(dateText);
        } else {
            dateView.setText("Date non spécifiée");
        }

        // Prix
        TextView priceView = findViewById(R.id.detailPrice);
        if (spectacle.getPrix() != null) {
            priceView.setText(String.format(Locale.getDefault(), "Prix: %.2f DT", spectacle.getPrix()));
        } else {
            priceView.setText("Prix non spécifié");
        }
        // Description
        TextView descriptionView = findViewById(R.id.detailDescription);
        descriptionView.setText(spectacle.getDescription() != null ?
                spectacle.getDescription() : "Aucune description disponible");

        // Places disponibles
        TextView ticketsView = findViewById(R.id.detailAvailableTickets);
        ticketsView.setText(getString(R.string.available_tickets,
                spectacle.getNbrSpectateur() != null ? spectacle.getNbrSpectateur() : 0));

        // Bouton Réserver
        findViewById(R.id.reserveButton).setOnClickListener(v -> startReservationActivity());
    }

    private void setupOtherDates() {
        LinearLayout container = findViewById(R.id.datesTableContainer);
        container.removeAllViews();

        if (spectacle == null || spectacle.getIdSpec() == null) {
            showNoDatesMessage(container);
            return;
        }

        showLoading();

        Call<List<Spectacle>> call = RetrofitClient.getApiService().getSpectaclesWithSameTitle(spectacle.getIdSpec());
        call.enqueue(new Callback<List<Spectacle>>() {
            @Override
            public void onResponse(Call<List<Spectacle>> call, Response<List<Spectacle>> response) {
                dismissLoading();
                if (response.isSuccessful() && response.body() != null) {
                    displayDates(container, response.body());
                } else {
                    showNoDatesMessage(container);
                }
            }

            @Override
            public void onFailure(Call<List<Spectacle>> call, Throwable t) {
                dismissLoading();
                showNoDatesMessage(container);
                Log.e("API Error", "Failed to get other dates", t);
            }
        });
    }

    private void displayDates(LinearLayout container, List<Spectacle> dates) {
        container.removeAllViews();

        if (dates.isEmpty()) {
            showNoDatesMessage(container);
            return;
        }

        TextView title = new TextView(this);
        title.setText("Autres dates pour ce spectacle :");
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(ContextCompat.getColor(this, R.color.black));
        title.setPadding(0, 0, 0, 16);
        container.addView(title);

        for (Spectacle date : dates) {
            View dateView = LayoutInflater.from(this).inflate(R.layout.item_date_location, container, false);

            TextView dateText = dateView.findViewById(R.id.dateText);
            TextView locationText = dateView.findViewById(R.id.locationText);

            // Formater la date
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM yyyy - HH:mm", Locale.getDefault());
            String formattedDate = date.getDate() != null ? dateFormat.format(date.getDate()) : "Date inconnue";
            if (date.getHeureDebut() != null) {
                formattedDate += " " + date.getHeureDebut();
            }
            dateText.setText(formattedDate);

            // Formater le lieu
            String location = "Lieu inconnu";
            if (date.getLieu() != null) {
                location = date.getLieu().getNomLieu() != null ? date.getLieu().getNomLieu() : "";
                if (date.getLieu().getVille() != null) {
                    location += ", " + date.getLieu().getVille();
                }
            }
            locationText.setText(location);

            // Ajouter un clic sur l'item
            // Dans la méthode displayDates(), modifier le onClickListener :
            dateView.setOnClickListener(v -> {
                Intent intent = new Intent(DetailsActivity.this, DetailsActivity.class);
                intent.putExtra("spectacle", (Parcelable) date); // Pas besoin de cast en Parcelable
                startActivity(intent);
            });

            container.addView(dateView);
        }
    }

    private void showNoDatesMessage(LinearLayout container) {
        TextView noDatesText = new TextView(this);
        noDatesText.setText("Aucune autre date disponible pour ce spectacle");
        noDatesText.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        noDatesText.setPadding(0, 16, 0, 0);
        container.addView(noDatesText);
    }

    private void showLoading() {
        // Implémentez votre indicateur de chargement ici
    }

    private void dismissLoading() {
        // Cachez votre indicateur de chargement ici
    }

    private void setupFavoriteButton() {
        ImageButton favoriteButton = findViewById(R.id.favoriteButton);
        updateFavoriteButtonIcon(favoriteButton);

        favoriteButton.setOnClickListener(v -> {
            if (spectacle.getIdSpec() == null) {
                Toast.makeText(this, "Erreur: Spectacle invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            if (favoriteManager.isFavorite(spectacle.getIdSpec())) {
                favoriteManager.removeFavorite(spectacle.getIdSpec());
                Toast.makeText(this, "Retiré des favoris", Toast.LENGTH_SHORT).show();
            } else {
                favoriteManager.addFavorite(spectacle);
                Toast.makeText(this, "Ajouté aux favoris", Toast.LENGTH_SHORT).show();
            }
            updateFavoriteButtonIcon(favoriteButton);
        });
    }

    private void updateFavoriteButtonIcon(ImageButton button) {
        if (spectacle.getIdSpec() != null) {
            button.setImageResource(favoriteManager.isFavorite(spectacle.getIdSpec()) ?
                    R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        }
    }

    private void startReservationActivity() {
        if (spectacle == null || spectacle.getDate() == null) {
            Toast.makeText(this, "Données du spectacle invalides", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(spectacle.getDate());

        Intent intent = new Intent(this, ReservationActivity.class);
        intent.putExtra("availableTickets", spectacle.getNbrSpectateur());
        intent.putExtra("spectacleId", spectacle.getIdSpec());
        intent.putExtra("titre", spectacle.getTitre());
        intent.putExtra("date", formattedDate);
        intent.putExtra("heureDebut", spectacle.getHeureDebut());

        if (spectacle.getLieu() != null) {
            intent.putExtra("nomLieu", spectacle.getLieu().getNomLieu());
            intent.putExtra("ville", spectacle.getLieu().getVille());
            intent.putExtra("googleMapsUrl", spectacle.getLieu().getGoogleMapsUrl());
        }

        startActivity(intent);
    }
}