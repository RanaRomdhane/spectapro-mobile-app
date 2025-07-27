package com.example.spectapro.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.spectapro.model.Spectacle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoriteManager {
    private static final String TAG = "FavoriteManager";
    private static final String PREF_NAME = "FavoritesPrefs";
    private static final String KEY_FAVORITES = "favorites";
    private static FavoriteManager instance;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    private FavoriteManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new GsonBuilder().create();
    }

    public static synchronized FavoriteManager getInstance(Context context) {
        if (instance == null) {
            instance = new FavoriteManager(context.getApplicationContext());
        }
        return instance;
    }

    public void addFavorite(Spectacle spectacle) {
        List<Spectacle> favorites = getFavorites();
        if (!isFavorite(spectacle.getIdSpec())) {
            favorites.add(spectacle);
            saveFavorites(favorites);
        }
    }

    public void removeFavorite(Long spectacleId) {
        List<Spectacle> favorites = getFavorites();
        for (int i = 0; i < favorites.size(); i++) {
            if (favorites.get(i).getIdSpec().equals(spectacleId)) {
                favorites.remove(i);
                saveFavorites(favorites);
                break;
            }
        }
    }

    public boolean isFavorite(Long spectacleId) {
        for (Spectacle spectacle : getFavorites()) {
            if (spectacle.getIdSpec().equals(spectacleId)) {
                return true;
            }
        }
        return false;
    }

    public List<Spectacle> getFavorites() {
        try {
            String json = sharedPreferences.getString(KEY_FAVORITES, null);
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }

            Type type = new TypeToken<List<Spectacle>>() {}.getType();
            List<Spectacle> favorites = gson.fromJson(json, type);
            return favorites != null ? favorites : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error getting favorites", e);
            return new ArrayList<>();
        }
    }

    private void saveFavorites(List<Spectacle> favorites) {
        try {
            String json = gson.toJson(favorites);
            sharedPreferences.edit()
                    .putString(KEY_FAVORITES, json)
                    .apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving favorites", e);
        }
    }






}