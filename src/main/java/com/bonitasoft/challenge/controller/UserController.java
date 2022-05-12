package com.bonitasoft.challenge.controller;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.bonitasoft.challenge.model.User;
import com.bonitasoft.challenge.repository.UserRepo;
import com.google.common.collect.Lists;

@RestController
@RequestMapping("/api/v1")
public class UserController {

	@Autowired
	UserRepo userRepo;

	@GetMapping(path = "/users", name = "Get all users")
	public List<User> allUsers() {

		return Lists.newArrayList(userRepo.findAll());
	}

	@GetMapping("/user/{id}")
	public User getUserByName(@NotNull @PathVariable("id") Long id) {

		return userRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user doesn't exist"));
	}

	@PostMapping(path = "/user")
	public User createUser(@Valid @RequestBody User user) {
		if (!userRepo.findByUserNameOrUserEmail(user.getUserName(), user.getUserEmail()).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user already exists");

		} else {
			return userRepo.save(user);
		}
	}

	@PutMapping("/user/{id}")
	public User updateUser(@NotNull @PathVariable Long id, @RequestBody User user) {

		Optional<User> userToBeUpdated = userRepo.findById(id);

		// If the user id exists in the DDBB
		if (userToBeUpdated.isPresent()) {

			List<User> usersWithUsernameOrEmail = userRepo.findByUserNameOrUserEmail(user.getUserName(),
					user.getUserEmail());

			// check if the username or email address already exist in the DDBB, because
			// they need to be unique
			boolean userNameOrEmailAlreadyUsed = usersWithUsernameOrEmail.stream().filter(u -> !u.getId().equals(id))
					.findAny().isPresent();

			if (userNameOrEmailAlreadyUsed) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user information already exists");

			} else {
				user.setId(id);
				return userRepo.save(user);

			}

		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user doesn't exist");

		}
	}

	@DeleteMapping("/user/{id}")
	public void deleteUser(@NotNull @PathVariable("id") Long id) {
		userRepo.deleteById(id);
	}
}