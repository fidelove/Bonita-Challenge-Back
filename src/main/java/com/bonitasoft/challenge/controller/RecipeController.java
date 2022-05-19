package com.bonitasoft.challenge.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bonitasoft.challenge.model.Ingredient;
import com.bonitasoft.challenge.model.Keyword;
import com.bonitasoft.challenge.model.Recipe;
import com.bonitasoft.challenge.model.RoleType;
import com.bonitasoft.challenge.model.User;
import com.google.common.collect.Lists;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class RecipeController extends AbstractController {

	Logger logger = LogManager.getLogger(RecipeController.class);

	/**
	 * API to list the recipes. If the param keywords is present, the request will
	 * be filtered, otherwise all recipes will be returned
	 * 
	 * @param sessionId The session ID
	 * @param keywords  Optional parameter. If it's present, it'll contain the
	 *                  keywords
	 * @return List containing the requested recipes
	 */
	@GetMapping("/recipes")
	public List<Recipe> getRecipesByKeywords(@RequestHeader(name = "sessionid") String sessionId,
			@RequestParam Optional<List<String>> keywords) {

		// Check if the session id is valid
		checkSessionId(sessionId);

		// If there is a filter by keywords
		if (keywords.isPresent()) {
			logger.info("Listing all recipes filtered by " + keywords.toString());

			// Check all the existing keywords
			List<Keyword> existingKeywords = keywords.get().stream().map(k -> keywordRepo.findOptionalByKeyword(k))
					.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

			// And filter by these keywords
			return recipeRepo.findByKeywordsIn(existingKeywords);

			// If there is no filter
		} else {
			logger.info("Listing all recipes");
			return Lists.newArrayList(recipeRepo.findAll());
		}
	}

	/**
	 * API to get all the recipes created by a user
	 * 
	 * @param sessionId The session ID
	 * @return List containing all the recipes created by the user
	 * 
	 * @exception ResponseStatusException In these cases: <br>
	 *                                    - When the user doesn't exist <br>
	 *                                    - When the user isn't a chef
	 */
	@GetMapping("/recipe")
	public List<Recipe> getRecipesByUser(@RequestHeader(name = "sessionid") String sessionId) {

		// Check if the session id is valid
		Long id = checkSessionId(sessionId);

		logger.info("Listing all recipes for user with ID %d", id);

		// If user exists and it has the right role
		User user = checkUser(id, RoleType.CHEF);
		return recipeRepo.findByAuthor(user);
	}

	/**
	 * API to create a new recipe
	 * 
	 * @param sessionId The session ID
	 * @param recipe    Information of the new recipe
	 * @return The created recipe
	 * 
	 * @exception ResponseStatusException In these cases: <br>
	 *                                    - When there is already a recipe created
	 *                                    by the same user with the same name <br>
	 *                                    - When the user doesn't exist <br>
	 *                                    - When the user isn't a chef
	 */
	@PostMapping("/recipe")
	public Recipe createRecipe(@RequestHeader(name = "sessionid") String sessionId, @Valid @RequestBody Recipe recipe) {

		// Check if the session id is valid
		Long id = checkSessionId(sessionId);

		logger.info(String.format("Create a new recipe for user with ID %d, with recipe information %s", id,
				recipe.toString()));

		// If user exists and it has the right role
		User user = checkUser(id, RoleType.CHEF);

		// Check that there is no other recipe created by the same user with the same
		// name
		if (recipeRepo.findOptionalByAuthorAndRecipeName(user, recipe.getRecipeName()).isEmpty()) {

			recipe.setAuthor(user);

			// Check if ingredients already exists
			recipe.getIngredients().stream().forEach(i -> {
				Optional<Ingredient> ingredient = ingredientRepo.findOptionalByIngredient(i.getIngredient());

				// If it doesn't exists, we create a new one
				if (ingredient.isEmpty()) {
					i.setId(ingredientRepo.save(i).getId());

					// Otherwise we just update the ID
				} else {
					i.setId(ingredient.get().getId());
				}
			});

			// Same thing for keywords
			recipe.getKeywords().stream().forEach(k -> {
				Optional<Keyword> keyword = keywordRepo.findOptionalByKeyword(k.getKeyword());
				if (keyword.isEmpty()) {
					k.setId(keywordRepo.save(k).getId());
				} else {
					k.setId(keyword.get().getId());
				}
			});
			return recipeRepo.save(recipe);

		} else {
			logger.error("The recipe already exists. Change the name of the recipe");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"The recipe already exists. Change the name of the recipe");
		}
	}

	/**
	 * API to update a recipe for an existing user
	 * 
	 * @param sessionId The session ID
	 * @param recipeId  ID of the recipe to be updated
	 * @param recipe    The object containing the info to be updated
	 * @return An object containing the new information of the recipe
	 * 
	 * @exception ResponseStatusException In these cases: <br>
	 *                                    - The user doesn't exist <br>
	 *                                    - The recipe doesn't exist <br>
	 *                                    - The user had already created a recipe
	 *                                    with the same name. A new name must be
	 *                                    chosen <br>
	 *                                    - When the user isn't a chef
	 */
	@PutMapping("/recipe/{recipeId}")
	public Recipe updateRecipe(@RequestHeader(name = "sessionid") String sessionId,
			@NotNull @PathVariable("recipeId") Long recipeId, @Valid @RequestBody Recipe recipe) {

		// Check if the session id is valid
		Long userId = checkSessionId(sessionId);

		logger.info(
				String.format("Updating an existing recipe with ID %d for user with ID %d, with recipe information %s",
						recipeId, userId, recipe.toString()));

		// If user exists and it has the right role
		checkUser(userId, RoleType.CHEF);

		// Check if the recipe exists
		Recipe recipeToBeUpdated = checkRecipe(recipeId);

		// Check if there is already a recipe with the same name
		Optional<Recipe> recipeWithSameName = recipeRepo
				.findOptionalByAuthorAndRecipeName(recipeToBeUpdated.getAuthor(), recipe.getRecipeName());
		if (recipeWithSameName.isEmpty() || recipeWithSameName.get().getId().equals(recipeToBeUpdated.getId())) {

			// Update the data and save the recipe
			recipe.setId(recipeToBeUpdated.getId());
			recipe.setAuthor(recipeToBeUpdated.getAuthor());
			recipe.setComments(recipeToBeUpdated.getComments());

			// Check if the ingredients already exist in DDBB
			recipe.getIngredients().stream().forEach(i -> {

				// We use findFirst because only one ingredient with the same name exists in the
				// DDBB
				Optional<Ingredient> existingIngredient = ingredientRepo.findOptionalByIngredient(i.getIngredient());
				if (existingIngredient.isPresent()) {
					i.setId(existingIngredient.get().getId());
				}

			});

			// Check if keywords already exist in DDBB
			recipe.getKeywords().stream().forEach(k -> {
				Optional<Keyword> existingKeyword = keywordRepo.findOptionalByKeyword(k.getKeyword());
				if (existingKeyword.isPresent()) {
					k.setId(existingKeyword.get().getId());
				}
			});

			return recipeRepo.save(recipe);

		} else {
			logger.error("There is already an existing recipe with this name. Please, change the name");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"There is already an existing recipe with this name. Please, change the name");

		}
	}

	/**
	 * API to delete the recipe
	 * 
	 * @param sessionId The session ID
	 * @param recipeId  ID of the recipe
	 * 
	 * @exception ResponseStatusException In these cases: <br>
	 *                                    - When the recipe doesn't exist <br>
	 *                                    <br>
	 *                                    - When the user isn't a chef - When the
	 *                                    user doesn't exist
	 */
	@DeleteMapping("/recipe/{recipeId}")
	public void deleteRecipe(@RequestHeader(name = "sessionid") String sessionId,
			@NotNull @PathVariable("recipeId") Long recipeId) {

		// Check if the session id is valid
		Long userId = checkSessionId(sessionId);

		logger.info(String.format("New request to delete the recipe with ID %d created by the user with ID %d",
				recipeId, userId));

		// If user exists and it has the right role
		User user = checkUser(userId, RoleType.CHEF);

		// If the recipe exists, delete all the comments, and finally the recipe
		Recipe recipeToBeDeleted = checkRecipe(recipeId);

		// Check if the recipe belongs to the user
		if (recipeToBeDeleted.getAuthor().getId().equals(userId)) {
			recipeRepo.delete(recipeToBeDeleted);

		} else {
			logger.error("The recipe doesn't belong to the user " + user.getUserName());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"The recipe doesn't belong to the user " + user.getUserName());

		}
	}
}