package com.example.day2day.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.day2day.data.local.entity.Course;
import java.util.List;

@Dao
public interface CourseDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertCourse(Course course);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertCourses(List<Course> courses);

  @Query("SELECT * FROM courses WHERE courseId = :courseId LIMIT 1")
  Course getCourseById(String courseId);

  @Query("SELECT * FROM courses ORDER BY courseId ASC")
  List<Course> getAllCourses();

  @Query("SELECT COUNT(*) FROM courses")
  int getCourseCount();
}
