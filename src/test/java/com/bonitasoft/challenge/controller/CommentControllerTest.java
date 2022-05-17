package com.bonitasoft.challenge.controller;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

import com.bonitasoft.challenge.model.Comment;
import com.bonitasoft.challenge.model.Recipe;
import com.bonitasoft.challenge.model.RoleType;
import com.bonitasoft.challenge.model.User;
import com.bonitasoft.challenge.repository.CommentRepo;
import com.bonitasoft.challenge.repository.RecipeRepo;
import com.bonitasoft.challenge.repository.UserRepo;

@SpringBootTest
public class CommentControllerTest {

	private static final long MOCK_USER_ID = 111L;
	private static final long MOCK_RECIPE_ID = 222L;
	private static final long MOCK_COMMENT_ID = 321L;
	private static final String MOCK_COMMENT = "MOCK COMMENT";

	@Autowired
	CommentController commentController;

	@MockBean
	RecipeRepo recipeRepo;

	@MockBean
	UserRepo userRepo;

	@MockBean
	CommentRepo commentRepo;

	@Test
	public void createCommentOK() {

		// Mock user and methods
		User mockUser = mock(User.class);
		when(mockUser.getRole()).thenReturn(RoleType.USER);
		when(mockUser.getId()).thenReturn(MOCK_USER_ID);
		Optional<User> optionalMockUser = Optional.of(mockUser);
		when(userRepo.findById(anyLong())).thenReturn(optionalMockUser);

		// Mock recipe
		Recipe mockRecipe = mock(Recipe.class);
		when(mockRecipe.getId()).thenReturn(MOCK_RECIPE_ID);
		when(mockRecipe.getAuthor()).thenReturn(mockUser);
		Optional<Recipe> optionalRecipe = Optional.of(mockRecipe);
		when(recipeRepo.findById(anyLong())).thenReturn(optionalRecipe);

		// Mock comment
		Comment mockSavedComment = mock(Comment.class);
		when(mockSavedComment.getComment()).thenReturn(MOCK_COMMENT);
		when(mockSavedComment.getId()).thenReturn(MOCK_COMMENT_ID);
		when(commentRepo.save(any(Comment.class))).thenReturn(mockSavedComment);

		Comment mockComment = mock(Comment.class);
		Comment createdComment = commentController.createComment(MOCK_USER_ID, MOCK_RECIPE_ID, mockComment);

		assertEquals(MOCK_COMMENT, createdComment.getComment());
		assertEquals(MOCK_COMMENT_ID, createdComment.getId());

	}

	@Test
	public void createCommentUserDoesntExist() {

		// Mock user and methods
		Optional<User> optionalMockUser = Optional.empty();
		when(userRepo.findById(anyLong())).thenReturn(optionalMockUser);

		// Mock comment
		Comment mockComment = mock(Comment.class);

		Exception exception = assertThrows(ResponseStatusException.class, () -> {
			commentController.createComment(MOCK_USER_ID, MOCK_RECIPE_ID, mockComment);
		});

		assertTrue(exception.getMessage().contains("The user doesn't exist"));

	}

	@Test
	public void createCommentRecipeDoesntExist() {

		// Mock user and methods
		User mockUser = mock(User.class);
		when(mockUser.getRole()).thenReturn(RoleType.USER);
		when(mockUser.getId()).thenReturn(MOCK_USER_ID);
		Optional<User> optionalMockUser = Optional.of(mockUser);
		when(userRepo.findById(anyLong())).thenReturn(optionalMockUser);

		// Mock recipe
		Optional<Recipe> optionalRecipe = Optional.empty();
		when(recipeRepo.findById(anyLong())).thenReturn(optionalRecipe);

		// Mock comment
		Comment mockComment = mock(Comment.class);

		Exception exception = assertThrows(ResponseStatusException.class, () -> {
			commentController.createComment(MOCK_USER_ID, MOCK_RECIPE_ID, mockComment);
		});

		assertTrue(exception.getMessage().contains("The recipe doesn't exist"));

	}
}
