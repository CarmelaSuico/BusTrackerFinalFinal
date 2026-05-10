package com.usc.lugarlangfinal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.usc.lugarlangfinal.adapters.ChatAdapter;
import com.usc.lugarlangfinal.models.ChatMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AIChatbotActivity extends AppCompatActivity {

    private static final String TAG = "AIChatbotActivity";
    private static final String DB_URL = "https://lugarlangfinal-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String API_FREE_LLM_URL = "https://apifreellm.com/api/v1/chat";
    private static final String API_FREE_LLM_KEY = "apf_e01xhboqhfcszahxvicusv13";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private RecyclerView rvChat;
    private TextInputEditText inputMessage;
    private ImageButton btnSend;
    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messages = new ArrayList<>();

    private final Map<String, Object> routeAnalytics = new HashMap<>();
    private final Map<String, Object> occupancyAnalytics = new HashMap<>();

    private FusedLocationProviderClient fusedLocationClient;
    private double userLat = 0.0;
    private double userLon = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chatbot);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Get location passed from commuterhome as a fallback
        userLat = getIntent().getDoubleExtra("user_lat", 0.0);
        userLon = getIntent().getDoubleExtra("user_lon", 0.0);

        rvChat = findViewById(R.id.rvChat);
        inputMessage = findViewById(R.id.inputMessage);
        btnSend = findViewById(R.id.btnSend);

        chatAdapter = new ChatAdapter(messages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        loadAnalyticsData();
        checkLocationPermission();

        btnSend.setOnClickListener(v -> {
            String message = inputMessage.getText() != null ? inputMessage.getText().toString().trim() : "";
            if (!message.isEmpty()) {
                inputMessage.setText("");
                addChatMessage(message, true);
                addChatMessage("Let me analyze that and give you a smart commuter answer...", false);
                handleUserMessage(message);
            }
        });
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchCurrentLocation();
        }
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    userLat = location.getLatitude();
                    userLon = location.getLongitude();
                    Log.d(TAG, "Location fetched: " + userLat + ", " + userLon);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied. AI suggestions might be less accurate.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addChatMessage(String text, boolean fromUser) {
        ChatMessage item = new ChatMessage(text, fromUser, System.currentTimeMillis());
        messages.add(item);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
    }

    private void removeTypingMessage() {
        int last = messages.size() - 1;
        if (last >= 0 && !messages.get(last).isFromUser()) {
            String text = messages.get(last).getText();
            if (text.startsWith("Let me analyze") || text.startsWith("Thinking")) {
                messages.remove(last);
                chatAdapter.notifyItemRemoved(last);
            }
        }
    }

    private void handleUserMessage(String userMessage) {
        new Thread(() -> {
            String response = getAssistantResponse(userMessage);
            runOnUiThread(() -> {
                removeTypingMessage();
                addChatMessage(response, false);
            });
        }).start();
    }

    private String getAssistantResponse(String userMessage) {
        String context = buildAnalyticsSummary();
        try {
            return callApiFreeLlm(userMessage, context);
        } catch (Exception e) {
            Log.e(TAG, "API Free LLM request failed", e);
            return "I could not reach the AI service right now. Error: " + e.getMessage() + "\n\n" + generateLocalResponse(userMessage, context);
        }
    }

    private void loadAnalyticsData() {
        DatabaseReference analyticsRef = FirebaseDatabase.getInstance(DB_URL).getReference("analytics");
        analyticsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                routeAnalytics.clear();
                occupancyAnalytics.clear();
                if (snapshot.hasChild("route_stats")) {
                    for (DataSnapshot routeSnapshot : snapshot.child("route_stats").getChildren()) {
                        routeAnalytics.put(routeSnapshot.getKey(), routeSnapshot.getValue());
                    }
                }
                if (snapshot.hasChild("occupancy")) {
                    for (DataSnapshot occupancySnapshot : snapshot.child("occupancy").getChildren()) {
                        occupancyAnalytics.put(occupancySnapshot.getKey(), occupancySnapshot.getValue());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                runOnUiThread(() -> Toast.makeText(AIChatbotActivity.this, "Analytics load failed: " + error.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String buildAnalyticsSummary() {
        StringBuilder summary = new StringBuilder();
        
        // Feed current time and user location to AI
        String currentTime = new SimpleDateFormat("HH:mm, EEEE", Locale.getDefault()).format(new Date());
        summary.append("Current Time: ").append(currentTime).append("\n");
        if (userLat != 0.0 && userLon != 0.0) {
            summary.append("User Current Coordinates: ").append(userLat).append(", ").append(userLon).append("\n");
        }

        if (routeAnalytics.isEmpty() && occupancyAnalytics.isEmpty()) {
            summary.append("Note: No specific historical route stats or occupancy patterns available yet.\n");
        } else {
            if (!routeAnalytics.isEmpty()) {
                summary.append("Historical Route Stats (Avg ETAs/Traffic Patterns):\n");
                for (Map.Entry<String, Object> entry : routeAnalytics.entrySet()) {
                    summary.append("- ").append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
                }
            }
            if (!occupancyAnalytics.isEmpty()) {
                summary.append("Occupancy Patterns (Seat Availability Trends):\n");
                for (Map.Entry<String, Object> entry : occupancyAnalytics.entrySet()) {
                    summary.append("- ").append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
                }
            }
        }
        
        return summary.toString();
    }

    private String generateLocalResponse(String userMessage, String context) {
        String normalized = userMessage.toLowerCase(Locale.ROOT);
        boolean askedETA = normalized.contains("eta") || normalized.contains("arrival") || normalized.contains("arrive") || normalized.contains("time");
        boolean askedSeats = normalized.contains("seat") || normalized.contains("crowd") || normalized.contains("full") || normalized.contains("available");
        boolean askedLeave = normalized.contains("leave") || normalized.contains("walking") || normalized.contains("best time") || normalized.contains("catch");

        if (askedETA) {
            return "Predictive ETA helper:\n" +
                    "• I use historical travel speeds and traffic patterns to improve arrival predictions.\n" +
                    "• If the route is late at 5 PM on Fridays, I suggest adding a buffer of 10–15 minutes.";
        }

        if (askedSeats) {
            return "Seat availability forecasting:\n" +
                    "• I predict 'Crowded', 'Standing Room Only', or 'Seats Available' based on past boarding patterns.\n" +
                    "• For example: The 8:00 AM bus is usually 95% full, while the 8:15 AM bus is typically only 40% full.";
        }

        if (askedLeave) {
            return "Best time to leave recommendations:\n" +
                    "• I compare the bus ETA, your walking distance (about 1.2m/s walking speed), and a safety margin.\n" +
                    "• If the bus is 10 minutes away and you need 8 minutes to reach the stop, you should leave now!";
        }

        return "I can help with predictive ETA, seat availability forecasting, and best-time-to-leave recommendations. " +
                "Try asking: 'What is the expected arrival time for my next bus?' or 'Will the 8:00 AM bus be crowded?'";
    }

    private String callApiFreeLlm(String userMessage, String context) throws Exception {
        String systemPersona = "You are 'LugarLang Assistant', an intelligent bus tracking helper in Cebu. " +
                "Use the provided DATA CONTEXT (historical stats, occupancy, current time, user location) to give smart advice.\n\n" +
                "CORE CAPABILITIES:\n" +
                "1. Intelligent Seat Forecasting: Predict 'Crowded' or 'Seats Available' based on 'Occupancy Patterns' for the current time/route.\n" +
                "2. Best Time to Leave: Compare bus ETA with current time and user's walking distance. Assume average walking speed of 5 km/h if distance is known.\n" +
                "3. Predictive ETA: Adjust standard schedules based on 'Historical Route Stats' (e.g., peak hour traffic).\n" +
                "4. Alternative Suggestions: If a bus is usually full, suggest a less crowded alternative nearby in time.\n\n" +
                "DATA CONTEXT:\n" + context;

        JSONObject payload = new JSONObject();
        payload.put("message", systemPersona + "\n\nUser Question: " + userMessage);
        payload.put("model", "apifreellm");

        URL url = new URL(API_FREE_LLM_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Authorization", "Bearer " + API_FREE_LLM_KEY);
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.toString().getBytes("UTF-8"));
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        Log.d(TAG, "API Response Code: " + responseCode);

        InputStream stream = responseCode == HttpURLConnection.HTTP_OK
                ? connection.getInputStream()
                : connection.getErrorStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            String responseText = responseBuilder.toString();
            Log.d(TAG, "API Response: " + responseText);

            JSONObject responseJson = new JSONObject(responseText);
            if (responseJson.optBoolean("success", false)) {
                return responseJson.optString("response", "No response content.");
            } else {
                return "API Error: " + responseJson.optString("message", "Unknown error");
            }
        }
    }
}
