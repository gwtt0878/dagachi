package com.gwtt.dagachi.dto;

import com.gwtt.dagachi.constants.PostingStatus;
import com.gwtt.dagachi.constants.PostingType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostingSearchCondition {
  private String title;
  private PostingType type;
  private PostingStatus status;
  private String authorNickname;

  private Double userLatitude;
  private Double userLongitude;
  private boolean sortByDistance;
}
