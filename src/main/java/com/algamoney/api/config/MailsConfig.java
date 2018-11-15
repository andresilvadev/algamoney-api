package com.algamoney.api.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.algamoney.api.config.property.AlgamoneyApiProperty;

@Configuration
public class MailsConfig {
	
	@Autowired
	private AlgamoneyApiProperty property;

	@Bean
	public JavaMailSender javaMailSender() {
		Properties props = new Properties();
		
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", true);
		props.put("mail.smtp.starttls.enable", true);
		props.put("mail.smtp.connectiontimeout", 10000);
		
		JavaMailSenderImpl mailSenderImpl = new JavaMailSenderImpl();
		mailSenderImpl.setJavaMailProperties(props);
		mailSenderImpl.setHost(property.getMail().getHost());
		mailSenderImpl.setPort(property.getMail().getPort());
		mailSenderImpl.setUsername(property.getMail().getUsername());
		mailSenderImpl.setPassword(property.getMail().getPassword());
		
		return mailSenderImpl;
		
	}
}
