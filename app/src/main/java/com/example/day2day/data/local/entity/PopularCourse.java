package com.example.day2day.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "popular_courses",
    foreignKeys =
        @ForeignKey(
            entity = Course.class,
            parentColumns = "courseId",
            childColumns = "courseId",
            onDelete = ForeignKey.CASCADE),
    indices = {@Index("courseId")})
public class PopularCourse {
  @PrimaryKey @NonNull public String courseId;

  public PopularCourse() {
    courseId = "";
  }

  @Ignore
  public PopularCourse(@NonNull String courseId) {
    this.courseId = courseId;
  }
}
