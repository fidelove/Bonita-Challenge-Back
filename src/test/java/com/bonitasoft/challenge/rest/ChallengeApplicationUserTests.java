package com.bonitasoft.challenge.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.bonitasoft.challenge.model.RoleType;
import com.bonitasoft.challenge.model.User;
import com.bonitasoft.challenge.model.UserLogged;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest
public class ChallengeApplicationUserTests {

	private MockMvc mvc;
	private ObjectMapper objectMapper;
	private HttpHeaders header;

	@Autowired
	WebApplicationContext webApplicationContext;

	@BeforeAll
	public void setup() throws Exception {
		mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);

		String uri = "/api/v1/login";

		User user = new User();
		user.setUserName("admin");
		user.setUserPassword("password");
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content(objectMapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		UserLogged userLogged = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserLogged.class);
		header = new HttpHeaders();
		header.add("sessionId", userLogged.getSessionId());

	}

	@Test
	@Order(1)
	public void loginUserDoesnExist() throws Exception {

		String uri = "/api/v1/login";

		User user = new User();
		user.setUserName("username");
		user.setUserPassword("password");
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content(objectMapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("The user login or password is wrong", mvcResult.getResponse().getErrorMessage());
	}

	@Test
	@Order(2)
	public void loginWrongPassword() throws Exception {

		String uri = "/api/v1/login";

		User user = new User();
		user.setUserName("admin");
		user.setUserPassword("wrongPassword");
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content(objectMapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("The user login or password is wrong", mvcResult.getResponse().getErrorMessage());
	}

	@Test
	@Order(3)
	public void loginOk() throws Exception {
		String uri = "/api/v1/login";

		User user = new User();
		user.setUserName("admin");
		user.setUserPassword("password");
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content(objectMapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());
		UserLogged userLogged = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserLogged.class);
		assertEquals(1l, userLogged.getId().longValue());
		assertNotNull(userLogged.getSessionId());
	}

	@Test
	@Order(4)
	public void loginLogoutOk() throws Exception {

		String uri = "/api/v1/login";

		User user = new User();
		user.setUserName("admin");
		user.setUserPassword("password");
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content(objectMapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		UserLogged userLogged = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserLogged.class);
		HttpHeaders loginHeader = new HttpHeaders();
		loginHeader.add("sessionId", userLogged.getSessionId());

		uri = "/api/v1/logout";

		mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).headers(loginHeader)
				.content(objectMapper.writeValueAsString(userLogged)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());
	}

	@Test
	@Order(5)
	public void logoutSessionIdDoesntExist() throws Exception {

		String uri = "/api/v1/logout";

		UserLogged userLogged = new UserLogged();
		userLogged.setSessionId("unexisting sessionId");
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).header("sessionId", "")
				.content(objectMapper.writeValueAsString(userLogged)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(401, mvcResult.getResponse().getStatus());
		assertEquals("The user wasn't logged in", mvcResult.getResponse().getErrorMessage());
	}

	@SuppressWarnings("unchecked")
	@Test
	@Order(6)
	public void getAllUsers() throws Exception {

		String uri = "/api/v1/users";

		MvcResult mvcResult = mvc
				.perform(MockMvcRequestBuilders.get(uri).headers(header).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());
		List<User> usersList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), List.class);
		assertEquals(5, usersList.size());
	}

	@Test
	@Order(8)
	public void getUserByIdOk() throws Exception {

		String uri = "/api/v1/user/3";

		MvcResult mvcResult = mvc
				.perform(MockMvcRequestBuilders.get(uri).headers(header).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		User user = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), User.class);
		assertEquals(200, mvcResult.getResponse().getStatus());
		assertEquals(3, user.getId().longValue());
	}

	@Test
	@Order(9)
	public void createUserNameAlreadyExists() throws Exception {

		String uri = "/api/v1/user";

		User newUser = new User();
		newUser.setUserName("chef1");
		newUser.setUserPassword("password");
		newUser.setUserEmail("chef3@bonita.com");
		newUser.setRole(RoleType.CHEF);
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).headers(header)
				.content(objectMapper.writeValueAsString(newUser)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("There is already a user with that username or email. Please change them",
				mvcResult.getResponse().getErrorMessage());
	}

	@Test
	@Order(10)
	public void createUserEmailAlreadyExists() throws Exception {

		String uri = "/api/v1/user";

		User newUser = new User();
		newUser.setUserName("chef3");
		newUser.setUserPassword("password");
		newUser.setUserEmail("chef1@bonita.com");
		newUser.setRole(RoleType.CHEF);
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).headers(header)
				.content(objectMapper.writeValueAsString(newUser)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("There is already a user with that username or email. Please change them",
				mvcResult.getResponse().getErrorMessage());
	}

	@Test
	@Order(11)
	public void createUserOk() throws Exception {

		String uri = "/api/v1/user";

		User newUser = new User();
		newUser.setUserName("chef3");
		newUser.setUserPassword("password");
		newUser.setUserEmail("chef3@bonita.com");
		newUser.setRole(RoleType.CHEF);
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).headers(header)
				.content(objectMapper.writeValueAsString(newUser)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		User user = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), User.class);

		assertEquals(200, mvcResult.getResponse().getStatus());
		assertEquals(6, user.getId().longValue());
	}

	@Test
	@Order(12)
	public void updateUserNameAlreadyExists() throws Exception {

		String uri = "/api/v1/user/5";

		User newUser = new User();
		newUser.setUserName("user1");
		newUser.setUserPassword("password");
		newUser.setUserEmail("user2@bonita.com");
		newUser.setRole(RoleType.USER);
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.put(uri).headers(header)
				.content(objectMapper.writeValueAsString(newUser)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("The user information already exists for another user", mvcResult.getResponse().getErrorMessage());
	}

	@Test
	@Order(13)
	public void updateUserEmailAlreadyExists() throws Exception {
		String uri = "/api/v1/user/5";

		User newUser = new User();
		newUser.setUserName("user2");
		newUser.setUserPassword("password");
		newUser.setUserEmail("user1@bonita.com");
		newUser.setRole(RoleType.USER);
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.put(uri).headers(header)
				.content(objectMapper.writeValueAsString(newUser)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("The user information already exists for another user", mvcResult.getResponse().getErrorMessage());
	}

	@Test
	@Order(14)
	public void updateUserOk() throws Exception {
		String uri = "/api/v1/user/5";

		User newUser = new User();
		newUser.setUserName("chef4");
		newUser.setUserPassword("password");
		newUser.setUserEmail("chef4@bonita.com");
		newUser.setRole(RoleType.CHEF);
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.put(uri).headers(header)
				.content(objectMapper.writeValueAsString(newUser)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		User user = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), User.class);

		assertEquals(200, mvcResult.getResponse().getStatus());
		assertEquals(5, user.getId().longValue());
		assertEquals("chef4", user.getUserName());
		assertEquals("password", user.getUserPassword());
		assertEquals("chef4@bonita.com", user.getUserEmail());
		assertEquals(RoleType.CHEF, user.getRole());
	}

	@Test
	@Order(15)
	public void deleteUserDoesntExist() throws Exception {

		String uri = "/api/v1/user/10";

		MvcResult mvcResult = mvc
				.perform(MockMvcRequestBuilders.delete(uri).headers(header).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		mvcResult = mvc
				.perform(MockMvcRequestBuilders.get(uri).headers(header).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("The user doesn't exist", mvcResult.getResponse().getErrorMessage());

	}

	@Test
	@Order(15)
	public void deleteUserOk() throws Exception {

		// First delete the user
		String uri = "/api/v1/user/6";

		MvcResult mvcResult = mvc
				.perform(MockMvcRequestBuilders.delete(uri).headers(header).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());

		// Then verify the user has been properly deleted
		mvcResult = mvc
				.perform(MockMvcRequestBuilders.get(uri).headers(header).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("The user doesn't exist", mvcResult.getResponse().getErrorMessage());
	}
}