package com.gwtt.dagachi.repository;

import com.gwtt.dagachi.dto.PostingSearchCondition;
import com.gwtt.dagachi.entity.Posting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostingRepositoryCustom {
  Page<Posting> searchPostings(PostingSearchCondition condition, Pageable pageable);
}
