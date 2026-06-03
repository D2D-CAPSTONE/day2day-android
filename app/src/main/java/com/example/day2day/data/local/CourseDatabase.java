package com.example.day2day.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.day2day.data.local.dao.CourseDao;
import com.example.day2day.data.local.dao.CoursePlaceDao;
import com.example.day2day.data.local.dao.FavoriteDao;
import com.example.day2day.data.local.dao.PopularCourseDao;
import com.example.day2day.data.local.dao.RecordDao;
import com.example.day2day.data.local.entity.Course;
import com.example.day2day.data.local.entity.CoursePlace;
import com.example.day2day.data.local.entity.Favorite;
import com.example.day2day.data.local.entity.PopularCourse;
import com.example.day2day.data.local.entity.Record;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {Course.class, CoursePlace.class, PopularCourse.class, Favorite.class, Record.class},
    version = 4,
    exportSchema = false)
public abstract class CourseDatabase extends RoomDatabase {
  private static volatile CourseDatabase instance;
  public static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(4);

  public abstract CourseDao courseDao();

  public abstract CoursePlaceDao coursePlaceDao();

  public abstract PopularCourseDao popularCourseDao();

  public abstract FavoriteDao favoriteDao();

  public abstract RecordDao recordDao();

  public static CourseDatabase getInstance(Context context) {
    if (instance == null) {
      synchronized (CourseDatabase.class) {
        if (instance == null) {
          instance =
              Room.databaseBuilder(
                      context.getApplicationContext(), CourseDatabase.class, "course_database")
                  .fallbackToDestructiveMigration()
                  .build();
        }
      }
    }
    return instance;
  }
}
