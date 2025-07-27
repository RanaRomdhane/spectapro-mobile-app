package com.example.spectapro;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationUtils {
    private static final String TAG = "NavigationUtils";

    public static void setupBottomNavigation(Activity activity, BottomNavigationView bottomNav, int selectedItemId) {
        try {
            if (bottomNav == null) {
                Log.e(TAG, "BottomNavigationView is null");
                return;
            }

            bottomNav.setOnNavigationItemSelectedListener(item -> {
                try {
                    int id = item.getItemId();

                    // Empêche la réouverture de la même activité
                    if ((id == R.id.nav_home && activity instanceof HomeActivity) ||
                            (id == R.id.nav_favorites && activity instanceof FavoritesActivity) ||
                            (id == R.id.nav_tickets && activity instanceof TicketsActivity) ||
                            (id == R.id.nav_profile && activity instanceof ProfileActivity)) {
                        return false;
                    }

                    Intent intent = null;
                    if (id == R.id.nav_home) {
                        intent = new Intent(activity, HomeActivity.class);
                    } else if (id == R.id.nav_favorites) {
                        intent = new Intent(activity, FavoritesActivity.class);
                    } else if (id == R.id.nav_tickets) {
                        intent = new Intent(activity, TicketsActivity.class);
                    } else if (id == R.id.nav_profile) {
                        intent = new Intent(activity, ProfileActivity.class);
                    }

                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        activity.startActivity(intent);
                        activity.overridePendingTransition(0, 0);
                        return true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Navigation error", e);
                }
                return false;
            });

            // Sélectionne l'item actif
            bottomNav.post(() -> bottomNav.setSelectedItemId(selectedItemId));
        } catch (Exception e) {
            Log.e(TAG, "Setup bottom nav failed", e);
        }
    }
}