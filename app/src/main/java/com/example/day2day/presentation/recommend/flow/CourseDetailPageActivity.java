package com.example.day2day.presentation.recommend.flow;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import com.example.day2day.data.local.entity.CoursePlace;
import com.example.day2day.data.local.entity.Favorite;
import com.example.day2day.data.local.entity.Record;
import java.util.ArrayList;
import java.util.List;

public class CourseDetailPageActivity extends AppCompatActivity {
  private CourseDatabase database;
  private String courseId;
  private Course currentCourse;
  private boolean isFavorite;
  private List<PlaceItem> currentPlaceItems = new ArrayList<>();

  private View confirmButton;
  private TextView confirmText;
  private boolean isConfirmed;
  private View shareButton;
  private View favoriteButton;
  private TextView favoriteText;
  private TextView titleText;
  private TextView timeText;
  private TextView distanceText;
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

    courseId = getIntent().getStringExtra(CourseContract.EXTRA_COURSE_ID);
    loadCourse();
  }

  // 뒤로가기 버튼, corseMapPage로만 가서 요청 들어온 곳으로 다시 돌아가게 해야됨
  private void bindViews() {
    View goCourseMapButton = findViewById(R.id.btn_course_detail_go_course_map);

    goCourseMapButton.setOnClickListener(v -> finish());

    confirmButton = findViewById(R.id.btn_course_detail_confirm);
    confirmText = findViewById(R.id.tv_course_detail_confirm);
    shareButton = findViewById(R.id.btn_course_detail_share);
    favoriteButton = findViewById(R.id.btn_course_detail_favorite);
    favoriteText = findViewById(R.id.tv_course_detail_favorite);
    titleText = findViewById(R.id.tv_course_detail_title);
    timeText = findViewById(R.id.tv_course_detail_time);
    distanceText = findViewById(R.id.tv_course_detail_distance);
    tagsText = findViewById(R.id.tv_course_detail_tags);
    rvPlaces = findViewById(R.id.rv_course_detail_places);
    rvPlaces.setLayoutManager(new LinearLayoutManager(this));
    rvPlaces.setNestedScrollingEnabled(false);

    confirmButton.setOnClickListener(v -> confirmCourse());
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
          Course course = database.courseDao().getCourseById(courseId);
          boolean favorite = database.favoriteDao().isFavorite(courseId) > 0;
          List<CoursePlace> coursePlaces = database.coursePlaceDao().getPlacesByCourseId(courseId);
          boolean confirmed = database.recordDao().isRecorded(courseId) > 0;

          runOnUiThread(
              () -> {
                currentCourse = course;
                isFavorite = favorite;
                isConfirmed = confirmed;
                if (currentCourse == null) {
                  showMissingCourse();
                  return;
                }
                List<PlaceItem> placeItems = new ArrayList<>();
                for (CoursePlace p : coursePlaces) {
                  placeItems.add(new PlaceItem(p.placeName, p.latitude, p.longitude));
                }
                currentPlaceItems = placeItems;
                renderCourse(currentCourse, placeItems);
                updateFavoriteUi();
                updateConfirmUi();
                shareButton.setEnabled(true);
              });
        });
  }

  // 가져온 코스 정보를 UI에 렌더링
  private void renderCourse(Course course, List<PlaceItem> placeItems) {
    titleText.setText(course.title);
    tagsText.setText(course.tagsText.replace(",", "  "));
    double totalMeters = calculateTotalDistance(placeItems);
    timeText.setText(formatTime(totalMeters));
    distanceText.setText(formatDistance(totalMeters));
    rvPlaces.setAdapter(new PlaceAdapter(placeItems, course.thumbColor));
  }

  private void confirmCourse() {
    if (currentCourse == null) {
      Toast.makeText(this, "코스 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
      return;
    }

    CourseDatabase.databaseExecutor.execute(
        () -> {
          if (isConfirmed) {
            database.recordDao().deleteRecordByCourseId(currentCourse.courseId);
          } else {
            database
                .recordDao()
                .insertRecord(new Record(currentCourse.courseId, System.currentTimeMillis(), ""));
          }

          boolean next = !isConfirmed;
          runOnUiThread(
              () -> {
                isConfirmed = next;
                updateConfirmUi();
                Toast.makeText(this, isConfirmed ? "코스가 기록됐어요." : "기록이 취소됐어요.", Toast.LENGTH_SHORT)
                    .show();
              });
        });
  }

  private void updateConfirmUi() {
    confirmText.setText(isConfirmed ? "확정취소" : "확정하기");
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
    List<String> names = new ArrayList<>();
    for (PlaceItem item : currentPlaceItems) names.add(item.name);
    String routeLine = TextUtils.join(" → ", names);
    String tagLine = course.tagsText != null ? course.tagsText.replace(",", " ") : "";

    StringBuilder sb = new StringBuilder();
    sb.append(course.title).append('\n');
    if (!routeLine.isEmpty()) sb.append(routeLine).append('\n');
    if (!tagLine.isEmpty()) sb.append(tagLine).append('\n');
    double totalMeters = calculateTotalDistance(currentPlaceItems);
    sb.append(formatTime(totalMeters)).append(" · ").append(formatDistance(totalMeters));
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
    timeText.setText("-");
    distanceText.setText("-");
    tagsText.setText("");
    favoriteText.setText("찜하기");
    favoriteButton.setEnabled(false);
    shareButton.setEnabled(false);
  }

  // 위경도 기준으로 두 좌표 사이의 직선 거리 계산
  private double calculateHaversineDistance(double lat1, double lng1, double lat2, double lng2) {
    final int R = 6371000;
    double phi1 = Math.toRadians(lat1);
    double phi2 = Math.toRadians(lat2);
    double deltaPhi = Math.toRadians(lat2 - lat1);
    double deltaLambda = Math.toRadians(lng2 - lng1);
    double a =
        Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2)
            + Math.cos(phi1)
                * Math.cos(phi2)
                * Math.sin(deltaLambda / 2)
                * Math.sin(deltaLambda / 2);
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  // 연속 장소들 사이 거리 합산
  private double calculateTotalDistance(List<PlaceItem> places) {
    double total = 0;
    for (int i = 0; i < places.size() - 1; i++) {
      PlaceItem from = places.get(i);
      PlaceItem to = places.get(i + 1);
      if (from.hasCoords() && to.hasCoords()) {
        total += calculateHaversineDistance(from.lat, from.lng, to.lat, to.lng);
      }
    }
    return total;
  }

  // 1000m 이상이면 km로, 미만이면 m로 표기
  private String formatDistance(double meters) {
    if (meters <= 0) return "-";
    if (meters >= 1000)
      return String.format(java.util.Locale.getDefault(), "총 %.1fkm", meters / 1000);
    return "총 " + (int) Math.round(meters) + "m";
  }

  // 도보 기준으로 시간 계산 67m/min -> 1km당 약 15분, 500m면 약 5분 -> 총 거리 기준으로 시간 표기
  private String formatTime(double meters) {
    if (meters <= 0) return "-";
    int minutes = (int) Math.round(meters / 67.0);
    if (minutes < 1) return "약 1분";
    if (minutes < 60) return "약 " + minutes + "분";
    int hours = minutes / 60;
    int rem = minutes % 60;
    return rem == 0 ? "약 " + hours + "시간" : "약 " + hours + "시간 " + rem + "분";
  }

  private static class PlaceItem {
    final String name;
    final double lat;
    final double lng;

    PlaceItem(String name) {
      this.name = name;
      this.lat = 0;
      this.lng = 0;
    }

    PlaceItem(String name, double lat, double lng) {
      this.name = name;
      this.lat = lat;
      this.lng = lng;
    }

    boolean hasCoords() {
      return lat != 0 || lng != 0;
    }
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

    private final List<PlaceItem> places;
    private final int thumbColor;

    PlaceAdapter(List<PlaceItem> places, int thumbColor) {
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
      PlaceItem item = places.get(position);

      holder.tvPlaceNumber.setText(String.valueOf(position + 1));
      holder.cvPlaceNumber.setCardBackgroundColor(BADGE_COLORS[position % BADGE_COLORS.length]);
      holder.tvPlaceTitle.setText(item.name);
      holder.viewThumb.setBackgroundColor(thumbColor);
      holder.viewLine.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);
      holder.layoutMoveInfo.setVisibility(isLast ? View.GONE : View.VISIBLE);

      if (!isLast) {
        holder.tvMoveInfo.setText(item.name + "에서 " + places.get(position + 1).name + "로 이동");
      }

      holder.btnNaverMap.setOnClickListener(
          v -> {
            if (item.hasCoords()) {
              String appUrl =
                  "naver://map?lat="
                      + item.lat
                      + "&lng="
                      + item.lng
                      + "&zoom=15&title="
                      + Uri.encode(item.name);
              String webUrl = "https://map.naver.com/p/?lat=" + item.lat + "&lng=" + item.lng;
              try {
                v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appUrl)));
              } catch (ActivityNotFoundException e) {
                v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)));
              }
            } else {
              String searchUrl = "https://map.naver.com/p/search/" + Uri.encode(item.name);
              v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl)));
            }
          });
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
      final CardView btnNaverMap;

      ViewHolder(View itemView) {
        super(itemView);
        cvPlaceNumber = itemView.findViewById(R.id.cv_place_number);
        tvPlaceNumber = itemView.findViewById(R.id.tv_place_number);
        viewThumb = itemView.findViewById(R.id.view_place_thumb);
        viewLine = itemView.findViewById(R.id.view_place_line);
        tvPlaceTitle = itemView.findViewById(R.id.tv_place_title);
        layoutMoveInfo = itemView.findViewById(R.id.layout_place_move_info);
        tvMoveInfo = itemView.findViewById(R.id.tv_place_move_info);
        btnNaverMap = itemView.findViewById(R.id.btn_naver_map);
      }
    }
  }
}
