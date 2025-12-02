package com.gwtt.dagachi.entity;

import com.gwtt.dagachi.constants.ParticipationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    name = "participations",
    indexes = {
      @Index(
          name = "idx_participations_posting",
          columnList = "posting_id, deleted_at, status DESC")
    })
@SQLDelete(sql = "UPDATE participations SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Participation extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "posting_id", nullable = false)
  private Posting posting;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "participant_id", nullable = false)
  private User participant;

  private LocalDateTime deletedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ParticipationStatus status;

  public boolean isActive() {
    return deletedAt == null;
  }

  @Builder
  public Participation(Posting posting, User participant) {
    this.posting = posting;
    this.participant = participant;
    this.status = ParticipationStatus.PENDING;
  }

  public void setStatus(ParticipationStatus status) {
    this.status = status;
  }
}
