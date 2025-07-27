package com.example.spectapro.model;

import java.io.Serializable;

public class Client implements Serializable {
    private Long idclt;
    private String nomclt;
    private String prenomclt;
    private String email;
    private String motP;
    private String tel;
    private String dateInscription;

    // Getters et setters
    public Long getIdclt() { return idclt; }
    public void setIdclt(Long idclt) { this.idclt = idclt; }

    public String getNomclt() { return nomclt; }
    public void setNomclt(String nomclt) { this.nomclt = nomclt; }

    public String getPrenomclt() { return prenomclt; }
    public void setPrenomclt(String prenomclt) { this.prenomclt = prenomclt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotP() { return motP; }
    public void setMotP(String motP) { this.motP = motP; }

    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }

    public String getDateInscription() { return dateInscription; }
    public void setDateInscription(String dateInscription) {
        this.dateInscription = dateInscription;
    }

    public String getFullName() {
        return (prenomclt != null ? prenomclt : "") + " " + (nomclt != null ? nomclt : "");
    }
}