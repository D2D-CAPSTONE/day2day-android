package com.example.day2day.presentation.recommend.flow;

import java.util.List;

public class CourseDto {
  public String courseName;
  public List<PlaceDto> places;

  public CourseDto(String courseName, List<PlaceDto> places) {
    this.courseName = courseName;
    this.places = places;
  }
}
