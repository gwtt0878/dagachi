package com.gwtt.dagachi.repository;

import com.gwtt.dagachi.entity.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
  Optional<User> findByIdForUpdate(Long id);

  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByNickname(String nickname);
}
