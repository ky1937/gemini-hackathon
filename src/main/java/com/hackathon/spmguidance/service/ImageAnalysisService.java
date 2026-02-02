package com.hackathon.spmguidance.service;

import java.util.ArrayList;
import java.util.Base64;
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
import org.springframework.web.multipart.MultipartFile;

import com.hackathon.spmguidance.dto.AnalysisResponse;
import com.hackathon.spmguidance.dto.Recommendation;
import com.hackathon.spmguidance.dto.SpmSubject;

@Service
public class ImageAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(ImageAnalysisService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String VISION_API_URL = 
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-pro-image-preview:generateContent";

    public AnalysisResponse analyzeImageAndRecommend(MultipartFile file, String interests, String location, String familyIncome) {
        AnalysisResponse response = new AnalysisResponse();
        
        try {
            logger.info("Analyzing SPM result image");
            
            byte[] imageBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            List<SpmSubject> subjects = extractSpmResults(base64Image);
            response.setExtractedSubjects(subjects);
            
            if (subjects.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Could not extract SPM results from image. Please ensure the image is clear and contains SPM grades.");
                return response;
            }
            
            String prompt = buildRecommendationPrompt(subjects, interests, location, familyIncome);
            String recommendations = getGeminiResponse(prompt, null);
            
            parseRecommendations(response, recommendations);
            
            response.setSuccess(true);
            response.setMessage("Analysis completed successfully");
            response.setSummary(recommendations);
            
            logger.info("Analysis completed successfully");
            
        } catch (Exception e) {
            logger.error("Error analyzing image", e);
            response.setSuccess(false);
            response.setMessage("Error analyzing image: " + e.getMessage());
        }
        
        return response;
    }

    private List<SpmSubject> extractSpmResults(String base64Image) {
        try {
            String prompt = "Analyze this SPM result slip image. Extract all subjects and their grades. " +
                          "Return ONLY a JSON array in this exact format: " +
                          "[{\"name\":\"Bahasa Melayu\",\"grade\":\"A\"},{\"name\":\"Mathematics\",\"grade\":\"A+\"}] " +
                          "Include all subjects visible in the image.";
            
            String jsonResponse = getGeminiResponse(prompt, base64Image);
            
            return parseSubjectsFromJson(jsonResponse);
            
        } catch (Exception e) {
            logger.error("Error extracting SPM results", e);
            return new ArrayList<>();
        }
    }

    private String getGeminiResponse(String prompt, String base64Image) {
        try {
            String url = VISION_API_URL + "?key=" + apiKey;
            
            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);
            parts.add(textPart);
            
            if (base64Image != null) {
                Map<String, Object> imagePart = new HashMap<>();
                Map<String, Object> inlineData = new HashMap<>();
                inlineData.put("mimeType", "image/jpeg");
                inlineData.put("data", base64Image);
                imagePart.put("inlineData", inlineData);
                parts.add(imagePart);
            }
            
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);
            
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            
            return extractTextFromResponse(response);
            
        } catch (Exception e) {
            logger.error("Error calling Gemini Vision API", e);
            throw new RuntimeException("Failed to analyze image", e);
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
            return "";
        } catch (Exception e) {
            logger.error("Error parsing response", e);
            return "";
        }
    }

    private List<SpmSubject> parseSubjectsFromJson(String jsonResponse) {
        List<SpmSubject> subjects = new ArrayList<>();
        try {
            jsonResponse = jsonResponse.replaceAll("```json", "").replaceAll("```", "").trim();
            
            jsonResponse = jsonResponse.trim();
            if (jsonResponse.startsWith("[") && jsonResponse.endsWith("]")) {
                jsonResponse = jsonResponse.substring(1, jsonResponse.length() - 1);
                String[] items = jsonResponse.split("},\\s*\\{");
                
                for (String item : items) {
                    item = item.replaceAll("[{}]", "").trim();
                    String[] pairs = item.split(",\\s*\"");
                    
                    String name = null;
                    String grade = null;
                    
                    for (String pair : pairs) {
                        String[] keyValue = pair.split(":\\s*\"");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].replaceAll("\"", "").trim();
                            String value = keyValue[1].replaceAll("\"", "").trim();
                            
                            if (key.equals("name")) {
                                name = value;
                            } else if (key.equals("grade")) {
                                grade = value;
                            }
                        }
                    }
                    
                    if (name != null && grade != null) {
                        subjects.add(new SpmSubject(name, grade));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing subjects JSON", e);
        }
        return subjects;
    }

    private String buildRecommendationPrompt(List<SpmSubject> subjects, String interests, String location, String familyIncome) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("As an AI Education Guidance Assistant for Malaysian students, analyze these SPM results and provide comprehensive recommendations:\n\n");
        
        prompt.append("SPM Results:\n");
        for (SpmSubject subject : subjects) {
            prompt.append("- ").append(subject.getName()).append(": ").append(subject.getGrade()).append("\n");
        }
        
        if (interests != null && !interests.isEmpty()) {
            prompt.append("\nStudent Interests: ").append(interests);
        }
        if (location != null && !location.isEmpty()) {
            prompt.append("\nPreferred Location: ").append(location);
        }
        if (familyIncome != null && !familyIncome.isEmpty()) {
            prompt.append("\nFamily Income Level: ").append(familyIncome);
        }
        
        prompt.append("\n\nProvide:\n");
        prompt.append("1. Top 5 Malaysian universities that match these results\n");
        prompt.append("2. Top 5 recommended courses/programs\n");
        prompt.append("3. Top 5 scholarships the student is eligible for\n");
        prompt.append("4. Brief analysis of strengths and suitable career paths\n");
        
        return prompt.toString();
    }

    private void parseRecommendations(AnalysisResponse response, String recommendations) {
        List<Recommendation> universities = new ArrayList<>();
        List<Recommendation> courses = new ArrayList<>();
        List<Recommendation> scholarships = new ArrayList<>();
        
        universities.add(new Recommendation("Universiti Malaya", "university", "Top ranked public university", "High", "Strong academic performance"));
        courses.add(new Recommendation("Computer Science", "course", "High demand field", "High", "Strong in Mathematics and Science"));
        scholarships.add(new Recommendation("JPA Scholarship", "scholarship", "Government scholarship", "Medium", "Good SPM results"));
        
        response.setUniversities(universities);
        response.setCourses(courses);
        response.setScholarships(scholarships);
    }
}