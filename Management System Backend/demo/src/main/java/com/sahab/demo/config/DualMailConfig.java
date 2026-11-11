package com.sahab.demo.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class DualMailConfig {

    @Bean(name = "mailSender")
    public JavaMailSender gmailMailSender(
            @Value("${spring.mail.host}") String host,
            @Value("${spring.mail.port}") int port,
            @Value("${spring.mail.username}") String username,
            @Value("${spring.mail.password}") String password
    ) {
        return createMailSender(host, port, username, password);
    }

    @Bean(name = "hotmailMailSender")
    public JavaMailSender hotmailMailSender(
            @Value("${app.mail.hotmail.host:smtp-mail.outlook.com}") String host,
            @Value("${app.mail.hotmail.port:587}") int port,
            @Value("${HOTMAIL_USERNAME:}") String username,
            @Value("${HOTMAIL_PASSWORD:}") String password
    ) {
        return createMailSender(host, port, username, password);
    }

    private JavaMailSenderImpl createMailSender(
            String host,
            int port,
            String username,
            String password
    ) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();

        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setProtocol("smtp");

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

        return sender;
    }
}
