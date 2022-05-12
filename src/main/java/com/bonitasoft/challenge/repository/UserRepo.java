package com.bonitasoft.challenge.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.bonitasoft.challenge.model.User;

@Repository
public interface UserRepo extends CrudRepository<User, Long> {

	List<User> findByUserNameOrUserEmail(String userName, String userEmail);

}