package com.bonitasoft.challenge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.bonitasoft.challenge.model.Recipe;
import com.bonitasoft.challenge.model.User;

@Repository
public interface RecipeRepo extends CrudRepository<Recipe, Long> {

	/**
	 * Gets all recipes created by the author
	 * 
	 * @param user
	 * @return List of recipes
	 */
	List<Recipe> findByAuthor(User user);

	/**
	 * Gets Optional object containing the recipe for the requested user, if it
	 * exists
	 * 
	 * @param user       Object containing the author of the recipe
	 * @param recipeName Name of the recipe
	 * @return Optional object containing the recipe
	 */
	Optional<Recipe> findOptionalByAuthorAndRecipeName(User user, String recipeName);
}
