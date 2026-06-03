package com.example.day2day.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.day2day.data.local.entity.CoursePlace;
import java.util.List;

@Dao
public interface CoursePlaceDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertPlaces(List<CoursePlace> places);

  @Query("SELECT * FROM course_places WHERE courseId = :courseId ORDER BY orderIndex ASC")
  List<CoursePlace> getPlacesByCourseId(String courseId);

  @Query("SELECT COUNT(*) FROM course_places")
  int getPlaceCount();
}
