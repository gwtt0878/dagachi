package com.gwtt.dagachi.dto;

import lombok.Getter;
import lombok.Builder;
import java.time.LocalDateTime;
import com.gwtt.dagachi.entity.Posting;

@Getter
@Builder
public class PostingResponse {
    private Long id;
    private String title;
    private String description;
    private String type; // PROJECT, STUDY
    private LocalDateTime createdAt;

    public static PostingResponse of(Posting posting) {
        return PostingResponse.builder()
                .id(posting.getId())
                .title(posting.getTitle())
                .description(posting.getDescription())
                .type(posting.getType())
                .createdAt(posting.getCreatedAt())
                .build();
    }
}