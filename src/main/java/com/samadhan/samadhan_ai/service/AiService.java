package com.samadhan.samadhan_ai.service;

import com.samadhan.samadhan_ai.model.ChatMessage;
import com.samadhan.samadhan_ai.repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class AiService {

    @Autowired
    private ChatRepository chatRepository;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public String askQuestion(String userQuery, String domain, String language) {
        String mockResponse = "";

        // Domain-specific keyword matching (simple but effective)
        String queryLower = userQuery.toLowerCase();

        if (domain.equals("agriculture")) {
            if (queryLower.contains("खरपतवार") || queryLower.contains("weed") || queryLower.contains("खरपतवार हटाना")) {
                if (language.equals("hi")) mockResponse = "धान की फसल में खरपतवार हटाने के लिए: 1) निराई-गुड़ाई करें, 2) खरपतवारनाशक दवा (जैसे बुटाक्लोर) का छिड़काव करें, 3) समय पर पानी निकालें।";
                else if (language.equals("mr")) mockResponse = "धानाच्या पिकातील तण काढण्यासाठी: 1) निराई-गुडाई करा, 2) तणनाशक (बुटाक्लोर) फवारा करा, 3) वेळेवर पाणी सोडा.";
                else mockResponse = "To remove weeds in paddy: 1) Hand weeding, 2) Spray butachlor herbicide, 3) Drain water at proper time.";
            }
            else if (queryLower.contains("गेहूं") || queryLower.contains("wheat")) {
                if (language.equals("hi")) mockResponse = "गेहूं की फसल के लिए यूरिया खाद डालें और नियमित सिंचाई करें। नजदीकी कृषि अधिकारी से संपर्क करें।";
                else mockResponse = "For wheat crop, use urea fertilizer and regular irrigation.";
            }
            else {
                if (language.equals("hi")) mockResponse = "कृपया फसल का नाम और समस्या बताएं। हम आपकी मदद करेंगे।";
                else mockResponse = "Please specify crop name and problem.";
            }
        }
        else if (domain.equals("health")) {
            if (queryLower.contains("सिर दर्द") || queryLower.contains("headache")) {
                if (language.equals("hi")) mockResponse = "सिरदर्द के लिए: पानी पिएं, आराम करें। यदि तेज दर्द हो तो पैरासिटामोल ले सकते हैं। डॉक्टर से सलाह लें।";
                else mockResponse = "For headache: drink water, rest. If severe, take paracetamol. Consult doctor.";
            }
            else if (queryLower.contains("बुखार") || queryLower.contains("fever")) {
                if (language.equals("hi")) mockResponse = "बुखार में आराम करें, गर्म पानी पिएं। यदि 3 दिन से अधिक हो तो डॉक्टर से मिलें।";
                else mockResponse = "For fever: rest, drink warm water. If more than 3 days, see doctor.";
            }
            else {
                if (language.equals("hi")) mockResponse = "कृपया अपनी बीमारी या लक्षण बताएं। हम बुनियादी सलाह दे सकते हैं।";
                else mockResponse = "Please tell your symptoms for basic advice.";
            }
        }
        else if (domain.equals("scheme")) {
            if (queryLower.contains("pm किसान") || queryLower.contains("kisan")) {
                if (language.equals("hi")) mockResponse = "PM किसान सम्मान निधि: हर साल 6000 रुपये तीन किस्तों में। आवेदन ग्राम पंचायत या CSC केंद्र पर करें। दस्तावेज़: आधार, भूमि रिकॉर्ड।";
                else mockResponse = "PM Kisan Samman Nidhi: Rs 6000/year in 3 installments. Apply at Gram Panchayat or CSC. Documents: Aadhaar, land records.";
            }
            else {
                if (language.equals("hi")) mockResponse = "कृपया योजना का नाम बताएं (जैसे PM किसान, आयुष्मान भारत, बेटी बचाओ)।";
                else mockResponse = "Please mention scheme name (PM Kisan, Ayushman Bharat, etc.).";
            }
        }
        else {
            mockResponse = "कृपया सही डोमेन चुनें: कृषि, स्वास्थ्य, या सरकारी योजना।";
        }

        // Save to database
        ChatMessage message = new ChatMessage(userQuery, mockResponse, domain, language);
        chatRepository.save(message);

        return mockResponse;
    }

    private String buildPrompt(String query, String domain, String language) {
        String domainInstruction = "";
        switch(domain) {
            case "agriculture":
                domainInstruction = "You are an agricultural expert for Indian farmers. Answer briefly in 1-2 sentences. ";
                break;
            case "health":
                domainInstruction = "You are a healthcare assistant for rural India. Give basic advice and suggest seeing a doctor. ";
                break;
            case "scheme":
                domainInstruction = "You are a government scheme expert. Tell about PM Kisan, Ayushman Bharat, or other relevant schemes. ";
                break;
            default:
                domainInstruction = "You are a helpful assistant for rural India. ";
        }

        String languageInstruction = "";
        switch(language) {
            case "hi":
                languageInstruction = "Respond in Hinglish (Hindi words with English alphabets). Keep very simple. ";
                break;
            case "mr":
                languageInstruction = "Respond in Marathi language using Devanagari script. Keep very simple. ";
                break;
            default:
                languageInstruction = "Respond in simple English. Use short sentences. ";
        }

        return domainInstruction + languageInstruction + "Question: " + query;
    }

    private String callGeminiAPI(String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;

        String jsonInput = "{\"contents\":[{\"parts\":[{\"text\":\"" + escapeJson(prompt) + "\"}]}]}";

        System.out.println("=== Calling Gemini API ===");
        System.out.println("URL: " + url);
        System.out.println("Prompt: " + prompt);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Send request
        try(OutputStream os = conn.getOutputStream()) {
            os.write(jsonInput.getBytes());
            os.flush();
        }

        // Read response
        int status = conn.getResponseCode();
        System.out.println("Response Status: " + status);

        BufferedReader br = new BufferedReader(new InputStreamReader(
                status == 200 ? conn.getInputStream() : conn.getErrorStream()
        ));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        conn.disconnect();

        String fullResponse = response.toString();
        System.out.println("Full Response: " + fullResponse);

        if (status == 200) {
            String text = extractTextFromGeminiResponse(fullResponse);
            if (text != null && !text.isEmpty()) {
                return text;
            } else {
                System.out.println("Could not extract text from response");
                return getFallbackResponse("general", "hi");
            }
        } else {
            System.out.println("Gemini API returned error " + status);
            return getFallbackResponse("general", "hi");
        }
    }
    private String extractTextFromGeminiResponse(String json) {
        // Look for "text":"..."
        int startIndex = json.indexOf("\"text\":\"");
        if (startIndex == -1) {
            // Try alternative pattern
            startIndex = json.indexOf("\"text\": \"");
            if (startIndex == -1) return null;
            startIndex += 8;
        } else {
            startIndex += 8; // length of "text":"
        }

        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex == -1) return null;

        String extracted = json.substring(startIndex, endIndex)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
        return extracted;
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String getFallbackResponse(String domain, String language) {
        if (language.equals("hi")) {
            if (domain.equals("agriculture")) return "नमस्ते! फसलों के लिए सही खाद और पानी का उपयोग करें। नजदीकी कृषि अधिकारी से संपर्क करें।";
            if (domain.equals("health")) return "अपना ख्याल रखें। बुखार होने पर डॉक्टर से मिलें। स्वच्छता बनाए रखें।";
            return "कृपया फिर से पूछें। मैं आपकी मदद करूंगा।";
        }
        return "Please ask again. I'm here to help you with farming, health, and government schemes.";
    }
}