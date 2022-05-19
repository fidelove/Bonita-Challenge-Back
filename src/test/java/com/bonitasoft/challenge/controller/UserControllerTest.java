package com.bonitasoft.challenge.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

import com.bonitasoft.challenge.model.RoleType;
import com.bonitasoft.challenge.model.User;
import com.bonitasoft.challenge.model.UserLogged;
import com.bonitasoft.challenge.repository.KeywordRepo;
import com.bonitasoft.challenge.repository.RecipeRepo;
import com.bonitasoft.challenge.repository.UserRepo;

@SpringBootTest
public class UserControllerTest {

	private static final String THIS_IS_A_FAKE_STRING = "THIS_IS_A_FAKE_STRING";
	private static final Long THIS_IS_A_FAKE_LONG = 1L;
	private static final Long THIS_IS_ANOTHER_FAKE_LONG = 2L;
	private static final String THIS_IS_A_FAKE_EMAIL = "email@bonita.com";
	private static final String MOCK_SESSION_ID = "MOCK_SESSION_ID";

	@Autowired
	UserController userController;

	@MockBean
	RecipeRepo recipeRepo;

	@MockBean
	KeywordRepo keywordRepo;

	@MockBean
	UserRepo userRepo;

	@MockBean
	Map<String, Long> sessionManager;

	@Test
	public void loginUserDoesnExist() throws Exception {

		// Mock the nonexistent user and methods
		User mockUser = mock(User.class);
		when(mockUser.getUserName()).thenReturn(THIS_IS_A_FAKE_STRING);
		when(userRepo.findOptionalByUserName(anyString())).thenReturn(Optional.empty());

		// Invoke method
		Exception exception = assertThrows(ResponseStatusException.class, () -> {
			userController.login(mockUser);
		});

		assertTrue(exception.getMessage().contains("The user login or password is wrong"));
	}

	@Test
	public void loginWrongPassword() throws Exception {

		// Mock user and methods
		User mockUser = mock(User.class);
		when(mockUser.getUserName()).thenReturn(THIS_IS_A_FAKE_STRING);
		when(mockUser.getUserPassword()).thenReturn(THIS_IS_A_FAKE_STRING);

		User ddbbMockUser = mock(User.class);
		when(ddbbMockUser.getUserPassword()).thenReturn(THIS_IS_A_FAKE_STRING + THIS_IS_A_FAKE_STRING);

		when(userRepo.findOptionalByUserName(anyString())).thenReturn(Optional.of(ddbbMockUser));

		// Invoke method
		Exception exception = assertThrows(ResponseStatusException.class, () -> {
			userController.login(mockUser);
		});

		assertTrue(exception.getMessage().contains("The user login or password is wrong"));
	}

	@Test
	public void loginOk() throws Exception {

		// Mock users and methods
		User mockUser = mock(User.class);
		when(mockUser.getUserName()).thenReturn(THIS_IS_A_FAKE_STRING);
		when(mockUser.getUserPassword()).thenReturn(THIS_IS_A_FAKE_STRING);

		User ddbbMockUser = mock(User.class);
		when(ddbbMockUser.getId()).thenReturn(THIS_IS_A_FAKE_LONG);
		when(ddbbMockUser.getRole()).thenReturn(RoleType.CHEF);
		when(ddbbMockUser.getUserEmail()).thenReturn(THIS_IS_A_FAKE_STRING);
		when(ddbbMockUser.getUserName()).thenReturn(THIS_IS_A_FAKE_STRING);
		when(ddbbMockUser.getUserPassword()).thenReturn(THIS_IS_A_FAKE_STRING);

		when(userRepo.findOptionalByUserName(anyString())).thenReturn(Optional.of(ddbbMockUser));

		// Invoke method
		UserLogged userLogged = userController.login(mockUser);

		assertEquals(THIS_IS_A_FAKE_LONG, userLogged.getId().longValue());
		assertNotNull(userLogged.getSessionId());
	}

