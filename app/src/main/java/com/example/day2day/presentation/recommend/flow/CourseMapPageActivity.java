package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.day2day.R;
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

  private NaverMap naverMap;
  private final List<Marker> activeMarkers = new ArrayList<>();
  private PathOverlay currentPath;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_course_map_page);

    View rootView = findViewById(android.R.id.content);
    Button nextButton = findViewById(R.id.btn_course_map_page_next);
    NavigationBarInsetHelper.applyBottomInset(rootView, nextButton);
    nextButton.setOnClickListener(
        v -> startActivity(new Intent(CourseMapPageActivity.this, CourseDetailPageActivity.class)));

    FragmentManager fm = getSupportFragmentManager();
    MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
    if (mapFragment == null) {
      mapFragment = MapFragment.newInstance();
      fm.beginTransaction().add(R.id.map, mapFragment).commit();
    }
    mapFragment.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull NaverMap naverMap) {
    this.naverMap = naverMap;
    List<CourseDto> courseList = fetchDummyCourses();

    RecyclerView rvCourseList = findViewById(R.id.rv_course_list);
    rvCourseList.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

    CourseAdapter adapter =
        new CourseAdapter(
            courseList,
                this::changeMapToCourse);
    rvCourseList.setAdapter(adapter);

    if (!courseList.isEmpty()) {
      changeMapToCourse(courseList.get(0));
    }
  }

  private void changeMapToCourse(CourseDto course) {
    if (naverMap == null) return;

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

  private List<CourseDto> fetchDummyCourses() {
    List<CourseDto> courses = new ArrayList<>();

    List<PlaceDto> yeonnamPlaces = new ArrayList<>();
    yeonnamPlaces.add(new PlaceDto("디어리스트 연남", 37.558384, 126.923055));
    yeonnamPlaces.add(new PlaceDto("라헬의부엌", 37.561569, 126.924827));
    yeonnamPlaces.add(new PlaceDto("인생네컷", 37.556209, 126.924003));
    courses.add(new CourseDto("연남동 데이트 코스", yeonnamPlaces));

    List<PlaceDto> hapjeongPlaces = new ArrayList<>();
    hapjeongPlaces.add(new PlaceDto("합정 옥동식", 37.5539, 126.9157));
    hapjeongPlaces.add(new PlaceDto("어반플랜트 합정", 37.5491176, 126.9150796));
    hapjeongPlaces.add(new PlaceDto("향수공방 아로마인드", 37.5501815, 126.9113645));
    courses.add(new CourseDto("합정 공방 데이트 코스", hapjeongPlaces));

    List<PlaceDto> hongdaePlaces = new ArrayList<>();
    hongdaePlaces.add(new PlaceDto("카카오프렌즈 홍대점", 37.5567, 126.9242));
    hongdaePlaces.add(new PlaceDto("빌라 더 다이닝 홍대본점", 37.5574, 126.9247));
    hongdaePlaces.add(new PlaceDto("피오니", 37.5500942974697, 126.919769002818));
    courses.add(new CourseDto("홍대 메인 스트릿 코스", hongdaePlaces));

    return courses;
  }
}
