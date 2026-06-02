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
  private NaverMap naverMap;
  private final List<Marker> activeMarkers = new ArrayList<>();
  private PathOverlay currentPath;
  private NearbyCourseEngine courseEngine;
  private CourseDatabase database;
  private CourseAdapter adapter;
  private final List<Course> courseList = new ArrayList<>();
  private Course selectedCourse;
  private ArrayList<String> selectedMoods;

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

    View rootView = findViewById(android.R.id.content);
    Button nextButton = findViewById(R.id.btn_course_map_page_next);
    NavigationBarInsetHelper.applyBottomInset(rootView, nextButton);
    nextButton.setOnClickListener(
        v -> {
          if (selectedCourse == null) {
            Toast.makeText(this, "코스를 선택해주세요.", Toast.LENGTH_SHORT).show();
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
      keyword = "강남역 데이트"; // 기본값
    }

    fetchCoursesIndividually(keyword);
  }

  private void fetchCoursesIndividually(String baseKeyword) {
    String baseDistrict = extractCoreDistrict(baseKeyword.replace(" 데이트", ""));
    Log.d(TAG, "원래 검색어: " + baseKeyword + " -> 정제된 검색어 베이스: " + baseDistrict);

    List<BackendPlaceResponse> allMeals = new ArrayList<>();
    List<BackendPlaceResponse> allCafes = new ArrayList<>();
    List<BackendPlaceResponse> allActivities = new ArrayList<>();

    List<String> activityKeywords = Arrays.asList("영화관", "PC방", "보드카페", "공원", "전시");
    int totalRequests = 2 + activityKeywords.size();
    AtomicInteger completedRequests = new AtomicInteger(0);

    // 1. 식당 검색
    courseEngine.fetchNearbyPlaces(
        baseDistrict + " 맛집",
        new NearbyCourseEngine.FetchPlacesCallback() {
          @Override
          public void onSuccess(List<BackendPlaceResponse> places) {
            Log.d(TAG, "맛집 검색 완료: " + places.size() + "개");
            allMeals.addAll(places);
            if (completedRequests.incrementAndGet() == totalRequests) {
              combineAndSave(baseKeyword, allMeals, allCafes, allActivities);
            }
          }

          @Override
          public void onFailure(String errorMessage) {
            Log.e(TAG, "맛집 검색 실패: " + errorMessage);
            if (completedRequests.incrementAndGet() == totalRequests) {
              combineAndSave(baseKeyword, allMeals, allCafes, allActivities);
            }
          }
        });

    // 2. 카페 검색
    courseEngine.fetchNearbyPlaces(
        baseDistrict + " 카페",
        new NearbyCourseEngine.FetchPlacesCallback() {
          @Override
          public void onSuccess(List<BackendPlaceResponse> places) {
            Log.d(TAG, "카페 검색 완료: " + places.size() + "개");
            allCafes.addAll(places);
            if (completedRequests.incrementAndGet() == totalRequests) {
              combineAndSave(baseKeyword, allMeals, allCafes, allActivities);
            }
          }

          @Override
          public void onFailure(String errorMessage) {
            Log.e(TAG, "카페 검색 실패: " + errorMessage);
            if (completedRequests.incrementAndGet() == totalRequests) {
              combineAndSave(baseKeyword, allMeals, allCafes, allActivities);
            }
          }
        });

    // 3. 여러 놀거리 카테고리 동시 검색
    for (String activityKeyword : activityKeywords) {
      courseEngine.fetchNearbyPlaces(
          baseDistrict + " " + activityKeyword,
          new NearbyCourseEngine.FetchPlacesCallback() {
            @Override
            public void onSuccess(List<BackendPlaceResponse> places) {
              Log.d(TAG, activityKeyword + " 검색 완료: " + places.size() + "개");
              allActivities.addAll(places);
              if (completedRequests.incrementAndGet() == totalRequests) {
                combineAndSave(baseKeyword, allMeals, allCafes, allActivities);
              }
            }

            @Override
            public void onFailure(String errorMessage) {
              Log.e(TAG, activityKeyword + " 검색 실패: " + errorMessage);
              if (completedRequests.incrementAndGet() == totalRequests) {
                combineAndSave(baseKeyword, allMeals, allCafes, allActivities);
              }
            }
          });
    }
  }

  private void combineAndSave(
      String keyword,
      List<BackendPlaceResponse> allMeals,
      List<BackendPlaceResponse> allCafes,
      List<BackendPlaceResponse> allActivities) {
    Log.d(TAG, "모든 개별 검색 완료. 조합 시작.");
    saveCoursesToDatabaseStrict(keyword, allMeals, allCafes, allActivities);
  }

  private String extractCoreDistrict(String fullAddress) {
    if (fullAddress == null || fullAddress.isEmpty()) return "강남역";
    String[] parts = fullAddress.trim().split("\\s+");
    if (parts.length == 0) return fullAddress;
    String lastPart = parts[parts.length - 1];
    String guPart = null;
    for (int i = parts.length - 1; i >= 0; i--) {
      if (parts[i].endsWith("구")) {
        guPart = parts[i];
        break;
      }
    }
    if (lastPart.endsWith("동")
        || lastPart.endsWith("역")
        || lastPart.endsWith("로")
        || lastPart.endsWith("길")) {
      return lastPart;
    } else if (guPart != null) {
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

    Log.d(TAG, "--- 최종 카테고리 필터링 완료 ---");
    Log.d(
        TAG,
        "분류 결과: 식사("
            + meals.size()
            + "개), 카페("
            + cafes.size()
            + "개), 놀거리("
            + activities.size()
            + "개)");

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

      String newCourseId = "API-" + UUID.randomUUID().toString();
      String courseTitle = keyword + " 추천 코스 " + (i + 1);
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
                  Double.parseDouble(place.getY()),
                  Double.parseDouble(place.getX()),
                  j));
        } catch (NumberFormatException e) {
          Log.e(TAG, "좌표 파싱 실패: " + place.getName(), e);
        }
      }
    }

    if (newCourses.isEmpty()) {
      Log.w(TAG, "DB에 저장할 코스가 없습니다.");
      return;
    }

    CourseDatabase.databaseExecutor.execute(
        () -> {
          database.courseDao().insertCourses(newCourses);
          database.coursePlaceDao().insertPlaces(newCoursePlaces);
          Log.d(TAG, newCourses.size() + "개의 코스와 " + newCoursePlaces.size() + "개의 장소를 DB에 저장했습니다.");

          runOnUiThread(
              () -> {
                courseList.clear();
                courseList.addAll(newCourses);
                adapter.notifyDataSetChanged();
                if (naverMap != null && !courseList.isEmpty()) {
                  changeMapToCourse(courseList.get(0));
                }
              });
        });
  }

  private List<BackendPlaceResponse> filterByCategory(
      List<BackendPlaceResponse> places, PlaceType type) {
    List<BackendPlaceResponse> categorized = new ArrayList<>();
    for (BackendPlaceResponse p : places) {
      PlaceType actualType = getPlaceType(p);
      if (actualType == type) {
        categorized.add(p);
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
        || category.contains("음식점")
        || category.contains("식당")
        || category.contains("고기")
        || category.contains("레스토랑")
        || category.contains("이탈리안")
        || category.contains("돈까스")
        || category.contains("냉면")
        || category.contains("해물")
        || category.contains("수제비")
        || category.contains("국밥")
        || category.contains("햄버거")) {
      return PlaceType.MEAL;
    } else if (category.contains("카페")
        || category.contains("디저트")
        || category.contains("커피")
        || category.contains("베이커리")
        || category.contains("다방")
        || category.contains("테마카페")) {
      return PlaceType.CAFE;
    } else if (category.contains("영화관")
        || category.contains("전시")
        || category.contains("공원")
        || category.contains("명소")
        || category.contains("오락")
        || category.contains("술집")
        || category.contains("주점")
        || category.contains("노래")
        || category.contains("게임")
        || category.contains("놀거리")
        || category.contains("보드카페")
        || category.contains("PC방")) {
      return PlaceType.ACTIVITY;
    }

    if (name.contains("식당") || name.contains("레스토랑") || name.contains("밥집")) {
      return PlaceType.MEAL;
    } else if (name.contains("카페") || name.contains("커피")) {
      return PlaceType.CAFE;
    } else if (name.contains("PC")
        || name.contains("노래")
        || name.contains("게임")
        || name.contains("공원")) {
      return PlaceType.ACTIVITY;
    }

    return PlaceType.OTHER;
  }

  @Override
  public void onMapReady(@NonNull NaverMap naverMap) {
    this.naverMap = naverMap;
    if (!courseList.isEmpty()) {
      changeMapToCourse(courseList.get(0));
    }
  }

  private void changeMapToCourse(Course course) {
    if (naverMap == null) return;
    this.selectedCourse = course;

    for (Marker marker : activeMarkers) {
      marker.setMap(null);
    }
    activeMarkers.clear();

    if (currentPath != null) {
      currentPath.setMap(null);
    }

    CourseDatabase.databaseExecutor.execute(
        () -> {
          List<CoursePlace> placesInCourse =
              database.coursePlaceDao().getPlacesByCourseId(course.courseId);
          runOnUiThread(
              () -> {
                List<LatLng> pathCords = new ArrayList<>();
                for (CoursePlace place : placesInCourse) {
                  LatLng position = new LatLng(place.latitude, place.longitude);
                  Marker newMarker = new Marker();
                  newMarker.setPosition(position);
                  newMarker.setCaptionText(place.placeName);
                  newMarker.setMap(naverMap);
                  activeMarkers.add(newMarker);
                  pathCords.add(position);
                }

                if (pathCords.size() >= 2) {
                  currentPath = new PathOverlay();
                  currentPath.setCoords(pathCords);
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
                }
              });
        });
  }

  private static class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private final List<Course> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
      void onItemClick(Course course);
    }

    public CourseAdapter(List<Course> items, OnItemClickListener listener) {
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

      TextView tvName = holder.itemView.findViewById(R.id.cc_title);
      if (tvName != null) {
        tvName.setText(course.title);
      }
      holder.itemView.setOnClickListener(
          v -> {
            if (listener != null) listener.onItemClick(course);
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
