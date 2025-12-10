package com.gwtt.dagachi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "posting_id", nullable = false)
  private Posting posting;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_comment_id", nullable = true)
  private Comment parentComment;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @OneToMany(mappedBy = "parentComment", fetch = FetchType.LAZY, orphanRemoval = false)
  private List<Comment> childComments = new ArrayList<>();

  private LocalDateTime deletedAt;

  @Column(nullable = false)
  private Integer depth = 0;

  @Builder
  public Comment(
      Posting posting, User author, Comment parentComment, String content, Integer depth) {
    this.posting = posting;
    this.author = author;
    this.parentComment = parentComment;
    this.content = content;
    this.depth = depth != null ? depth : 0;
  }

  public void updateContent(String content) {
    this.content = content;
  }

  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }

  public void deletedContent() {
    this.content = "[삭제된 댓글입니다.]";
  }
}
