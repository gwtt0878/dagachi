package com.gwtt.dagachi.repository;

import com.gwtt.dagachi.entity.Posting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostingRepository extends JpaRepository<Posting, Long> {}
