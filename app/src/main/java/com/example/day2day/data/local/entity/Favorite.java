package com.example.day2day.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "favorites",
    foreignKeys =
        @ForeignKey(
            entity = Course.class,
            parentColumns = "courseId",
            childColumns = "courseId",
            onDelete = ForeignKey.CASCADE),
    indices = {@Index(value = "courseId", unique = true)})
public class Favorite {
  @PrimaryKey(autoGenerate = true)
  public int favoriteId;

  public String courseId;
  public long savedAt;

  public Favorite() {}

  @Ignore
  public Favorite(String courseId, long savedAt) {
    this.courseId = courseId;
    this.savedAt = savedAt;
  }
}
