package in.bawvpl.Authify.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${twilio.sid:}")
    private String accountSid;

    @Value("${twilio.token:}")
    private String authToken;

    @Value("${twilio.number:}")
    private String fromNumber;

    // ================= INIT =================
    @PostConstruct
    public void init() {
        if (smsEnabled && !accountSid.isBlank() && !authToken.isBlank()) {
            Twilio.init(accountSid, authToken);
            log.info("✅ Twilio initialized");
        } else {
            log.warn("⚠️ Twilio not initialized (disabled or missing config)");
        }
    }

    // ================= GENERIC SEND =================
    @Override
    public boolean sendSms(String phoneNumber, String message) {

        if (!smsEnabled) {
            log.info("📵 SMS disabled");
            return false;
        }

        if (!isValid(phoneNumber, message)) return false;

        String formattedPhone = formatPhone(phoneNumber);

        try {
            Message msg = Message.creator(
                    new PhoneNumber(formattedPhone),
                    new PhoneNumber(fromNumber),
                    message
            ).create();

            log.info("✅ SMS sent to {} | SID={}", formattedPhone, msg.getSid());
            return true;

        } catch (Exception e) {
            log.error("❌ SMS failed for {}: {}", formattedPhone, e.getMessage());
            return false; // ✅ DO NOT THROW
        }
    }

    // ================= VERIFY OTP =================
    @Override
    public boolean sendVerificationOtp(String phoneNumber, String otp) {
        return sendSms(phoneNumber, "Your verification OTP is: " + otp);
    }

    // ================= RESET OTP =================
    @Override
    public boolean sendResetOtp(String phoneNumber, String otp) {
        return sendSms(phoneNumber, "Your reset OTP is: " + otp);
    }

    // ================= VALIDATION =================
    private boolean isValid(String phoneNumber, String message) {

        if (phoneNumber == null || phoneNumber.isBlank()) {
            log.warn("⚠️ Phone number missing");
            return false;
        }

        if (message == null || message.isBlank()) {
            log.warn("⚠️ Message empty");
            return false;
        }

        if (accountSid.isBlank() || authToken.isBlank() || fromNumber.isBlank()) {
            log.warn("⚠️ Twilio config missing");
            return false;
        }

        return true;
    }

    // ================= FORMAT =================
    private String formatPhone(String phoneNumber) {

        String clean = phoneNumber.replaceAll("\\D", "");

        if (clean.length() == 10) {
            return "+91" + clean;
        }

        if (clean.startsWith("91") && clean.length() == 12) {
            return "+" + clean;
        }

        if (phoneNumber.startsWith("+")) {
            return phoneNumber;
        }

        return "+" + clean;
    }
}