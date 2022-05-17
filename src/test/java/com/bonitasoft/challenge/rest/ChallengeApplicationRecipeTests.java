package com.bonitasoft.challenge.rest;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.bonitasoft.challenge.model.Ingredient;
import com.bonitasoft.challenge.model.Keyword;
import com.bonitasoft.challenge.model.Recipe;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest
public class ChallengeApplicationRecipeTests {

	private MockMvc mvc;
	private ObjectMapper objectMapper;

	@Autowired
	WebApplicationContext webApplicationContext;

	@BeforeAll
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);
	}

	@SuppressWarnings("unchecked")
	@Test
	@Order(1)
	public void getRecipesList() throws Exception {
		String uri = "/api/v1/recipes";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());
		List<Recipe> recipeList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), List.class);
		assertEquals(6, recipeList.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	@Order(2)
	public void getRecipesByKeywords() throws Exception {
		String uri = "/api/v1/recipes?keywords=Keyword4";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());
		List<Recipe> recipeList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), List.class);
		assertEquals(2, recipeList.size());

	}

	@Test
	@Order(3)
	public void getRecipeByAdmin() throws Exception {

		String uri = "/api/v1/user/1/recipe";

		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("The role of the user doesn't allow the requested operation",
				mvcResult.getResponse().getErrorMessage());
	}

	@SuppressWarnings("unchecked")
	@Test
	@Order(4)
	public void getRecipeByChef() throws Exception {

		String uri = "/api/v1/user/2/recipe";

		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());
		List<Recipe> recipeList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), List.class);
		assertEquals(3, recipeList.size());
	}

	@Test
	@Order(5)
	public void createRecipeOk() throws Exception {

		String uri = "/api/v1/user/2/recipe";

		Recipe newRecipe = new Recipe();
		newRecipe.setRecipeName("Recipe 7 by user 2");
		List<Ingredient> ingredientsList = List.of(new Ingredient("Ingredient 4"), new Ingredient("Ingredient 13"));
		newRecipe.setIngredients(ingredientsList);
		List<Keyword> keywordsList = List.of(new Keyword("Keyword6"), new Keyword("Keyword13"));
		newRecipe.setKeywords(keywordsList);

		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content(objectMapper.writeValueAsString(newRecipe)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());
		Recipe recipe = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Recipe.class);
		assertEquals(7l, recipe.getId().longValue());
		assertEquals(newRecipe.getRecipeName(), recipe.getRecipeName());
		assertEquals(newRecipe.getIngredients().get(0).getIngredient(), recipe.getIngredients().get(0).getIngredient());
		assertEquals(4, recipe.getIngredients().get(0).getId().longValue());
		assertEquals(13, recipe.getIngredients().get(1).getId().longValue());
		assertEquals(6, recipe.getKeywords().get(0).getId().longValue());
		assertEquals(13, recipe.getKeywords().get(1).getId().longValue());
	}

	@Test
	@Order(6)
	public void createRecipeDuplicated() throws Exception {

		String uri = "/api/v1/user/2/recipe";

		Recipe newRecipe = new Recipe();
		newRecipe.setRecipeName("Recipe 7 by user 2");
		List<Ingredient> ingredientsList = List.of(new Ingredient("Ingredient 4"), new Ingredient("Ingredient 13"));
		newRecipe.setIngredients(ingredientsList);
		List<Keyword> keywordsList = List.of(new Keyword("Keyword6"), new Keyword("Keyword13"));
		newRecipe.setKeywords(keywordsList);

		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content(objectMapper.writeValueAsString(newRecipe)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("The recipe already exists. Change the name of the recipe",
				mvcResult.getResponse().getErrorMessage());
	}

	@Test
	@Order(7)
	public void createRecipeUserDoesntExist() throws Exception {

		String uri = "/api/v1/user/10/recipe";

		Recipe newRecipe = new Recipe();
		newRecipe.setRecipeName("Recipe");
		List<Ingredient> ingredientsList = List.of(new Ingredient("Ingredient"));
		newRecipe.setIngredients(ingredientsList);
		List<Keyword> keywordsList = List.of(new Keyword("Keyword"));
		newRecipe.setKeywords(keywordsList);

		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content(objectMapper.writeValueAsString(newRecipe)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("The user doesn't exist", mvcResult.getResponse().getErrorMessage());
	}

	@Test
	@Order(8)
	public void updateRecipeOk() throws Exception {

		String uri = "/api/v1/user/2/recipe/7";

		Recipe newRecipe = new Recipe();
		newRecipe.setRecipeName("New name");
		List<Ingredient> ingredientsList = List.of(new Ingredient("Ingredient 4"));
		newRecipe.setIngredients(ingredientsList);
		List<Keyword> keywordsList = List.of(new Keyword("Keyword10"), new Keyword("Keyword11"),
				new Keyword("Keyword13"));
		newRecipe.setKeywords(keywordsList);

		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.put(uri)
				.content(objectMapper.writeValueAsString(newRecipe)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());
		Recipe recipe = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Recipe.class);
		assertEquals(newRecipe.getRecipeName(), recipe.getRecipeName());
		assertEquals(1, recipe.getIngredients().size());
		assertEquals(newRecipe.getIngredients().get(0).getIngredient(), recipe.getIngredients().get(0).getIngredient());
		assertEquals(3, recipe.getKeywords().size());
		assertEquals(10, recipe.getKeywords().get(0).getId().longValue());
		assertEquals(11, recipe.getKeywords().get(1).getId().longValue());
	}

	@Test
	@Order(9)
	public void updateRecipeDoesntExist() throws Exception {

		String uri = "/api/v1/user/2/recipe/10";

		Recipe newRecipe = new Recipe();
		newRecipe.setRecipeName("Recipe");
		List<Ingredient> ingredientsList = List.of(new Ingredient("Ingredient"));
		newRecipe.setIngredients(ingredientsList);
		List<Keyword> keywordsList = List.of(new Keyword("Keyword"));
		newRecipe.setKeywords(keywordsList);

		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.put(uri)
				.content(objectMapper.writeValueAsString(newRecipe)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("The recipe doesn't exist", mvcResult.getResponse().getErrorMessage());
	}

	@Test
	@Order(10)
	public void updateRecipeSameName() throws Exception {

		String uri = "/api/v1/user/2/recipe/1";

		Recipe newRecipe = new Recipe();
		newRecipe.setRecipeName("Recipe 2 by user 2");
		List<Ingredient> ingredientsList = List.of(new Ingredient("Ingredient"));
		newRecipe.setIngredients(ingredientsList);
		List<Keyword> keywordsList = List.of(new Keyword("Keyword"));
		newRecipe.setKeywords(keywordsList);

		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.put(uri)
				.content(objectMapper.writeValueAsString(newRecipe)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("There is already an existing recipe with this name. Please, change the name",
				mvcResult.getResponse().getErrorMessage());
	}

	@Test
	@Order(10)
	public void deleteRecipeDoesntBelongToUser() throws Exception {

		String uri = "/api/v1/user/3/recipe/2";

		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.delete(uri).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("The recipe doesn't belong to the user ",
				mvcResult.getResponse().getErrorMessage().substring(0, 38));
	}

	@Test
	@Order(11)
	public void deleteRecipeOk() throws Exception {

		String uri = "/api/v1/user/2/recipe/7";

		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.delete(uri).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());
	}
}