	@Test
	public void logoutOk() throws Exception {

		// Mock the user and methods
		UserLogged mockUser = mock(UserLogged.class);
		when(mockUser.getSessionId()).thenReturn(THIS_IS_A_FAKE_STRING);
		when(sessionManager.get(anyString())).thenReturn(THIS_IS_A_FAKE_LONG);
		when(sessionManager.remove(anyString())).thenReturn(THIS_IS_A_FAKE_LONG);
		when(userRepo.findById(anyLong())).thenReturn(Optional.of(mock(User.class)));

		// Mock session ID
		when(sessionManager.get(anyString())).thenReturn(THIS_IS_A_FAKE_LONG);

		// Invoke method
		userController.logout(MOCK_SESSION_ID);
	}

	@Test
	public void logoutSessionIdDoesntExist() throws Exception {

		// Mock the user and methods
		UserLogged mockUser = mock(UserLogged.class);
		when(mockUser.getSessionId()).thenReturn(THIS_IS_A_FAKE_STRING);
		when(sessionManager.get(anyString())).thenReturn(null);
		when(sessionManager.remove(anyString())).thenReturn(THIS_IS_A_FAKE_LONG);
		when(userRepo.findById(anyLong())).thenReturn(Optional.of(mock(User.class)));

		// Invoke method
		Exception exception = assertThrows(ResponseStatusException.class, () -> {
			userController.logout(MOCK_SESSION_ID);
		});

		assertTrue(exception.getMessage().contains("The user wasn't logged in"));
	}

	@Test
	public void getAllUsers() throws Exception {

		// Mock user and method
		User mockUser = mock(User.class);
		when(mockUser.getId()).thenReturn(THIS_IS_A_FAKE_LONG);
		User[] arrayMockedUsers = { mockUser };
		Iterable<User> allMockedUsers = () -> Arrays.stream(arrayMockedUsers).iterator();
		when(userRepo.findAll()).thenReturn(allMockedUsers);

		// Mock session ID
		when(sessionManager.get(anyString())).thenReturn(THIS_IS_A_FAKE_LONG);

		// Invoke method
		List<User> allUsers = userController.allUsers(MOCK_SESSION_ID);

		assertEquals(1, allUsers.size());
		assertEquals(THIS_IS_A_FAKE_LONG, allUsers.get(0).getId().longValue());
	}

	@Test
	public void getUserByIdDoesntExist() throws Exception {

		// Mock methods
		when(userRepo.findById(anyLong())).thenReturn(Optional.empty());

		// Mock session ID
		when(sessionManager.get(anyString())).thenReturn(THIS_IS_A_FAKE_LONG);

		// Invoke method
		Exception exception = assertThrows(ResponseStatusException.class, () -> {
			userController.getUserById(MOCK_SESSION_ID, THIS_IS_A_FAKE_LONG);
		});

		assertTrue(exception.getMessage().contains("The user doesn't exist"));
	}

	@Test
	public void getUserByIdOk() throws Exception {

		// Mock user and method
		User mockUser = mock(User.class);
		when(mockUser.getId()).thenReturn(THIS_IS_A_FAKE_LONG);
		when(userRepo.findById(anyLong())).thenReturn(Optional.of(mockUser));

		// Mock session ID
		when(sessionManager.get(anyString())).thenReturn(THIS_IS_A_FAKE_LONG);

		// Invoke method
		User userById = userController.getUserById(MOCK_SESSION_ID, THIS_IS_A_FAKE_LONG);

		assertEquals(THIS_IS_A_FAKE_LONG, userById.getId().longValue());
	}

	@Test
	public void createUserNameAlreadyExists() throws Exception {

		// Mock user and methods
		User mockUser = mock(User.class);
		List<User> userList = Arrays.asList(mockUser);
		when(mockUser.getUserName()).thenReturn(THIS_IS_A_FAKE_STRING);
		when(mockUser.getUserEmail()).thenReturn(THIS_IS_A_FAKE_EMAIL);
		when(userRepo.findByUserNameOrUserEmail(anyString(), anyString())).thenReturn(userList);

		// Mock session ID
		when(sessionManager.get(anyString())).thenReturn(THIS_IS_A_FAKE_LONG);

		// Invoke method
		Exception exception = assertThrows(ResponseStatusException.class, () -> {
			userController.createUser(MOCK_SESSION_ID, mockUser);
		});

		assertTrue(exception.getMessage()
				.contains("There is already a user with that username or email. Please change them"));

	}

