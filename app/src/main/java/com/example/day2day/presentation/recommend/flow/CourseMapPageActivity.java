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

public class CourseMapPageActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final String TAG = "CourseMapPageActivity";
  private NaverMap naverMap;
  private final List<Marker> activeMarkers = new ArrayList<>();
  private PathOverlay currentPath;
  private NearbyCourseEngine courseEngine;
  private CourseAdapter adapter;
  private final List<CourseDto> courseList = new ArrayList<>();
  private CourseDto selectedCourse;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_course_map_page);

    courseEngine = new NearbyCourseEngine();

    View rootView = findViewById(android.R.id.content);
    Button nextButton = findViewById(R.id.btn_course_map_page_next);
    NavigationBarInsetHelper.applyBottomInset(rootView, nextButton);
    nextButton.setOnClickListener(
        v -> {
          Intent intent = new Intent(CourseMapPageActivity.this, CourseDetailPageActivity.class);
          if (selectedCourse != null) {
            intent.putExtra("SELECTED_COURSE", selectedCourse);
          }
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
      keyword = "연남동 데이트";
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
            List<PlaceDto> validPlaces = new ArrayList<>();
            for (BackendPlaceResponse place : places) {
              if (place.getX() == null
                  || place.getX().isEmpty()
                  || place.getY() == null
                  || place.getY().isEmpty()) {
                Log.w(TAG, "좌표값이 없는 장소 데이터는 건너뜁니다: " + place.getName());
                continue;
              }

              try {
                double lat = Double.parseDouble(place.getY());
                double lng = Double.parseDouble(place.getX());
                validPlaces.add(new PlaceDto(place.getName(), lat, lng));
              } catch (NumberFormatException e) {
                Log.e(
                    TAG,
                    "좌표 파싱 실패: " + place.getName() + ", x=" + place.getX() + ", y=" + place.getY(),
                    e);
              }
            }

            if (!validPlaces.isEmpty()) {
              runOnUiThread(
                  () -> {
                    Log.d(TAG, "UI 업데이트 시작. 유효한 장소 개수: " + validPlaces.size());
                    courseList.clear();

                    int courseCount = 0;
                    for (int i = 0; i < validPlaces.size() && courseCount < 3; i += 3) {
                      int end = Math.min(i + 3, validPlaces.size());
                      List<PlaceDto> coursePlaces = new ArrayList<>(validPlaces.subList(i, end));
                      String courseName = keyword + " 추천 코스 " + (courseCount + 1);
                      courseList.add(new CourseDto(courseName, coursePlaces));
                      courseCount++;
                    }

                    adapter.notifyDataSetChanged();
                    if (naverMap != null && !courseList.isEmpty()) {
                      changeMapToCourse(courseList.get(0));
                    }
                  });
            } else {
              runOnUiThread(
                  () -> {
                    Log.w(TAG, "유효한 장소가 없어 UI에 표시할 내용이 없습니다.");
                    Toast.makeText(CourseMapPageActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT)
                        .show();
                  });
            }
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

  @Override
  public void onMapReady(@NonNull NaverMap naverMap) {
    this.naverMap = naverMap;

    if (!courseList.isEmpty()) {
      changeMapToCourse(courseList.get(0));
    }
  }

  private void changeMapToCourse(CourseDto course) {
    if (naverMap == null) return;
    this.selectedCourse = course;

    for (Marker marker : activeMarkers) {
      marker.setMap(null);
    }
    activeMarkers.clear();

    if (currentPath != null) {
      currentPath.setMap(null);
    }

    List<LatLng> pathCords = new ArrayList<>();

    for (PlaceDto place : course.places) {
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

    if (!course.places.isEmpty()) {
      PlaceDto firstPlace = course.places.get(0);
      CameraUpdate cameraUpdate =
          CameraUpdate.scrollTo(new LatLng(firstPlace.latitude, firstPlace.longitude))
              .animate(CameraAnimation.Easing);
      naverMap.moveCamera(cameraUpdate);
    }
  }

  private static class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private final List<CourseDto> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
      void onItemClick(CourseDto course);
    }

    public CourseAdapter(List<CourseDto> items, OnItemClickListener listener) {
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
      CourseDto course = items.get(position);

      TextView tvName = holder.itemView.findViewById(R.id.cc_title);
      if (tvName != null) {
        tvName.setText(course.courseName);
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
