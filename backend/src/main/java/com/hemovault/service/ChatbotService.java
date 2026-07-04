package com.hemovault.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hemovault.dto.InventorySummary;
import com.hemovault.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ChatbotService {

    @Value("${google.gemini.api.key}")
    private String geminiApiKey;

    @Value("${google.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public ChatbotService(InventoryService inventoryService, ObjectMapper objectMapper) {
        this.inventoryService = inventoryService;
        this.objectMapper = objectMapper;
    }

    public String chat(String userMessage, User user) {
        String systemPrompt = buildSystemPrompt(user);
        try {
            return callGemini(userMessage, systemPrompt);
        } catch (IOException e) {
            log.error("Gemini API network error: {}", e.getMessage());
            return "I'm having trouble connecting to the AI service right now. Please try again in a moment.";
        } catch (Exception e) {
            log.error("Gemini API error: {}", e.getMessage());
            return "An error occurred while processing your request. Please try again.";
        }
    }

    private String buildSystemPrompt(User user) {
        List<InventorySummary> inventory = inventoryService.getSummary();

        StringBuilder inv = new StringBuilder();
        for (InventorySummary s : inventory) {
            inv.append(String.format("  %s: %.1f units [%s]%n",
                    s.getBloodGroupLabel(), s.getTotalUnits(), s.getStockStatus()));
        }

        return """
                You are HemoVault AI Assistant — an intelligent assistant for the HemoVault Smart Blood Bank Management System.
                You have access to real-time blood inventory data and system knowledge.

                === CURRENT BLOOD INVENTORY ===
                %s
                === CURRENT USER ===
                Name: %s
                Role: %s
                Admin Access: %s

                === BLOOD COMPATIBILITY RULES ===
                - O- is the universal donor (can donate to all blood groups)
                - AB+ is the universal recipient (can receive from all blood groups)
                - O+ can donate to: A+, B+, AB+, O+
                - A+ can donate to: A+, AB+
                - A- can donate to: A+, A-, AB+, AB-
                - B+ can donate to: B+, AB+
                - B- can donate to: B+, B-, AB+, AB-
                - AB- can donate to: AB+, AB-

                === DONOR ELIGIBILITY CRITERIA ===
                - Age: 18–65 years
                - Weight: minimum 50 kg
                - Hemoglobin: minimum 12.5 g/dL
                - Blood pressure: systolic 90–160, diastolic 60–100 mmHg
                - Last donation: minimum 90 days ago
                - No illness in past 2 weeks, not pregnant, not on blood thinners, no tattoo in 6 months

                === SYSTEM RULES ===
                - Blood requests require a clinical reason (minimum 20 characters)
                - When a request is approved, blood is automatically deducted from inventory using FIFO
                - Sensitive donor data (contact, email, weight, BP) is visible to admins only
                - Blood units expire 35 days after collection
                - Shortage alerts: WARNING below threshold, CRITICAL below critical threshold

                === YOUR ROLE ===
                - Answer questions about blood inventory, compatibility, eligibility, and requests
                - Provide helpful guidance to hospital staff and donors
                - If asked about sensitive operations (approvals, deletions), remind non-admins of access levels
                - Be concise, helpful, and medically accurate
                - If you don't know something specific, say so honestly
                """.formatted(
                inv.toString(),
                user.getFullName() != null ? user.getFullName() : user.getUsername(),
                user.getRole().name(),
                user.getIsAdmin() ? "Yes" : "No"
        );
    }

    private String callGemini(String userMessage, String systemPrompt) throws IOException {
        // Gemini API endpoint
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + geminiModel + ":generateContent?key=" + geminiApiKey;

        // Build request body
        ObjectNode requestBody = objectMapper.createObjectNode();

        // System instruction
        ObjectNode systemInstruction = objectMapper.createObjectNode();
        ObjectNode systemPart = objectMapper.createObjectNode();
        systemPart.put("text", systemPrompt);
        ArrayNode systemParts = objectMapper.createArrayNode();
        systemParts.add(systemPart);
        systemInstruction.set("parts", systemParts);
        requestBody.set("systemInstruction", systemInstruction);

        // Contents
        ArrayNode contents = objectMapper.createArrayNode();
        ObjectNode content = objectMapper.createObjectNode();
        content.put("role", "user");
        ArrayNode parts = objectMapper.createArrayNode();
        ObjectNode part = objectMapper.createObjectNode();
        part.put("text", userMessage);
        parts.add(part);
        content.set("parts", parts);
        contents.add(content);
        requestBody.set("contents", contents);

        // Generation config
        ObjectNode genConfig = objectMapper.createObjectNode();
        genConfig.put("maxOutputTokens", 1024);
        genConfig.put("temperature", 0.7);
        requestBody.set("generationConfig", genConfig);

        RequestBody body = RequestBody.create(
                objectMapper.writeValueAsString(requestBody),
                MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "no body";
                log.error("Gemini API error {}: {}", response.code(), errorBody);
                return "The AI service returned an error (HTTP " + response.code() + "). Please try again.";
            }

            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);

            // Parse Gemini response: candidates[0].content.parts[0].text
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode text = candidates.get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text");
                if (!text.isMissingNode()) {
                    return text.asText();
                }
            }

            log.error("Unexpected Gemini response structure: {}", responseBody);
            return "I received an unexpected response from the AI service. Please try again.";
        }
    }
}
