package com.example.day2day.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "courses")
public class Course {
  @PrimaryKey @NonNull public String courseId;
  public String title;
  public String routeText;
  public String tagsText;
  public String ratingText;
  public int thumbColor;

  public Course() {
    courseId = "";
  }

  @Ignore
  public Course(
      @NonNull String courseId,
      String title,
      String routeText,
      String tagsText,
      String ratingText,
      int thumbColor) {
    this.courseId = courseId;
    this.title = title;
    this.routeText = routeText;
    this.tagsText = tagsText;
    this.ratingText = ratingText;
    this.thumbColor = thumbColor;
  }
}
