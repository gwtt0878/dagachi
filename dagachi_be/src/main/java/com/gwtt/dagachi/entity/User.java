package com.gwtt.dagachi.entity;

import com.gwtt.dagachi.constants.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "users",
    indexes = {@Index(name = "idx_users_nickname", columnList = "nickname")})
@SQLDelete(
    sql =
        """
  UPDATE users
  SET deleted_at = NOW(),
      username = CONCAT(username, '_deleted_', id, '_', NOW()),
      nickname = CONCAT(nickname, '_deleted_', id, '_', NOW())
  WHERE id = ?""")
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(nullable = false, length = 100)
  private String password;

  @Column(nullable = false, length = 50, unique = true)
  private String nickname;

  private LocalDateTime deletedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Builder
  public User(String username, String password, String nickname, Role role) {
    this.username = username;
    this.password = password;
    this.nickname = nickname;
    this.role = role;
  }
}