	@Test
	public void createUserOk() throws Exception {

		// Mock user and methods
		User mockUser = mock(User.class);
		when(mockUser.getId()).thenReturn(THIS_IS_A_FAKE_LONG);
		when(mockUser.getUserEmail()).thenReturn(THIS_IS_A_FAKE_EMAIL);
		when(userRepo.findByUserNameOrUserEmail(anyString(), anyString())).thenReturn(Collections.emptyList());
		when(userRepo.save(any(User.class))).thenReturn(mockUser);

		// Mock session ID
		when(sessionManager.get(anyString())).thenReturn(THIS_IS_A_FAKE_LONG);

		// Invoke method
		User createdUser = userController.createUser(MOCK_SESSION_ID, mockUser);
		assertEquals(THIS_IS_A_FAKE_LONG, createdUser.getId().longValue());
	}

	@Test
	public void updateUserNameAlreadyExists() throws Exception {

		// Mock user and methods
		User mockUser = mock(User.class);
		when(mockUser.getId()).thenReturn(THIS_IS_ANOTHER_FAKE_LONG);
		when(userRepo.findById(anyLong())).thenReturn(Optional.of(mockUser));
		List<User> userList = Arrays.asList(mockUser);
		when(userRepo.findByUserNameOrUserEmail(THIS_IS_A_FAKE_STRING, THIS_IS_A_FAKE_EMAIL)).thenReturn(userList);
		when(userRepo.save(any(User.class))).thenReturn(mockUser);

		User mockUpdateUser = mock(User.class);
		when(mockUpdateUser.getUserName()).thenReturn(THIS_IS_A_FAKE_STRING);
		when(mockUpdateUser.getUserEmail()).thenReturn(THIS_IS_A_FAKE_EMAIL);

		// Mock session ID
		when(sessionManager.get(anyString())).thenReturn(THIS_IS_A_FAKE_LONG);

		// Invoke method
		Exception exception = assertThrows(ResponseStatusException.class, () -> {
			userController.updateUser(MOCK_SESSION_ID, THIS_IS_A_FAKE_LONG, mockUpdateUser);
		});

		assertTrue(exception.getMessage().contains("The user information already exists for another user"));

	}

	@Test
	public void updateUserOk() throws Exception {
		// Mock user and methods
		User mockUser = mock(User.class);
		when(mockUser.getId()).thenReturn(THIS_IS_ANOTHER_FAKE_LONG);
		when(mockUser.getUserName()).thenReturn(THIS_IS_A_FAKE_STRING);
		when(mockUser.getUserEmail()).thenReturn(THIS_IS_A_FAKE_EMAIL);
		when(userRepo.findById(anyLong())).thenReturn(Optional.of(mockUser));
		when(userRepo.findByUserNameOrUserEmail(THIS_IS_A_FAKE_STRING, THIS_IS_A_FAKE_EMAIL))
				.thenReturn(Collections.emptyList());
		when(userRepo.save(any(User.class))).thenReturn(mockUser);
		User mockUpdateUser = mock(User.class);

		// Mock session ID
		when(sessionManager.get(anyString())).thenReturn(THIS_IS_A_FAKE_LONG);

		// Invoke method
		User updatedUser = userController.updateUser(MOCK_SESSION_ID, THIS_IS_A_FAKE_LONG, mockUpdateUser);

		assertEquals(THIS_IS_ANOTHER_FAKE_LONG, updatedUser.getId());
		assertTrue(THIS_IS_A_FAKE_STRING.equals(updatedUser.getUserName()));
		assertTrue(THIS_IS_A_FAKE_EMAIL.equals(updatedUser.getUserEmail()));
	}

	@Test
	public void deleteUserOk() throws Exception {

		// Mock methods
		when(userRepo.findById(anyLong())).thenReturn(Optional.of(mock(User.class)));
		when(recipeRepo.findByAuthor(any(User.class))).thenReturn(Collections.emptyList());

		// Mock session ID
		when(sessionManager.get(anyString())).thenReturn(THIS_IS_A_FAKE_LONG);

		userController.deleteUser(MOCK_SESSION_ID, THIS_IS_A_FAKE_LONG);
	}
}
