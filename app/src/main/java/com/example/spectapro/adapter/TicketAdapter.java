package com.example.spectapro.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.spectapro.R;
import com.example.spectapro.TicketDetailActivity;
import com.example.spectapro.model.Billet;
import com.example.spectapro.model.Spectacle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private List<Billet> ticketList = new ArrayList<>();
    private final OnTicketClickListener listener;
    private final Context context;

    public interface OnTicketClickListener {
        void onTicketClick(Billet billet);
    }

    public TicketAdapter(Context context, OnTicketClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void updateList(List<Billet> newList) {
        this.ticketList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Billet billet = ticketList.get(position);
        Spectacle spectacle = billet.getSpectacle() != null ? billet.getSpectacle() : new Spectacle();

        // Afficher toutes les informations
        holder.tvBookingCode.setText("Réservation #" + billet.getIdBillet());
        holder.tvEventTitle.setText(spectacle.getTitre());

        // Formatage de la date
        if (spectacle.getDate() != null) {
            holder.tvEventDate.setText(new SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())
                    .format(spectacle.getDate()));
        } else {
            holder.tvEventDate.setText("Date inconnue");
        }

        // Lieu
        if (spectacle.getLieu() != null) {
            holder.tvEventLocation.setText(spectacle.getLieu().getNomLieu() + ", " + spectacle.getLieu().getVille());
        } else {
            holder.tvEventLocation.setText("Lieu non spécifié");
        }

        holder.tvTicketType.setText(billet.getCategorie());
        holder.tvTicketPrice.setText(String.format(Locale.getDefault(), "%.2fDT", billet.getPrix()));

        holder.itemView.setOnClickListener(v -> {
            // Lorsqu'on clique, on ouvre à nouveau l'activité de confirmation
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, TicketDetailActivity.class);
            intent.putExtra("billet", billet);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingCode, tvEventTitle, tvEventDate, tvEventLocation,
                tvTicketType, tvTicketPrice, tvConfirmationStatus;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingCode = itemView.findViewById(R.id.tvBookingCode);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvTicketType = itemView.findViewById(R.id.tvTicketType);
            tvTicketPrice = itemView.findViewById(R.id.tvTicketPrice);
            tvConfirmationStatus = itemView.findViewById(R.id.tvConfirmationStatus);
        }
    }
}