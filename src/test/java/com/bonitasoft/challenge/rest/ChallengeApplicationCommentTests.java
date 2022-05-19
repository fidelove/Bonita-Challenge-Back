package com.bonitasoft.challenge.rest;

import static org.junit.Assert.assertEquals;

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

import com.bonitasoft.challenge.model.Comment;
import com.bonitasoft.challenge.model.User;
import com.bonitasoft.challenge.model.UserLogged;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest
public class ChallengeApplicationCommentTests {

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
		user.setUserName("user1");
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
	public void createCommentOK() throws Exception {
		String uri = "/api/v1/recipe/2/comment";
		Comment newComment = new Comment();
		newComment.setComment("Comment 22");
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).headers(header)
				.content(objectMapper.writeValueAsString(newComment)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());
		Comment comment = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Comment.class);
		assertEquals(22, comment.getId().longValue());
	}

	@Test
	@Order(2)
	public void createCommentRecipeDoesntExist() throws Exception {
		String uri = "/api/v1/recipe/20/comment";
		Comment newComment = new Comment();
		newComment.setComment("Comment");
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).headers(header)
				.content(objectMapper.writeValueAsString(newComment)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("The recipe doesn't exist", mvcResult.getResponse().getErrorMessage());
	}

	@Test
	@Order(3)
	public void createCommentDifferentSessionId() throws Exception {

		String uri = "/api/v1/recipe/2/comment";
		Comment newComment = new Comment();
		newComment.setComment("Comment");
		header.clear();
		header.add("sessionId", "");
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).headers(header)
				.content(objectMapper.writeValueAsString(newComment)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(401, mvcResult.getResponse().getStatus());
		assertEquals("The user wasn't logged in", mvcResult.getResponse().getErrorMessage());
	}
}