package com.bonitasoft.challenge.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.bonitasoft.challenge.model.Keyword;

@Repository
public interface KeywordRepo extends CrudRepository<Keyword, Long> {

}
