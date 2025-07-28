package com.example.spectapro;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.spectapro.model.Billet;
import com.example.spectapro.model.Client;
import com.example.spectapro.model.Spectacle;
import com.example.spectapro.network.ApiService;
import com.example.spectapro.network.RetrofitClient;
import com.example.spectapro.storage.SharedPrefManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationActivity extends AppCompatActivity {
    private static final String MAILJET_API_KEY;
    private static final String MAILJET_SECRET_KEY;

    private int ticketCount = 1;
    private String ticketType = "NORMAL";
    private double ticketPrice = 5.0;
    private int availableTickets = 0;
    private String paymentMethod = "CREDIT_CARD";

    private TextView tvTicketCount, tvTotalPrice, tvEventTitle, tvEventDate,
            tvEventLocation, tvEventAddress, tvAvailableTickets;
    private ImageView eventImage;
    private String googleMapsUrl;
    private Long spectacleId;
    private EditText etGuestEmail, etGuestFirstName, etGuestLastName, etGuestPhone;
    private MaterialCardView guestInfoCard;
    private AlertDialog loadingDialog;

    // Champs de paiement
    private TextInputEditText etCardNumber, etExpiryDate, etCvv, etCardHolderName;
    private TextInputEditText etMobileNumber;
    private Spinner mobileOperatorSpinner, walletProviderSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        setupToolbar();
        initViews();
        setupEventDetails();
        setupPaymentMethodSelection();
        setupListeners();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        tvTicketCount = findViewById(R.id.tvTicketCount);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvEventTitle = findViewById(R.id.eventTitle);
        tvEventDate = findViewById(R.id.eventDate);
        tvEventLocation = findViewById(R.id.eventLocation);
        tvEventAddress = findViewById(R.id.eventAddress);
        eventImage = findViewById(R.id.eventImage);
        tvAvailableTickets = findViewById(R.id.tvAvailableTickets);

        guestInfoCard = findViewById(R.id.guestInfoCard);
        etGuestEmail = findViewById(R.id.etGuestEmail);
        etGuestFirstName = findViewById(R.id.etGuestFirstName);
        etGuestLastName = findViewById(R.id.etGuestLastName);
        etGuestPhone = findViewById(R.id.etGuestPhone);

        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etCvv = findViewById(R.id.etCvv);
        etCardHolderName = findViewById(R.id.etCardHolderName);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        mobileOperatorSpinner = findViewById(R.id.mobileOperatorSpinner);
        walletProviderSpinner = findViewById(R.id.walletProviderSpinner);

        setupSpinners();

        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            guestInfoCard.setVisibility(View.VISIBLE);
        }

        tvTicketCount.setText(String.valueOf(ticketCount));
        updateTotalPrice();
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> mobileAdapter = ArrayAdapter.createFromResource(this,
                R.array.mobile_operators, android.R.layout.simple_spinner_item);
        mobileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mobileOperatorSpinner.setAdapter(mobileAdapter);

        ArrayAdapter<CharSequence> walletAdapter = ArrayAdapter.createFromResource(this,
                R.array.wallet_providers, android.R.layout.simple_spinner_item);
        walletAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        walletProviderSpinner.setAdapter(walletAdapter);
    }

    private void setupEventDetails() {
        Intent intent = getIntent();
        availableTickets = intent.getIntExtra("availableTickets", 0);
        spectacleId = intent.getLongExtra("spectacleId", -1);

        String imageUrl = intent.getStringExtra("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.concert_placeholder)
                        .error(R.drawable.ic_category_outline)
                        .into(eventImage);
            } catch (Exception e) {
                Log.e("ImageLoad", "Error loading image", e);
                eventImage.setImageResource(R.drawable.concert_placeholder);
            }
        } else {
            eventImage.setImageResource(R.drawable.concert_placeholder);
        }

        tvEventTitle.setText(intent.getStringExtra("titre"));

        String dateStr = intent.getStringExtra("date");
        String timeStr = intent.getStringExtra("heureDebut");
        tvEventDate.setText(String.format("%s • %s", dateStr, timeStr));

        String location = intent.getStringExtra("nomLieu");
        String city = intent.getStringExtra("ville");
        tvEventLocation.setText(location);
        tvEventAddress.setText(city);

        googleMapsUrl = intent.getStringExtra("googleMapsUrl");
        updateAvailableTicketsDisplay();
    }

    private void updateAvailableTicketsDisplay() {
        tvAvailableTickets.setText(String.format(Locale.getDefault(), "(disponibles: %d)", availableTickets));
        tvAvailableTickets.setTextColor(availableTickets < 5 ?
                getResources().getColor(R.color.red) :
                getResources().getColor(R.color.gray_dark));
    }

    private void setupPaymentMethodSelection() {
        RadioGroup paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        paymentMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            findViewById(R.id.creditCardForm).setVisibility(View.GONE);
            findViewById(R.id.mobilePaymentForm).setVisibility(View.GONE);
            findViewById(R.id.electronicWalletForm).setVisibility(View.GONE);

            if (checkedId == R.id.radioCreditCard) {
                paymentMethod = "CREDIT_CARD";
                findViewById(R.id.creditCardForm).setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.radioMobilePayment) {
                paymentMethod = "MOBILE_PAYMENT";
                findViewById(R.id.mobilePaymentForm).setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.radioElectronicWallet) {
                paymentMethod = "ELECTRONIC_WALLET";
                findViewById(R.id.electronicWalletForm).setVisibility(View.VISIBLE);
            }
        });

        ((MaterialRadioButton) findViewById(R.id.radioCreditCard)).setChecked(true);
    }

    private void setupListeners() {
        findViewById(R.id.btnDecrease).setOnClickListener(v -> {
            if (ticketCount > 1) {
                ticketCount--;
                tvTicketCount.setText(String.valueOf(ticketCount));
                updateTotalPrice();
            }
        });

        findViewById(R.id.btnIncrease).setOnClickListener(v -> {
            int maxTickets = Math.min(10, availableTickets);
            if (ticketCount < maxTickets) {
                ticketCount++;
                tvTicketCount.setText(String.valueOf(ticketCount));
                updateTotalPrice();
            } else {
                String message = availableTickets <= 10 ?
                        getString(R.string.only_x_tickets_left, availableTickets) :
                        getString(R.string.max_10_tickets);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        RadioGroup ticketTypeGroup = findViewById(R.id.ticketTypeGroup);
        ticketTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioStandard) {
                ticketType = "NORMAL";
                ticketPrice = 5.0;
            } else if (checkedId == R.id.radioVIP) {
                ticketType = "SILVER";
                ticketPrice = 10.0;
            } else if (checkedId == R.id.radioPremium) {
                ticketType = "GOLD";
                ticketPrice = 30.0;
            }
            updateTotalPrice();
        });

        findViewById(R.id.btnLocation).setOnClickListener(v -> {
            if (googleMapsUrl != null && !googleMapsUrl.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsUrl));
                startActivity(browserIntent);
            } else {
                Toast.makeText(this, "Aucun lien officiel disponible", Toast.LENGTH_SHORT).show();
            }
        });

        MaterialButton btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> {
            if (validateReservation()) {
                confirmReservation();
            }
        });
    }

    private boolean validateReservation() {
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            if (!validateGuestFields()) {
                return false;
            }
        }

        if (ticketCount > availableTickets) {
            showError(getString(R.string.only_x_tickets_left, availableTickets));
            return false;
        }
        if (spectacleId == -1) {
            showError(getString(R.string.invalid_event));
            return false;
        }
        if (ticketCount < 1 || ticketCount > 10) {
            showError(getString(R.string.invalid_ticket_count));
            return false;
        }

        return validatePaymentFields();
    }

    private boolean validateGuestFields() {
        String firstName = etGuestFirstName.getText().toString().trim();
        if (firstName.isEmpty()) {
            etGuestFirstName.setError("Veuillez entrer votre prénom");
            etGuestFirstName.requestFocus();
            return false;
        }

        String lastName = etGuestLastName.getText().toString().trim();
        if (lastName.isEmpty()) {
            etGuestLastName.setError("Veuillez entrer votre nom");
            etGuestLastName.requestFocus();
            return false;
        }

        String email = etGuestEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etGuestEmail.setError("Veuillez entrer votre email");
            etGuestEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etGuestEmail.setError("Email invalide");
            etGuestEmail.requestFocus();
            return false;
        }

        String phone = etGuestPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            etGuestPhone.setError("Veuillez entrer votre téléphone");
            etGuestPhone.requestFocus();
            return false;
        }
        if (!Patterns.PHONE.matcher(phone).matches()) {
            etGuestPhone.setError("Téléphone invalide");
            etGuestPhone.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validatePaymentFields() {
        switch (paymentMethod) {
            case "CREDIT_CARD":
                return validateCreditCardFields();
            case "MOBILE_PAYMENT":
                return validateMobilePaymentFields();
            case "ELECTRONIC_WALLET":
                return true;
            default:
                showError("Méthode de paiement non reconnue");
                return false;
        }
    }

    private boolean validateCreditCardFields() {
        String cardNumber = etCardNumber.getText().toString().trim();
        if (cardNumber.isEmpty()) {
            etCardNumber.setError("Veuillez entrer le numéro de carte");
            etCardNumber.requestFocus();
            return false;
        }
        if (cardNumber.length() < 16) {
            etCardNumber.setError("Numéro de carte invalide");
            etCardNumber.requestFocus();
            return false;
        }

        String expiryDate = etExpiryDate.getText().toString().trim();
        if (expiryDate.isEmpty()) {
            etExpiryDate.setError("Veuillez entrer la date d'expiration");
            etExpiryDate.requestFocus();
            return false;
        }

        String cvv = etCvv.getText().toString().trim();
        if (cvv.isEmpty()) {
            etCvv.setError("Veuillez entrer le CVV");
            etCvv.requestFocus();
            return false;
        }
        if (cvv.length() < 3) {
            etCvv.setError("CVV invalide");
            etCvv.requestFocus();
            return false;
        }

        String cardHolder = etCardHolderName.getText().toString().trim();
        if (cardHolder.isEmpty()) {
            etCardHolderName.setError("Veuillez entrer le nom du titulaire");
            etCardHolderName.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateMobilePaymentFields() {
        String mobileNumber = etMobileNumber.getText().toString().trim();
        if (mobileNumber.isEmpty()) {
            etMobileNumber.setError("Veuillez entrer votre numéro de téléphone");
            etMobileNumber.requestFocus();
            return false;
        }
        if (!Patterns.PHONE.matcher(mobileNumber).matches()) {
            etMobileNumber.setError("Numéro de téléphone invalide");
            etMobileNumber.requestFocus();
            return false;
        }
        return true;
    }

    private void updateTotalPrice() {
        double total = ticketCount * ticketPrice;
        tvTotalPrice.setText(String.format(Locale.getDefault(), "%.2f DT", total));
    }

    private void confirmReservation() {
        if (!isNetworkAvailable()) {
            showErrorDialog("Pas de connexion internet",
                    "Veuillez vérifier votre connexion internet et réessayer.");
            return;
        }

        try {
            if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
                if (etGuestEmail.getText().toString().trim().isEmpty() ||
                        etGuestFirstName.getText().toString().trim().isEmpty() ||
                        etGuestLastName.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Veuillez remplir tous les champs requis", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            showLoadingDialog();

            Billet billet = new Billet();
            billet.setPrix(ticketCount * ticketPrice);
            billet.setCategorie(ticketType);
            billet.setVendu("Non");

            Spectacle spectacle = new Spectacle();
            spectacle.setIdSpec(spectacleId);
            billet.setSpectacle(spectacle);

            ApiService apiService = RetrofitClient.getApiService();
            Call<Billet> call;

            if (SharedPrefManager.getInstance(this).isLoggedIn()) {
                Client client = SharedPrefManager.getInstance(this).getClient();
                billet.setClient(client);
                call = apiService.createBillet(billet);
            } else {
                Client guestClient = new Client();
                guestClient.setEmail(etGuestEmail.getText().toString().trim());
                guestClient.setPrenomclt(etGuestFirstName.getText().toString().trim());
                guestClient.setNomclt(etGuestLastName.getText().toString().trim());
                guestClient.setTel(etGuestPhone.getText().toString().trim());
                guestClient.setIdclt(0L);
                billet.setClient(guestClient);
                call = apiService.createGuestReservation(billet);
            }

            call.enqueue(new Callback<Billet>() {
                @Override
                public void onResponse(Call<Billet> call, Response<Billet> response) {
                    dismissLoadingDialog();

                    if (response.isSuccessful() && response.body() != null) {
                        Billet createdBillet = response.body();
                        updateAvailableTickets(spectacleId, ticketCount);
                        sendConfirmationEmail(createdBillet);
                        showConfirmation(createdBillet);

                        if (!SharedPrefManager.getInstance(ReservationActivity.this).isLoggedIn()) {
                            SharedPrefManager.getInstance(ReservationActivity.this)
                                    .saveGuestInfo(
                                            etGuestFirstName.getText().toString().trim(),
                                            etGuestLastName.getText().toString().trim(),
                                            etGuestEmail.getText().toString().trim(),
                                            etGuestPhone.getText().toString().trim()
                                    );
                        }
                    } else {
                        handleApiError(response);
                    }
                }

                @Override
                public void onFailure(Call<Billet> call, Throwable t) {
                    dismissLoadingDialog();
                    handleNetworkError(t);
                }
            });
        } catch (Exception e) {
            dismissLoadingDialog();
            Log.e("Reservation", "Exception", e);
            showErrorDialog("Erreur", "Une erreur inattendue s'est produite: " + e.getMessage());
        }
    }

    private void handleApiError(Response<Billet> response) {
        try {
            String error = response.errorBody() != null ?
                    response.errorBody().string() : "Erreur inconnue du serveur";
            Log.e("Reservation", "API Error: " + error);
            showErrorDialog("Erreur de réservation", error);
        } catch (Exception e) {
            Log.e("Reservation", "Error parsing error response", e);
            showErrorDialog("Erreur", "Impossible de traiter la réponse du serveur");
        }
    }

    private void handleNetworkError(Throwable t) {
        Log.e("Reservation", "Network error", t);

        String errorMessage = "Erreur réseau";
        if (t instanceof java.net.SocketTimeoutException) {
            errorMessage = "Le serveur ne répond pas - temps d'attente dépassé";
        } else if (t instanceof java.net.ConnectException) {
            errorMessage = "Impossible de se connecter au serveur";
        } else if (t instanceof java.net.UnknownHostException) {
            errorMessage = "Serveur inaccessible";
        }

        showErrorDialog("Erreur réseau", errorMessage);
    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.dialog_loading);
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void sendConfirmationEmail(Billet billet) {
        String email;
        String name;

        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            Client client = SharedPrefManager.getInstance(this).getClient();
            email = client.getEmail();
            name = client.getPrenomclt() + " " + client.getNomclt();
        } else {
            email = etGuestEmail.getText().toString().trim();
            name = etGuestFirstName.getText().toString().trim() + " " +
                    etGuestLastName.getText().toString().trim();
        }

        ApiService.EmailRequest emailRequest = new ApiService.EmailRequest(
                email,
                name,
                tvEventTitle.getText().toString(),
                tvEventDate.getText().toString(),
                tvEventLocation.getText().toString() + ", " + tvEventAddress.getText().toString(),
                ticketType,
                ticketCount,
                ticketCount * ticketPrice,
                paymentMethod,
                billet.getIdBillet()
        );

        ApiService apiService = RetrofitClient.getApiService();
        Call<Void> call = apiService.sendConfirmationEmail(emailRequest);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Log.e("EmailError", "Erreur serveur: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e("EmailError", "Erreur: " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("EmailError", "Erreur réseau", t);
            }
        });
    }

    private String getPaymentMethodName(String method) {
        switch (method) {
            case "CREDIT_CARD": return "Carte bancaire";
            case "MOBILE_PAYMENT": return "Paiement mobile";
            case "ELECTRONIC_WALLET": return "Porte-monnaie électronique";
            default: return method;
        }
    }

    private void updateAvailableTickets(Long spectacleId, int ticketsReserved) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<Spectacle> call = apiService.updateAvailableTickets(spectacleId, ticketsReserved);

        call.enqueue(new Callback<Spectacle>() {
            @Override
            public void onResponse(Call<Spectacle> call, Response<Spectacle> response) {
                if (response.isSuccessful() && response.body() != null) {
                    availableTickets = response.body().getNbrSpectateur();
                    updateAvailableTicketsDisplay();
                } else {
                    Log.e("Reservation", "Failed to update available tickets");
                }
            }

            @Override
            public void onFailure(Call<Spectacle> call, Throwable t) {
                Log.e("Reservation", "Erreur mise à jour places", t);
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e("Reservation", message);
    }

    private void showConfirmation(Billet billet) {
        try {
            String reservationDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            String reservationTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            Intent intent;
            if (SharedPrefManager.getInstance(this).isLoggedIn()) {
                intent = new Intent(this, ConfirmationActivity.class);
            } else {
                intent = new Intent(this, ReservationGuestActivity.class);
                intent.putExtra("guestName",
                        etGuestFirstName.getText().toString() + " " + etGuestLastName.getText().toString());
                intent.putExtra("guestEmail", etGuestEmail.getText().toString());
            }

            intent.putExtra("bookingCode", billet.getIdBillet().toString());
            intent.putExtra("eventTitle", getIntent().getStringExtra("titre"));
            intent.putExtra("eventDate", getIntent().getStringExtra("date"));
            intent.putExtra("eventLocation", getIntent().getStringExtra("nomLieu"));
            intent.putExtra("ticketType", ticketType);
            intent.putExtra("ticketCount", ticketCount);
            intent.putExtra("totalPrice", ticketCount * ticketPrice);
            intent.putExtra("imageUrl", getIntent().getStringExtra("imageUrl"));
            intent.putExtra("paymentMethod", paymentMethod);
            intent.putExtra("reservationDate", reservationDate);
            intent.putExtra("reservationTime", reservationTime);

            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e("Confirmation", "Error starting confirmation activity", e);
            Toast.makeText(this, "Erreur d'affichage de confirmation", Toast.LENGTH_SHORT).show();
        }
    }

    public void onLocationClick(View view) {
        if (googleMapsUrl != null && !googleMapsUrl.isEmpty()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsUrl));
            startActivity(browserIntent);
        } else {
            Toast.makeText(this, "Aucun lien officiel disponible", Toast.LENGTH_SHORT).show();
        }
    }
}