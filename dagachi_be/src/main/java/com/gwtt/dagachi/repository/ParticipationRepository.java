package com.gwtt.dagachi.repository;

import com.gwtt.dagachi.constants.ParticipationStatus;
import com.gwtt.dagachi.entity.Participation;
import com.gwtt.dagachi.entity.Posting;
import com.gwtt.dagachi.entity.User;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
  @Query(
      "SELECT p FROM Participation p "
          + "LEFT JOIN FETCH p.posting "
          + "LEFT JOIN FETCH p.participant "
          + "WHERE p.posting = :posting AND p.deletedAt IS NULL")
  List<Participation> findByPostingFetched(@Param("posting") Posting posting);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT p FROM Participation p JOIN FETCH p.posting WHERE p.id = :id AND p.deletedAt IS NULL")
  Optional<Participation> findByIdWithPostingForUpdate(@Param("id") Long id);

  List<Participation> findByPostingId(Long postingId);

  boolean existsByParticipantAndPosting(User user, Posting posting);

  @Query(
      "SELECT p FROM Participation p "
          + "LEFT JOIN FETCH p.posting "
          + "LEFT JOIN FETCH p.participant "
          + "WHERE p.participant = :user AND p.posting = :posting AND p.deletedAt IS NULL")
  Optional<Participation> findByParticipantAndPosting(
      @Param("user") User user, @Param("posting") Posting posting);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT p FROM Participation p "
          + "LEFT JOIN FETCH p.posting "
          + "LEFT JOIN FETCH p.participant "
          + "WHERE p.participant = :user AND p.posting = :posting AND p.deletedAt IS NULL")
  Optional<Participation> findByParticipantAndPostingForUpdate(
      @Param("user") User user, @Param("posting") Posting posting);

  int countByPostingAndStatus(Posting posting, ParticipationStatus status);
}
