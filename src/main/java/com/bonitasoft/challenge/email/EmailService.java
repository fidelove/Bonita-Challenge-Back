package com.bonitasoft.challenge.email;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.bonitasoft.challenge.model.User;

@Component
@Service
public class EmailService {

	Logger logger = LogManager.getLogger(EmailService.class);

	/**
	 * Send an email to the new created user
	 * 
	 * @param user Object containing the user information
	 * @throws MessagingException
	 * @throws AddressException
	 * @throws IOException
	 */
	@Async("taskExecutor")
	public void sendEmailNewUser(User user) {

		try {
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");

			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("fidel.besada@gmail.com", "BE.sa.DA.13");
				}
			});
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("bonitasoft.challenge@gmail.com", false));

			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getUserEmail()));
			msg.setSubject("New account created");
			msg.setSentDate(new Date());

			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(String.format(
					"Your new account has been created successfully!! The username is %s and the password is %s",
					user.getUserName(), user.getUserPassword()), "text/html");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			msg.setContent(multipart);
			Transport.send(msg);

		} catch (MessagingException e) {
			logger.error("The email couldn't be sent with this error: " + e.getMessage(), e);
		}
	}
}