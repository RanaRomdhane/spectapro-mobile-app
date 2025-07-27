package com.example.spectapro.model;

import java.util.Objects;

public class Categorie {
    private String type;
    private Double vmaxb;
    private Double vminb;

    // Constructeurs
    public Categorie() {
    }

    public Categorie(String type, Double vminb, Double vmaxb) {
        this.type = type;
        this.vminb = vminb;
        this.vmaxb = vmaxb;
    }

    // Getters et Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getVmaxb() {
        return vmaxb;
    }

    public void setVmaxb(Double vmaxb) {
        this.vmaxb = vmaxb;
    }

    public Double getVminb() {
        return vminb;
    }

    public void setVminb(Double vminb) {
        this.vminb = vminb;
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categorie categorie = (Categorie) o;
        return Objects.equals(type, categorie.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return "Categorie{" +
                "type='" + type + '\'' +
                ", vmaxb=" + vmaxb +
                ", vminb=" + vminb +
                '}';
    }
}