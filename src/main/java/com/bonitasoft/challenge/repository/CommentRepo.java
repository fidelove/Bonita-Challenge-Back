package com.bonitasoft.challenge.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.bonitasoft.challenge.model.Comment;

@Repository
public interface CommentRepo extends CrudRepository<Comment, Long> {

}
