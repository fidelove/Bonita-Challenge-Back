package com.bonitasoft.challenge.controller;

import java.time.LocalDateTime;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bonitasoft.challenge.model.Comment;
import com.bonitasoft.challenge.model.Recipe;
import com.bonitasoft.challenge.model.RoleType;
import com.bonitasoft.challenge.model.User;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class CommentController extends AbstractController {

	Logger logger = LogManager.getLogger(CommentController.class);

	/**
	 * Create a new comment to a recipe by a user
	 * 
	 * @param sessionId The session ID
	 * @param recipeId  ID of the recipe that will receive the comment
	 * @param comment   The comment to be created
	 * @return An object containing all the information regarding the comment
	 */
	@PostMapping("/recipe/{recipeId}/comment")
	public Comment createComment(@RequestHeader(name = "sessionid") String sessionId,
			@NotNull @PathVariable("recipeId") Long recipeId, @Valid @RequestBody Comment comment) {

		// Check if the session id is valid
		Long userId = checkSessionId(sessionId);

		logger.info(String.format(
				"Creating a new comment by user with ID %d, in recipe with ID %d with the next comment: %s", userId,
				recipeId, comment.getComment()));

		// Check if the user and the recipe exist
		User userCreatingComment = checkUser(userId, RoleType.USER);
		Recipe recipeToBeCommented = checkRecipe(recipeId);

		// Set information to the comment, and save the comment
		comment.setAuthor(userCreatingComment);
		comment.setRecipe(recipeToBeCommented);
		comment.setCreated(LocalDateTime.now());

		logger.info("The new comment will be finally created");
		return commentRepo.save(comment);
	}
}