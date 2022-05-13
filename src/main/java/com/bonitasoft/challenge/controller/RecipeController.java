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

import com.bonitasoft.challenge.model.Ingredient;
import com.bonitasoft.challenge.model.Keyword;
import com.bonitasoft.challenge.model.Recipe;
import com.bonitasoft.challenge.model.RoleType;
import com.bonitasoft.challenge.model.User;
import com.bonitasoft.challenge.repository.CommentRepo;
import com.bonitasoft.challenge.repository.IngredientRepo;
import com.bonitasoft.challenge.repository.KeywordRepo;
import com.bonitasoft.challenge.repository.RecipeRepo;
import com.bonitasoft.challenge.repository.UserRepo;

@RestController
@RequestMapping("/api/v1/user")
public class RecipeController {

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

	/**
	 * API to get all the recipes created by a user
	 * 
	 * @param id ID of the user
	 * @return List containing all the recipes created by the user
	 * 
	 * @exception ResponseStatusException In these cases: <br>
	 *                                    - When the user doesn't exist <br>
	 *                                    - When the user isn't a chef
	 */
	@GetMapping("/{id}/recipe")
	public List<Recipe> getRecipesByUser(@NotNull @PathVariable("id") Long id) {

		// If user exists, read all recipes
		Optional<User> user = userRepo.findById(id);
		if (user.isPresent()) {

			// We can perform the action if the user is a CHEF
			if (RoleType.CHEF.equals(user.get().getRole())) {
				return recipeRepo.findByAuthor(user.get());

			} else {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user isn't a chef");
			}

		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user doesn't exist");
		}
	}

	/**
	 * API to create a new recipe
	 * 
	 * @param id     ID of the user creating the new recipe
	 * @param recipe Information of the new recipe
	 * @return The created recipe
	 * 
	 * @exception ResponseStatusException In these cases: <br>
	 *                                    - When there is already a recipe created
	 *                                    by the same user with the same name <br>
	 *                                    - When the user doesn't exist <br>
	 *                                    - When the user isn't a chef
	 */
	@PostMapping("/{id}/recipe")
	public Recipe createRecipe(@NotNull @PathVariable("id") Long id, @Valid @RequestBody Recipe recipe) {

		Optional<User> user = userRepo.findById(id);
		if (user.isPresent()) {

			// We can perform the action if the user is a CHEF
			if (RoleType.CHEF.equals(user.get().getRole())) {

				// Check that there is no other recipe created by the same user with the same
				// name
				if (recipeRepo.findOptionalByAuthorAndRecipeName(user.get(), recipe.getRecipeName()).isEmpty()) {

					recipe.setAuthor(user.get());

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
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
							"The recipe already exists. Change the name of the recipe");
				}

			} else {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user isn't a chef");
			}

		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user doesn't exist");
		}
	}

	/**
	 * API to update a recipe for an existing usr
	 * 
	 * @param id       ID of the user who has created the recipe
	 * @param recipeId ID of the recipe to be updated
	 * @param recipe   The object containing the info to be updated
	 * @return An object containing the new information of the recipe
	 * 
	 * @exception ResponseStatusException In these cases: <br>
	 *                                    - The user doesn't exist <br>
	 *                                    - The recipe doesn't exist <br>
	 *                                    - The user had already created a recipe
	 *                                    with the same name. A new name must be
	 *                                    choosen <br>
	 *                                    - When the user isn't a chef
	 */
	@PutMapping("/{id}/recipe/{recipeId}")
	public Recipe updateRecipe(@NotNull @PathVariable("id") Long id, @NotNull @PathVariable("recipeId") Long recipeId,
			@Valid @RequestBody Recipe recipe) {

		// Check if the user exists
		Optional<User> user = userRepo.findById(id);
		if (user.isPresent()) {

			// We can perform the action if the user is a CHEF
			if (RoleType.CHEF.equals(user.get().getRole())) {

				// Check if the recipe exists
				Optional<Recipe> recipeToBeUpdated = recipeRepo.findById(recipeId);
				if (recipeToBeUpdated.isPresent()) {

					// Check if there is already a recipe with the same name
					Optional<Recipe> recipeWithSameName = recipeRepo.findOptionalByAuthorAndRecipeName(
							recipeToBeUpdated.get().getAuthor(), recipe.getRecipeName());
					if (recipeWithSameName.isEmpty()
							|| recipeWithSameName.get().getId().equals(recipeToBeUpdated.get().getId())) {

						// Update the data and save the recipe
						recipe.setId(recipeToBeUpdated.get().getId());
						recipe.setAuthor(recipeToBeUpdated.get().getAuthor());
						recipe.setComments(recipeToBeUpdated.get().getComments());

						// Check if the ingredients already exist in DDBB
						recipe.getIngredients().stream().forEach(i -> {

							// We use findFirst because only one ingredient with the same name exists in the
							// DDBB
							Optional<Ingredient> existingIngredient = recipeToBeUpdated.get().getIngredients().stream()
									.filter(j -> j.getIngredient().equals(i.getIngredient())).findFirst();
							if (existingIngredient.isPresent()) {
								i.setId(existingIngredient.get().getId());
							}

						});

						// Check if keywords already exist in DDBB
						recipe.getKeywords().stream().forEach(k -> {
							Optional<Keyword> existingKeyword = recipeToBeUpdated.get().getKeywords().stream()
									.filter(l -> l.getKeyword().equals(k.getKeyword())).findFirst();
							if (existingKeyword.isPresent()) {
								k.setId(existingKeyword.get().getId());
							}
						});

						return recipeRepo.save(recipe);

					} else {
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
								"There is already an existing recipe with this name. Please, change the name");

					}

				} else {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The recipe doesn't exist");

				}

			} else {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user isn't a chef");
			}

		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user doesn't exist");
		}
	}

	/**
	 * API to delete the recipe
	 * 
	 * @param id       ID of the author
	 * @param recipeId ID of the recipe
	 * 
	 * @exception ResponseStatusException In these cases: <br>
	 *                                    - When the recipe doesn't exist <br>
	 *                                    <br>
	 *                                    - When the user isn't a chef - When the
	 *                                    user doesn't exist
	 */
	@DeleteMapping("/{id}/recipe/{recipeId}")
	public void deleteRecipe(@NotNull @PathVariable("id") Long id, @NotNull @PathVariable("recipeId") Long recipeId) {

		// Check if the user exists
		Optional<User> user = userRepo.findById(id);
		if (user.isPresent()) {

			// We can perform the action if the user is a CHEF
			if (RoleType.CHEF.equals(user.get().getRole())) {

				// If the recipe exists, delete all the comments, and finally the recipe
				Optional<Recipe> recipeToBeDeleted = recipeRepo.findById(recipeId);

				if (recipeToBeDeleted.isPresent()) {

					// Check if the recipe belongs to the user
					if (recipeToBeDeleted.get().getAuthor().getId().equals(id)) {
						// TODO: check if this is needed
//						recipeToBeDeleted.get().getComments().stream().forEach(c -> commentRepo.delete(c));
						recipeRepo.delete(recipeToBeDeleted.get());

					} else {
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
								"The recipe doesn't belong to the user " + user.get().getUserName());

					}
				} else {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The recipe doesn't exist");

				}

			} else {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user isn't a chef");
			}

		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user doesn't exist");

		}
	}

}
