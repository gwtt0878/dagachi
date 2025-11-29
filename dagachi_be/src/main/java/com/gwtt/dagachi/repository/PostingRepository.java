package com.gwtt.dagachi.repository;

import com.gwtt.dagachi.entity.Posting;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostingRepository extends JpaRepository<Posting, Long>, PostingRepositoryCustom {
  List<Posting> findByAuthorId(Long authorId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM Posting p WHERE p.id = :id")
  Optional<Posting> findByIdForUpdate(Long id);

  @Query(
      "SELECT p FROM Posting p JOIN p.participations pa WHERE pa.participant.id = :participantId")
  Page<Posting> findJoinedPostingsByParticipantId(Long participantId, Pageable pageable);

  @Query("SELECT p FROM Posting p WHERE p.author.id = :authorId")
  Page<Posting> findAuthoredPostingsByAuthorId(Long authorId, Pageable pageable);
}
