package com.gwtt.dagachi.service;

import com.gwtt.dagachi.constants.ParticipationStatus;
import com.gwtt.dagachi.constants.PostingStatus;
import com.gwtt.dagachi.dto.ParticipationResponseDto;
import com.gwtt.dagachi.dto.ParticipationSimpleResponseDto;
import com.gwtt.dagachi.entity.Participation;
import com.gwtt.dagachi.entity.Posting;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.exception.DagachiException;
import com.gwtt.dagachi.exception.ErrorCode;
import com.gwtt.dagachi.repository.ParticipationRepository;
import com.gwtt.dagachi.repository.PostingRepository;
import com.gwtt.dagachi.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipationService {
  private final PostingRepository postingRepository;
  private final UserRepository userRepository;
  private final ParticipationRepository participationRepository;

  @Transactional(readOnly = true)
  public ParticipationSimpleResponseDto getSimpleParticipation(Long userId, Long postingId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new DagachiException(ErrorCode.USER_NOT_FOUND));
    Posting posting =
        postingRepository
            .findById(postingId)
            .orElseThrow(() -> new DagachiException(ErrorCode.POSTING_NOT_FOUND));

    Participation participation =
        participationRepository.findByParticipantAndPosting(user, posting).orElse(null);

    // 참가하지 않은 경우 -1 반환
    if (participation == null) {
      return ParticipationSimpleResponseDto.builder()
          .participationId(-1L)
          .createdAt(LocalDateTime.now())
          .status(ParticipationStatus.PENDING)
          .build();
    }
    return ParticipationSimpleResponseDto.of(participation);
  }

  @Transactional(readOnly = true)
  public ParticipationResponseDto getMyParticipation(Long userId, Long postingId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new DagachiException(ErrorCode.USER_NOT_FOUND));
    Posting posting =
        postingRepository
            .findById(postingId)
            .orElseThrow(() -> new DagachiException(ErrorCode.POSTING_NOT_FOUND));

    Participation participation =
        participationRepository
            .findByParticipantAndPosting(user, posting)
            .orElseThrow(() -> new DagachiException(ErrorCode.PARTICIPATION_NOT_FOUND));

    return ParticipationResponseDto.of(participation);
  }

  @Transactional(readOnly = true)
  public List<ParticipationResponseDto> getParticipationsByPostingId(Long userId, Long postingId) {
    Posting posting =
        postingRepository
            .findByIdFetched(postingId)
            .orElseThrow(() -> new DagachiException(ErrorCode.POSTING_NOT_FOUND));

    if (!posting.getAuthor().getId().equals(userId)) {
      throw new DagachiException(ErrorCode.POSTING_NOT_AUTHORIZED);
    }

    List<Participation> participations = participationRepository.findByPostingFetched(posting);
    return participations.stream().map(ParticipationResponseDto::of).toList();
  }

  @Transactional
  public void joinPosting(Long userId, Long postingId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new DagachiException(ErrorCode.USER_NOT_FOUND));
    Posting posting =
        postingRepository
            .findByIdForUpdate(postingId)
            .orElseThrow(() -> new DagachiException(ErrorCode.POSTING_NOT_FOUND));

    if (posting.getAuthor().getId().equals(userId)) {
      throw new DagachiException(ErrorCode.USER_NOT_AUTHORIZED);
    }

    if (participationRepository.existsByParticipantAndPosting(user, posting)) {
      throw new DagachiException(ErrorCode.PARTICIPATION_ALREADY_JOINED);
    }

    if (posting.getStatus().equals(PostingStatus.COMPLETED)
        || posting.getStatus().equals(PostingStatus.RECRUITED)) {
      throw new DagachiException(ErrorCode.POSTING_ALREADY_RECRUITED);
    }

    Participation participation =
        Participation.builder().posting(posting).participant(user).build();

    participationRepository.save(participation);
  }

  @Transactional
  public void leavePosting(Long userId, Long postingId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new DagachiException(ErrorCode.USER_NOT_FOUND));
    Posting posting =
        postingRepository
            .findById(postingId)
            .orElseThrow(() -> new DagachiException(ErrorCode.POSTING_NOT_FOUND));
    Participation participation =
        participationRepository
            .findByParticipantAndPostingForUpdate(user, posting)
            .orElseThrow(() -> new DagachiException(ErrorCode.PARTICIPATION_NOT_FOUND));

    if (participation.getStatus().equals(ParticipationStatus.APPROVED)) {
      throw new DagachiException(ErrorCode.PARTICIPATION_ALREADY_APPROVED);
    }
    if (participation.getStatus().equals(ParticipationStatus.REJECTED)) {
      throw new DagachiException(ErrorCode.PARTICIPATION_ALREADY_REJECTED);
    }
    participationRepository.delete(participation);
  }

  @Transactional
  public void approveUser(Long authorId, Long participationId) {
    User author =
        userRepository
            .findById(authorId)
            .orElseThrow(() -> new DagachiException(ErrorCode.USER_NOT_FOUND));
    Participation participation =
        participationRepository
            .findByIdWithPostingForUpdate(participationId)
            .orElseThrow(() -> new DagachiException(ErrorCode.PARTICIPATION_NOT_FOUND));

    Posting posting =
        postingRepository
            .findByIdForUpdate(participation.getPosting().getId())
            .orElseThrow(() -> new DagachiException(ErrorCode.POSTING_NOT_FOUND));

    if (!posting.getAuthor().getId().equals(author.getId())) {
      throw new DagachiException(ErrorCode.POSTING_NOT_AUTHORIZED);
    }

    if (posting.getStatus().equals(PostingStatus.COMPLETED)
        || posting.getStatus().equals(PostingStatus.RECRUITED)) {
      throw new DagachiException(ErrorCode.POSTING_ALREADY_RECRUITED);
    }

    if (participation.getStatus().equals(ParticipationStatus.APPROVED)) {
      throw new DagachiException(ErrorCode.PARTICIPATION_ALREADY_APPROVED);
    }

    int currentApprovedUsers =
        participationRepository.countByPostingAndStatus(posting, ParticipationStatus.APPROVED);

    if (currentApprovedUsers >= posting.getMaxCapacity()) {
      throw new DagachiException(ErrorCode.PARTICIPATION_MAX_CAPACITY_EXCEEDED);
    }

    participation.setStatus(ParticipationStatus.APPROVED);

    if (currentApprovedUsers + 1 == posting.getMaxCapacity()) {
      posting.setStatus(PostingStatus.RECRUITED);
    }
  }

  @Transactional
  public void rejectUser(Long authorId, Long participationId) {
    User author =
        userRepository
            .findById(authorId)
            .orElseThrow(() -> new DagachiException(ErrorCode.USER_NOT_FOUND));
    Participation participation =
        participationRepository
            .findByIdWithPostingForUpdate(participationId)
            .orElseThrow(() -> new DagachiException(ErrorCode.PARTICIPATION_NOT_FOUND));

    Posting posting =
        postingRepository
            .findByIdForUpdate(participation.getPosting().getId())
            .orElseThrow(() -> new DagachiException(ErrorCode.POSTING_NOT_FOUND));

    if (!posting.getAuthor().getId().equals(author.getId())) {
      throw new DagachiException(ErrorCode.POSTING_NOT_AUTHORIZED);
    }

    participation.setStatus(ParticipationStatus.REJECTED);

    if (posting.getStatus().equals(PostingStatus.RECRUITED)) {
      posting.setStatus(PostingStatus.RECRUITING);
    }
  }
}
