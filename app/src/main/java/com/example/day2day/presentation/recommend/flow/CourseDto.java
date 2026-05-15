package com.example.day2day.presentation.recommend.flow;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class CourseDto implements Serializable {
  public String courseName;
  public List<PlaceDto> places;
  public List<String> tags;
  public String badgeText;

  public CourseDto(String courseName, List<PlaceDto> places) {
    this(courseName, places, Collections.emptyList(), "");
  }

  public CourseDto(String courseName, List<PlaceDto> places, List<String> tags, String badgeText) {
    this.courseName = courseName;
    this.places = places;
    this.tags = tags;
    this.badgeText = badgeText;
  }
}
