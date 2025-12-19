package com.gwtt.dagachi.repository;

import com.gwtt.dagachi.entity.Posting;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostingRepository extends JpaRepository<Posting, Long>, PostingRepositoryCustom {
  @Query("SELECT p FROM Posting p " + "LEFT JOIN FETCH p.author " + "WHERE p.deletedAt IS NULL")
  Page<Posting> findAllFetched(Pageable pageable);

  @Query(
      "SELECT p FROM Posting p "
          + "LEFT JOIN FETCH p.author "
          + "WHERE p.id = :id AND p.deletedAt IS NULL")
  Optional<Posting> findByIdFetched(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT p FROM Posting p LEFT JOIN FETCH p.author "
          + "WHERE p.id = :id AND p.deletedAt IS NULL")
  Optional<Posting> findByIdForUpdate(Long id);

  @Query(
      "SELECT p FROM Posting p JOIN p.participations pa LEFT JOIN FETCH p.author "
          + "WHERE pa.participant.id = :participantId "
          + "AND p.deletedAt IS NULL")
  Page<Posting> findJoinedPostingsByParticipantId(Long participantId, Pageable pageable);

  @Query(
      "SELECT p FROM Posting p LEFT JOIN FETCH p.author "
          + "WHERE p.author.id = :authorId "
          + "AND p.deletedAt IS NULL")
  Page<Posting> findAuthoredPostingsByAuthorId(Long authorId, Pageable pageable);
}
