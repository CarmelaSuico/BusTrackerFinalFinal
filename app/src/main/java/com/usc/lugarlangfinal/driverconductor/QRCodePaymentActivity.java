package com.usc.lugarlangfinal.driverconductor;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.usc.lugarlangfinal.R;

public class QRCodePaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_payment);

        MaterialButton btnPaymentConfirmed = findViewById(R.id.btnPaymentConfirmed);
        MaterialButton btnUseSMS = findViewById(R.id.btnUseSMS);

        btnPaymentConfirmed.setOnClickListener(v -> {
            // Returns back to the previous screen
            finish();
        });

        btnUseSMS.setOnClickListener(v -> {
            // No function yet as requested
            Toast.makeText(this, "SMS function coming soon", Toast.LENGTH_SHORT).show();
        });
    }
}