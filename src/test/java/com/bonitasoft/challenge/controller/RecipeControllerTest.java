package com.bonitasoft.challenge.controller;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

import com.bonitasoft.challenge.model.Comment;
import com.bonitasoft.challenge.model.Ingredient;
import com.bonitasoft.challenge.model.Keyword;
import com.bonitasoft.challenge.model.Recipe;
import com.bonitasoft.challenge.model.RoleType;
import com.bonitasoft.challenge.model.User;
import com.bonitasoft.challenge.repository.IngredientRepo;
import com.bonitasoft.challenge.repository.KeywordRepo;
import com.bonitasoft.challenge.repository.RecipeRepo;
import com.bonitasoft.challenge.repository.UserRepo;

@SpringBootTest
class RecipeControllerTest {

	private static final String THIS_IS_A_FAKE_STRING = "THIS_IS_A_FAKE_STRING";
	private static final int THIS_IS_A_FAKE_INT = 123;
	private static final long THIS_IS_A_FAKE_LONG = 111L;
	private static final long THIS_IS_ANOTHER_FAKE_LONG = 222L;

	@Autowired
	RecipeController recipeController;

	@MockBean
	RecipeRepo recipeRepo;

	@MockBean
	KeywordRepo keywordRepo;

	@MockBean
	UserRepo userRepo;

	@MockBean
	IngredientRepo ingredientRepo;

