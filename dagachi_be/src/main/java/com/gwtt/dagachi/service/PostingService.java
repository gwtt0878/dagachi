package com.gwtt.dagachi.service;

import com.gwtt.dagachi.constants.Role;
import com.gwtt.dagachi.dto.PostingCreateRequestDto;
import com.gwtt.dagachi.dto.PostingResponseDto;
import com.gwtt.dagachi.dto.PostingSearchCondition;
import com.gwtt.dagachi.dto.PostingSimpleResponseDto;
import com.gwtt.dagachi.dto.PostingUpdateRequestDto;
import com.gwtt.dagachi.entity.Location;
import com.gwtt.dagachi.entity.Posting;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.exception.DagachiException;
import com.gwtt.dagachi.exception.ErrorCode;
import com.gwtt.dagachi.repository.PostingRepository;
import com.gwtt.dagachi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostingService {
  private final PostingRepository postingRepository;
  private final UserRepository userRepository;

  public Page<PostingSimpleResponseDto> getPostings(Pageable pageable) {
    Page<Posting> postings = postingRepository.findAllFetched(pageable);
    return postings.map(PostingSimpleResponseDto::of);
  }

  @Cacheable(value = "posting", key = "#id")
  public PostingResponseDto getPostingById(Long id) {
    Posting posting =
        postingRepository
            .findByIdFetched(id)
            .orElseThrow(() -> new DagachiException(ErrorCode.POSTING_NOT_FOUND));
    return PostingResponseDto.of(posting);
  }

  @Transactional
  @CacheEvict(
      value = {"posting", "postings"},
      allEntries = true)
  public PostingResponseDto createPosting(
      Long authorId, PostingCreateRequestDto postingRequestDto) {
    User user =
        userRepository
            .findById(authorId)
            .orElseThrow(() -> new DagachiException(ErrorCode.USER_NOT_FOUND));

    Posting posting =
        Posting.builder()
            .title(postingRequestDto.getTitle())
            .description(postingRequestDto.getDescription())
            .type(postingRequestDto.getType())
            .maxCapacity(postingRequestDto.getMaxCapacity())
            .author(user)
            .location(
                Location.of(postingRequestDto.getLatitude(), postingRequestDto.getLongitude()))
            .build();

    Posting savedPosting = postingRepository.save(posting);
    Posting fetchedPosting =
        postingRepository
            .findByIdFetched(savedPosting.getId())
            .orElseThrow(() -> new DagachiException(ErrorCode.INTERNAL_SERVER_ERROR));
    return PostingResponseDto.of(fetchedPosting);
  }

  @Transactional
  @Caching(
    evict = {
      @CacheEvict(value = "posting", key = "#id"),
      @CacheEvict(value = "postings", allEntries = true)
    }
  )
  public PostingResponseDto updatePosting(
      Long id, Long currentUserId, PostingUpdateRequestDto postingUpdateRequestDto) {
    Posting posting =
        postingRepository
            .findByIdForUpdate(id)
            .orElseThrow(() -> new DagachiException(ErrorCode.POSTING_NOT_FOUND));
    checkAuthorization(posting, currentUserId);
    posting.update(postingUpdateRequestDto);
    return PostingResponseDto.of(posting);
  }

  @Transactional
  @Caching(
    evict = {
      @CacheEvict(value = "posting", key = "#id"),
      @CacheEvict(value = "postings", allEntries = true)
    }
  )
  public void deletePosting(Long id, Long currentUserId) {
    Posting posting =
        postingRepository
            .findByIdForUpdate(id)
            .orElseThrow(() -> new DagachiException(ErrorCode.POSTING_NOT_FOUND));
    checkAuthorization(posting, currentUserId);
    postingRepository.delete(posting);
  }

  public Page<PostingSimpleResponseDto> searchPostings(
      PostingSearchCondition condition, Pageable pageable) {
    Page<Posting> postings = postingRepository.searchPostings(condition, pageable);
    return postings.map(PostingSimpleResponseDto::of);
  }

  private void checkAuthorization(Posting posting, Long currentUserId) {
    User currentUser =
        userRepository
            .findById(currentUserId)
            .orElseThrow(() -> new DagachiException(ErrorCode.USER_NOT_FOUND));

    if (currentUser.getRole().equals(Role.ADMIN)) {
      return;
    }

    if (!posting.getAuthor().getId().equals(currentUser.getId())) {
      throw new DagachiException(ErrorCode.POSTING_NOT_AUTHORIZED);
    }
  }
}
