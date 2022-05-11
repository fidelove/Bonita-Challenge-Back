package com.bonitasoft.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.bonitasoft.challenge.repository.RecipeRepo;
import com.bonitasoft.challenge.repository.UserRepo;

import javax.transaction.Transactional;

@Component
public class AppCommandRunner implements CommandLineRunner {

	@Autowired
	private RecipeRepo recipesRepo;
	
	@Autowired
	private UserRepo usersRepo;

	@Transactional
	@Override
	public void run(String... args) throws Exception {
		System.out.println("Recipes:");
		recipesRepo.findAll()
				.forEach(o -> System.out.println(o.toString()));

		System.out.println("Users:");
		usersRepo.findAll()
				.forEach(p -> System.out.println(p.toString()));
	}

}
