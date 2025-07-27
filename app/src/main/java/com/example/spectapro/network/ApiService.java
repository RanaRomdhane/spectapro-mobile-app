package com.example.spectapro.network;

import com.example.spectapro.model.Billet;
import com.example.spectapro.model.Categorie;
import com.example.spectapro.model.Client;
import com.example.spectapro.model.Lieu;
import com.example.spectapro.model.Spectacle;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // Authentification
    @GET("api/clients/login")
    Call<Client> login(
            @Query("email") String email,
            @Query("motP") String password
    );

    @GET("api/clients/register")
    Call<RegisterResponse> register(
            @Query("nomclt") String nom,
            @Query("prenomclt") String prenom,
            @Query("tel") String telephone,
            @Query("email") String email,
            @Query("motP") String password
    );

    // Spectacles
    @GET("api/spectacles")
    Call<List<Spectacle>> getAllSpectacles();

    @GET("api/spectacles/search")
    Call<List<Spectacle>> searchSpectacles(
            @Query("titre") String titre,
            @Query("date") String date,
            @Query("ville") String ville
    );

    // Lieux
    @GET("api/lieux")
    Call<List<Lieu>> getAllLieux();

    @GET("api/lieux/ville/{ville}")
    Call<List<Lieu>> getLieuxByVille(@Query("ville") String ville);

    // Billets
    @POST("api/billets")
    Call<Billet> createBillet(@Body Billet billet);

    @GET("api/spectacles/{id}")
    Call<Spectacle> getSpectacleById(@Path("id") Long id);

    @GET("api/billets/spectacle/{idSpec}")
    Call<List<Billet>> getBilletsBySpectacle(@Query("idSpec") Long idSpec);

    @GET("api/billets/client/{clientId}")
    Call<List<Billet>> getTicketsByClient(@Path("clientId") Long clientId);

    // Catégories
    @GET("api/categories")
    Call<List<Categorie>> getAllCategories();

    @GET("clients/{id}")
    Call<Client> getClientById(@Path("id") Long idClient);

    @GET("api/billets/count/{clientId}")
    Call<Integer> getTicketCount(@Path("clientId") Long clientId);

    @GET("api/favoris/count/{clientId}")
    Call<Integer> getFavoriteCount(@Path("clientId") Long clientId);

    @GET("api/clients/email/{email}")
    Call<Client> getClientByEmail(@Path("email") String email);

    @POST("api/spectacles/updateTickets")
    Call<Spectacle> updateAvailableTickets(
            @Query("idSpec") Long idSpec,
            @Query("ticketsReserved") int ticketsReserved
    );


    // Classes pour les réponses JSON
    class LoginResponse {
        private String status;
        private String message;

        public String getStatus() { return status; }
        public String getMessage() { return message; }
    }

    class RegisterResponse {
        private Long idclt;
        private String email;
        private String status;
        private String error;

        public Long getIdclt() { return idclt; }
        public String getEmail() { return email; }
        public String getStatus() { return status; }
        public String getError() { return error; }
    }

    @POST("api/billets/guest")
    Call<Billet> createGuestReservation(@Body Billet billet);

    // Optionnel - Pour l'envoi d'email côté serveur
    @GET("api/spectacles/sameTitle/{id}")
    Call<List<Spectacle>> getSpectaclesWithSameTitle(@Path("id") Long id);

    @POST("api/emails/send")
    Call<Void> sendConfirmationEmail(@Body EmailRequest emailRequest);

    // Classe interne pour la requête email
    class EmailRequest {
        private String recipientEmail;
        private String recipientName;
        private String eventTitle;
        private String eventDate;
        private String eventLocation;
        private String ticketType;
        private int ticketCount;
        private double totalPrice;
        private String paymentMethod;
        private Long reservationId;

        // Constructeur
        public EmailRequest(String recipientEmail, String recipientName,
                            String eventTitle, String eventDate,
                            String eventLocation, String ticketType,
                            int ticketCount, double totalPrice,
                            String paymentMethod, Long reservationId) {
            this.recipientEmail = recipientEmail;
            this.recipientName = recipientName;
            this.eventTitle = eventTitle;
            this.eventDate = eventDate;
            this.eventLocation = eventLocation;
            this.ticketType = ticketType;
            this.ticketCount = ticketCount;
            this.totalPrice = totalPrice;
            this.paymentMethod = paymentMethod;
            this.reservationId = reservationId;
        }

        // Getters (nécessaires pour la sérialisation)
        public String getRecipientEmail() { return recipientEmail; }
        public String getRecipientName() { return recipientName; }
        public String getEventTitle() { return eventTitle; }
        public String getEventDate() { return eventDate; }
        public String getEventLocation() { return eventLocation; }
        public String getTicketType() { return ticketType; }
        public int getTicketCount() { return ticketCount; }
        public double getTotalPrice() { return totalPrice; }
        public String getPaymentMethod() { return paymentMethod; }
        public Long getReservationId() { return reservationId; }
    }



}