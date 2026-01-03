package com.shieldauth.managers;

import com.shieldauth.ShieldAuth;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class EmailManager {

    private final ShieldAuth plugin;

    public EmailManager(ShieldAuth plugin) {
        this.plugin = plugin;
    }

    public void sendVerificationEmail(String to, String code, String playerName) {
        if (!plugin.getConfigManager().getConfig().getBoolean("email.enabled", false)) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String host = plugin.getConfigManager().getConfig().getString("email.smtp-host", "smtp.gmail.com");
                int port = plugin.getConfigManager().getConfig().getInt("email.smtp-port", 587);
                String username = plugin.getConfigManager().getConfig().getString("email.smtp-username", "");
                String password = plugin.getConfigManager().getConfig().getString("email.smtp-password", "");
                boolean ssl = plugin.getConfigManager().getConfig().getBoolean("email.smtp-ssl", true);
                String from = plugin.getConfigManager().getConfig().getString("email.from-address", "noreply@shieldauth.com");

                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.host", host);
                props.put("mail.smtp.port", String.valueOf(port));

                if (ssl) {
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.ssl.protocols", "TLSv1.2");
                }

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject("ShieldAuth - Email Verification");

                String content = "<html><body>" +
                        "<h2>ShieldAuth Email Verification</h2>" +
                        "<p>Hello " + playerName + ",</p>" +
                        "<p>Your verification code is: <strong>" + code + "</strong></p>" +
                        "<p>Use /verifyemail " + code + " in-game to verify your email.</p>" +
                        "<p>If you did not request this, please ignore this email.</p>" +
                        "</body></html>";

                message.setContent(content, "text/html; charset=utf-8");

                Transport.send(message);

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send verification email: " + e.getMessage());
            }
        });
    }

    public void sendPasswordResetEmail(String to, String code, String playerName) {
        if (!plugin.getConfigManager().getConfig().getBoolean("email.enabled", false)) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String host = plugin.getConfigManager().getConfig().getString("email.smtp-host", "smtp.gmail.com");
                int port = plugin.getConfigManager().getConfig().getInt("email.smtp-port", 587);
                String username = plugin.getConfigManager().getConfig().getString("email.smtp-username", "");
                String password = plugin.getConfigManager().getConfig().getString("email.smtp-password", "");
                boolean ssl = plugin.getConfigManager().getConfig().getBoolean("email.smtp-ssl", true);
                String from = plugin.getConfigManager().getConfig().getString("email.from-address", "noreply@shieldauth.com");

                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.host", host);
                props.put("mail.smtp.port", String.valueOf(port));

                if (ssl) {
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.ssl.protocols", "TLSv1.2");
                }

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject("ShieldAuth - Password Reset");

                String content = "<html><body>" +
                        "<h2>ShieldAuth Password Reset</h2>" +
                        "<p>Hello " + playerName + ",</p>" +
                        "<p>Your password reset code is: <strong>" + code + "</strong></p>" +
                        "<p>If you did not request this, please change your password immediately.</p>" +
                        "</body></html>";

                message.setContent(content, "text/html; charset=utf-8");

                Transport.send(message);

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send password reset email: " + e.getMessage());
            }
        });
    }
}
