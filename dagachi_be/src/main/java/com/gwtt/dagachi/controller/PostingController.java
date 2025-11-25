package com.gwtt.dagachi.controller;

import com.gwtt.dagachi.dto.PostingResponseDto;
import com.gwtt.dagachi.service.PostingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/postings")
@RequiredArgsConstructor
public class PostingController {

  private final PostingService postingService;

  @GetMapping
  public List<PostingResponseDto> getPostings() {
    return postingService.getAllPostings();
  }
}
