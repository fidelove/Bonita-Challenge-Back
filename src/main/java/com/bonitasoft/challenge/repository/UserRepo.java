package com.bonitasoft.challenge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.bonitasoft.challenge.model.User;

@Repository
public interface UserRepo extends CrudRepository<User, Long> {

	/**
	 * Gets the users identified by the user name if it exists
	 * 
	 * @param userName The user name requested
	 * @return Optional object containing the User information, if it exists
	 */
	Optional<User> findOptionalByUserName(String userName);

	/**
	 * Gets all the users containing the user name or the user email
	 * 
	 * @param userName  The user name requested
	 * @param userEmail The email requested
	 * @return List containing all the matches
	 */
	List<User> findByUserNameOrUserEmail(String userName, String userEmail);

}