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
import com.example.spectapro.R;
import com.example.spectapro.model.Spectacle;
import com.example.spectapro.storage.FavoriteManager;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Spectacle> spectacleList;
    private final Context context;
    private final FavoriteManager favoriteManager;
    private final SimpleDateFormat dateFormat;
    private final OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Spectacle spectacle);
        void onReservationClick(Spectacle spectacle);
    }

    public EventAdapter(List<Spectacle> spectacleList, Context context, OnItemClickListener listener) {
        this.spectacleList = new ArrayList<>(spectacleList);
        this.context = context;
        this.favoriteManager = FavoriteManager.getInstance(context);
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        this.itemClickListener = listener;
    }

    public void updateList(List<Spectacle> newList) {
        this.spectacleList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Spectacle spectacle = spectacleList.get(position);

        if (spectacle.getLieu() != null && spectacle.getLieu().getPhotoUrl() != null) {
            Glide.with(context)
                    .load(spectacle.getLieu().getPhotoUrl())
                    .placeholder(R.drawable.concert_placeholder)
                    .into(holder.eventImage);
        } else {
            holder.eventImage.setImageResource(R.drawable.concert_placeholder);
        }

        holder.eventTitle.setText(spectacle.getTitre() != null ? spectacle.getTitre() : "");

        if (spectacle.getLieu() != null) {
            String locationText = spectacle.getLieu().getNomLieu() != null ? spectacle.getLieu().getNomLieu() : "";
            if (spectacle.getLieu().getVille() != null) {
                locationText += ", " + spectacle.getLieu().getVille();
            }
            holder.eventLocation.setText(locationText);
        } else {
            holder.eventLocation.setText("");
        }

        String dateStr = spectacle.getDate() != null ? dateFormat.format(spectacle.getDate()) : "Date inconnue";
        String timeStr = spectacle.getHeureDebut() != null ? spectacle.getHeureDebut() : "Heure inconnue";
        holder.eventDate.setText(String.format("%s - %s", dateStr, timeStr));

        holder.eventDescription.setText(spectacle.getDescription() != null ?
                spectacle.getDescription() : "Aucune description disponible");

        holder.eventAvailableTickets.setText(String.format("Places disponibles: %d",
                spectacle.getNbrSpectateur() != null ? spectacle.getNbrSpectateur() : 0));

        if (spectacle.getPrix() != null) {
            holder.eventPrice.setText(String.format(Locale.getDefault(), "%.2f DT", spectacle.getPrix()));
        } else {
            holder.eventPrice.setText("Prix N/A");
        }

        updateFavoriteIcon(holder.favoriteButton, spectacle.getIdSpec());

        holder.favoriteButton.setOnClickListener(v -> {
            if (favoriteManager.isFavorite(spectacle.getIdSpec())) {
                favoriteManager.removeFavorite(spectacle.getIdSpec());
                holder.favoriteButton.setImageResource(R.drawable.ic_heart_outline);
                Toast.makeText(context, "Retiré des favoris", Toast.LENGTH_SHORT).show();
            } else {
                favoriteManager.addFavorite(spectacle);
                holder.favoriteButton.setImageResource(R.drawable.ic_heart_filled);
                Toast.makeText(context, "Ajouté aux favoris", Toast.LENGTH_SHORT).show();
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
    }

    private void updateFavoriteIcon(ImageButton button, Long spectacleId) {
        if (spectacleId != null) {
            button.setImageResource(
                    favoriteManager.isFavorite(spectacleId)
                            ? R.drawable.ic_heart_filled
                            : R.drawable.ic_heart_outline
            );
        }
    }

    @Override
    public int getItemCount() {
        return spectacleList != null ? spectacleList.size() : 0;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        ImageButton favoriteButton;
        TextView eventTitle;
        TextView eventLocation;
        TextView eventDate;
        TextView eventDescription;
        TextView eventAvailableTickets;
        TextView eventPrice;
        MaterialButton detailsButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventDescription = itemView.findViewById(R.id.eventDescription);
            eventAvailableTickets = itemView.findViewById(R.id.eventAvailableTickets);
            eventPrice = itemView.findViewById(R.id.eventPrice);
            detailsButton = itemView.findViewById(R.id.detailsButton);
        }
    }
}