	@Test
	public void testGetAllRecipes() {

		Recipe mockRecipe = mock(Recipe.class);
		when(mockRecipe.getId()).thenReturn(THIS_IS_A_FAKE_LONG);
		Recipe[] arrayRecipe = { mockRecipe };
		Iterable<Recipe> returnedRecipe = () -> Arrays.stream(arrayRecipe).iterator();

		Arrays.asList(mockRecipe).iterator();
		when(recipeRepo.findAll()).thenReturn(returnedRecipe);

		List<Recipe> recipesByKeywords = recipeController.getRecipesByKeywords(Optional.empty());

		assertEquals(recipesByKeywords.size(), 1);
		assertEquals(recipesByKeywords.get(0).getId().longValue(), THIS_IS_A_FAKE_LONG);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetRecipesWithFilter() {

		Keyword mockKeyword = mock(Keyword.class);
		Optional<Keyword> optionalKeyword = Optional.of(mockKeyword);
		when(keywordRepo.findOptionalByKeyword(anyString())).thenReturn(optionalKeyword);

		List<Recipe> recipeList = mock(List.class);
		when(recipeList.size()).thenReturn(THIS_IS_A_FAKE_INT);
		when(recipeList.toString()).thenReturn(THIS_IS_A_FAKE_STRING);
		when(recipeRepo.findByKeywordsIn(anyList())).thenReturn(recipeList);

		List<String> keywords = Arrays.asList("filter1", "filter2");
		List<Recipe> recipesByKeywords = recipeController.getRecipesByKeywords(Optional.of(keywords));

		assertEquals(recipesByKeywords.size(), THIS_IS_A_FAKE_INT);
		assertEquals(recipesByKeywords.toString(), THIS_IS_A_FAKE_STRING);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetRecipeByUser() {

		User mockUser = mock(User.class);
		when(mockUser.getRole()).thenReturn(RoleType.CHEF);
		Optional<User> userFoundById = Optional.of(mockUser);
		when(userRepo.findById(anyLong())).thenReturn(userFoundById);

		List<Recipe> recipeList = mock(List.class);
		when(recipeList.size()).thenReturn(THIS_IS_A_FAKE_INT);
		when(recipeList.toString()).thenReturn(THIS_IS_A_FAKE_STRING);
		when(recipeRepo.findByAuthor(any())).thenReturn(recipeList);

		List<Recipe> recipesByUser = recipeController.getRecipesByUser(1L);

		assertEquals(recipesByUser.size(), THIS_IS_A_FAKE_INT);
		assertEquals(recipesByUser.toString(), THIS_IS_A_FAKE_STRING);

	}

	@Test
	public void testCreateRecipeOk() {

		// Mocking user
		User userMock = mock(User.class);
		when(userMock.getRole()).thenReturn(RoleType.CHEF);
		Optional<User> optionalUserMock = Optional.of(userMock);
		when(userRepo.findById(anyLong())).thenReturn(optionalUserMock);

		// Mocking existing recipes
		Optional<Recipe> optionalRecipe = Optional.empty();
		when(recipeRepo.findOptionalByAuthorAndRecipeName(any(User.class), anyString())).thenReturn(optionalRecipe);

		// Mocking the recipe
		Recipe recipeMock = mock(Recipe.class);
		when(recipeMock.getRecipeName()).thenReturn(THIS_IS_A_FAKE_STRING);
		when(recipeMock.getId()).thenReturn(THIS_IS_ANOTHER_FAKE_LONG);

		// Mocking ingredients
		Ingredient existingIngredient = mock(Ingredient.class);
		when(existingIngredient.getId()).thenReturn(THIS_IS_A_FAKE_LONG);
		Optional<Ingredient> optionalIngredient = Optional.of(existingIngredient);
		when(ingredientRepo.findOptionalByIngredient(anyString())).thenReturn(optionalIngredient);

		// Mocking keywords
		Keyword existingKeyword = mock(Keyword.class);
		when(existingKeyword.getId()).thenReturn(THIS_IS_ANOTHER_FAKE_LONG);
		Optional<Keyword> optionalKeyword = Optional.of(existingKeyword);
		when(keywordRepo.findOptionalByKeyword(anyString())).thenReturn(optionalKeyword);

		when(recipeRepo.save(any(Recipe.class))).thenReturn(recipeMock);

		// Invoke method
		Recipe createdRecipe = recipeController.createRecipe(1l, recipeMock);

		assertEquals(THIS_IS_A_FAKE_STRING, createdRecipe.getRecipeName());
		assertEquals(THIS_IS_ANOTHER_FAKE_LONG, createdRecipe.getId());
	}

	@Test()
	public void testCreateRecipeUserDoesntExist() {

		// Mocking user
		Optional<User> optionalUserMock = Optional.empty();
		when(userRepo.findById(anyLong())).thenReturn(optionalUserMock);

		Recipe recipeMock = mock(Recipe.class);

		// Invoke method
		Exception exception = assertThrows(ResponseStatusException.class, () -> {
			recipeController.createRecipe(1l, recipeMock);
		});

		assertTrue(exception.getMessage().contains("The user doesn't exist"));
	}

	@Test
	public void testCreateRecipeRoleIncorrect() {

		User mockUser = mock(User.class);
		when(mockUser.getRole()).thenReturn(RoleType.ADMIN);
		Optional<User> userFoundById = Optional.of(mockUser);
		when(userRepo.findById(anyLong())).thenReturn(userFoundById);

		Recipe recipeMock = mock(Recipe.class);
		// Invoke method
		Exception exception = assertThrows(ResponseStatusException.class, () -> {
			recipeController.createRecipe(1l, recipeMock);
		});

		assertTrue(exception.getMessage().contains("The role of the user doesn't allow the requested operation"));
	}

	@Test
	public void updateRecipeOk() {

		// Mock recipe
		Recipe mockRecipe = mock(Recipe.class);
		when(mockRecipe.getId()).thenReturn(THIS_IS_A_FAKE_LONG);
		when(mockRecipe.getAuthor()).thenReturn(mock(User.class));
		when(mockRecipe.getComments()).thenReturn(new ArrayList<Comment>());

		Optional<Recipe> optionalRecipe = Optional.of(mockRecipe);
		when(recipeRepo.findById(anyLong())).thenReturn(optionalRecipe);
		when(recipeRepo.findOptionalByAuthorAndRecipeName(any(User.class), anyString())).thenReturn(Optional.empty());

		// Mock user and methods
		User mockUser = mock(User.class);
		when(mockUser.getRole()).thenReturn(RoleType.CHEF);
		Optional<User> optionalMockUser = Optional.of(mockUser);
		when(userRepo.findById(anyLong())).thenReturn(optionalMockUser);

		// Recipe with data to be updated
		Recipe mockNewRecipe = mock(Recipe.class);
		when(mockNewRecipe.getId()).thenReturn(THIS_IS_ANOTHER_FAKE_LONG);
		when(mockNewRecipe.getRecipeName()).thenReturn(THIS_IS_A_FAKE_STRING);
		when(mockNewRecipe.getIngredients()).thenReturn(new ArrayList<Ingredient>());

		// Mocke the save
		when(recipeRepo.save(any(Recipe.class))).thenReturn(mockNewRecipe);

		Recipe updatedRecipe = recipeController.updateRecipe(THIS_IS_A_FAKE_LONG, THIS_IS_ANOTHER_FAKE_LONG,
				mockNewRecipe);

		assertEquals(THIS_IS_ANOTHER_FAKE_LONG, updatedRecipe.getId());
		assertEquals(THIS_IS_A_FAKE_STRING, updatedRecipe.getRecipeName());

	}

	@Test
	public void updateRecipeDoesnExist() {

		// Mock user and methods
		User mockUser = mock(User.class);
		when(mockUser.getRole()).thenReturn(RoleType.CHEF);
		Optional<User> optionalMockUser = Optional.of(mockUser);
		when(userRepo.findById(anyLong())).thenReturn(optionalMockUser);

		// Mock recipe doesnt exist
		Optional<Recipe> optionalRecipe = Optional.empty();
		when(recipeRepo.findById(anyLong())).thenReturn(optionalRecipe);

		Exception exception = assertThrows(ResponseStatusException.class, () -> {
			recipeController.updateRecipe(THIS_IS_A_FAKE_LONG, THIS_IS_ANOTHER_FAKE_LONG, mock(Recipe.class));
		});

		assertTrue(exception.getMessage().contains("The recipe doesn't exist"));
	}

	@Test
	public void updateRecipeAnotherWithSameName() {

		// Mock user and methods
		User mockUser = mock(User.class);
		when(mockUser.getRole()).thenReturn(RoleType.CHEF);
		Optional<User> optionalMockUser = Optional.of(mockUser);
		when(userRepo.findById(anyLong())).thenReturn(optionalMockUser);

		// Mock recipe
		Recipe mockRecipe = mock(Recipe.class);
		when(mockRecipe.getId()).thenReturn(THIS_IS_A_FAKE_LONG);
		when(mockRecipe.getAuthor()).thenReturn(mock(User.class));
		when(mockRecipe.getComments()).thenReturn(new ArrayList<Comment>());

		// Mock recipe
		Recipe anotherMockRecipe = mock(Recipe.class);
		when(anotherMockRecipe.getId()).thenReturn(THIS_IS_ANOTHER_FAKE_LONG);

		// Mocking repo calls
		Optional<Recipe> optionalRecipe = Optional.of(mockRecipe);
		when(recipeRepo.findById(anyLong())).thenReturn(optionalRecipe);
		Optional<Recipe> anotherOptionalRecipe = Optional.of(anotherMockRecipe);
		when(recipeRepo.findOptionalByAuthorAndRecipeName(any(User.class), anyString()))
				.thenReturn(anotherOptionalRecipe);

		// Recipe with data to be updated
		Recipe mockNewRecipe = mock(Recipe.class);
		when(mockNewRecipe.getId()).thenReturn(THIS_IS_ANOTHER_FAKE_LONG);
		when(mockNewRecipe.getRecipeName()).thenReturn(THIS_IS_A_FAKE_STRING);
		when(mockNewRecipe.getIngredients()).thenReturn(new ArrayList<Ingredient>());

		Exception exception = assertThrows(ResponseStatusException.class, () -> {
			recipeController.updateRecipe(THIS_IS_A_FAKE_LONG, THIS_IS_A_FAKE_LONG, mockNewRecipe);
		});

		assertTrue(exception.getMessage()
				.contains("There is already an existing recipe with this name. Please, change the name"));
	}

	@Test
	public void deleteRecipeOk() {

		// Mock user and methods
		User mockUser = mock(User.class);
		when(mockUser.getRole()).thenReturn(RoleType.CHEF);
		when(mockUser.getId()).thenReturn(THIS_IS_A_FAKE_LONG);
		Optional<User> optionalMockUser = Optional.of(mockUser);
		when(userRepo.findById(anyLong())).thenReturn(optionalMockUser);

		// Mock recipe
		Recipe mockRecipe = mock(Recipe.class);
		when(mockRecipe.getId()).thenReturn(THIS_IS_ANOTHER_FAKE_LONG);
		when(mockRecipe.getAuthor()).thenReturn(mockUser);
		Optional<Recipe> optionalRecipe = Optional.of(mockRecipe);
		when(recipeRepo.findById(anyLong())).thenReturn(optionalRecipe);

		recipeController.deleteRecipe(THIS_IS_A_FAKE_LONG, THIS_IS_ANOTHER_FAKE_LONG);
	}

	@Test
	public void deleteRecipeBelongsAnotherUser() {

		// Mock user and methods
		User mockUser = mock(User.class);
		when(mockUser.getRole()).thenReturn(RoleType.CHEF);
		when(mockUser.getId()).thenReturn(THIS_IS_A_FAKE_LONG);
		Optional<User> optionalMockUser = Optional.of(mockUser);
		when(userRepo.findById(anyLong())).thenReturn(optionalMockUser);

		// Mock recipe
		Recipe mockRecipe = mock(Recipe.class);
		User anotherMockUser = mock(User.class);
		when(anotherMockUser.getId()).thenReturn(THIS_IS_ANOTHER_FAKE_LONG);
		when(mockRecipe.getId()).thenReturn(THIS_IS_A_FAKE_LONG);
		when(mockRecipe.getAuthor()).thenReturn(anotherMockUser);
		Optional<Recipe> optionalRecipe = Optional.of(mockRecipe);
		when(recipeRepo.findById(anyLong())).thenReturn(optionalRecipe);

		Exception exception = assertThrows(ResponseStatusException.class, () -> {
			recipeController.deleteRecipe(THIS_IS_A_FAKE_LONG, THIS_IS_ANOTHER_FAKE_LONG);
		});

		assertTrue(exception.getMessage().contains("The recipe doesn't belong to the user "));

	}
}