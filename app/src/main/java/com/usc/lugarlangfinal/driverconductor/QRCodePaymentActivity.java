package com.usc.lugarlangfinal.driverconductor;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.usc.lugarlangfinal.R;

public class QRCodePaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_payment);

        MaterialButton btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
        MaterialButton btnConfirmSMS = findViewById(R.id.btnConfirmSMS);
        TextInputEditText etSMSInput = findViewById(R.id.etSMSInput);

        if (btnConfirmPayment != null) {
            btnConfirmPayment.setOnClickListener(v -> {
                // Returns back to the previous screen
                finish();
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
                        Toast.makeText(this, "receipt sent", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "09 numbers must be 11 digits long", Toast.LENGTH_SHORT).show();
                    }
                } else if (smsNumber.startsWith("9")) {
                    if (smsNumber.length() == 10) {
                        Toast.makeText(this, "receipt sent", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "9 numbers must be 10 digits long", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Number must start with 09 or 9", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}