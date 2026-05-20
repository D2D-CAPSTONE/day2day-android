package com.example.day2day.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.day2day.data.local.entity.Course;
import com.example.day2day.data.local.entity.Record;
import java.util.List;

@Dao
public interface RecordDao {
  @Insert
  void insertRecord(Record record);

  @Query("SELECT * FROM records ORDER BY visitedAt DESC")
  List<Record> getAllRecords();

  @Query(
      "SELECT courses.* FROM courses "
          + "JOIN records ON courses.courseId = records.courseId "
          + "ORDER BY records.visitedAt DESC")
  List<Course> getRecordedCourses();
}
