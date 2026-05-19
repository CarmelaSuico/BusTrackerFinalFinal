package com.usc.lugarlangfinal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.usc.lugarlangfinal.commuter.SeachingOriginDesti;

public class Settings extends AppCompatActivity {
    LinearLayout btnHomePage, btnSearch, btnSetting, btnAIHelp;

    MaterialButton btnPortal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnHomePage = findViewById(R.id.btnhomepage);
        btnSearch = findViewById(R.id.btnsearch);
        btnSetting = findViewById(R.id.btnsetting);
        btnAIHelp = findViewById(R.id.btnaihelp);
        btnPortal = findViewById(R.id.btnPortal);

        btnSetting.setSelected(true);

        btnHomePage.setOnClickListener(v -> {
            startActivity(new Intent(Settings.this, commuterhome.class));
        });

        btnSearch.setOnClickListener(v -> {
            startActivity(new Intent(Settings.this, SeachingOriginDesti.class));
        });

        btnAIHelp.setOnClickListener(v -> {
            startActivity(new Intent(Settings.this, AIChatbotActivity.class));
        });

        btnPortal.setOnClickListener(v -> {
            startActivity(new Intent(Settings.this, LoginPage.class));
        });
    }
}