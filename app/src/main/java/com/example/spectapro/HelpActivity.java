package com.example.spectapro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // Configuration de la toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Aide");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Gestion des clics sur les FAQs
        setupFAQClickListeners();
    }

    private void setupFAQClickListeners() {
        findViewById(R.id.faq1).setOnClickListener(v -> showFAQDetail("Comment réserver un billet", "Pour réserver, Vous cliquez sur boutton réserver et préciser le nombre de billets puis saisir les informations de votre carte bancaire pour le paiement."));
        findViewById(R.id.faq2).setOnClickListener(v -> showFAQDetail("Annuler une réservation", "Pour annuler, vous devez contacter le service support de spectpro par mail ou par téléphone."));
        findViewById(R.id.faq3).setOnClickListener(v -> showFAQDetail("Problème de paiement", "En cas de problème, vérifier d'abord votre carte bancaire ou contacter le service support pour plus d'informations."));

        // Gestion du clic sur le téléphone
        findViewById(R.id.phoneLayout).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+216 22 14 14 39"));
            startActivity(intent);
        });

        // Gestion du clic sur l'email
        findViewById(R.id.emailLayout).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@spectapro.com"));
            startActivity(intent);
        });
    }

    private void showFAQDetail(String question, String answer) {
        // Vous pouvez implémenter un dialogue ou une nouvelle activité
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(question)
                .setMessage(answer)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}