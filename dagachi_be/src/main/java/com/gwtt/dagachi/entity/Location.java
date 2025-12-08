package com.gwtt.dagachi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {
  @Column(precision = 8)
  private double latitude;

  @Column(precision = 8)
  private double longitude;

  @Builder
  public Location(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public static Location of(double latitude, double longitude) {
    return Location.builder().latitude(latitude).longitude(longitude).build();
  }
}
