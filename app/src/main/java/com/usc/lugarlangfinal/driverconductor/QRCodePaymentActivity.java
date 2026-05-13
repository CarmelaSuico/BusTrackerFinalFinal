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

        MaterialButton btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
        MaterialButton btnConfirmSMS = findViewById(R.id.btnConfirmSMS);

        if (btnConfirmPayment != null) {
            btnConfirmPayment.setOnClickListener(v -> {
                // Returns back to the previous screen
                finish();
            });
        }

        if (btnConfirmSMS != null) {
            btnConfirmSMS.setOnClickListener(v -> {
                // Shows toast and returns back to the previous screen
                Toast.makeText(this, "receipt sent", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }
}