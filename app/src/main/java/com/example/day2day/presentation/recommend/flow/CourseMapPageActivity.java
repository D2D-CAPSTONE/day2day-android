package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.day2day.R;
import com.example.day2day.data.CourseContract;
import com.example.day2day.data.local.CourseDatabase;
import com.example.day2day.data.local.entity.Course;
import com.example.day2day.data.local.entity.CoursePlace;
import com.example.day2day.data.remote.BackendPlaceResponse;
import com.example.day2day.data.remote.NearbyCourseEngine;
import com.example.day2day.presentation.common.NavigationBarInsetHelper;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PathOverlay;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CourseMapPageActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final String TAG = "CourseMapPageActivity";
  private static final double DEFAULT_LATITUDE = 37.5666102;
  private static final double DEFAULT_LONGITUDE = 126.9783881;
  private static final double DEFAULT_ZOOM = 14.5;

  private NaverMap naverMap;
  private final List<Marker> activeMarkers = new ArrayList<>();
  private PathOverlay currentPath;
  private NearbyCourseEngine courseEngine;
  private CourseDatabase database;
  private CourseAdapter adapter;
  private final List<Course> courseList = new ArrayList<>();
  private Course selectedCourse;
  private ArrayList<String> selectedMoods;
  private boolean useCoordinateSearch;
  private double latitude = DEFAULT_LATITUDE;
  private double longitude = DEFAULT_LONGITUDE;
  private String locationLabel = "현재 위치";

  private enum PlaceType {
    MEAL,
    CAFE,
    ACTIVITY,
    OTHER
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_course_map_page);

    courseEngine = new NearbyCourseEngine();
    database = CourseDatabase.getInstance(this);
    useCoordinateSearch =
        getIntent().getBooleanExtra(RecommendFlowContract.EXTRA_USE_COORDINATE_SEARCH, false);
    latitude = getIntent().getDoubleExtra(RecommendFlowContract.EXTRA_LATITUDE, DEFAULT_LATITUDE);
    longitude =
        getIntent().getDoubleExtra(RecommendFlowContract.EXTRA_LONGITUDE, DEFAULT_LONGITUDE);

    String extraLocationLabel =
        getIntent().getStringExtra(RecommendFlowContract.EXTRA_LOCATION_LABEL);
    if (extraLocationLabel != null && !extraLocationLabel.isEmpty()) {
      locationLabel = extraLocationLabel;
    }

    View rootView = findViewById(android.R.id.content);
    Button nextButton = findViewById(R.id.btn_course_map_page_next);
    NavigationBarInsetHelper.applyBottomInset(rootView, nextButton);
    nextButton.setOnClickListener(
        v -> {
          if (selectedCourse == null) {
            Toast.makeText(this, "코스를 선택해 주세요.", Toast.LENGTH_SHORT).show();
            return;
          }
          Intent intent = new Intent(CourseMapPageActivity.this, CourseDetailPageActivity.class);
          intent.putExtra(CourseContract.EXTRA_COURSE_ID, selectedCourse.courseId);

          String keyword = getIntent().getStringExtra("FILTER_KEYWORD");
          ArrayList<String> moods = getIntent().getStringArrayListExtra("FILTER_MOODS");
          String sortOrder = getIntent().getStringExtra("FILTER_SORT");

          intent.putExtra("FILTER_KEYWORD", keyword);
          intent.putStringArrayListExtra("FILTER_MOODS", moods);
          intent.putExtra("FILTER_SORT", sortOrder);

          startActivity(intent);
        });

    RecyclerView rvCourseList = findViewById(R.id.rv_course_list);
    rvCourseList.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

    adapter = new CourseAdapter(courseList, this::changeMapToCourse);
    rvCourseList.setAdapter(adapter);

    FragmentManager fm = getSupportFragmentManager();
    MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
    if (mapFragment == null) {
      mapFragment = MapFragment.newInstance();
      fm.beginTransaction().add(R.id.map, mapFragment).commit();
    }
    mapFragment.getMapAsync(this);

    String keyword = getIntent().getStringExtra("FILTER_KEYWORD");
    selectedMoods = getIntent().getStringArrayListExtra("FILTER_MOODS");
    if (keyword == null || keyword.isEmpty()) {
      keyword = useCoordinateSearch ? locationLabel : "서울 데이트";
    }

    fetchCoursesIndividually(keyword);
  }

  private void fetchCoursesIndividually(String baseKeyword) {
    String baseDistrict = extractCoreDistrict(baseKeyword.replace(" 데이트", ""));
    Log.d(TAG, "검색 기준: " + baseKeyword + ", 좌표 검색 여부: " + useCoordinateSearch);

    List<BackendPlaceResponse> allMeals = new ArrayList<>();
    List<BackendPlaceResponse> allCafes = new ArrayList<>();
    List<BackendPlaceResponse> allActivities = new ArrayList<>();

    List<String> activityKeywords = Arrays.asList("영화관", "PC방", "보드게임카페", "공원", "전시");
    int totalRequests = 2 + activityKeywords.size();
    AtomicInteger completedRequests = new AtomicInteger(0);

    requestPlaces(
        buildSearchQuery(baseDistrict, "맛집"),
        allMeals,
        totalRequests,
        completedRequests,
        () -> combineAndSave(baseKeyword, allMeals, allCafes, allActivities));

    requestPlaces(
        buildSearchQuery(baseDistrict, "카페"),
        allCafes,
        totalRequests,
        completedRequests,
        () -> combineAndSave(baseKeyword, allMeals, allCafes, allActivities));

    for (String activityKeyword : activityKeywords) {
      requestPlaces(
          buildSearchQuery(baseDistrict, activityKeyword),
          allActivities,
          totalRequests,
          completedRequests,
          () -> combineAndSave(baseKeyword, allMeals, allCafes, allActivities));
    }
  }

  private String buildSearchQuery(String baseDistrict, String categoryKeyword) {
    if (useCoordinateSearch) {
      return categoryKeyword;
    }
    return baseDistrict + " " + categoryKeyword;
  }

  private void requestPlaces(
      String query,
      List<BackendPlaceResponse> target,
      int totalRequests,
      AtomicInteger completedRequests,
      Runnable onCompleted) {
    NearbyCourseEngine.FetchPlacesCallback callback =
        new NearbyCourseEngine.FetchPlacesCallback() {
          @Override
          public void onSuccess(List<BackendPlaceResponse> places) {
            target.addAll(places);
            if (completedRequests.incrementAndGet() == totalRequests) {
              onCompleted.run();
            }
          }

          @Override
          public void onFailure(String errorMessage) {
            Log.w(TAG, "검색 실패: " + query + ", " + errorMessage);
            if (completedRequests.incrementAndGet() == totalRequests) {
              onCompleted.run();
            }
          }
        };

    if (useCoordinateSearch) {
      courseEngine.fetchNearbyPlacesByCoordinate(
          query, String.valueOf(longitude), String.valueOf(latitude), callback);
      return;
    }

    courseEngine.fetchNearbyPlaces(query, callback);
  }

  private void combineAndSave(
      String keyword,
      List<BackendPlaceResponse> allMeals,
      List<BackendPlaceResponse> allCafes,
      List<BackendPlaceResponse> allActivities) {
    Log.d(TAG, "모든 카테고리 검색 완료. 코스를 조합합니다.");
    saveCoursesToDatabaseStrict(keyword, allMeals, allCafes, allActivities);
  }

  private String extractCoreDistrict(String fullAddress) {
    if (fullAddress == null || fullAddress.isEmpty()) {
      return "서울";
    }

    String[] parts = fullAddress.trim().split("\\s+");
    if (parts.length == 0) {
      return fullAddress;
    }

    String lastPart = parts[parts.length - 1];
    String guPart = null;
    for (int i = parts.length - 1; i >= 0; i--) {
      if (parts[i].endsWith("구")) {
        guPart = parts[i];
        break;
      }
    }

    if (lastPart.endsWith("동")
        || lastPart.endsWith("구")
        || lastPart.endsWith("로")
        || lastPart.endsWith("길")) {
      return lastPart;
    }

    if (guPart != null) {
      return guPart;
    }

    return lastPart;
  }

  private void saveCoursesToDatabaseStrict(
      String keyword,
      List<BackendPlaceResponse> rawMeals,
      List<BackendPlaceResponse> rawCafes,
      List<BackendPlaceResponse> rawActivities) {

    List<BackendPlaceResponse> meals = filterByCategory(rawMeals, PlaceType.MEAL);
    List<BackendPlaceResponse> cafes = filterByCategory(rawCafes, PlaceType.CAFE);
    List<BackendPlaceResponse> activities = filterByCategory(rawActivities, PlaceType.ACTIVITY);

    Log.d(
        TAG,
        "분류 결과: 식사("
            + meals.size()
            + "), 카페("
            + cafes.size()
            + "), 놀거리("
            + activities.size()
            + ")");

    if (meals.isEmpty() || cafes.isEmpty() || activities.isEmpty()) {
      runOnUiThread(() -> Toast.makeText(this, "코스를 조합할 장소가 부족합니다.", Toast.LENGTH_SHORT).show());
      return;
    }

    List<Course> newCourses = new ArrayList<>();
    List<CoursePlace> newCoursePlaces = new ArrayList<>();
    int maxCourses = Math.min(3, Math.min(meals.size(), Math.min(cafes.size(), activities.size())));

    for (int i = 0; i < maxCourses; i++) {
      List<BackendPlaceResponse> coursePlacesResponse =
          List.of(meals.get(i), cafes.get(i), activities.get(i));

      String newCourseId = "API-" + UUID.randomUUID();
      String titlePrefix =
          locationLabel != null && !locationLabel.isEmpty() ? locationLabel : keyword;
      String courseTitle = titlePrefix + " 추천 코스 " + (i + 1);
      String routeText =
          coursePlacesResponse.stream()
              .map(BackendPlaceResponse::getName)
              .collect(Collectors.joining(" > "));

      Course course =
          new Course(
              newCourseId, courseTitle, routeText, "#추천", "★ 4.5", Color.parseColor("#FF6699"));
      newCourses.add(course);

      for (int j = 0; j < coursePlacesResponse.size(); j++) {
        BackendPlaceResponse place = coursePlacesResponse.get(j);
        try {
          newCoursePlaces.add(
              new CoursePlace(
                  newCourseId,
                  place.getName(),
                  place.getImageUrl(),
                  Double.parseDouble(place.getY()),
                  Double.parseDouble(place.getX()),
                  j));
        } catch (NumberFormatException e) {
          Log.e(TAG, "좌표 파싱 실패: " + place.getName(), e);
        }
      }
    }

    if (newCourses.isEmpty()) {
      Log.w(TAG, "저장할 코스가 없습니다.");
      return;
    }

    CourseDatabase.databaseExecutor.execute(
        () -> {
          populateMissingImageUrls(newCoursePlaces);
          database.courseDao().insertCourses(newCourses);
          database.coursePlaceDao().insertPlaces(newCoursePlaces);

          runOnUiThread(
              () -> {
                courseList.clear();
                courseList.addAll(newCourses);
                adapter.notifyDataSetChanged();
                if (naverMap != null && !courseList.isEmpty()) {
                  changeMapToCourse(courseList.get(0));
                } else if (naverMap != null && useCoordinateSearch) {
                  moveCameraToRequestedLocation();
                }
              });
        });
  }

  private void populateMissingImageUrls(List<CoursePlace> coursePlaces) {
    for (CoursePlace coursePlace : coursePlaces) {
      if (coursePlace.imageUrl != null && !coursePlace.imageUrl.isEmpty()) {
        continue;
      }

      String resolvedImageUrl =
          courseEngine.resolveImageUrl(
              coursePlace.placeName, coursePlace.latitude, coursePlace.longitude);
      if (resolvedImageUrl != null && !resolvedImageUrl.isEmpty()) {
        coursePlace.imageUrl = resolvedImageUrl;
      }
    }
  }

  private List<BackendPlaceResponse> filterByCategory(
      List<BackendPlaceResponse> places, PlaceType type) {
    List<BackendPlaceResponse> categorized = new ArrayList<>();
    for (BackendPlaceResponse place : places) {
      if (getPlaceType(place) == type) {
        categorized.add(place);
      }
    }
    return categorized;
  }

  private PlaceType getPlaceType(BackendPlaceResponse place) {
    String category = place.getCategory() != null ? place.getCategory().toString() : "";
    String name = place.getName() != null ? place.getName() : "";

    if (category.contains("한식")
        || category.contains("일식")
        || category.contains("중식")
        || category.contains("양식")
        || category.contains("파스타")
        || category.contains("패스트")
        || category.contains("식당")
        || category.contains("고기")
        || category.contains("레스토랑")
        || category.contains("이탈리안")
        || category.contains("카레")
        || category.contains("면")
        || category.contains("수제비")
        || category.contains("국밥")
        || category.contains("햄버거")) {
      return PlaceType.MEAL;
    }

    if (category.contains("카페")
        || category.contains("디저트")
        || category.contains("커피")
        || category.contains("베이커리")
        || category.contains("브런치카페")) {
      return PlaceType.CAFE;
    }

    if (category.contains("영화관")
        || category.contains("전시")
        || category.contains("공원")
        || category.contains("명소")
        || category.contains("보드게임카페")
        || category.contains("PC방")
        || category.contains("노래")
        || category.contains("게임")
        || category.contains("놀거리")) {
      return PlaceType.ACTIVITY;
    }

    if (name.contains("식당") || name.contains("레스토랑") || name.contains("맛집")) {
      return PlaceType.MEAL;
    }
    if (name.contains("카페") || name.contains("커피")) {
      return PlaceType.CAFE;
    }
    if (name.contains("PC") || name.contains("노래") || name.contains("게임") || name.contains("공원")) {
      return PlaceType.ACTIVITY;
    }

    return PlaceType.OTHER;
  }

  @Override
  public void onMapReady(@NonNull NaverMap naverMap) {
    this.naverMap = naverMap;
    if (useCoordinateSearch) {
      moveCameraToRequestedLocation();
    }
    if (!courseList.isEmpty()) {
      changeMapToCourse(courseList.get(0));
    }
  }

  private void moveCameraToRequestedLocation() {
    if (naverMap == null) {
      return;
    }

    CameraUpdate cameraUpdate =
        CameraUpdate.toCameraPosition(
                new CameraPosition(new LatLng(latitude, longitude), DEFAULT_ZOOM))
            .animate(CameraAnimation.Easing);
    naverMap.moveCamera(cameraUpdate);
  }

  private void changeMapToCourse(Course course) {
    if (naverMap == null) {
      return;
    }
    selectedCourse = course;

    for (Marker marker : activeMarkers) {
      marker.setMap(null);
    }
    activeMarkers.clear();

    if (currentPath != null) {
      currentPath.setMap(null);
      currentPath = null;
    }

    CourseDatabase.databaseExecutor.execute(
        () -> {
          List<CoursePlace> placesInCourse =
              database.coursePlaceDao().getPlacesByCourseId(course.courseId);
          runOnUiThread(
              () -> {
                List<LatLng> pathCoords = new ArrayList<>();
                for (CoursePlace place : placesInCourse) {
                  LatLng position = new LatLng(place.latitude, place.longitude);
                  Marker marker = new Marker();
                  marker.setPosition(position);
                  marker.setCaptionText(place.placeName);
                  marker.setMap(naverMap);
                  activeMarkers.add(marker);
                  pathCoords.add(position);
                }

                if (pathCoords.size() >= 2) {
                  currentPath = new PathOverlay();
                  currentPath.setCoords(pathCoords);
                  currentPath.setColor(Color.parseColor("#FF6699"));
                  currentPath.setWidth(12);
                  currentPath.setOutlineWidth(0);
                  currentPath.setMap(naverMap);
                }

                if (!placesInCourse.isEmpty()) {
                  CoursePlace firstPlace = placesInCourse.get(0);
                  CameraUpdate cameraUpdate =
                      CameraUpdate.scrollTo(new LatLng(firstPlace.latitude, firstPlace.longitude))
                          .animate(CameraAnimation.Easing);
                  naverMap.moveCamera(cameraUpdate);
                } else if (useCoordinateSearch) {
                  moveCameraToRequestedLocation();
                }
              });
        });
  }

  private static class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private final List<Course> items;
    private final OnItemClickListener listener;

    interface OnItemClickListener {
      void onItemClick(Course course);
    }

    CourseAdapter(List<Course> items, OnItemClickListener listener) {
      this.items = items;
      this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view =
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.item_course_card, parent, false);
      return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      Course course = items.get(position);
      TextView titleView = holder.itemView.findViewById(R.id.cc_title);
      if (titleView != null) {
        titleView.setText(course.title);
      }
      holder.itemView.setOnClickListener(
          v -> {
            if (listener != null) {
              listener.onItemClick(course);
            }
          });
    }

    @Override
    public int getItemCount() {
      return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
      ViewHolder(View itemView) {
        super(itemView);
      }
    }
  }
}
