package com.gwtt.dagachi.service;

import com.gwtt.dagachi.constants.Role;
import com.gwtt.dagachi.dto.PostingCreateRequestDto;
import com.gwtt.dagachi.dto.PostingResponseDto;
import com.gwtt.dagachi.dto.PostingSimpleResponseDto;
import com.gwtt.dagachi.dto.PostingUpdateRequestDto;
import com.gwtt.dagachi.entity.Posting;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.repository.PostingRepository;
import com.gwtt.dagachi.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostingService {
  private final PostingRepository postingRepository;
  private final UserRepository userRepository;

  public List<PostingSimpleResponseDto> getAllPostings() {
    return postingRepository.findAll().stream().map(PostingSimpleResponseDto::of).toList();
  }

  public PostingResponseDto getPostingById(Long id) {
    Posting posting =
        postingRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다."));
    return PostingResponseDto.of(posting);
  }

  @Transactional
  public PostingResponseDto createPosting(
      Long authorId, PostingCreateRequestDto postingRequestDto) {
    User user =
        userRepository
            .findById(authorId)
            .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

    Posting posting =
        Posting.builder()
            .title(postingRequestDto.getTitle())
            .description(postingRequestDto.getDescription())
            .type(postingRequestDto.getType())
            .maxCapacity(postingRequestDto.getMaxCapacity())
            .author(user)
            .build();

    Posting savedPosting = postingRepository.save(posting);

    return PostingResponseDto.of(savedPosting);
  }

  @Transactional
  public PostingResponseDto updatePosting(
      Long id, Long currentUserId, PostingUpdateRequestDto postingUpdateRequestDto) {
    Posting posting =
        postingRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다."));
    checkAuthorization(posting, currentUserId);
    posting.update(postingUpdateRequestDto);
    Posting updatedPosting = postingRepository.save(posting);
    return PostingResponseDto.of(updatedPosting);
  }

  @Transactional
  public void deletePosting(Long id, Long currentUserId) {
    Posting posting =
        postingRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("해당 게시글을 찾을 수 없습니다."));
    checkAuthorization(posting, currentUserId);
    postingRepository.delete(posting);
  }

  private void checkAuthorization(Posting posting, Long currentUserId) {
    User currentUser =
        userRepository
            .findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

    if (currentUser.getRole().equals(Role.ADMIN)) {
      return;
    }

    if (!posting.getAuthor().getId().equals(currentUser.getId())) {
      throw new RuntimeException("해당 게시글의 수정/삭제 권한이 없습니다.");
    }
  }
}
