package com.usc.lugarlangfinal;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AIChatbotActivity extends AppCompatActivity {

    private static final String DB_URL = "https://lugarlangfinal-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_API_KEY = ""; // Set this via a secure backend or environment variable instead of hard-coding in production.

    private RecyclerView rvChat;
    private TextInputEditText inputMessage;
    private ImageButton btnSend;
    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messages = new ArrayList<>();

    private final Map<String, Object> routeAnalytics = new HashMap<>();
    private final Map<String, Object> occupancyAnalytics = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chatbot);

        rvChat = findViewById(R.id.rvChat);
        inputMessage = findViewById(R.id.inputMessage);
        btnSend = findViewById(R.id.btnSend);

        chatAdapter = new ChatAdapter(messages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        loadAnalyticsData();

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
        if (!OPENAI_API_KEY.isEmpty()) {
            try {
                return callOpenAIAssistant(userMessage, context);
            } catch (Exception e) {
                e.printStackTrace();
                return "I could not reach the AI service right now. " + generateLocalResponse(userMessage, context);
            }
        }
        return generateLocalResponse(userMessage, context);
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
        if (routeAnalytics.isEmpty() && occupancyAnalytics.isEmpty()) {
            return "No route analytics or occupancy history is available yet.";
        }

        StringBuilder summary = new StringBuilder();
        if (!routeAnalytics.isEmpty()) {
            summary.append("Route historical analytics loaded for ").append(routeAnalytics.size()).append(" routes.\n");
        }
        if (!occupancyAnalytics.isEmpty()) {
            summary.append("Occupancy patterns loaded for ").append(occupancyAnalytics.size()).append(" routes.\n");
        }
        return summary.toString();
    }

    private String generateLocalResponse(String userMessage, String context) {
        String normalized = userMessage.toLowerCase(Locale.ROOT);
        boolean askedETA = normalized.contains("eta") || normalized.contains("arrival") || normalized.contains("arrive") || normalized.contains("time");
        boolean askedSeats = normalized.contains("seat") || normalized.contains("crowd") || normalized.contains("full") || normalized.contains("available");
        boolean askedLeave = normalized.contains("leave") || normalized.contains("walking") || normalized.contains("best time") || normalized.contains("catch");

        if (askedETA) {
            String routeInfo = routeAnalytics.isEmpty() ? "I don't have specific historical travel times yet." : "I can use your historical route data to improve arrival predictions.";
            return "Predictive ETA helper:\n" +
                    "• Travel speed and traffic patterns are best derived from past route runs. " + routeInfo + "\n" +
                    "• If the route is late at 5 PM on Fridays, the app should add a buffer of 10–15 minutes.\n" +
                    "• If weather is rainy, estimate slower speeds and longer dwell time at stops.\n" +
                    "To complete this, store route history under analytics/route_stats and call the model from backend or local heuristics.";
        }

        if (askedSeats) {
            String occupancyInfo = occupancyAnalytics.isEmpty() ? "I don't have full occupancy history yet." : "I can forecast crowdedness from historical seat usage by route and hour.";
            return "Seat availability forecasting:\n" +
                    "• Predict 'Crowded', 'Standing Room Only', or 'Seats Available' based on past boarding patterns. " + occupancyInfo + "\n" +
                    "• For better accuracy, store trip load patterns in analytics/occupancy and update them after each trip.\n" +
                    "• If a camera is available, use ML Kit or TensorFlow Lite on-device to count passengers and write the result to Firebase.";
        }

        if (askedLeave) {
            return "Best time to leave recommendations:\n" +
                    "• Compare the bus ETA, your walking distance, and a safety margin.\n" +
                    "• If the bus is 10 minutes away and you need 8 minutes to reach the stop, tell the commuter to leave now.\n" +
                    "• If the next scheduled bus is often less crowded, offer it as an alternative.\n" +
                    "This can be built from live location, walking distance, and historical route occupancy.";
        }

        return "I can help with predictive ETA, seat availability forecasting, and best-time-to-leave recommendations. " +
                "Try asking: 'What is the expected arrival time for my next bus?' or 'Will the 8:00 AM bus be crowded?'";
    }

    private String callOpenAIAssistant(String userMessage, String context) throws Exception {
        JSONObject systemMessage = new JSONObject()
                .put("role", "system")
                .put("content", "You are an intelligent commuter assistant for a bus tracking Android app. Provide concise, actionable commuter guidance based on route history, traffic, weather, stop dwell time, and seat availability.");

        JSONObject userMessageObj = new JSONObject()
                .put("role", "user")
                .put("content", "Context: " + context + "\nUser: " + userMessage);

        JSONArray messagesArray = new JSONArray();
        messagesArray.put(systemMessage);
        messagesArray.put(userMessageObj);

        JSONObject payload = new JSONObject();
        payload.put("model", "gpt-3.5-turbo");
        payload.put("messages", messagesArray);
        payload.put("max_tokens", 400);
        payload.put("temperature", 0.7);

        URL url = new URL(OPENAI_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.toString().getBytes("UTF-8"));
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        InputStream stream = responseCode == HttpURLConnection.HTTP_OK
                ? connection.getInputStream()
                : connection.getErrorStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            JSONObject responseJson = new JSONObject(responseBuilder.toString());
            JSONArray choices = responseJson.optJSONArray("choices");
            if (choices != null && choices.length() > 0) {
                JSONObject messageObject = choices.getJSONObject(0).getJSONObject("message");
                return messageObject.optString("content", "I couldn't generate a response.");
            }
            return "I couldn't parse the AI response.";
        }
    }
}
