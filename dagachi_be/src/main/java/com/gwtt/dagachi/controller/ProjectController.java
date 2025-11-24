package com.gwtt.dagachi.controller;

import com.gwtt.dagachi.dto.ProjectResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    @GetMapping
    public List<ProjectResponse> getProjectsAndStudies() {
        return Arrays.asList(
            ProjectResponse.builder().id(1L).title("React 웹 개발 프로젝트").description("React와 Spring Boot를 활용한 풀스택 웹 개발").type("PROJECT").build(),
            ProjectResponse.builder().id(2L).title("알고리즘 스터디").description("매주 알고리즘 문제 풀이 및 리뷰").type("STUDY").build(),
            ProjectResponse.builder().id(3L).title("AI 챗봇 개발").description("API를 활용한 챗봇 서비스 개발").type("PROJECT").build(),
            ProjectResponse.builder().id(4L).title("Java 백엔드 스터디").description("Spring Framework 심화 학습").type("STUDY").build()
        );
    }
}

