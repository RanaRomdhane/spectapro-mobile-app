package com.example.spectapro.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.spectapro.model.Billet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BilletManager {
    private static final String PREF_NAME = "BilletPrefs";
    private static final String KEY_BILLETS = "billets";
    private static BilletManager instance;
    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    private BilletManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized BilletManager getInstance(Context context) {
        if (instance == null) {
            instance = new BilletManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveBillets(List<Billet> billets) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_BILLETS, gson.toJson(billets));
        editor.apply();
    }

    public int getBilletCount() {
        return getBillets().size();
    }

    public void clear() {
        preferences.edit().remove(KEY_BILLETS).apply();
    }

    public void addBillet(Billet billet) {
        List<Billet> billets = getBillets();
        billets.add(billet);
        Log.d("BilletManager", "Ajout d'un billet. Total: " + billets.size());
        saveBillets(billets);
    }

    public List<Billet> getBillets() {
        String json = preferences.getString(KEY_BILLETS, null);
        if (json == null) {
            Log.d("BilletManager", "Aucun billet stocké");
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Billet>>(){}.getType();
        List<Billet> billets = gson.fromJson(json, type);
        Log.d("BilletManager", "Récupération de " + billets.size() + " billets");
        return billets;
    }

    // Nouvelle méthode pour synchroniser la liste des billets
    public void syncBillets(List<Billet> newBillets) {
        saveBillets(newBillets);
    }

    // Nouvelle méthode pour définir le nombre de billets (en ajustant la liste)
    public void setBilletCount(int count) {
        List<Billet> currentBillets = getBillets();

        if (count < 0) {
            count = 0;
        }

        // Si le nouveau count est inférieur, on tronque la liste
        if (count < currentBillets.size()) {
            List<Billet> newBillets = new ArrayList<>(currentBillets.subList(0, count));
            saveBillets(newBillets);
        }
        // Si le nouveau count est supérieur, on ne fait rien (on ne peut pas créer des billets vides)
        // Ou vous pourriez ajouter des billets vides selon votre logique métier
        Log.d("BilletManager", "Nombre de billets défini à: " + count);
    }
}