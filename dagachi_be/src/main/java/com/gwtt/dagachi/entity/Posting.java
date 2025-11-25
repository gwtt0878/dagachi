package com.gwtt.dagachi.entity;

import com.gwtt.dagachi.constants.PostingStatus;
import com.gwtt.dagachi.constants.PostingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.gwtt.dagachi.dto.PostingUpdateRequestDto;

@Entity
@Table(name = "postings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Posting extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false, length = 20)
  private PostingType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PostingStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @Column(nullable = false)
  private int maxCapacity;

  @Builder
  public Posting(String title, String description, PostingType type, int maxCapacity, User author) {
    this.title = title;
    this.author = author;
    this.description = description;
    this.type = type;
    this.maxCapacity = maxCapacity;
    this.status = PostingStatus.RECRUITING;
  }

  public void update(PostingUpdateRequestDto updateRequestDto) {
    this.title = updateRequestDto.getTitle();
    this.description = updateRequestDto.getDescription();
    this.type = updateRequestDto.getType();
    this.maxCapacity = updateRequestDto.getMaxCapacity();
    this.status = updateRequestDto.getStatus();
  }
}
