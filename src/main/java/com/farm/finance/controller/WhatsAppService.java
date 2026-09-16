package com.farm.finance.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppService {

    // ===== TWILIO CREDENTIALS =====
    // Step 1: Go to twilio.com, sign up for free account
    // Step 2: Get Account SID, Auth Token, and WhatsApp number
    private static final String ACCOUNT_SID = "your_account_sid_here";
    private static final String AUTH_TOKEN = "your_auth_token_here";
    private static final String TWILIO_WHATSAPP_NUMBER = "whatsapp:+14155238886";

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendWhatsAppMessage(String toNumber, String message) {
        try {
            Message.creator(
                    new PhoneNumber("whatsapp:" + toNumber),
                    new PhoneNumber(TWILIO_WHATSAPP_NUMBER),
                    message
            ).create();
            System.out.println("✅ WhatsApp message sent to: " + toNumber);
        } catch (Exception e) {
            System.out.println("❌ Failed to send WhatsApp: " + e.getMessage());
        }
    }

    public void sendBudgetAlertWhatsApp(String toNumber, double expense, double budget) {
        String message = "🚨 BUDGET ALERT!\n" +
                "Your expense ₹" + expense + " exceeded budget ₹" + budget + "!\n" +
                "Check your farm expenses now! 🌾";
        sendWhatsAppMessage(toNumber, message);
    }

    public void sendProfitMilestoneWhatsApp(String toNumber, double profit) {
        String message = "🎉 PROFIT MILESTONE!\n" +
                "Congratulations! Your farm made ₹" + profit + " profit!\n" +
                "Keep up the great work! 🌾";
        sendWhatsAppMessage(toNumber, message);
    }
}