package com.gwtt.dagachi.controller;

import com.gwtt.dagachi.dto.PostingResponse;
import com.gwtt.dagachi.service.PostingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/postings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PostingController {

  private final PostingService postingService;

  @GetMapping
  public List<PostingResponse> getPostings() {
    return postingService.getAllPostings();
  }
}
