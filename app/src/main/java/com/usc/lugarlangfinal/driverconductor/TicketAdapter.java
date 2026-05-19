package com.usc.lugarlangfinal.driverconductor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.usc.lugarlangfinal.R;
import com.usc.lugarlangfinal.models.Ticket;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<Ticket> ticketList;

    public TicketAdapter(List<Ticket> ticketList) {
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket_history, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);
        holder.tvOrigin.setText(ticket.getOrigin());
        holder.tvDestination.setText(ticket.getDestination());
        holder.tvTimestamp.setText(ticket.getTimestamp());
        holder.tvTotal.setText(String.format("Php %.2f", ticket.getTotal_fare()));
        holder.tvType.setText(ticket.getPassenger_type());
        holder.tvPayment.setText("Paid via: " + ticket.getPayment_method());
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrigin, tvDestination, tvTimestamp, tvTotal, tvType, tvPayment;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrigin = itemView.findViewById(R.id.tvHistoryOrigin);
            tvDestination = itemView.findViewById(R.id.tvHistoryDestination);
            tvTimestamp = itemView.findViewById(R.id.tvHistoryTimestamp);
            tvTotal = itemView.findViewById(R.id.tvHistoryTotal);
            tvType = itemView.findViewById(R.id.tvHistoryType);
            tvPayment = itemView.findViewById(R.id.tvHistoryPayment);
        }
    }
}