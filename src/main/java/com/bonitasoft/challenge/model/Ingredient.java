package com.bonitasoft.challenge.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.lang.NonNull;

@Entity
@Table(name = "ingredients")
public class Ingredient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ingredient_id")
	@NonNull
	private Long id;

	@Column(name = "ingredient", unique = true)
	@NonNull
	private String ingredient;

	/**
	 * 
	 */
	public Ingredient() {
		super();
	}

	/**
	 * @param ingredient
	 */
	public Ingredient(String ingredient) {
		super();
		this.ingredient = ingredient;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the ingredient
	 */
	public String getIngredient() {
		return ingredient;
	}

	/**
	 * @param ingredient the ingredient to set
	 */
	public void setIngredient(String ingredient) {
		this.ingredient = ingredient;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Ingredient [id=").append(id).append(", ingredient=").append(ingredient).append("]");
		return builder.toString();
	}
}