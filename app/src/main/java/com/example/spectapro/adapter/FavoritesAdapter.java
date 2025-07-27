package com.example.spectapro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.spectapro.FavoritesActivity;
import com.example.spectapro.R;
import com.example.spectapro.model.Spectacle;
import com.example.spectapro.storage.FavoriteManager;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
    private List<Spectacle> favorites;
    private final Context context;
    private final FavoriteManager favoriteManager;
    private final SimpleDateFormat dateFormat;
    private final OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Spectacle spectacle);
        void onReservationClick(Spectacle spectacle);
    }

    public FavoritesAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.favoriteManager = FavoriteManager.getInstance(context);
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        this.favorites = new ArrayList<>();
        this.itemClickListener = listener;
    }

    public void setFavorites(List<Spectacle> favorites) {
        this.favorites = favorites != null ? favorites : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Spectacle spectacle = favorites.get(position);

        try {
            if (spectacle.getLieu() != null && spectacle.getLieu().getPhotoUrl() != null) {
                Glide.with(context)
                        .load(spectacle.getLieu().getPhotoUrl())
                        .placeholder(R.drawable.concert_placeholder)
                        .into(holder.eventImage);
            } else {
                holder.eventImage.setImageResource(R.drawable.concert_placeholder);
            }

            holder.eventTitle.setText(spectacle.getTitre());

            if (spectacle.getLieu() != null) {
                String locationText = spectacle.getLieu().getNomLieu() != null ? spectacle.getLieu().getNomLieu() : "";
                if (spectacle.getLieu().getVille() != null) {
                    locationText += ", " + spectacle.getLieu().getVille();
                }
                holder.eventLocation.setText(locationText);
            } else {
                holder.eventLocation.setText("Lieu inconnu");
            }

            String dateStr = spectacle.getDate() != null ? dateFormat.format(spectacle.getDate()) : "Date inconnue";
            String timeStr = spectacle.getHeureDebut() != null ? spectacle.getHeureDebut() : "Heure inconnue";
            holder.eventDate.setText(String.format("%s - %s", dateStr, timeStr));

            holder.eventDescription.setText(spectacle.getDescription() != null ?
                    spectacle.getDescription() : "Aucune description disponible");

            holder.eventAvailableTickets.setText(String.format("Places disponibles: %d",
                    spectacle.getNbrSpectateur() != null ? spectacle.getNbrSpectateur() : 0));

            // Afficher le prix
            if (spectacle.getPrix() != null) {
                holder.eventPrice.setText(String.format(Locale.getDefault(), "%.2f DT", spectacle.getPrix()));
            } else {
                holder.eventPrice.setText("Prix N/A");
            }

            holder.removeButton.setOnClickListener(v -> {
                favoriteManager.removeFavorite(spectacle.getIdSpec());
                favorites.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());
                Toast.makeText(context, "Retiré des favoris", Toast.LENGTH_SHORT).show();

                if (getItemCount() == 0 && context instanceof FavoritesActivity) {
                    ((FavoritesActivity) context).updateEmptyView();
                }
            });

            holder.detailsButton.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(spectacle);
                }
            });

            holder.itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(spectacle);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventTitle;
        TextView eventLocation;
        TextView eventDate;
        TextView eventDescription;
        TextView eventAvailableTickets;
        TextView eventPrice;
        ImageButton removeButton;
        MaterialButton detailsButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventDescription = itemView.findViewById(R.id.eventDescription);
            eventAvailableTickets = itemView.findViewById(R.id.eventAvailableTickets);
            eventPrice = itemView.findViewById(R.id.eventPrice);
            removeButton = itemView.findViewById(R.id.removeButton);
            detailsButton = itemView.findViewById(R.id.detailsButton);
        }
    }
}