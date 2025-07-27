package com.example.spectapro.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Objects;

public class Lieu implements Serializable, Parcelable {
    private Long idLieu;
    private String nomLieu;
    private String adresse;
    private String ville;
    private Integer capacite;
    private char supprime;
    private String photoUrl;
    private String googleMapsUrl;

    public Lieu() {}

    protected Lieu(Parcel in) {
        if (in.readByte() == 0) {
            idLieu = null;
        } else {
            idLieu = in.readLong();
        }
        nomLieu = in.readString();
        adresse = in.readString();
        ville = in.readString();
        if (in.readByte() == 0) {
            capacite = null;
        } else {
            capacite = in.readInt();
        }
        supprime = (char) in.readInt();
        photoUrl = in.readString();
        googleMapsUrl = in.readString();
    }

    public static final Creator<Lieu> CREATOR = new Creator<Lieu>() {
        @Override
        public Lieu createFromParcel(Parcel in) {
            return new Lieu(in);
        }

        @Override
        public Lieu[] newArray(int size) {
            return new Lieu[size];
        }
    };

    // Getters et Setters
    public Long getIdLieu() { return idLieu; }
    public void setIdLieu(Long idLieu) { this.idLieu = idLieu; }

    public String getNomLieu() { return nomLieu; }
    public void setNomLieu(String nomLieu) { this.nomLieu = nomLieu; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public Integer getCapacite() { return capacite; }
    public void setCapacite(Integer capacite) { this.capacite = capacite; }

    public char getSupprime() { return supprime; }
    public void setSupprime(char supprime) { this.supprime = supprime; }

    public boolean isSupprime() { return this.supprime == 'Y' || this.supprime == 'y'; }
    public void setSupprime(boolean supprime) { this.supprime = supprime ? 'Y' : 'N'; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getGoogleMapsUrl() { return googleMapsUrl; }
    public void setGoogleMapsUrl(String googleMapsUrl) { this.googleMapsUrl = googleMapsUrl; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (idLieu == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(idLieu);
        }
        dest.writeString(nomLieu);
        dest.writeString(adresse);
        dest.writeString(ville);
        if (capacite == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(capacite);
        }
        dest.writeInt((int) supprime);
        dest.writeString(photoUrl);
        dest.writeString(googleMapsUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lieu lieu = (Lieu) o;
        return Objects.equals(idLieu, lieu.idLieu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLieu);
    }

    @Override
    public String toString() {
        return "Lieu{" +
                "nomLieu='" + nomLieu + '\'' +
                ", ville='" + ville + '\'' +
                '}';
    }
}