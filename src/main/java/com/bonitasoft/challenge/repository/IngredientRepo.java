package com.bonitasoft.challenge.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.bonitasoft.challenge.model.Ingredient;

@Repository
public interface IngredientRepo extends CrudRepository<Ingredient, Long> {

}
