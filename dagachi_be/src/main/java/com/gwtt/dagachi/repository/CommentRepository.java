package com.gwtt.dagachi.repository;

import com.gwtt.dagachi.entity.Comment;
import com.gwtt.dagachi.entity.Posting;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
  @Query("SELECT c FROM Comment c WHERE c.posting = :posting order by c.createdAt desc")
  List<Comment> findByPosting(@Param("posting") Posting posting);
}
