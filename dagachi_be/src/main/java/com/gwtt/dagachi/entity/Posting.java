package com.gwtt.dagachi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "postings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Posting {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(length = 500)
  private String description;

  @Column(nullable = false, length = 20)
  private String type; // PROJECT, STUDY

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public Posting(String title, String description, String type) {
    this.title = title;
    this.description = description;
    this.type = type;
  }

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
