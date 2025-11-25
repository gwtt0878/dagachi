package com.gwtt.dagachi.service;

import com.gwtt.dagachi.dto.PostingResponseDto;
import com.gwtt.dagachi.repository.PostingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostingService {

  private final PostingRepository postingRepository;

  public List<PostingResponseDto> getAllPostings() {
    return postingRepository.findAll().stream().map(PostingResponseDto::of).toList();
  }
}
