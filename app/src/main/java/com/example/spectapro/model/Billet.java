package com.example.spectapro.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Billet implements Serializable {
    private Long idBillet;
    private Double prix;
    private String vendu ; // 'Y' ou 'N'
    private String categorie; // "GOLD", "SILVER" ou "NORMAL"
    private Spectacle spectacle;
    private Client client;

    public Billet() {}

    public Billet(Double prix, String categorie, Spectacle spectacle, Client client) {
        this.prix = prix;
        this.vendu = "Y";
        this.categorie = categorie != null ? categorie : "NORMAL";
        this.spectacle = spectacle;
        this.client = client;
    }

    // Getters et Setters
    public Long getIdBillet() { return idBillet; }
    public void setIdBillet(Long idBillet) { this.idBillet = idBillet; }

    public Double getPrix() { return prix != null ? prix : 0.0; }
    public void setPrix(Double prix) { this.prix = prix; }

    public String getVendu() { return vendu; }
    public void setVendu(String vendu) {
        if (!vendu.equals("Oui") && !vendu.equals("Non")) {
            throw new IllegalArgumentException("VENDU doit être 'Oui' ou 'Non'");
        }
        this.vendu = vendu;
    }

    // Méthode utilitaire
    public boolean isVenduBoolean() {
        return "Y".equalsIgnoreCase(this.vendu);
    }


    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) {
        // Forcer la majuscule
        if (categorie != null) {
            categorie = categorie.toUpperCase();
        }
        if (!"GOLD".equals(categorie) && !"SILVER".equals(categorie)
                && !"NORMAL".equals(categorie)) {
            throw new IllegalArgumentException("Catégorie invalide: " + categorie);
        }
        this.categorie = categorie;
    }

    public Spectacle getSpectacle() { return spectacle; }
    public void setSpectacle(Spectacle spectacle) { this.spectacle = spectacle; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    @SerializedName("paymentLast4")
    private String paymentLast4;

    // Getters et setters
    public String getPaymentLast4() {
        return paymentLast4;
    }

    public void setPaymentLast4(String paymentLast4) {
        this.paymentLast4 = paymentLast4;
    }
}