package com.example.spectapro;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.spectapro.model.Billet;
import com.example.spectapro.model.Spectacle;
import com.google.android.material.appbar.MaterialToolbar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TicketDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        // Récupération du billet
        Billet billet = getBilletFromIntent();
        if (billet == null) {
            finish();
            return;
        }

        // Configuration de l'interface
        setupToolbar();
        displayTicketDetails(billet);
        setupButtons(billet);
    }

    private Billet getBilletFromIntent() {
        try {
            Billet billet = (Billet) getIntent().getSerializableExtra("billet");
            if (billet == null || billet.getSpectacle() == null) {
                return null;
            }
            return billet;
        } catch (Exception e) {
            return null;
        }
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Détails du billet");
        }
    }

    private void displayTicketDetails(Billet billet) {
        Spectacle spectacle = billet.getSpectacle();

        // Affichage des informations de base
        ((TextView) findViewById(R.id.booking_code)).setText(getString(R.string.booking_code_format, billet.getIdBillet()));
        ((TextView) findViewById(R.id.event_title)).setText(spectacle.getTitre());

        // Formatage de la date
        String dateStr = formatEventDate(spectacle.getDate());
        ((TextView) findViewById(R.id.event_date)).setText(dateStr);

        // Formatage du lieu
        String location = formatEventLocation(spectacle);
        ((TextView) findViewById(R.id.event_location)).setText(location);

        // Informations sur le billet
        ((TextView) findViewById(R.id.ticket_type)).setText(billet.getCategorie());
        ((TextView) findViewById(R.id.ticket_quantity)).setText(String.valueOf(
                calculateTicketQuantity(billet)));
        ((TextView) findViewById(R.id.total_price)).setText(getString(R.string.price_format, billet.getPrix()));

        // Chargement de l'image
    }

    private String formatEventDate(Date date) {
        if (date == null) return getString(R.string.unknown_date);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.FRENCH);
        return dateFormat.format(date);
    }

    private String formatEventLocation(Spectacle spectacle) {
        if (spectacle.getLieu() == null) return getString(R.string.unknown_location);

        return spectacle.getLieu().getNomLieu() + ", " + spectacle.getLieu().getVille();
    }

    private int calculateTicketQuantity(Billet billet) {
        double pricePerTicket = getPricePerTicket(billet.getCategorie());
        return (int) Math.ceil(billet.getPrix() / pricePerTicket);
    }



    private void setupButtons(Billet billet) {
        // Bouton Terminé
        findViewById(R.id.btn_done).setOnClickListener(v -> {
            navigateToHome();
        });

        // Bouton Partager
        findViewById(R.id.btn_share).setOnClickListener(v -> {
            shareTicketDetails(billet);
        });
    }

    private void navigateToHome() {
        startActivity(new Intent(this, HomeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        finish();
    }

    private double getPricePerTicket(String category) {
        if (category == null) return 30.0;

        switch (category.toUpperCase()) {
            case "GOLD": return 5.0;
            case "SILVER": return 10.0;
            default: return 30.0;
        }
    }

    private void shareTicketDetails(Billet billet) {
        Spectacle spectacle = billet.getSpectacle();
        String shareText = String.format(
                "%s\n\n%s: %s\n%s: %s\n%s: %s\n%s: %s\n%s: %.2fDT\n%s: %s",
                getString(R.string.share_title),
                getString(R.string.event_label), spectacle.getTitre(),
                getString(R.string.date_label), formatEventDate(spectacle.getDate()),
                getString(R.string.location_label), formatEventLocation(spectacle),
                getString(R.string.ticket_type_label), billet.getCategorie(),
                getString(R.string.total_price_label), billet.getPrix(),
                getString(R.string.booking_code_label), billet.getIdBillet()
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}