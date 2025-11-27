package com.gwtt.dagachi.service;

import com.gwtt.dagachi.constants.ParticipationStatus;
import com.gwtt.dagachi.constants.PostingStatus;
import com.gwtt.dagachi.dto.ParticipationResponseDto;
import com.gwtt.dagachi.entity.Participation;
import com.gwtt.dagachi.entity.Posting;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.repository.ParticipationRepository;
import com.gwtt.dagachi.repository.PostingRepository;
import com.gwtt.dagachi.repository.UserRepository;
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
  public boolean isParticipating(Long userId, Long postingId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));
    Posting posting =
        postingRepository
            .findById(postingId)
            .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다."));

    return participationRepository.existsByParticipantAndPosting(user, posting);
  }

  @Transactional(readOnly = true)
  public ParticipationResponseDto getMyParticipation(Long userId, Long postingId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));
    Posting posting =
        postingRepository
            .findById(postingId)
            .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다."));

    Participation participation =
        participationRepository
            .findByParticipantAndPosting(user, posting)
            .orElseThrow(() -> new RuntimeException("참가 정보를 찾을 수 없습니다."));

    return ParticipationResponseDto.of(participation);
  }

  @Transactional(readOnly = true)
  public List<ParticipationResponseDto> getParticipationsByPostingId(Long userId, Long postingId) {
    Posting posting =
        postingRepository
            .findById(postingId)
            .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다."));

    if (!posting.getAuthor().getId().equals(userId)) {
      throw new RuntimeException("해당 게시글의 작성자가 아닙니다.");
    }

    List<Participation> participations = participationRepository.findByPostingId(postingId);
    return participations.stream().map(ParticipationResponseDto::of).toList();
  }

  @Transactional
  public void joinPosting(Long userId, Long postingId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));
    Posting posting =
        postingRepository
            .findByIdForUpdate(postingId)
            .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다."));

    if (posting.getParticipations().size() >= posting.getMaxCapacity()) {
      throw new RuntimeException("해당 게시글의 최대 참여 인원을 초과했습니다.");
    }

    if (posting.getAuthor().getId().equals(userId)) {
      throw new RuntimeException("본인이 참여할 수 없습니다.");
    }

    if (participationRepository.existsByParticipantAndPosting(user, posting)) {
      throw new RuntimeException("이미 해당 게시글에 참여했습니다.");
    }

    if (posting.getStatus().equals(PostingStatus.COMPLETED)) {
      throw new RuntimeException("해당 게시글의 참여가 종료되었습니다.");
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
            .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));
    Posting posting =
        postingRepository
            .findById(postingId)
            .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다."));
    Participation participation =
        participationRepository
            .findByParticipantAndPosting(user, posting)
            .orElseThrow(() -> new RuntimeException("참가 정보를 찾을 수 없습니다."));

    if (participation.getStatus().equals(ParticipationStatus.APPROVED)) {
      throw new RuntimeException("해당 참여 정보는 이미 승인되었습니다.");
    }
    if (participation.getStatus().equals(ParticipationStatus.REJECTED)) {
      throw new RuntimeException("해당 참여 정보는 이미 거절되었습니다.");
    }

    participationRepository.delete(participation);
  }

  @Transactional
  public void approveUser(Long authorId, Long participationId) {
    User author =
        userRepository
            .findById(authorId)
            .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));
    Participation participation =
        participationRepository
            .findByIdForUpdate(participationId)
            .orElseThrow(() -> new RuntimeException("해당 참여 정보를 찾을 수 없습니다."));

    Posting posting = participation.getPosting();

    if (!posting.getAuthor().getId().equals(author.getId())) {
      throw new RuntimeException("해당 게시글의 작성자가 아닙니다.");
    }

    if (posting.getStatus().equals(PostingStatus.COMPLETED)
        || posting.getStatus().equals(PostingStatus.IN_PROGRESS)) {
      throw new RuntimeException("해당 게시글의 참여가 종료되었습니다.");
    }

    if (participation.getStatus().equals(ParticipationStatus.APPROVED)) {
      throw new RuntimeException("해당 참여 정보는 이미 승인되었습니다.");
    }

    int currentApprovedUsers =
        participationRepository.countByPostingAndStatus(posting, ParticipationStatus.APPROVED);

    if (currentApprovedUsers >= posting.getMaxCapacity()) {
      throw new RuntimeException("해당 게시글의 최대 참여 인원을 초과했습니다.");
    }

    participation.setStatus(ParticipationStatus.APPROVED);
    participationRepository.save(participation);

    if (currentApprovedUsers == posting.getMaxCapacity()) {
      posting.setStatus(PostingStatus.COMPLETED);
      postingRepository.save(posting);
    }
  }

  @Transactional
  public void rejectUser(Long authorId, Long participationId) {
    User author =
        userRepository
            .findById(authorId)
            .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));
    Participation participation =
        participationRepository
            .findByIdForUpdate(participationId)
            .orElseThrow(() -> new RuntimeException("해당 참여 정보를 찾을 수 없습니다."));

    Posting posting = participation.getPosting();

    if (!posting.getAuthor().getId().equals(author.getId())) {
      throw new RuntimeException("해당 게시글의 작성자가 아닙니다.");
    }

    participation.setStatus(ParticipationStatus.REJECTED);
    participationRepository.save(participation);
  }
}
