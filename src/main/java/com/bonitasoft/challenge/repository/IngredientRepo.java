package com.bonitasoft.challenge.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.bonitasoft.challenge.model.Ingredient;

@Repository
public interface IngredientRepo extends CrudRepository<Ingredient, Long> {

	/**
	 * Get the ingredient if it exists
	 * 
	 * @param ingredient Name of the ingredient
	 * @return Optional containing th ingredient, if it exists
	 */
	Optional<Ingredient> findOptionalByIngredient(String ingredient);
}
