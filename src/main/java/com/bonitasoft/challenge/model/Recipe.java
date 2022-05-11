package com.bonitasoft.challenge.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.lang.NonNull;

@Entity
@Table(name = "recipes")
public class Recipe {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recipe_id")
	@NonNull
	private Long id;

	@ManyToOne(targetEntity = User.class)
	@JoinColumn(name = "author_id")
	@NonNull
	private User author;

	@Column(name = "recipe_name")
	@NonNull
	private String recipeName;

	@ManyToMany
	@JoinTable(name = "ingredients_recipes", joinColumns = { @JoinColumn(name = "recipe_id") }, inverseJoinColumns = {
			@JoinColumn(name = "ingredient_id") })
	private List<Ingredient> ingredients;

	@ManyToMany
	@JoinTable(name = "keywords_recipes", joinColumns = { @JoinColumn(name = "recipe_id") }, inverseJoinColumns = {
			@JoinColumn(name = "keyword_id") })
	private List<Keyword> keywords;

	@OneToMany(mappedBy = "recipe", cascade = CascadeType.REMOVE)
	private List<Comment> comments;

	/**
	 * 
	 */
	public Recipe() {
		super();
		this.ingredients = new ArrayList<Ingredient>();
		this.keywords = new ArrayList<Keyword>();
		this.comments = new ArrayList<Comment>();
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
	 * @return the author
	 */
	public User getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(User author) {
		this.author = author;
	}

	/**
	 * @return the recipeName
	 */
	public String getRecipeName() {
		return recipeName;
	}

	/**
	 * @param recipeName the recipeName to set
	 */
	public void setRecipeName(String recipeName) {
		this.recipeName = recipeName;
	}

	/**
	 * @return the ingredients
	 */
	public List<Ingredient> getIngredients() {
		return ingredients;
	}

	/**
	 * @return the keywords
	 */
	public List<Keyword> getKeywords() {
		return keywords;
	}

	/**
	 * @return the comments
	 */
	public List<Comment> getComments() {
		return comments;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Recipe [id=").append(id).append(", author=").append(author).append(", recipeName=")
				.append(recipeName).append(", ingredients=").append(ingredients).append(", keywords=").append(keywords)
				.append(", comments=").append(comments).append("]");
		return builder.toString();
	}
}