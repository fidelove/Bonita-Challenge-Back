package com.bonitasoft.challenge.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.bonitasoft.challenge.model.Keyword;

@Repository
public interface KeywordRepo extends CrudRepository<Keyword, Long> {

	/**
	 * Gets the optional object containing the keyword, if it exists
	 * 
	 * @param keyword The keyword
	 * @return An Optional object containing the keyword, if it exists
	 */
	Optional<Keyword> findOptionalByKeyword(String keyword);

}
