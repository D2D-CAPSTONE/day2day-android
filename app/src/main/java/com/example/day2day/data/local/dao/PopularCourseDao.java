package com.example.day2day.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.day2day.data.local.entity.Course;
import com.example.day2day.data.local.entity.PopularCourse;
import java.util.List;

@Dao
public interface PopularCourseDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertPopularCourses(List<PopularCourse> popularCourses);

  @Query(
      "SELECT c.* FROM courses c "
          + "INNER JOIN popular_courses pc ON c.courseId = pc.courseId "
          + "ORDER BY c.courseId ASC "
          + "LIMIT :limit OFFSET :offset")
  List<Course> getPopularCoursesPaged(int limit, int offset);

  @Query("SELECT COUNT(*) FROM popular_courses")
  int getPopularCourseCount();
}
