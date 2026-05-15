package com.example.day2day.presentation.recommend.flow;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.day2day.R;
import com.example.day2day.data.CourseContract;
import com.example.day2day.data.local.CourseDatabase;
import com.example.day2day.data.local.CourseSeedData;
import com.example.day2day.data.local.entity.Course;
import com.example.day2day.data.local.entity.Favorite;
import com.example.day2day.presentation.common.NavigationBarInsetHelper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CourseDetailPageActivity extends AppCompatActivity {
  private static final double WALKING_METERS_PER_MINUTE = 67d;
  private static final int[] STEP_ACCENT_COLORS = {
    Color.parseColor("#E8506A"),
    Color.parseColor("#3D8BFD"),
    Color.parseColor("#F08C00"),
    Color.parseColor("#2B8A3E")
  };
  private static final int[] STEP_SURFACE_COLORS = {
    Color.parseColor("#FCE8EC"),
    Color.parseColor("#E7F4FF"),
    Color.parseColor("#FFF4D6"),
    Color.parseColor("#E9F7EF")
  };

  private CourseDatabase database;
  private String currentCourseId;
  private boolean favoriteSupported;
  private boolean isFavorite;
  private DetailCourseModel currentCourse;

  private TextView shareButton;
  private TextView favoriteButton;
  private TextView titleText;
  private TextView subtitleText;
  private TextView durationText;
  private TextView distanceText;
  private TextView ratingText;
  private TextView placeCountText;
  private LinearLayout tagContainer;
  private TextView highlightText;
  private TextView startText;
  private TextView endText;
  private TextView routeText;
  private TextView noteText;
  private LinearLayout timelineContainer;
  private TextView emptyStateText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_course_detail_page);

    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }

    database = CourseDatabase.getInstance(this);
    bindViews();
    applyInsets();
    loadCourse();
  }

  private void bindViews() {
    findViewById(R.id.btn_course_detail_back).setOnClickListener(v -> finish());

    shareButton = findViewById(R.id.btn_course_detail_share);
    favoriteButton = findViewById(R.id.btn_course_detail_favorite);
    titleText = findViewById(R.id.tv_course_detail_title);
    subtitleText = findViewById(R.id.tv_course_detail_subtitle);
    durationText = findViewById(R.id.tv_course_detail_duration);
    distanceText = findViewById(R.id.tv_course_detail_distance);
    ratingText = findViewById(R.id.tv_course_detail_rating);
    placeCountText = findViewById(R.id.tv_course_detail_place_count);
    tagContainer = findViewById(R.id.ll_course_detail_tag_container);
    highlightText = findViewById(R.id.tv_course_detail_highlight);
    startText = findViewById(R.id.tv_course_detail_start);
    endText = findViewById(R.id.tv_course_detail_end);
    routeText = findViewById(R.id.tv_course_detail_route);
    noteText = findViewById(R.id.tv_course_detail_note);
    timelineContainer = findViewById(R.id.ll_course_detail_timeline_container);
    emptyStateText = findViewById(R.id.tv_course_detail_empty_state);

    shareButton.setOnClickListener(v -> shareCourse());
    favoriteButton.setOnClickListener(v -> toggleFavorite());
  }

  private void applyInsets() {
    View rootView = findViewById(android.R.id.content);
    View headerView = findViewById(R.id.course_detail_header);
    NavigationBarInsetHelper.applyTopInset(rootView, headerView);
  }

  private void loadCourse() {
    CourseDto recommendedCourse = extractRecommendedCourse();
    if (recommendedCourse != null) {
      favoriteSupported = false;
      isFavorite = false;
      currentCourseId = null;
      currentCourse = buildRecommendedCourseModel(recommendedCourse);
      renderCourse(currentCourse);
      updateFavoriteUi();
      return;
    }

    currentCourseId = getIntent().getStringExtra(CourseContract.EXTRA_COURSE_ID);
    if (TextUtils.isEmpty(currentCourseId)) {
      showMissingCourse();
      return;
    }

    CourseDatabase.databaseExecutor.execute(
        () -> {
          ensureSeedCourses();
          Course course = database.courseDao().getCourseById(currentCourseId);
          boolean favorite =
              course != null && database.favoriteDao().isFavorite(currentCourseId) > 0;
          DetailCourseModel detailCourse = course == null ? null : buildStoredCourseModel(course);

          runOnUiThread(
              () -> {
                if (detailCourse == null) {
                  showMissingCourse();
                  return;
                }

                currentCourse = detailCourse;
                favoriteSupported = true;
                isFavorite = favorite;
                renderCourse(detailCourse);
                updateFavoriteUi();
              });
        });
  }

  private void ensureSeedCourses() {
    if (database.courseDao().getCourseCount() == 0) {
      database.courseDao().insertCourses(CourseSeedData.getPopularCourses());
    }
  }

  private CourseDto extractRecommendedCourse() {
    Serializable payload =
        getIntent().getSerializableExtra(CourseContract.EXTRA_RECOMMENDED_COURSE);
    if (payload instanceof CourseDto) {
      return (CourseDto) payload;
    }
    return null;
  }

  private DetailCourseModel buildStoredCourseModel(Course course) {
    List<String> tags = sanitizeTags(splitTags(course.tagsText));
    List<String> routeItems = splitRoute(course.routeText);
    List<DetailPlaceModel> places = new ArrayList<>();

    for (int i = 0; i < routeItems.size(); i++) {
      String placeName = routeItems.get(i);
      String chipLabel = inferSeedChip(placeName, tags, i, routeItems.size());
      String metaText = buildSeedMetaText(i, routeItems.size());
      String bodyText = buildSeedBodyText(tags, placeName, i, routeItems.size());
      places.add(
          new DetailPlaceModel(
              placeName,
              chipLabel,
              metaText,
              bodyText,
              "",
              Double.NaN,
              Double.NaN,
              STEP_ACCENT_COLORS[i % STEP_ACCENT_COLORS.length],
              STEP_SURFACE_COLORS[i % STEP_SURFACE_COLORS.length]));
    }

    String title = safeText(course.title, "추천 코스");
    String subtitle = buildSubtitle(tags, places.size(), false);
    String duration = formatDurationMinutes(estimateStoredDurationMinutes(places.size(), tags));
    String distance = "도보 중심 동선";
    String rating = safeText(course.ratingText, "인기 코스");
    String highlight = buildHighlightText(routeItems, tags);
    String start = routeItems.isEmpty() ? "정보 없음" : routeItems.get(0);
    String end = routeItems.isEmpty() ? "정보 없음" : routeItems.get(routeItems.size() - 1);
    String route = routeItems.isEmpty() ? "동선을 준비 중이에요." : TextUtils.join(" → ", routeItems);
    String note = "인기 코스로 저장된 순서예요. 앞에서부터 천천히 따라가면 흐름이 자연스럽습니다.";

    return new DetailCourseModel(
        title, subtitle, duration, distance, rating, tags, highlight, start, end, route, note,
        places);
  }

  private DetailCourseModel buildRecommendedCourseModel(CourseDto course) {
    List<PlaceDto> safePlaces = course.places == null ? Collections.emptyList() : course.places;
    List<String> tags = sanitizeTags(course.tags);
    if (tags.isEmpty()) {
      tags = deriveTagsFromPlaces(safePlaces);
    }

    List<DetailPlaceModel> places = new ArrayList<>();
    for (int i = 0; i < safePlaces.size(); i++) {
      PlaceDto place = safePlaces.get(i);
      String chipLabel = resolveRecommendedChip(place, i, safePlaces.size());
      String metaText = buildRecommendedMetaText(place, i, safePlaces.size());
      String bodyText =
          !TextUtils.isEmpty(place.address) ? place.address : "현재 위치와 취향을 기준으로 바로 들르기 좋은 장소예요.";
      places.add(
          new DetailPlaceModel(
              safeText(place.placeName, "추천 장소"),
              chipLabel,
              metaText,
              bodyText,
              safeText(place.placeUrl, ""),
              place.latitude,
              place.longitude,
              STEP_ACCENT_COLORS[i % STEP_ACCENT_COLORS.length],
              STEP_SURFACE_COLORS[i % STEP_SURFACE_COLORS.length]));
    }

    List<String> routeItems = extractPlaceNames(safePlaces);
    String title = safeText(course.courseName, "주변 추천 코스");
    String subtitle = buildSubtitle(tags, places.size(), true);
    String duration = formatDurationMinutes(estimateRecommendedDurationMinutes(safePlaces));
    String distance =
        !TextUtils.isEmpty(course.badgeText)
            ? course.badgeText
            : formatDistanceText(computeRouteDistanceMeters(safePlaces));
    String rating = "현위치 기준 추천";
    String highlight = buildHighlightText(routeItems, tags);
    String start = routeItems.isEmpty() ? "정보 없음" : routeItems.get(0);
    String end = routeItems.isEmpty() ? "정보 없음" : routeItems.get(routeItems.size() - 1);
    String route = routeItems.isEmpty() ? "추천 동선을 준비 중이에요." : TextUtils.join(" → ", routeItems);
    String note = "카드를 눌렀을 때 보이던 지도 동선을 그대로 따라갈 수 있게 정리했어요.";

    return new DetailCourseModel(
        title, subtitle, duration, distance, rating, tags, highlight, start, end, route, note,
        places);
  }

  private void renderCourse(DetailCourseModel course) {
    titleText.setText(course.title);
    subtitleText.setText(course.subtitle);
    durationText.setText(course.durationText);
    distanceText.setText(course.distanceText);
    ratingText.setText(course.ratingText);
    placeCountText.setText(String.format(Locale.KOREA, "%d곳", course.places.size()));
    highlightText.setText(course.highlightText);
    startText.setText(course.startPlaceText);
    endText.setText(course.endPlaceText);
    routeText.setText(course.routeText);
    noteText.setText(course.noteText);

    bindTags(course.tags);
    bindTimeline(course.places);

    emptyStateText.setVisibility(course.places.isEmpty() ? View.VISIBLE : View.GONE);
    timelineContainer.setVisibility(course.places.isEmpty() ? View.GONE : View.VISIBLE);

    shareButton.setEnabled(true);
    shareButton.setAlpha(1f);
  }

  private void bindTags(List<String> tags) {
    tagContainer.removeAllViews();
    List<String> visibleTags =
        tags == null || tags.isEmpty() ? Collections.singletonList("추천 코스") : tags;

    for (int i = 0; i < visibleTags.size() && i < 4; i++) {
      TextView tagView = new TextView(this);
      tagView.setText("#" + visibleTags.get(i));
      tagView.setTextColor(ContextCompat.getColor(this, R.color.rose));
      tagView.setTextSize(12f);
      tagView.setBackgroundResource(R.drawable.shape_tag_rose);
      tagView.setPadding(dpToPx(12), dpToPx(7), dpToPx(12), dpToPx(7));

      LinearLayout.LayoutParams params =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      if (i > 0) {
        params.setMarginStart(dpToPx(8));
      }
      tagContainer.addView(tagView, params);
    }
  }

  private void bindTimeline(List<DetailPlaceModel> places) {
    timelineContainer.removeAllViews();
    if (places == null || places.isEmpty()) {
      return;
    }

    LayoutInflater inflater = LayoutInflater.from(this);
    for (int i = 0; i < places.size(); i++) {
      DetailPlaceModel place = places.get(i);
      View stepView = inflater.inflate(R.layout.item_course_detail_step, timelineContainer, false);
      bindStepView(stepView, place, i, places.size());
      timelineContainer.addView(stepView);

      if (i < places.size() - 1) {
        View transferView =
            inflater.inflate(R.layout.item_course_detail_transfer, timelineContainer, false);
        TextView transferInfo = transferView.findViewById(R.id.tv_course_detail_transfer_info);
        transferInfo.setText(buildTransferText(place, places.get(i + 1)));
        timelineContainer.addView(transferView);
      }
    }
  }

  private void bindStepView(View stepView, DetailPlaceModel place, int index, int totalCount) {
    TextView numberView = stepView.findViewById(R.id.tv_course_detail_step_number);
    View lineView = stepView.findViewById(R.id.view_course_detail_step_line);
    TextView chipView = stepView.findViewById(R.id.tv_course_detail_step_chip);
    TextView titleView = stepView.findViewById(R.id.tv_course_detail_step_title);
    TextView metaView = stepView.findViewById(R.id.tv_course_detail_step_meta);
    TextView bodyView = stepView.findViewById(R.id.tv_course_detail_step_body);
    TextView actionView = stepView.findViewById(R.id.btn_course_detail_step_action);

    numberView.setText(String.valueOf(index + 1));
    numberView.setBackground(createRoundedBackground(place.accentColor, place.accentColor, 999f));
    lineView.setVisibility(index == totalCount - 1 ? View.GONE : View.VISIBLE);

    chipView.setText(place.chipLabel);
    chipView.setTextColor(place.accentColor);
    chipView.setBackground(
        createRoundedBackground(place.surfaceColor, withAlpha(place.accentColor, 0.2f), 999f));

    titleView.setText(place.name);
    metaView.setText(place.metaText);
    bodyView.setText(place.bodyText);

    if (TextUtils.isEmpty(place.actionUrl)) {
      actionView.setVisibility(View.GONE);
      actionView.setOnClickListener(null);
    } else {
      actionView.setVisibility(View.VISIBLE);
      actionView.setOnClickListener(v -> openPlaceLink(place.actionUrl));
    }
  }

  private void toggleFavorite() {
    if (!favoriteSupported || currentCourse == null || TextUtils.isEmpty(currentCourseId)) {
      Toast.makeText(this, "이 코스는 찜 기능을 아직 지원하지 않아요.", Toast.LENGTH_SHORT).show();
      return;
    }

    CourseDatabase.databaseExecutor.execute(
        () -> {
          if (isFavorite) {
            database.favoriteDao().deleteFavoriteByCourseId(currentCourseId);
          } else {
            database
                .favoriteDao()
                .insertFavorite(new Favorite(currentCourseId, System.currentTimeMillis()));
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
    if (!favoriteSupported) {
      favoriteButton.setVisibility(View.GONE);
      return;
    }

    favoriteButton.setVisibility(View.VISIBLE);
    favoriteButton.setEnabled(true);
    favoriteButton.setAlpha(1f);
    favoriteButton.setText(isFavorite ? "찜 취소" : "찜하기");
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

  private void openPlaceLink(String url) {
    if (TextUtils.isEmpty(url)) {
      Toast.makeText(this, "이 장소는 연결할 외부 정보가 없어요.", Toast.LENGTH_SHORT).show();
      return;
    }

    try {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    } catch (ActivityNotFoundException e) {
      Toast.makeText(this, "링크를 열 수 있는 앱을 찾지 못했어요.", Toast.LENGTH_SHORT).show();
    }
  }

  private String buildShareText(DetailCourseModel course) {
    List<String> names = new ArrayList<>();
    for (DetailPlaceModel place : course.places) {
      names.add(place.name);
    }

    StringBuilder builder = new StringBuilder();
    builder.append(course.title).append('\n');
    builder.append(course.subtitle).append('\n');
    if (!names.isEmpty()) {
      builder.append(TextUtils.join(" → ", names)).append('\n');
    }
    if (!course.tags.isEmpty()) {
      builder.append(buildHashTagLine(course.tags)).append('\n');
    }
    builder
        .append(course.durationText)
        .append(" · ")
        .append(course.distanceText)
        .append(" · ")
        .append(String.format(Locale.KOREA, "%d곳", course.places.size()));
    return builder.toString();
  }

  private void showMissingCourse() {
    currentCourse = null;
    currentCourseId = null;
    favoriteSupported = false;
    isFavorite = false;

    titleText.setText("코스 정보를 찾을 수 없어요");
    subtitleText.setText("이전 화면으로 돌아가서 다시 코스를 골라 주세요.");
    durationText.setText("정보 없음");
    distanceText.setText("정보 없음");
    ratingText.setText("정보 없음");
    placeCountText.setText("0곳");
    highlightText.setText("전달된 코스 정보가 없어 상세 내용을 표시하지 못했습니다.");
    startText.setText("정보 없음");
    endText.setText("정보 없음");
    routeText.setText("코스 경로를 다시 확인해 주세요.");
    noteText.setText("홈 또는 추천 코스 화면에서 다시 진입하면 정상적으로 볼 수 있어요.");

    bindTags(Collections.singletonList("안내"));
    timelineContainer.removeAllViews();
    timelineContainer.setVisibility(View.GONE);
    emptyStateText.setVisibility(View.VISIBLE);

    shareButton.setEnabled(false);
    shareButton.setAlpha(0.5f);
    updateFavoriteUi();
  }

  private List<String> splitRoute(String routeText) {
    if (TextUtils.isEmpty(routeText)) {
      return Collections.emptyList();
    }

    List<String> items = new ArrayList<>();
    for (String item : routeText.split("\\s*>\\s*")) {
      String clean = safeText(item, "");
      if (!clean.isEmpty()) {
        items.add(clean);
      }
    }
    return items;
  }

  private List<String> splitTags(String tagsText) {
    if (TextUtils.isEmpty(tagsText)) {
      return Collections.emptyList();
    }

    List<String> items = new ArrayList<>();
    for (String item : tagsText.split(",")) {
      String clean = safeText(item, "").replace("#", "");
      if (!clean.isEmpty()) {
        items.add(clean);
      }
    }
    return items;
  }

  private List<String> sanitizeTags(List<String> tags) {
    if (tags == null || tags.isEmpty()) {
      return new ArrayList<>();
    }

    List<String> result = new ArrayList<>();
    for (String tag : tags) {
      String clean = safeText(tag, "").replace("#", "");
      if (!clean.isEmpty() && !result.contains(clean)) {
        result.add(clean);
      }
    }
    return result;
  }

  private List<String> deriveTagsFromPlaces(List<PlaceDto> places) {
    List<String> tags = new ArrayList<>();
    for (PlaceDto place : places) {
      String tag = compactCategory(place.categoryName);
      if (!tag.isEmpty() && !tags.contains(tag)) {
        tags.add(tag);
      }
      if (tags.size() >= 3) {
        break;
      }
    }

    if (tags.isEmpty()) {
      tags.add("주변추천");
    }
    return tags;
  }

  private List<String> extractPlaceNames(List<PlaceDto> places) {
    List<String> names = new ArrayList<>();
    for (PlaceDto place : places) {
      String clean = safeText(place.placeName, "");
      if (!clean.isEmpty()) {
        names.add(clean);
      }
    }
    return names;
  }

  private String buildSubtitle(List<String> tags, int placeCount, boolean recommended) {
    String moodText;
    if (tags == null || tags.isEmpty()) {
      moodText = recommended ? "지금 주변에서 가볍게 즐기기 좋은" : "차분하게 둘러보기 좋은";
    } else {
      moodText = TextUtils.join(" · ", tags.subList(0, Math.min(2, tags.size()))) + " 무드로 즐기는";
    }
    return moodText + " " + Math.max(placeCount, 1) + "곳 코스";
  }

  private String buildHighlightText(List<String> routeItems, List<String> tags) {
    if (routeItems == null || routeItems.isEmpty()) {
      return "분위기와 동선을 기준으로 다시 추천받으면 더 풍성한 코스를 볼 수 있어요.";
    }

    String firstPlace = routeItems.get(0);
    String lastPlace = routeItems.get(routeItems.size() - 1);
    String tagText =
        tags == null || tags.isEmpty()
            ? "동선 흐름"
            : TextUtils.join(", ", tags.subList(0, Math.min(2, tags.size())));
    return firstPlace + "에서 시작해 " + lastPlace + "까지 이어지는 " + tagText + " 중심 코스예요.";
  }

  private String inferSeedChip(String placeName, List<String> tags, int index, int totalCount) {
    String combined =
        (safeText(placeName, "") + " " + TextUtils.join(" ", tags)).toLowerCase(Locale.KOREA);
    if (containsAny(combined, "카페", "커피", "베이커리", "디저트")) {
      return "카페";
    }
    if (containsAny(combined, "식당", "맛집", "브런치", "레스토랑", "주점")) {
      return "식사";
    }
    if (containsAny(combined, "공원", "전시", "미술", "산책", "영화", "서점")) {
      return "놀거리";
    }
    if (index == 0) {
      return "시작 스팟";
    }
    if (index == totalCount - 1) {
      return "마무리 스팟";
    }
    return "중간 스팟";
  }

  private String buildSeedMetaText(int index, int totalCount) {
    if (index == 0) {
      return "코스의 첫 분위기를 여는 장소";
    }
    if (index == totalCount - 1) {
      return "마지막까지 여운을 남기는 장소";
    }
    return "코스 흐름을 이어 주는 " + (index + 1) + "번째 장소";
  }

  private String buildSeedBodyText(List<String> tags, String placeName, int index, int totalCount) {
    if (tags != null && !tags.isEmpty()) {
      return TextUtils.join(", ", tags.subList(0, Math.min(2, tags.size())))
          + " 느낌을 살리기 좋은 장소로 "
          + placeName
          + "를 배치했어요.";
    }

    if (index == 0) {
      return "코스 시작점으로 가볍게 들어가기 좋은 장소예요.";
    }
    if (index == totalCount - 1) {
      return "코스를 마무리하며 천천히 머무르기 좋은 장소예요.";
    }
    return "앞뒤 장소 사이에서 분위기를 매끄럽게 이어 주는 포인트예요.";
  }

  private String resolveRecommendedChip(PlaceDto place, int index, int totalCount) {
    String category = compactCategory(place.categoryName);
    if (!category.isEmpty()) {
      return category;
    }
    if (index == 0) {
      return "첫 장소";
    }
    if (index == totalCount - 1) {
      return "마무리";
    }
    return "중간 코스";
  }

  private String buildRecommendedMetaText(PlaceDto place, int index, int totalCount) {
    List<String> parts = new ArrayList<>();
    String category = compactCategory(place.categoryName);
    if (!category.isEmpty()) {
      parts.add(category);
    }
    if (place.distanceMeters > 0) {
      parts.add("시작점에서 " + formatDistanceText(place.distanceMeters));
    }
    if (parts.isEmpty()) {
      parts.add(index == totalCount - 1 ? "마무리하기 좋은 추천 장소" : "근처 추천 장소");
    }
    return TextUtils.join(" · ", parts);
  }

  private String compactCategory(String categoryName) {
    if (TextUtils.isEmpty(categoryName)) {
      return "";
    }

    String[] segments = categoryName.split(">");
    String value = safeText(segments[segments.length - 1], "");
    if (value.length() > 8) {
      return value.substring(0, 8).trim();
    }
    return value;
  }

  private int estimateStoredDurationMinutes(int placeCount, List<String> tags) {
    int stayMinutes = placeCount * 40;
    int moveMinutes = Math.max(placeCount - 1, 0) * 12;
    int bonusMinutes =
        containsAny(TextUtils.join(" ", tags).toLowerCase(Locale.KOREA), "카페", "브런치") ? 15 : 0;
    return stayMinutes + moveMinutes + bonusMinutes;
  }

  private int estimateRecommendedDurationMinutes(List<PlaceDto> places) {
    if (places == null || places.isEmpty()) {
      return 60;
    }

    int totalMinutes = 0;
    for (int i = 0; i < places.size(); i++) {
      PlaceDto place = places.get(i);
      totalMinutes += estimateStayMinutes(place.categoryName);

      if (place.distanceMeters > 0 && i == 0) {
        totalMinutes +=
            Math.max(5, (int) Math.round(place.distanceMeters / WALKING_METERS_PER_MINUTE));
      }

      if (i < places.size() - 1) {
        int moveDistance =
            roundDistance(
                distanceBetween(
                    place.latitude,
                    place.longitude,
                    places.get(i + 1).latitude,
                    places.get(i + 1).longitude));
        totalMinutes += Math.max(5, (int) Math.round(moveDistance / WALKING_METERS_PER_MINUTE));
      }
    }

    return totalMinutes;
  }

  private int estimateStayMinutes(String categoryName) {
    String lower = safeText(categoryName, "").toLowerCase(Locale.KOREA);
    if (containsAny(lower, "카페", "베이커리", "디저트")) {
      return 45;
    }
    if (containsAny(lower, "식당", "맛집", "브런치", "레스토랑", "주점")) {
      return 60;
    }
    return 50;
  }

  private int computeRouteDistanceMeters(List<PlaceDto> places) {
    if (places == null || places.isEmpty()) {
      return 0;
    }

    int distance = 0;
    if (places.get(0).distanceMeters > 0) {
      distance += places.get(0).distanceMeters;
    }

    for (int i = 0; i < places.size() - 1; i++) {
      distance +=
          roundDistance(
              distanceBetween(
                  places.get(i).latitude,
                  places.get(i).longitude,
                  places.get(i + 1).latitude,
                  places.get(i + 1).longitude));
    }
    return distance;
  }

  private String buildTransferText(DetailPlaceModel from, DetailPlaceModel to) {
    if (from.hasCoordinates() && to.hasCoordinates()) {
      int meters =
          roundDistance(distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude));
      int walkMinutes = Math.max(3, (int) Math.round(meters / WALKING_METERS_PER_MINUTE));
      return "도보 약 " + walkMinutes + "분 · " + formatDistanceText(meters);
    }

    return "다음 장소로 천천히 분위기를 이어 가기 좋은 구간이에요.";
  }

  private String formatDurationMinutes(int totalMinutes) {
    int safeMinutes = Math.max(totalMinutes, 30);
    int hours = safeMinutes / 60;
    int minutes = safeMinutes % 60;
    if (hours == 0) {
      return "약 " + minutes + "분";
    }
    if (minutes == 0) {
      return "약 " + hours + "시간";
    }
    return "약 " + hours + "시간 " + minutes + "분";
  }

  private String formatDistanceText(int meters) {
    int safeMeters = Math.max(meters, 0);
    if (safeMeters < 1000) {
      return safeMeters + "m";
    }
    return String.format(Locale.KOREA, "%.1fkm", safeMeters / 1000d);
  }

  private String buildHashTagLine(List<String> tags) {
    List<String> hashTags = new ArrayList<>();
    for (int i = 0; i < tags.size() && i < 4; i++) {
      hashTags.add("#" + tags.get(i));
    }
    return TextUtils.join(" ", hashTags);
  }

  private boolean containsAny(String value, String... needles) {
    for (String needle : needles) {
      if (value.contains(needle.toLowerCase(Locale.KOREA))) {
        return true;
      }
    }
    return false;
  }

  private String safeText(String value, String fallback) {
    return TextUtils.isEmpty(value) ? fallback : value.trim();
  }

  private int roundDistance(double meters) {
    return (int) Math.round(meters);
  }

  private double distanceBetween(
      double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
    if (Double.isNaN(startLatitude)
        || Double.isNaN(startLongitude)
        || Double.isNaN(endLatitude)
        || Double.isNaN(endLongitude)) {
      return 0d;
    }

    double earthRadius = 6371000d;
    double dLat = Math.toRadians(endLatitude - startLatitude);
    double dLng = Math.toRadians(endLongitude - startLongitude);
    double a =
        Math.sin(dLat / 2d) * Math.sin(dLat / 2d)
            + Math.cos(Math.toRadians(startLatitude))
                * Math.cos(Math.toRadians(endLatitude))
                * Math.sin(dLng / 2d)
                * Math.sin(dLng / 2d);
    double c = 2d * Math.atan2(Math.sqrt(a), Math.sqrt(1d - a));
    return earthRadius * c;
  }

  private GradientDrawable createRoundedBackground(
      @ColorInt int fillColor, @ColorInt int strokeColor, float radiusDp) {
    GradientDrawable drawable = new GradientDrawable();
    drawable.setShape(GradientDrawable.RECTANGLE);
    drawable.setColor(fillColor);
    drawable.setCornerRadius(dpToPx(radiusDp));
    drawable.setStroke(dpToPx(1), strokeColor);
    return drawable;
  }

  private int withAlpha(@ColorInt int color, double alphaFraction) {
    int alpha = (int) Math.round(Math.max(0d, Math.min(1d, alphaFraction)) * 255d);
    return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
  }

  private int dpToPx(float dp) {
    return Math.round(dp * getResources().getDisplayMetrics().density);
  }

  private static final class DetailCourseModel {
    private final String title;
    private final String subtitle;
    private final String durationText;
    private final String distanceText;
    private final String ratingText;
    private final List<String> tags;
    private final String highlightText;
    private final String startPlaceText;
    private final String endPlaceText;
    private final String routeText;
    private final String noteText;
    private final List<DetailPlaceModel> places;

    private DetailCourseModel(
        String title,
        String subtitle,
        String durationText,
        String distanceText,
        String ratingText,
        List<String> tags,
        String highlightText,
        String startPlaceText,
        String endPlaceText,
        String routeText,
        String noteText,
        List<DetailPlaceModel> places) {
      this.title = title;
      this.subtitle = subtitle;
      this.durationText = durationText;
      this.distanceText = distanceText;
      this.ratingText = ratingText;
      this.tags = tags == null ? Collections.emptyList() : tags;
      this.highlightText = highlightText;
      this.startPlaceText = startPlaceText;
      this.endPlaceText = endPlaceText;
      this.routeText = routeText;
      this.noteText = noteText;
      this.places = places == null ? Collections.emptyList() : places;
    }
  }

  private static final class DetailPlaceModel {
    private final String name;
    private final String chipLabel;
    private final String metaText;
    private final String bodyText;
    private final String actionUrl;
    private final double latitude;
    private final double longitude;
    private final int accentColor;
    private final int surfaceColor;

    private DetailPlaceModel(
        String name,
        String chipLabel,
        String metaText,
        String bodyText,
        String actionUrl,
        double latitude,
        double longitude,
        int accentColor,
        int surfaceColor) {
      this.name = name;
      this.chipLabel = chipLabel;
      this.metaText = metaText;
      this.bodyText = bodyText;
      this.actionUrl = actionUrl;
      this.latitude = latitude;
      this.longitude = longitude;
      this.accentColor = accentColor;
      this.surfaceColor = surfaceColor;
    }

    private boolean hasCoordinates() {
      return !Double.isNaN(latitude) && !Double.isNaN(longitude);
    }
  }
}
