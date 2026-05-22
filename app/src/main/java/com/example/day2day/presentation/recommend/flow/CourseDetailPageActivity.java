package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.day2day.R;
import com.example.day2day.data.CourseContract;
import com.example.day2day.data.local.CourseDatabase;
import com.example.day2day.data.local.entity.Course;
import com.example.day2day.data.local.entity.Favorite;
import java.util.Arrays;
import java.util.List;

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
  private RecyclerView rvPlaces;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_course_detail_page);

    // daytoday 상단바 제거
    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }

    database = CourseDatabase.getInstance(this);

    bindViews();

    CourseDto dto = (CourseDto) getIntent().getSerializableExtra("SELECTED_COURSE");
    if (dto != null) {
      loadCourseFromDto(dto);
    } else {
      courseId = getIntent().getStringExtra(CourseContract.EXTRA_COURSE_ID);
      loadCourse();
    }
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
    rvPlaces = findViewById(R.id.rv_course_detail_places);
    rvPlaces.setLayoutManager(new LinearLayoutManager(this));
    rvPlaces.setNestedScrollingEnabled(false);

    shareButton.setOnClickListener(v -> shareCourse());
    favoriteButton.setOnClickListener(v -> toggleFavorite());
  }

  private void loadCourseFromDto(CourseDto dto) {
    StringBuilder routeBuilder = new StringBuilder();
    for (int i = 0; i < dto.places.size(); i++) {
      if (i > 0) routeBuilder.append(" > ");
      routeBuilder.append(dto.places.get(i).placeName);
    }

    Course course =
        new Course(
            dto.courseName,
            dto.courseName,
            routeBuilder.toString(),
            "",
            "",
            android.graphics.Color.LTGRAY);

    courseId = course.courseId;

    CourseDatabase.databaseExecutor.execute(
        () -> {
          database.courseDao().insertCourse(course);
          boolean favorite = database.favoriteDao().isFavorite(courseId) > 0;
          runOnUiThread(
              () -> {
                currentCourse = course;
                isFavorite = favorite;
                renderCourse(course);
                updateFavoriteUi();
                shareButton.setEnabled(true);
              });
        });
  }

  // courseId로 코스 정보를 가져오기
  private void loadCourse() {
    if (courseId == null || courseId.isEmpty()) {
      showMissingCourse();
      return;
    }

    CourseDatabase.databaseExecutor.execute(
        () -> {
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

    String[] routes = course.routeText.split(" > ");
    placeCountText.setText("장소 " + routes.length + "곳");
    rvPlaces.setAdapter(new PlaceAdapter(Arrays.asList(routes), course.thumbColor));
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
    favoriteText.setText("찜하기");
    favoriteButton.setEnabled(false);
    shareButton.setEnabled(false);
  }

  // 들어오는 코스 리스트 개수에 맞춰 UI 구현
  private static class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {

    private static final int[] BADGE_COLORS = {
      Color.parseColor("#E8506A"),
      Color.parseColor("#6BBDE8"),
      Color.parseColor("#6BC87A"),
      Color.parseColor("#E8A350"),
      Color.parseColor("#9B6BE8"),
    };

    private final List<String> places;
    private final int thumbColor;

    PlaceAdapter(List<String> places, int thumbColor) {
      this.places = places;
      this.thumbColor = thumbColor;
    }

    // 장소 카드 UI가 필요할 때마다 item_place_card.xml로 뷰를 찍어냄
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view =
          LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place_card, parent, false);
      return new ViewHolder(view);
    }

    // 마지막 장소인지 판별 -> UI 처리 (선, 이동 정보 등)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      boolean isLast = position == places.size() - 1;

      holder.tvPlaceNumber.setText(String.valueOf(position + 1));
      holder.cvPlaceNumber.setCardBackgroundColor(BADGE_COLORS[position % BADGE_COLORS.length]);
      holder.tvPlaceTitle.setText(places.get(position));
      holder.viewThumb.setBackgroundColor(thumbColor);
      holder.viewLine.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);
      holder.layoutMoveInfo.setVisibility(isLast ? View.GONE : View.VISIBLE);

      if (!isLast) {
        holder.tvMoveInfo.setText(places.get(position) + "에서 " + places.get(position + 1) + "로 이동");
      }
    }

    @Override
    public int getItemCount() {
      return places.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
      final CardView cvPlaceNumber;
      final TextView tvPlaceNumber;
      final View viewThumb;
      final View viewLine;
      final TextView tvPlaceTitle;
      final LinearLayout layoutMoveInfo;
      final TextView tvMoveInfo;

      ViewHolder(View itemView) {
        super(itemView);
        cvPlaceNumber = itemView.findViewById(R.id.cv_place_number);
        tvPlaceNumber = itemView.findViewById(R.id.tv_place_number);
        viewThumb = itemView.findViewById(R.id.view_place_thumb);
        viewLine = itemView.findViewById(R.id.view_place_line);
        tvPlaceTitle = itemView.findViewById(R.id.tv_place_title);
        layoutMoveInfo = itemView.findViewById(R.id.layout_place_move_info);
        tvMoveInfo = itemView.findViewById(R.id.tv_place_move_info);
      }
    }
  }
}
