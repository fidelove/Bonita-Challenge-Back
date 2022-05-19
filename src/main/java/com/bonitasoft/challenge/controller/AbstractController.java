package com.bonitasoft.challenge.controller;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.bonitasoft.challenge.model.Recipe;
import com.bonitasoft.challenge.model.RoleType;
import com.bonitasoft.challenge.model.User;
import com.bonitasoft.challenge.repository.CommentRepo;
import com.bonitasoft.challenge.repository.IngredientRepo;
import com.bonitasoft.challenge.repository.KeywordRepo;
import com.bonitasoft.challenge.repository.RecipeRepo;
import com.bonitasoft.challenge.repository.UserRepo;

public abstract class AbstractController {

	@Autowired
	UserRepo userRepo;

	@Autowired
	RecipeRepo recipeRepo;

	@Autowired
	IngredientRepo ingredientRepo;

	@Autowired
	KeywordRepo keywordRepo;

	@Autowired
	CommentRepo commentRepo;

	@Autowired
	Map<String, Long> sessionManager;

	Logger logger = LogManager.getLogger(AbstractController.class);

	/**
	 * Check if the user already exists
	 * 
	 * @param userId ID of the user to be checked
	 * @return An object containing the information of the user
	 * 
	 * @exception ResponseStatusException When the user doesn't exist
	 */
	protected User checkUserExists(Long userId) {
		Optional<User> user = userRepo.findById(userId);

		// If the user doesn't exist
		if (user.isEmpty()) {
			logger.error("The user doesn't exist");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user doesn't exist");
		}

		return user.get();
	}

	/**
	 * Check if the user exists, and if it has the right role
	 * 
	 * @param id       ID of the user to be checked
	 * @param roleType The Roles that the user must be
	 * @return The user
	 * 
	 * @exception ResponseStatusException In these cases: <br>
	 *                                    - When the user doesn't exist <br>
	 *                                    - When the role of the user doesn't allow
	 *                                    the requested operation
	 */
	protected User checkUser(Long id, RoleType... roleType) {

		logger.info(String.format("Checking if the user with ID %d exists, and if it has the right role %s", id,
				roleType.toString()));
		User user = checkUserExists(id);
		logger.info("User info: " + user.toString());

		// If the role doesn't allow the requested operation
		if (Stream.of(roleType).filter(r -> r.equals(user.getRole())).findAny().isEmpty()) {
			logger.error("The role of the user doesn't allow the requested operation");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"The role of the user doesn't allow the requested operation");
		}

		return user;
	}

	/**
	 * Check if the recipe exists
	 * 
	 * @param recipeId ID of the recipe to be checked
	 * @return Object containing the recipe
	 * 
	 * @exception ResponseStatusException If the recipe doesn't exist
	 */
	protected Recipe checkRecipe(Long recipeId) {

		// Check if the recipe exists
		Optional<Recipe> recipe = recipeRepo.findById(recipeId);
		if (recipe.isEmpty()) {
			logger.error("The recipe doesn't exist");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The recipe doesn't exist");
		}

		return recipe.get();
	}

	/**
	 * Check if the sessionId is correct
	 * 
	 * @param sessionId the session ID
	 * 
	 * @exception ResponseStatusException if the session ID is incorrect
	 */
	protected Long checkSessionId(String sessionId) {

		Long userId = sessionManager.get(sessionId);
		if (userId == null) {
			logger.error("The user wasn't logged in");
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The user wasn't logged in");
		} else {
			return userId;
		}
	}
}
