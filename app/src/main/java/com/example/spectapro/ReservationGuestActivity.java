package com.example.spectapro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class ReservationGuestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        // Récupération des données
        Intent intent = getIntent();
        String bookingCode = intent.getStringExtra("bookingCode");
        String eventTitle = intent.getStringExtra("eventTitle");
        String eventDate = intent.getStringExtra("eventDate");
        String eventLocation = intent.getStringExtra("eventLocation");
        String ticketType = intent.getStringExtra("ticketType");
        int ticketCount = intent.getIntExtra("ticketCount", 1);
        double totalPrice = intent.getDoubleExtra("totalPrice", 0);
        String imageUrl = intent.getStringExtra("imageUrl");
        String paymentMethod = intent.getStringExtra("paymentMethod");
        String reservationDate = intent.getStringExtra("reservationDate");
        String reservationTime = intent.getStringExtra("reservationTime");
        String guestName = intent.getStringExtra("guestName");
        String guestEmail = intent.getStringExtra("guestEmail");

        // Configuration de la toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Affichage des données
        ((TextView) findViewById(R.id.booking_code)).setText("N° " + bookingCode);
        ((TextView) findViewById(R.id.event_title)).setText(eventTitle);
        ((TextView) findViewById(R.id.event_date)).setText(eventDate);
        ((TextView) findViewById(R.id.event_location)).setText(eventLocation);
        ((TextView) findViewById(R.id.ticket_type)).setText(getTicketTypeDisplayName(ticketType));
        ((TextView) findViewById(R.id.ticket_quantity)).setText(String.valueOf(ticketCount));
        ((TextView) findViewById(R.id.total_price)).setText(String.format("%.2f DT", totalPrice));
        ((TextView) findViewById(R.id.reservation_date)).setText(reservationDate);
        ((TextView) findViewById(R.id.reservation_time)).setText(reservationTime);
        ((TextView) findViewById(R.id.payment_method)).setText(getPaymentMethodDisplayName(paymentMethod));

        // Affichage des infos guest
        TextView tvGuestInfo = findViewById(R.id.guest_info);
        tvGuestInfo.setVisibility(View.VISIBLE);
        tvGuestInfo.setText(String.format("Réservé par: %s (%s)", guestName, guestEmail));

        // Chargement de l'image
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.concert_placeholder)
                    .into((ImageView) findViewById(R.id.event_image));
        }

        // Configuration des boutons
        MaterialButton btnDone = findViewById(R.id.btn_done);
        btnDone.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });

        MaterialButton btnShare = findViewById(R.id.btn_share);
        btnShare.setOnClickListener(v -> shareBookingDetails());
    }

    private String getTicketTypeDisplayName(String type) {
        switch (type) {
            case "NORMAL": return "Standard";
            case "SILVER": return "VIP";
            case "GOLD": return "Premium";
            default: return type;
        }
    }

    private String getPaymentMethodDisplayName(String method) {
        switch (method) {
            case "CREDIT_CARD": return "Carte bancaire";
            case "MOBILE_PAYMENT": return "Paiement mobile";
            case "ELECTRONIC_WALLET": return "Porte-monnaie électronique";
            default: return method;
        }
    }

    private void shareBookingDetails() {
        Intent intent = getIntent();
        String shareText = String.format(
                "Ma réservation SpectaPro (Invité)\n\n" +
                        "Événement: %s\n" +
                        "Date: %s\n" +
                        "Lieu: %s\n" +
                        "Type de billet: %s\n" +
                        "Quantité: %d\n" +
                        "Prix total: %.2f DT\n" +
                        "Code: %s\n" +
                        "Réservé par: %s (%s)",
                intent.getStringExtra("eventTitle"),
                intent.getStringExtra("eventDate"),
                intent.getStringExtra("eventLocation"),
                getTicketTypeDisplayName(intent.getStringExtra("ticketType")),
                intent.getIntExtra("ticketCount", 1),
                intent.getDoubleExtra("totalPrice", 0),
                intent.getStringExtra("bookingCode"),
                intent.getStringExtra("guestName"),
                intent.getStringExtra("guestEmail")
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Partager via"));
    }
}