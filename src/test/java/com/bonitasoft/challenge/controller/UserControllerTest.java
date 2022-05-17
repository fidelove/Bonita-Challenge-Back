package com.bonitasoft.challenge.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.bonitasoft.challenge.repository.KeywordRepo;
import com.bonitasoft.challenge.repository.RecipeRepo;
import com.bonitasoft.challenge.repository.UserRepo;

@SpringBootTest
public class UserControllerTest {

	@Autowired
	UserController userController;

	@MockBean
	RecipeRepo recipeRepo;

	@MockBean
	KeywordRepo keywordRepo;

	@MockBean
	UserRepo userRepo;

}
