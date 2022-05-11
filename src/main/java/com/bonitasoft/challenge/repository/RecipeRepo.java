package com.bonitasoft.challenge.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.bonitasoft.challenge.model.Recipe;

@Repository
public interface RecipeRepo extends CrudRepository<Recipe, Long> {

}
