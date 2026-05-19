package com.usc.lugarlangfinal.driverconductor;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.usc.lugarlangfinal.R;
import com.usc.lugarlangfinal.models.Ticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TicketHistoryActivity extends AppCompatActivity {

    private RecyclerView rvTicketHistory;
    private TicketAdapter adapter;
    private List<Ticket> ticketList;
    private TextView tvEmptyHistory;
    private ImageButton btnBack;

    private String companyName, plateNumber;
    private final String DB_URL = "https://lugarlangfinal-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_history);

        companyName = getIntent().getStringExtra("COMPANY_NAME");
        plateNumber = getIntent().getStringExtra("PLATE_NUMBER");

        rvTicketHistory = findViewById(R.id.rvTicketHistory);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        ticketList = new ArrayList<>();
        adapter = new TicketAdapter(ticketList);
        rvTicketHistory.setLayoutManager(new LinearLayoutManager(this));
        rvTicketHistory.setAdapter(adapter);

        fetchTicketHistory();
    }

    private void fetchTicketHistory() {
        if (companyName == null || plateNumber == null) {
            Toast.makeText(this, "Error: Missing context", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance(DB_URL).getReference("ticket_logs")
                .child(companyName)
                .child(plateNumber);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ticketList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Ticket ticket = ds.getValue(Ticket.class);
                        if (ticket != null) {
                            ticketList.add(ticket);
                        }
                    }
                    // Reverse to show latest first
                    Collections.reverse(ticketList);
                    adapter.notifyDataSetChanged();
                    tvEmptyHistory.setVisibility(View.GONE);
                } else {
                    tvEmptyHistory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketHistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}