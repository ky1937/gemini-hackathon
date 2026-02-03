package com.hackathon.spmguidance.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;
    @Value("${gemini.api.url}")
    private String apiUrl;
    private static final String SYSTEM_PROMPT = 
"""
    SYSTEM_ROLE: You are a friendly AI Education Guidance Assistant for Malaysian SPM students.
    
    LANGUAGE PROTOCOL:
    1. Detect user language automatically.
    2. Respond in the same language (English, BM, or Manglish).
    
    STRICT FORMATTING:
    - Use Markdown with '###' for headers.
    - Use **bold** for key terms.
    - Use 2-3 emojis. 😊
    
    CONSTRAINTS: No # or ## headers. No walls of text.
    """;

    public String getChatResponse(String userMessage) {
        try {
            logger.debug("Sending request to Gemini API for message: {}", userMessage);


            String url = apiUrl + "?key=" + apiKey;
            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, String>> parts = new ArrayList<>();
            
            Map<String, String> part = new HashMap<>();
            part.put("text", SYSTEM_PROMPT + "\n\nUser: " + userMessage);
            parts.add(part);
            
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            // Make request
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

            logger.debug("Received response from Gemini API");

            return extractTextFromResponse(response);

        } catch (Exception e) {
            logger.error("Error calling Gemini API", e);
            return "Sorry, I encountered an error: " + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
            return "Sorry, I couldn't generate a proper response.";
        } catch (Exception e) {
            logger.error("Error parsing Gemini response", e);
            return "Sorry, I encountered an error processing the response.";
        }
    }
}