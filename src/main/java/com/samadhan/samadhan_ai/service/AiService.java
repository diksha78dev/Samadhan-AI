package com.samadhan.samadhan_ai.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.samadhan.samadhan_ai.model.ChatMessage;
import com.samadhan.samadhan_ai.repository.ChatRepository;

@Service
public class AiService {

    @Autowired
    private ChatRepository chatRepository;
    
    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    public String askQuestion(String userQuery, String domain, String language) {
        String response = generateSmartMockResponse(userQuery, domain, language);
        
        // Save to database
        ChatMessage message = new ChatMessage(userQuery, response, domain, language);
        message.setCreatedAt(LocalDateTime.now());
        chatRepository.save(message);
        
        return response;
    }
    
    private String generateSmartMockResponse(String query, String domain, String language) {
        String queryLower = query.toLowerCase();
        
        // AGRICULTURE DOMAIN
        if (domain.equals("agriculture")) {
            // Weed removal in paddy/rice
            if (queryLower.contains("खरपतवार") || queryLower.contains("weed") || 
                (queryLower.contains("धान") || queryLower.contains("paddy") || queryLower.contains("rice")) && queryLower.contains("हटाना")) {
                if (language.equals("hi")) {
                    return "धान की फसल में खरपतवार हटाने के लिए: 1) निराई-गुड़ाई करें, 2) खरपतवारनाशक दवा (जैसे बुटाक्लोर) का छिड़काव करें, 3) समय पर पानी निकालें।";
                } else if (language.equals("mr")) {
                    return "धानाच्या पिकातील तण काढण्यासाठी: 1) निराई-गुडाई करा, 2) तणनाशक (बुटाक्लोर) फवारा करा, 3) वेळेवर पाणी सोडा.";
                } else {
                    return "To remove weeds in paddy: 1) Hand weeding, 2) Spray butachlor herbicide, 3) Drain water at proper time.";
                }
            }
            // Wheat crop
            else if (queryLower.contains("गेहूं") || queryLower.contains("wheat")) {
                if (language.equals("hi")) {
                    return "गेहूं की फसल के लिए यूरिया खाद डालें और नियमित सिंचाई करें। नजदीकी कृषि अधिकारी से संपर्क करें।";
                } else if (language.equals("mr")) {
                    return "गहू पिकासाठी युरिया खत घाला आणि नियमित पाणी द्या. जवळच्या कृषी अधिकाऱ्याशी संपर्क साधा.";
                } else {
                    return "For wheat crop, use urea fertilizer and regular irrigation. Consult local agriculture officer.";
                }
            }
            // Fertilizer
            else if (queryLower.contains("fertilizer") || queryLower.contains("खाद") || queryLower.contains("उर्वरक")) {
                if (language.equals("hi")) {
                    return "फसल के अनुसार खाद चुनें: गेहूं-धान के लिए यूरिया और डीएपी, सब्जियों के लिए गोबर खाद अच्छा है। मिट्टी परीक्षण जरूर कराएं।";
                } else {
                    return "Choose fertilizer based on crop: Urea/DAP for wheat-paddy, compost for vegetables. Get soil testing done.";
                }
            }
            // Pest control
            else if (queryLower.contains("pest") || queryLower.contains("कीट") || queryLower.contains("कीड़ा")) {
                if (language.equals("hi")) {
                    return "कीट नियंत्रण के लिए: 1) नीम का तेल छिड़कें, 2) जैविक कीटनाशक का उपयोग करें, 3) कृषि अधिकारी से सलाह लें।";
                } else {
                    return "For pest control: 1) Spray neem oil, 2) Use organic pesticides, 3) Consult agriculture officer.";
                }
            }
            // Default agriculture
            else {
                if (language.equals("hi")) {
                    return "कृपया फसल का नाम और समस्या बताएं (जैसे: गेहूं, धान, सब्जी)। हम आपकी मदद करेंगे।";
                } else {
                    return "Please specify crop name and problem (e.g., wheat, paddy, vegetables). I'll help you.";
                }
            }
        }
        
        // HEALTH DOMAIN
        else if (domain.equals("health")) {
            // Headache
            if (queryLower.contains("सिर दर्द") || queryLower.contains("headache")) {
                if (language.equals("hi")) {
                    return "सिरदर्द के लिए: पानी पिएं, आराम करें। यदि तेज दर्द हो तो पैरासिटामोल ले सकते हैं। डॉक्टर से सलाह लें।";
                } else if (language.equals("mr")) {
                    return "डोकेदुखीसाठी: पाणी प्या, विश्रांती घ्या. तीव्र दुखी असेल तर पॅरासिटामोल घेऊ शकता. डॉक्टरांचा सल्ला घ्या.";
                } else {
                    return "For headache: drink water, rest. If severe, take paracetamol. Consult doctor.";
                }
            }
            // Fever
            else if (queryLower.contains("बुखार") || queryLower.contains("fever")) {
                if (language.equals("hi")) {
                    return "बुखार में आराम करें, गर्म पानी पिएं। यदि 3 दिन से अधिक हो तो डॉक्टर से मिलें। पैरासिटामोल ले सकते हैं।";
                } else {
                    return "For fever: rest, drink warm water. If more than 3 days, see doctor. Can take paracetamol.";
                }
            }
            // Default health
            else {
                if (language.equals("hi")) {
                    return "कृपया अपनी बीमारी या लक्षण बताएं (जैसे: सिरदर्द, बुखार, खांसी)। हम बुनियादी सलाह दे सकते हैं। गंभीर होने पर डॉक्टर से मिलें।";
                } else {
                    return "Please tell your symptoms (e.g., headache, fever, cough). I can give basic advice. See doctor if serious.";
                }
            }
        }
        
        // SCHEMES DOMAIN
        else if (domain.equals("scheme")) {
            if (queryLower.contains("pm किसान") || queryLower.contains("kisan")) {
                if (language.equals("hi")) {
                    return "PM किसान सम्मान निधि: हर साल 6000 रुपये तीन किस्तों में। आवेदन ग्राम पंचायत या CSC केंद्र पर करें। दस्तावेज़: आधार, भूमि रिकॉर्ड।";
                } else {
                    return "PM Kisan Samman Nidhi: Rs 6000/year in 3 installments. Apply at Gram Panchayat or CSC. Documents: Aadhaar, land records.";
                }
            } else if (queryLower.contains("आयुष्मान") || queryLower.contains("ayushman")) {
                if (language.equals("hi")) {
                    return "आयुष्मान भारत योजना: 5 लाख रुपये तक का मुफ्त इलाज। पात्रता: Ration card से देख सकते हैं। नजदीकी अस्पताल में संपर्क करें।";
                } else {
                    return "Ayushman Bharat: Free treatment up to Rs 5 lakh. Check eligibility via ration card. Contact nearby empaneled hospital.";
                }
            } else {
                if (language.equals("hi")) {
                    return "कृपया योजना का नाम बताएं (जैसे: PM किसान, आयुष्मान भारत, बेटी बचाओ)। मैं जानकारी दूंगा।";
                } else {
                    return "Please mention scheme name (PM Kisan, Ayushman Bharat, Beti Bachao). I'll provide information.";
                }
            }
        }
        
        // FALLBACK
        if (language.equals("hi")) {
            return "नमस्ते! मैं सामाधान AI हूं। कृपया कृषि, स्वास्थ्य, या सरकारी योजना से संबंधित सवाल पूछें। मैं हिंदी, मराठी और अंग्रेजी में मदद कर सकता हूं।";
        } else {
            return "Namaste! I'm Samadhan AI. Please ask questions related to agriculture, health, or government schemes. I can help in Hindi, Marathi, and English.";
        }
    }
}