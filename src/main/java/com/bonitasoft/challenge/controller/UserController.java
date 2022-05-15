package com.bonitasoft.challenge.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bonitasoft.challenge.model.Recipe;
import com.bonitasoft.challenge.model.User;
import com.google.common.collect.Lists;

@RestController
@RequestMapping("/api/v1")
public class UserController extends AbstractController {

	Logger logger = LogManager.getLogger(UserController.class);

	/**
	 * Send an email to the new created user
	 * 
	 * @param user Object containing the user information
	 * @throws MessagingException
	 * @throws AddressException
	 * @throws IOException
	 */
	private void sendEmailNewUser(User user) {

		try {
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");

			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("bonitasoft.challenge@gmail.com", "BoNiTaSoFtPaSsWoRd");
				}
			});
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("bonitasoft.challenge@gmail.com", false));

			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getUserEmail()));
			msg.setSubject("New account created");
			msg.setContent("Your new account has been created successfully", "text/html");
			msg.setSentDate(new Date());

			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent("Your new account has been created successfully", "text/html");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			msg.setContent(multipart);
			Transport.send(msg);

		} catch (MessagingException e) {
			logger.error("The email couldn't be sent with this error: " + e.getMessage(), e);
		}
	}

	@GetMapping(path = "/users")
	public List<User> allUsers() {

		logger.info("New request to list all existing users");
		return Lists.newArrayList(userRepo.findAll());
	}

	/**
	 * API to get the user object identified by the ID
	 * 
	 * @param id ID defining the requested user
	 * @return Requested user
	 * 
	 * @exception ResponseStatusException When the user doesn't exist
	 */
	@GetMapping("/user/{id}")
	public User getUserById(@NotNull @PathVariable("id") Long id) {

		logger.info("New request to list the user with ID " + id);
		return userRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user doesn't exist"));
	}

	/**
	 * API to create a new user
	 * 
	 * @param user the user information to create a new user
	 * @return The created user
	 * 
	 * @exception ResponseStatusException When te user already exists
	 */
	@PostMapping(path = "/user")
	public User createUser(@Valid @RequestBody User user) {

		logger.info("New request to create a new user with this information" + user.toString());

		if (!userRepo.findByUserNameOrUserEmail(user.getUserName(), user.getUserEmail()).isEmpty()) {
			logger.error("The user already exists");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user already exists");

		} else {
			sendEmailNewUser(user);
			return userRepo.save(user);
		}
	}

	/**
	 * API to update a user information
	 * 
	 * @param id   ID to identify the user to be updated
	 * @param user The new user information
	 * @return The updated user
	 * 
	 * @exception ResponseStatusException When: <br>
	 *                                    - The user information already exists <br>
	 *                                    - The user doesn't exist
	 */
	@PutMapping("/user/{id}")
	public User updateUser(@NotNull @PathVariable Long id, @RequestBody User user) {

		logger.info(String.format("New request to update the existing user with ID %d with this information: %s", id,
				user.toString()));

		// If the user id exists in the DDBB
		checkUserExists(id);

		List<User> usersWithUsernameOrEmail = userRepo.findByUserNameOrUserEmail(user.getUserName(),
				user.getUserEmail());

		// check if the username or email address already exist in the DDBB, because
		// they need to be unique
		boolean userNameOrEmailAlreadyUsed = usersWithUsernameOrEmail.stream().filter(u -> !u.getId().equals(id))
				.findAny().isPresent();

		if (userNameOrEmailAlreadyUsed) {
			logger.error("The user information already exists");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user information already exists");

		} else {
			user.setId(id);
			return userRepo.save(user);
		}
	}

	/**
	 * API to delete the user information
	 * 
	 * @param id ID to identify the user
	 * 
	 * @exception ResponseStatusException When the user doesn't exist
	 */
	@DeleteMapping("/user/{id}")
	public void deleteUser(@NotNull @PathVariable("id") Long id) {

		logger.info("New request to delete the existing user with ID " + id);

		User userToBeDeleted = checkUserExists(id);

		List<Recipe> recipesToBeDeleted = recipeRepo.findByAuthor(userToBeDeleted);
		recipesToBeDeleted.stream().forEach(r -> {
			// TODO: igual el comments no hace falta
//				r.getComments().stream().forEach(c -> {
//					commentRepo.delete(c);
//				});
			recipeRepo.delete(r);
		});

		// And finally delete the user
		userRepo.delete(userToBeDeleted);
	}
}