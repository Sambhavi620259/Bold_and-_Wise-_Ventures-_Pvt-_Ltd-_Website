package in.bawvpl.Authify.service;

import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:no-reply@authify.com}")
    private String from;

    // =====================================================
    // COMMON HTML EMAIL SENDER
    // =====================================================

    public void sendHtmlEmail(

            String to,

            String subject,

            String htmlContent
    ) {

        // =====================================================
        // VALIDATION
        // =====================================================

        if (

                to == null ||

                        to.isBlank()

        ) {

            log.error(
                    "Email recipient is empty"
            );

            return;
        }

        if (

                subject == null ||

                        subject.isBlank()

        ) {

            log.error(
                    "Email subject is empty"
            );

            return;
        }

        if (

                htmlContent == null ||

                        htmlContent.isBlank()

        ) {

            log.error(
                    "Email content is empty"
            );

            return;
        }

        try {

            log.info(
                    "Sending email to {}",
                    to
            );

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(

                            message,

                            true,

                            "UTF-8"
                    );

            helper.setTo(
                    to.trim()
            );

            helper.setFrom(
                    "Authify <" + from + ">"
            );

            helper.setSubject(
                    subject
            );

            helper.setText(
                    htmlContent,
                    true
            );

            mailSender.send(message);

            log.info(
                    "Email sent successfully to {}",
                    to
            );

        } catch (Exception e) {

            log.error(
                    "Failed to send email to {}",
                    to,
                    e
            );

            throw new RuntimeException(
                    "Email sending failed"
            );
        }
    }

    // =====================================================
    // COMMON OTP TEMPLATE
    // =====================================================

    public void sendOtpEmail(

            String to,

            String subject,

            String otp
    ) {

        if (

                otp == null ||

                        otp.isBlank()

        ) {

            log.error(
                    "OTP is empty"
            );

            return;
        }

        String html = """
                <div style="
                    font-family:Arial,sans-serif;
                    line-height:1.6;
                    color:#111;
                    max-width:600px;
                    margin:auto;
                    padding:20px;
                ">

                    <h2 style="
                        color:#2e6cff;
                    ">
                        Your OTP Code
                    </h2>

                    <p>
                        Use the OTP below to continue.
                    </p>

                    <div style="
                        font-size:34px;
                        font-weight:bold;
                        letter-spacing:8px;
                        color:#2e6cff;
                        margin:30px 0;
                        text-align:center;
                    ">
                        %s
                    </div>

                    <p>
                        This OTP is valid for 5 minutes.
                    </p>

                    <p>
                        Never share your OTP with anyone.
                    </p>

                    <br/>

                    <p>
                        Regards,
                        <br/>
                        Authify Team
                    </p>

                </div>
                """.formatted(otp);

        sendHtmlEmail(
                to,
                subject,
                html
        );
    }

    // =====================================================
    // VERIFICATION OTP
    // =====================================================

    public void sendVerificationOtpEmail(

            String to,

            String otp
    ) {

        sendOtpEmail(

                to,

                "Verification OTP",

                otp
        );
    }

    // =====================================================
    // LOGIN OTP
    // =====================================================

    public void sendLoginOtpEmail(

            String to,

            String otp
    ) {

        sendOtpEmail(

                to,

                "Login OTP",

                otp
        );
    }

    // =====================================================
    // RESET PASSWORD OTP
    // =====================================================

    public void sendResetOtpEmail(

            String to,

            String otp
    ) {

        sendOtpEmail(

                to,

                "Reset Password OTP",

                otp
        );
    }

    // =====================================================
    // EMAIL VERIFICATION LINK
    // =====================================================

    public void sendVerificationEmail(

            String to,

            String verificationLink
    ) {

        if (

                verificationLink == null ||

                        verificationLink.isBlank()

        ) {

            log.error(
                    "Verification link is empty"
            );

            return;
        }

        String html = """
                <div style="
                    font-family:Arial,sans-serif;
                    line-height:1.7;
                    color:#111;
                    max-width:600px;
                    margin:auto;
                    padding:20px;
                ">

                    <h2 style="
                        color:#2e6cff;
                    ">
                        Verify Your Email
                    </h2>

                    <p>
                        Thank you for registering with Authify.
                    </p>

                    <p>
                        Please click the button below
                        to verify your email address.
                    </p>

                    <div style="
                        margin:30px 0;
                        text-align:center;
                    ">

                        <a href="%s" style="
                            display:inline-block;
                            background:#2e6cff;
                            color:#ffffff;
                            text-decoration:none;
                            padding:14px 28px;
                            border-radius:8px;
                            font-weight:600;
                        ">
                            Verify Email
                        </a>

                    </div>

                    <p>
                        If the button does not work,
                        copy and paste this link into your browser:
                    </p>

                    <p style="
                        word-break:break-all;
                    ">

                        <a href="%s">
                            %s
                        </a>

                    </p>

                    <br/>

                    <p>
                        This verification link expires in 24 hours.
                    </p>

                    <p>
                        If you did not create this account,
                        you can safely ignore this email.
                    </p>

                    <br/>

                    <p>
                        Regards,
                        <br/>
                        Authify Team
                    </p>

                </div>
                """.formatted(

                verificationLink,

                verificationLink,

                verificationLink
        );

        sendHtmlEmail(

                to,

                "Verify Your Email",

                html
        );
    }
    // =====================================================
// ADMIN INVITE EMAIL
// =====================================================

    public void sendAdminInvite(

            String to,

            String fullName,

            String inviteLink,

            String role
    ) {

        String html = """
            <div style="
                font-family:Arial,sans-serif;
                line-height:1.7;
                color:#111;
                max-width:600px;
                margin:auto;
                padding:20px;
            ">

                <h2 style="
                    color:#2e6cff;
                ">
                    Admin Invitation
                </h2>

                <p>
                    Hello %s,
                </p>

                <p>
                    You have been invited to join Authify as:
                    <strong>%s</strong>
                </p>

                <div style="
                    margin:30px 0;
                    text-align:center;
                ">

                    <a href="%s" style="
                        display:inline-block;
                        background:#2e6cff;
                        color:#ffffff;
                        text-decoration:none;
                        padding:14px 28px;
                        border-radius:8px;
                        font-weight:600;
                    ">
                        Accept Invitation
                    </a>

                </div>

                <p>
                    If the button does not work,
                    copy and paste this link:
                </p>

                <p style="
                    word-break:break-all;
                ">
                    <a href="%s">%s</a>
                </p>

                <p>
                    This invitation expires in 24 hours.
                </p>

                <br/>

                <p>
                    Regards,
                    <br/>
                    Authify Team
                </p>

            </div>
            """.formatted(

                fullName,

                role,

                inviteLink,

                inviteLink,

                inviteLink
        );

        sendHtmlEmail(
                to,
                "Admin Invitation",
                html
        );
    }
}