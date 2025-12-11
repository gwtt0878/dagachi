package com.gwtt.dagachi.repository;

import com.gwtt.dagachi.entity.Comment;
import com.gwtt.dagachi.entity.Posting;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
  @Query(
      "SELECT c FROM Comment c "
          + "LEFT JOIN FETCH c.author "
          + "LEFT JOIN FETCH c.parentComment "
          + "WHERE c.posting = :posting AND c.deletedAt IS NULL "
          + "ORDER BY c.createdAt ASC")
  List<Comment> findByPostingFetched(@Param("posting") Posting posting);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT c FROM Comment c LEFT JOIN FETCH c.author "
          + "WHERE c.id = :id AND c.deletedAt IS NULL")
  Optional<Comment> findByIdForUpdate(@Param("id") Long id);

  @Query(
      "SELECT c FROM Comment c "
          + "LEFT JOIN FETCH c.author "
          + "LEFT JOIN FETCH c.parentComment "
          + "WHERE c.id = :id "
          + "AND c.deletedAt IS NULL")
  Optional<Comment> findByIdFetched(@Param("id") Long id);
}
