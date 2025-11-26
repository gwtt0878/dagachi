package com.gwtt.dagachi.service;

import com.gwtt.dagachi.constants.PostingStatus;
import com.gwtt.dagachi.entity.Participation;
import com.gwtt.dagachi.entity.Posting;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.repository.ParticipationRepository;
import com.gwtt.dagachi.repository.PostingRepository;
import com.gwtt.dagachi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipationService {
  private final PostingRepository postingRepository;
  private final UserRepository userRepository;
  private final ParticipationRepository participationRepository;

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
}
