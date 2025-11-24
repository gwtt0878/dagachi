package com.gwtt.dagachi.dto;

import lombok.Getter;
import lombok.Builder;

@Getter
@Builder
public class ProjectResponse {
    private Long id;
    private String title;
    private String description;
    private String type; // PROJECT, STUDY
}