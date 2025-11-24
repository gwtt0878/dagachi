package com.gwtt.dagachi.service;

import com.gwtt.dagachi.dto.PostingResponse;
import com.gwtt.dagachi.repository.PostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostingService {
    
    private final PostingRepository postingRepository;
    
    public List<PostingResponse> getAllPostings() {
        return postingRepository.findAll().stream()
                .map(PostingResponse::of)
                .toList();
    }
}
