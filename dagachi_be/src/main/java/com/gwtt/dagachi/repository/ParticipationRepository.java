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

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM Participation p WHERE p.id = :id")
  Optional<Participation> findByIdForUpdate(Long id);

  List<Participation> findByPostingId(Long postingId);

  boolean existsByParticipantAndPosting(User user, Posting posting);

  Optional<Participation> findByParticipantAndPosting(User user, Posting posting);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM Participation p WHERE p.participant = :user AND p.posting = :posting")
  Optional<Participation> findByParticipantAndPostingForUpdate(
      @Param("user") User user, @Param("posting") Posting posting);

  int countByPostingAndStatus(Posting posting, ParticipationStatus status);
}
