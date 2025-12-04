package org.example.uzsql.service;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailService {

    private final MailtrapClient client;
    private final String fromEmail;
    private final String fromName;

    public MailService(@Value("${mailtrap.token}") String token, @Value("${mailtrap.from.email:hello@demomailtrap.co}") String fromEmail, @Value("${mailtrap.from.name:UzSQL Service}") String fromName) {
        MailtrapConfig config = new MailtrapConfig.Builder().token(token).build();

        this.client = MailtrapClientFactory.createMailtrapClient(config);
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }


    public void sendCredentialsEmail(String dbName, String username, String password, String recipientEmail) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            System.out.println("Email manzil bo'sh bo'lmasligi kerak");
            return;
        }
        String html = """
                <!DOCTYPE html>
                <html lang="uz">
                <head>
                    <meta charset="UTF-8">
                    <title>MySQL Baza Ma'lumotlari</title>
                </head>
                <body style="font-family: Arial, sans-serif; background:#f3f4f6; padding:20px;">
                <div style="max-width:600px; margin:auto; background:white; border-radius:16px; padding:24px;
                box-shadow:0 10px 30px rgba(15,23,42,0.08); border:1px solid #e5e7eb;">
                
                    <h2 style="color:#0f172a; text-align:center; font-size:22px; margin-bottom:10px;">
                        📦 MySQL baza ma'lumotlari tayyor
                    </h2>
                
                    <p style="font-size:14px; color:#374151;">
                        Siz uchun yangi MySQL ma'lumotlar bazasi yaratildi 👋
                    </p>
                
                    <div style="background:#020617; padding:14px; border-radius:10px; color:#e5e7eb;
                    font-family:Consolas, monospace; font-size:13px; line-height:1.7;">
                
                        DB_NAME = <span style="color:#93c5fd;">%s</span><br>
                        DB_USER = <span style="color:#93c5fd;">%s</span><br>
                        DB_PASS = <span style="color:#93c5fd;">%s</span><br>
                        DB_HOST = <span style="color:#93c5fd;">45.92.173.158</span><br>
                        DB_PORT = <span style="color:#93c5fd;">3306</span>
                
                    </div>
                
                    <p style="font-size:12px; color:#6b7280; margin-top:10px;">
                        Ushbu ma'lumotlarni xavfsiz joyda saqlang.
                    </p>
                
                </div>
                </body>
                </html>
                """.formatted(dbName, username, password);


        MailtrapMail mail = MailtrapMail.builder().from(new Address(fromEmail, fromName)).to(List.of(new Address(recipientEmail))).subject("UzSQL • Yangi MySQL bazangiz yaratildi").html(html).build();

        try {
            var response = client.send(mail);
            System.out.println("Mailtrap javobi: " + response);
        } catch (Exception e) {
            System.out.println("Email yuborishda xatolik: " + e.getMessage());
        }
    }


}
