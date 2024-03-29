package com.bonitasoft.challenge.controller;

import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bonitasoft.challenge.email.EmailService;
import com.bonitasoft.challenge.model.Recipe;
import com.bonitasoft.challenge.model.User;
import com.bonitasoft.challenge.model.UserLogged;
import com.google.common.collect.Lists;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class UserController extends AbstractController {

	@Resource
	private EmailService emailService;

//	/**
//	 * Bean used to encode passwords to be stored in the DDBB
//	 * 
//	 * @return
//	 */
//	@Bean
//	public PasswordEncoder encoder() {
//		return new BCryptPasswordEncoder();
//	}

	Logger logger = LogManager.getLogger(UserController.class);

	/**
	 * API used to login
	 * 
	 * @param user
	 * @return True if the login and the password matches the information in the
	 *         DDBB, false otherwise
	 */
	@PostMapping("/login")
	public UserLogged login(@RequestBody User user) {

		logger.info("New login request");
		Optional<User> optionalUser = userRepo.findOptionalByUserName(user.getUserName());
		if (optionalUser.isPresent() && user.getUserPassword().equals(optionalUser.get().getUserPassword())) {
			UserLogged userLogged = new UserLogged(optionalUser.get());
			userLogged.setSessionId(RandomStringUtils.randomAlphanumeric(40));
			sessionManager.put(userLogged.getSessionId(), optionalUser.get().getId());
			return userLogged;

		} else {
			logger.error("The user login or password is wrong");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user login or password is wrong");
		}
	}

	/**
	 * API used to login
	 * 
	 * @param sessionId
	 * @return True if the login and the password matches the information in the
	 *         DDBB, false otherwise
	 */
	@GetMapping("/logout")
	public boolean logout(@RequestHeader(name = "sessionid") String sessionId) {

		// Check if the session id is valid
		Long userId = checkSessionId(sessionId);
		if (userId != null) {
			if (userRepo.findById(userId).isPresent()) {
				sessionManager.remove(sessionId);
				return true;

			} else {
				logger.error("The user login doesn't exist");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user login doesn't exist");
			}
		} else {
			return false;
		}
	}

	/**
	 * Get all users
	 * 
	 * @param sessionId The session ID
	 * @return A list with all users
	 */
	@GetMapping(path = "/users")
	public List<User> allUsers(@RequestHeader(name = "sessionid") String sessionId) {
		logger.info("New request to list all existing users");

		// Check if the session id is valid
		checkSessionId(sessionId);
		return Lists.newArrayList(userRepo.findAll());
	}

	/**
	 * API to get the user object identified by the ID
	 * 
	 * @param sessionId The session ID
	 * @param id        The user ID of the user to be updated
	 * @return Requested user
	 * 
	 * @exception ResponseStatusException When the user doesn't exist
	 */
	@GetMapping("/user/{id}")
	public User getUserById(@RequestHeader(name = "sessionid") String sessionId, @NotNull @PathVariable("id") Long id) {

		// Check if the session id is valid
		checkSessionId(sessionId);

		logger.info("New request to list the user with ID " + id);

		return userRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user doesn't exist"));
	}

	/**
	 * API to create a new user
	 * 
	 * @param sessionId The session ID
	 * @param user      the user information to create a new user
	 * @return The created user
	 * 
	 * @exception ResponseStatusException When te user already exists
	 */
	@PostMapping(path = "/user")
	public User createUser(@RequestHeader(name = "sessionid") String sessionId, @Valid @RequestBody User user) {

		logger.info("New request to create a new user with this information" + user.toString());

		// Check if the session id is valid
		checkSessionId(sessionId);

		if (!userRepo.findByUserNameOrUserEmail(user.getUserName(), user.getUserEmail()).isEmpty()) {
			logger.error("There is already a user with that username or email. Please change them");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"There is already a user with that username or email. Please change them");

		} else {
			emailService.sendEmailNewUser(user);
//			user.setUserPassword(encoder().encode(user.getUserPassword()));
			return userRepo.save(user);
		}
	}

	/**
	 * API to update a user information
	 * 
	 * @param sessionId The session ID
	 * @param id        The user ID of the user to be updated
	 * @param user      The new user information
	 * @return The updated user
	 * 
	 * @exception ResponseStatusException When: <br>
	 *                                    - The user information already exists <br>
	 *                                    - The user doesn't exist
	 */
	@PutMapping("/user/{id}")
	public User updateUser(@RequestHeader(name = "sessionid") String sessionId, @NotNull @PathVariable("id") Long id,
			@RequestBody User user) {

		// Check if the session id is valid
		checkSessionId(sessionId);

		logger.info(String.format("New request to update the existing user with ID %d with this information: %s", id,
				user.toString()));

		// If the user id exists in the DDBB
		checkUserExists(id);

		List<User> usersWithUsernameOrEmail = userRepo.findByUserNameOrUserEmail(user.getUserName(),
				user.getUserEmail());

		// check if the username or email address already exist in the DDBB, because
		// they need to be unique
		if (usersWithUsernameOrEmail.stream().filter(u -> !u.getId().equals(id)).findAny().isPresent()) {

			logger.error("The user information already exists for another user");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"The user information already exists for another user");

		} else {
			user.setId(id);
			return userRepo.save(user);
		}
	}

	/**
	 * API to delete the user information
	 * 
	 * @param sessionId The session ID
	 * @param id        The user ID of the user to be updated
	 * 
	 * @exception ResponseStatusException When the user doesn't exist
	 */
	@DeleteMapping("/user/{id}")
	public boolean deleteUser(@RequestHeader(name = "sessionid") String sessionId,
			@NotNull @PathVariable("id") Long id) {

		logger.info("New request to delete the existing user with ID " + id);

		// Check if the session id is valid
		checkSessionId(sessionId);

		User userToBeDeleted = checkUserExists(id);

		List<Recipe> recipesToBeDeleted = recipeRepo.findByAuthor(userToBeDeleted);
		recipesToBeDeleted.stream().forEach(r -> {
			recipeRepo.delete(r);
		});

		// And finally delete the user
		userRepo.delete(userToBeDeleted);
		return true;
	}
}