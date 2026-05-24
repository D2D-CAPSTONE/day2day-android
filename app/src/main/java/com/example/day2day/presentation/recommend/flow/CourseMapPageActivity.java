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
import java.util.List;
import java.util.UUID;
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
    if (keyword == null || keyword.isEmpty()) {
      keyword = "강남역 데이트"; // 기본값
    }
    fetchCoursesFromServer(keyword);
  }

  private void fetchCoursesFromServer(String keyword) {
    courseEngine.fetchNearbyPlaces(
        keyword,
        new NearbyCourseEngine.FetchPlacesCallback() {
          @Override
          public void onSuccess(List<BackendPlaceResponse> places) {
            Log.d(TAG, "서버 통신 성공. 받은 장소 개수: " + places.size());
            saveCoursesToDatabase(keyword, places);
          }

          @Override
          public void onFailure(String errorMessage) {
            Log.e(TAG, "서버 통신 실패: " + errorMessage);
            runOnUiThread(
                () ->
                    Toast.makeText(CourseMapPageActivity.this, errorMessage, Toast.LENGTH_SHORT)
                        .show());
          }
        });
  }

  private void saveCoursesToDatabase(String keyword, List<BackendPlaceResponse> places) {
    List<BackendPlaceResponse> meals = new ArrayList<>();
    List<BackendPlaceResponse> cafes = new ArrayList<>();
    List<BackendPlaceResponse> activities = new ArrayList<>();

    Log.d(TAG, "--- 카테고리 분류 시작 ---");
    for (BackendPlaceResponse place : places) {
      PlaceType type = getPlaceType(place.getCategory());
      Log.d(TAG, "장소: " + place.getName() + " / 카테고리: " + place.getCategory() + " -> 분류: " + type);
      switch (type) {
        case MEAL:
          meals.add(place);
          break;
        case CAFE:
          cafes.add(place);
          break;
        case ACTIVITY:
          activities.add(place);
          break;
        default:
          break;
      }
    }
    Log.d(TAG, "--- 카테고리 분류 완료 ---");
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

  private PlaceType getPlaceType(String category) {
    if (category == null) return PlaceType.OTHER;
    if (category.contains("한식")
        || category.contains("일식")
        || category.contains("중식")
        || category.contains("양식")
        || category.contains("파스타")
        || category.contains("음식점")
        || category.contains("레스토랑")) {
      return PlaceType.MEAL;
    } else if (category.contains("카페") || category.contains("디저트")) {
      return PlaceType.CAFE;
    } else if (category.contains("영화관")
        || category.contains("전시")
        || category.contains("공원")
        || category.contains("오락")
        || category.contains("술집")
        || category.contains("PC방")) {
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