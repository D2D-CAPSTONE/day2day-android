package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.day2day.R;
import com.example.day2day.data.CourseContract;
import com.example.day2day.data.local.CourseDatabase;
import com.example.day2day.data.local.CourseSeedData;
import com.example.day2day.data.local.entity.Course;
import com.example.day2day.data.local.entity.Favorite;

public class CourseDetailPageActivity extends AppCompatActivity {
  private CourseDatabase database;
  private String courseId;
  private Course currentCourse;
  private boolean isFavorite;

  private View shareButton;
  private View favoriteButton;
  private TextView favoriteText;
  private TextView titleText;
  private TextView ratingText;
  private TextView placeCountText;
  private TextView tagsText;
  private TextView place1TitleText;
  private TextView place2TitleText;
  private TextView moveInfoText;
  private View place1ThumbView;
  private View place2ThumbView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_course_detail_page);

    // daytoday 상단바 제거
    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }

    database = CourseDatabase.getInstance(this);
    courseId = getIntent().getStringExtra(CourseContract.EXTRA_COURSE_ID);

    bindViews();
    loadCourse();
  }

  // 뒤로가기 버튼, corseMapPage로만 가서 요청 들어온 곳으로 다시 돌아가게 해야됨
  private void bindViews() {
    View goCourseMapButton = findViewById(R.id.btn_course_detail_go_course_map);

    goCourseMapButton.setOnClickListener(v -> finish());

    shareButton = findViewById(R.id.btn_course_detail_share);
    favoriteButton = findViewById(R.id.btn_course_detail_favorite);
    favoriteText = findViewById(R.id.tv_course_detail_favorite);
    titleText = findViewById(R.id.tv_course_detail_title);
    ratingText = findViewById(R.id.tv_course_detail_rating);
    placeCountText = findViewById(R.id.tv_course_detail_place_count);
    tagsText = findViewById(R.id.tv_course_detail_tags);
    place1TitleText = findViewById(R.id.tv_course_detail_place1_title);
    place2TitleText = findViewById(R.id.tv_course_detail_place2_title);
    moveInfoText = findViewById(R.id.tv_course_detail_move_info);
    place1ThumbView = findViewById(R.id.view_course_detail_place1_thumb);
    place2ThumbView = findViewById(R.id.view_course_detail_place2_thumb);

    shareButton.setOnClickListener(v -> shareCourse());
    favoriteButton.setOnClickListener(v -> toggleFavorite());
  }

  // courseId로 코스 정보를 가져오기
  private void loadCourse() {
    if (courseId == null || courseId.isEmpty()) {
      showMissingCourse();
      return;
    }

    CourseDatabase.databaseExecutor.execute(
        () -> {
          if (database.courseDao().getCourseCount() == 0) {
            database.courseDao().insertCourses(CourseSeedData.getPopularCourses());
          }

          Course course = database.courseDao().getCourseById(courseId);
          boolean favorite = database.favoriteDao().isFavorite(courseId) > 0;

          runOnUiThread(
              () -> {
                currentCourse = course;
                isFavorite = favorite;
                if (currentCourse == null) {
                  showMissingCourse();
                  return;
                }
                renderCourse(currentCourse);
                updateFavoriteUi();
                shareButton.setEnabled(true);
              });
        });
  }

  // 가져온 코스 정보를 UI에 렌더링
  private void renderCourse(Course course) {
    titleText.setText(course.title);
    ratingText.setText(course.ratingText);
    tagsText.setText(course.tagsText.replace(",", "  "));
    place1ThumbView.setBackgroundColor(course.thumbColor);
    place2ThumbView.setBackgroundColor(course.thumbColor);

    String[] routes = course.routeText.split(" > ");
    placeCountText.setText("장소 " + routes.length + "곳");

    if (routes.length > 0) {
      place1TitleText.setText(routes[0]);
    }
    if (routes.length > 1) {
      place2TitleText.setText(routes[1]);
    } else {
      place2TitleText.setText("다음 장소");
    }

    if (routes.length > 1) {
      moveInfoText.setText(routes[0] + "에서 " + routes[1] + "로 이동");
    } else {
      moveInfoText.setText("코스 동선을 확인해보세요");
    }
  }

  private void shareCourse() {
    if (currentCourse == null) {
      Toast.makeText(this, "공유할 코스 정보를 찾지 못했어요.", Toast.LENGTH_SHORT).show();
      return;
    }

    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.setType("text/plain");
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentCourse.title);
    shareIntent.putExtra(Intent.EXTRA_TEXT, buildShareText(currentCourse));
    startActivity(Intent.createChooser(shareIntent, "코스 공유"));
  }

  private String buildShareText(Course course) {
    String[] routes = course.routeText != null ? course.routeText.split(" > ") : new String[0];
    String routeLine = TextUtils.join(" → ", routes);
    String tagLine = course.tagsText != null ? course.tagsText.replace(",", " ") : "";

    StringBuilder sb = new StringBuilder();
    sb.append(course.title).append('\n');
    if (!routeLine.isEmpty()) sb.append(routeLine).append('\n');
    if (!tagLine.isEmpty()) sb.append(tagLine).append('\n');
    sb.append(course.ratingText).append(" · 장소 ").append(routes.length).append("곳");
    return sb.toString();
  }

  // 찜하기
  private void toggleFavorite() {
    if (currentCourse == null) {
      Toast.makeText(this, "코스 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
      return;
    }

    CourseDatabase.databaseExecutor.execute(
        () -> {
          if (isFavorite) {
            database.favoriteDao().deleteFavoriteByCourseId(currentCourse.courseId);
          } else {
            database
                .favoriteDao()
                .insertFavorite(new Favorite(currentCourse.courseId, System.currentTimeMillis()));
          }

          boolean nextFavoriteState = !isFavorite;
          runOnUiThread(
              () -> {
                isFavorite = nextFavoriteState;
                updateFavoriteUi();
                Toast.makeText(
                        this, isFavorite ? "찜 목록에 추가했습니다." : "찜 목록에서 삭제했습니다.", Toast.LENGTH_SHORT)
                    .show();
              });
        });
  }

  private void updateFavoriteUi() {
    favoriteText.setText(isFavorite ? "찜 취소" : "찜하기");
    favoriteButton.setEnabled(true);
  }

  private void showMissingCourse() {
    titleText.setText("코스 정보를 찾을 수 없습니다");
    ratingText.setText("-");
    placeCountText.setText("장소 0곳");
    tagsText.setText("");
    place1TitleText.setText("코스 정보 없음");
    place2TitleText.setText("코스 정보 없음");
    moveInfoText.setText("이전 화면에서 전달된 courseId를 확인해주세요.");
    favoriteText.setText("찜하기");
    favoriteButton.setEnabled(false);
    shareButton.setEnabled(false);
  }
}
