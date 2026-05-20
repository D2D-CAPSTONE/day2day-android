package com.example.day2day.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.day2day.data.local.entity.Course;
import com.example.day2day.data.local.entity.Favorite;
import java.util.List;

@Dao
public interface FavoriteDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertFavorite(Favorite favorite);

  @Query("DELETE FROM favorites WHERE courseId = :courseId")
  void deleteFavoriteByCourseId(String courseId);

  @Query("SELECT COUNT(*) FROM favorites WHERE courseId = :courseId")
  int isFavorite(String courseId);

  @Query("SELECT courseId FROM favorites ORDER BY savedAt DESC")
  List<String> getFavoriteCourseIds();

  @Query(
      "SELECT courses.* FROM courses "
          + "JOIN favorites ON courses.courseId = favorites.courseId "
          + "ORDER BY favorites.savedAt DESC")
  List<Course> getFavoriteCourses();
}
