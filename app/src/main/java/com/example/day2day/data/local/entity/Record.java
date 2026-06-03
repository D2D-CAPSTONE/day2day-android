package com.example.day2day.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "records",
    foreignKeys =
        @ForeignKey(
            entity = Course.class,
            parentColumns = "courseId",
            childColumns = "courseId",
            onDelete = ForeignKey.CASCADE),
    indices = {@Index("courseId")})
public class Record {
  @PrimaryKey(autoGenerate = true)
  public int recordId;

  public String courseId;
  public long visitedAt;
  public String memo;

  public Record() {}

  @Ignore
  public Record(String courseId, long visitedAt, String memo) {
    this.courseId = courseId;
    this.visitedAt = visitedAt;
    this.memo = memo;
  }
}
