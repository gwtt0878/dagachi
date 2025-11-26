package com.gwtt.dagachi.service;

import com.gwtt.dagachi.dto.PostingSimpleResponseDto;
import com.gwtt.dagachi.dto.UserResponseDto;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.repository.PostingRepository;
import com.gwtt.dagachi.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PostingRepository postingRepository;

  public UserResponseDto getUserById(Long id) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    List<PostingSimpleResponseDto> authoredPostings =
        postingRepository.findByAuthorId(id).stream().map(PostingSimpleResponseDto::of).toList();
    List<PostingSimpleResponseDto> joinedPostings =
        postingRepository.findJoinedPostingsByParticipantId(id).stream()
            .map(PostingSimpleResponseDto::of)
            .toList();
    return UserResponseDto.of(user, authoredPostings, joinedPostings);
  }
}
