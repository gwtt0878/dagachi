package com.gwtt.dagachi.service;

import com.gwtt.dagachi.dto.PostingSimpleResponseDto;
import com.gwtt.dagachi.dto.UserResponseDto;
import com.gwtt.dagachi.entity.Posting;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.exception.DagachiException;
import com.gwtt.dagachi.exception.ErrorCode;
import com.gwtt.dagachi.repository.PostingRepository;
import com.gwtt.dagachi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PostingRepository postingRepository;

  public UserResponseDto getUserById(Long id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new DagachiException(ErrorCode.USER_NOT_FOUND));
    return UserResponseDto.of(user);
  }

  public Page<PostingSimpleResponseDto> getJoinedPostingsByUserId(Long userId, Pageable pageable) {
    Page<Posting> postings = postingRepository.findJoinedPostingsByParticipantId(userId, pageable);
    return postings.map(PostingSimpleResponseDto::of);
  }

  public Page<PostingSimpleResponseDto> getAuthoredPostingsByUserId(
      Long userId, Pageable pageable) {
    Page<Posting> postings = postingRepository.findAuthoredPostingsByAuthorId(userId, pageable);
    return postings.map(PostingSimpleResponseDto::of);
  }
}
