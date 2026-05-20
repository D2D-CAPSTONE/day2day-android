package com.example.day2day.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "course_places",
    foreignKeys =
        @ForeignKey(
            entity = Course.class,
            parentColumns = "courseId",
            childColumns = "courseId",
            onDelete = ForeignKey.CASCADE),
    indices = {@Index("courseId")})
public class CoursePlace {
  @PrimaryKey(autoGenerate = true)
  public int id;

  public String courseId;
  public String placeName;
  public double latitude;
  public double longitude;
  public int orderIndex;

  public CoursePlace() {}

  @Ignore
  public CoursePlace(
      String courseId, String placeName, double latitude, double longitude, int orderIndex) {
    this.courseId = courseId;
    this.placeName = placeName;
    this.latitude = latitude;
    this.longitude = longitude;
    this.orderIndex = orderIndex;
  }
}
