package com.example.spectapro.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.spectapro.model.Client;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SharedPrefManager {
    private static final String PREF_NAME = "SpectaProPrefs";
    private static final String KEY_CLIENT = "client_data";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_GUEST_FIRST_NAME = "guest_first_name";
    private static final String KEY_GUEST_LAST_NAME = "guest_last_name";
    private static final String KEY_GUEST_EMAIL = "guest_email";
    private static final String KEY_GUEST_PHONE = "guest_phone";

    private static SharedPrefManager instance;
    private final SharedPreferences prefs;
    private final Gson gson;

    private SharedPrefManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new GsonBuilder()
                .serializeNulls()
                .create();
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    public void saveClient(Client client) {
        try {
            String clientJson = gson.toJson(client);
            Log.d("SharedPref", "Sauvegarde client: " + clientJson);

            prefs.edit()
                    .putString(KEY_CLIENT, clientJson)
                    .putBoolean(KEY_IS_LOGGED_IN, true)
                    .apply();
        } catch (Exception e) {
            Log.e("SharedPref", "Erreur sauvegarde client", e);
        }
    }

    public Client getClient() {
        try {
            String json = prefs.getString(KEY_CLIENT, null);
            if (json == null) {
                Log.d("SharedPref", "Aucun client trouvé");
                return null;
            }

            Log.d("SharedPref", "Client chargé: " + json);
            Client client = gson.fromJson(json, Client.class);

            if (client.getEmail() == null || client.getIdclt() == null) {
                Log.e("SharedPref", "Données client incomplètes");
                return null;
            }

            return client;
        } catch (Exception e) {
            Log.e("SharedPref", "Erreur lecture client", e);
            return null;
        }
    }

    public void saveGuestInfo(String firstName, String lastName, String email, String phone) {
        prefs.edit()
                .putString(KEY_GUEST_FIRST_NAME, firstName)
                .putString(KEY_GUEST_LAST_NAME, lastName)
                .putString(KEY_GUEST_EMAIL, email)
                .putString(KEY_GUEST_PHONE, phone)
                .apply();
    }

    public String getGuestFirstName() {
        return prefs.getString(KEY_GUEST_FIRST_NAME, "");
    }

    public String getGuestLastName() {
        return prefs.getString(KEY_GUEST_LAST_NAME, "");
    }

    public String getGuestEmail() {
        return prefs.getString(KEY_GUEST_EMAIL, "");
    }

    public String getGuestPhone() {
        return prefs.getString(KEY_GUEST_PHONE, "");
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void clear() {
        prefs.edit()
                .remove(KEY_CLIENT)
                .remove(KEY_IS_LOGGED_IN)
                .remove(KEY_GUEST_FIRST_NAME)
                .remove(KEY_GUEST_LAST_NAME)
                .remove(KEY_GUEST_EMAIL)
                .remove(KEY_GUEST_PHONE)
                .apply();
    }
}