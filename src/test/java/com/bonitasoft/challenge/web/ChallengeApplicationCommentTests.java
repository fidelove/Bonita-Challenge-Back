package com.bonitasoft.challenge.web;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.bonitasoft.challenge.model.Comment;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest
public class ChallengeApplicationCommentTests {

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

	@Test
	@Order(1)
	public void createCommentOK() throws Exception {
		String uri = "/api/v1/user/4/recipe/2/comment";
		Comment newComment = new Comment();
		newComment.setComment("Comment 22");
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content(objectMapper.writeValueAsString(newComment)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());
		Comment comment = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Comment.class);
		assertEquals(22, comment.getId().longValue());
	}

	@Test
	@Order(2)
	public void createCommentRecipeDoesntExist() throws Exception {
		String uri = "/api/v1/user/4/recipe/20/comment";
		Comment newComment = new Comment();
		newComment.setComment("Comment");
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content(objectMapper.writeValueAsString(newComment)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("The recipe doesn't exist", mvcResult.getResponse().getErrorMessage());
	}

	@Test
	@Order(3)
	public void createCommentUserDoesntExist() throws Exception {

		String uri = "/api/v1/user/10/recipe/2/comment";
		Comment newComment = new Comment();
		newComment.setComment("Comment");
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content(objectMapper.writeValueAsString(newComment)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(400, mvcResult.getResponse().getStatus());
		assertEquals("The user doesn't exist", mvcResult.getResponse().getErrorMessage());
	}
}