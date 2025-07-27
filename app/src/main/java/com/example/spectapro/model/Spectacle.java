package com.example.spectapro.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

public class Spectacle implements Serializable, Parcelable {
    private Long idSpec;
    private String titre;
    private Date date;
    private String heureDebut;
    private Integer duree;
    private Integer nbrSpectateur;
    private Lieu lieu;
    private Double prix;
    private String description;

    public Spectacle() {}

    protected Spectacle(Parcel in) {
        if (in.readByte() == 0) {
            idSpec = null;
        } else {
            idSpec = in.readLong();
        }
        titre = in.readString();
        long tmpDate = in.readLong();
        date = tmpDate != -1 ? new Date(tmpDate) : null;
        heureDebut = in.readString();
        if (in.readByte() == 0) {
            duree = null;
        } else {
            duree = in.readInt();
        }
        if (in.readByte() == 0) {
            nbrSpectateur = null;
        } else {
            nbrSpectateur = in.readInt();
        }
        lieu = in.readParcelable(Lieu.class.getClassLoader());
        if (in.readByte() == 0) {
            prix = null;
        } else {
            prix = in.readDouble();
        }
        description = in.readString();
    }

    @SerializedName("heureDebut")
    public void setHeureDebutFromTime(Time time) {
        if (time != null) {
            this.heureDebut = time.toString();
        }
    }

    public static final Creator<Spectacle> CREATOR = new Creator<Spectacle>() {
        @Override
        public Spectacle createFromParcel(Parcel in) {
            return new Spectacle(in);
        }

        @Override
        public Spectacle[] newArray(int size) {
            return new Spectacle[size];
        }
    };

    // Getters et Setters
    public Long getIdSpec() { return idSpec; }
    public void setIdSpec(Long idSpec) { this.idSpec = idSpec; }

    public String getTitre() { return titre != null ? titre : "Titre inconnu"; }
    public void setTitre(String titre) { this.titre = titre; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getHeureDebut() { return heureDebut != null ? heureDebut : ""; }
    public void setHeureDebut(String heureDebut) { this.heureDebut = heureDebut; }

    public Integer getDuree() { return duree; }
    public void setDuree(Integer duree) { this.duree = duree; }

    public Integer getNbrSpectateur() { return nbrSpectateur; }
    public void setNbrSpectateur(Integer nbrSpectateur) { this.nbrSpectateur = nbrSpectateur; }

    public Lieu getLieu() { return lieu; }
    public void setLieu(Lieu lieu) { this.lieu = lieu; }

    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public void decreaseAvailableTickets(int count) {
        if (nbrSpectateur != null) {
            nbrSpectateur = Math.max(0, nbrSpectateur - count);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (idSpec == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(idSpec);
        }
        dest.writeString(titre);
        dest.writeLong(date != null ? date.getTime() : -1);
        dest.writeString(heureDebut);
        if (duree == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(duree);
        }
        if (nbrSpectateur == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(nbrSpectateur);
        }
        dest.writeParcelable(lieu, flags);
        if (prix == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(prix);
        }
        dest.writeString(description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Spectacle spectacle = (Spectacle) o;
        return idSpec.equals(spectacle.idSpec);
    }

    @Override
    public int hashCode() {
        return idSpec.hashCode();
    }
}