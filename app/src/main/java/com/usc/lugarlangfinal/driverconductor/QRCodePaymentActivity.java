package com.usc.lugarlangfinal.driverconductor;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.usc.lugarlangfinal.R;

import java.util.HashMap;
import java.util.Map;

public class QRCodePaymentActivity extends AppCompatActivity {

    private String companyName, plateNumber, origin, destination, passengerType, routeCode;
    private double regularFare, discount, totalFare;
    private final String DB_URL = "https://lugarlangfinal-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_payment);

        // Extract data from Intent
        companyName = getIntent().getStringExtra("COMPANY_NAME");
        plateNumber = getIntent().getStringExtra("PLATE_NUMBER");
        origin = getIntent().getStringExtra("ORIGIN");
        destination = getIntent().getStringExtra("DESTINATION");
        passengerType = getIntent().getStringExtra("PASSENGER_TYPE");
        routeCode = getIntent().getStringExtra("ROUTE_CODE");
        regularFare = getIntent().getDoubleExtra("REGULAR_FARE", 0);
        discount = getIntent().getDoubleExtra("DISCOUNT", 0);
        totalFare = getIntent().getDoubleExtra("TOTAL_FARE", 0);

        MaterialButton btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
        MaterialButton btnConfirmSMS = findViewById(R.id.btnConfirmSMS);
        TextInputEditText etSMSInput = findViewById(R.id.etSMSInput);

        if (btnConfirmPayment != null) {
            btnConfirmPayment.setOnClickListener(v -> {
                saveTicketToFirebase("QR");
            });
        }

        if (btnConfirmSMS != null && etSMSInput != null) {
            btnConfirmSMS.setOnClickListener(v -> {
                String smsNumber = etSMSInput.getText().toString().trim();
                
                if (smsNumber.isEmpty()) {
                    Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!smsNumber.matches("\\d+")) {
                    Toast.makeText(this, "Number must contain only digits", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (smsNumber.startsWith("09")) {
                    if (smsNumber.length() == 11) {
                        saveTicketToFirebase("SMS: " + smsNumber);
                    } else {
                        Toast.makeText(this, "09 numbers must be 11 digits long", Toast.LENGTH_SHORT).show();
                    }
                } else if (smsNumber.startsWith("9")) {
                    if (smsNumber.length() == 10) {
                        saveTicketToFirebase("SMS: " + smsNumber);
                    } else {
                        Toast.makeText(this, "9 numbers must be 10 digits long", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Number must start with 09 or 9", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveTicketToFirebase(String paymentMethod) {
        if (companyName == null || plateNumber == null) {
            Toast.makeText(this, "Error: Missing context", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference ticketsRef = FirebaseDatabase.getInstance(DB_URL).getReference("ticket_logs")
                .child(companyName)
                .child(plateNumber);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.getDefault());
        String readableTimestamp = sdf.format(new java.util.Date());

        Map<String, Object> ticket = new HashMap<>();
        ticket.put("origin", origin);
        ticket.put("destination", destination);
        ticket.put("passenger_type", passengerType);
        ticket.put("regular_fare", Math.round(regularFare * 100.0) / 100.0);
        ticket.put("discount", Math.round(discount * 100.0) / 100.0);
        ticket.put("total_fare", Math.round(totalFare * 100.0) / 100.0);
        ticket.put("timestamp", readableTimestamp);
        ticket.put("route_code", routeCode);
        ticket.put("payment_method", paymentMethod);

        ticketsRef.push().setValue(ticket)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Ticket Issued & Payment Confirmed", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save ticket", Toast.LENGTH_SHORT).show();
                });
    }
}