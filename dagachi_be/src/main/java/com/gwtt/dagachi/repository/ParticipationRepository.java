package com.gwtt.dagachi.repository;

import com.gwtt.dagachi.entity.Participation;
import com.gwtt.dagachi.entity.Posting;
import com.gwtt.dagachi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
  boolean existsByParticipantAndPosting(User user, Posting posting);
}
