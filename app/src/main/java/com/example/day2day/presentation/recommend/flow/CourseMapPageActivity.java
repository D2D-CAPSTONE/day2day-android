package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.day2day.R;
import com.example.day2day.data.CourseContract;
import com.example.day2day.data.local.CourseDatabase;
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
import java.util.Collections;
import java.util.List;

public class CourseMapPageActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final double DEFAULT_LATITUDE = 37.5666102;
  private static final double DEFAULT_LONGITUDE = 126.9783881;
  private static final double DEFAULT_ZOOM = 15.5;
  private static final int[] COURSE_COLORS = {
    Color.parseColor("#FCE8EC"),
    Color.parseColor("#E7F4FF"),
    Color.parseColor("#FFF4D6"),
    Color.parseColor("#E9F7EF"),
    Color.parseColor("#FFF0E6")
  };

  private NaverMap naverMap;
  private final List<Marker> activeMarkers = new ArrayList<>();
  private final List<CourseDto> courses = new ArrayList<>();
  private PathOverlay currentPath;
  private CourseAdapter adapter;
  private TextView titleText;
  private View loadingOverlay;
  private ProgressBar loadingProgressBar;
  private TextView loadingMessageText;
  private Button actionButton;
  private double latitude = DEFAULT_LATITUDE;
  private double longitude = DEFAULT_LONGITUDE;
  private long generationSeed;
  private ArrayList<String> selectedKeywords = new ArrayList<>();
  private String courseOrder = RecommendFlowContract.COURSE_ORDER_ACTIVITY_FOOD_CAFE;
  private String sortMode = RecommendFlowContract.SORT_RECOMMENDED;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_course_map_page);

    latitude = getIntent().getDoubleExtra(RecommendFlowContract.EXTRA_LATITUDE, DEFAULT_LATITUDE);
    longitude =
        getIntent().getDoubleExtra(RecommendFlowContract.EXTRA_LONGITUDE, DEFAULT_LONGITUDE);
    generationSeed =
        getIntent()
            .getLongExtra(RecommendFlowContract.EXTRA_GENERATION_SEED, System.currentTimeMillis());

    ArrayList<String> extraKeywords =
        getIntent().getStringArrayListExtra(RecommendFlowContract.EXTRA_SELECTED_KEYWORDS);
    if (extraKeywords != null) {
      selectedKeywords = extraKeywords;
    }

    String extraCourseOrder = getIntent().getStringExtra(RecommendFlowContract.EXTRA_COURSE_ORDER);
    if (extraCourseOrder != null && !extraCourseOrder.isEmpty()) {
      courseOrder = extraCourseOrder;
    }

    String extraSortMode = getIntent().getStringExtra(RecommendFlowContract.EXTRA_SORT_MODE);
    if (extraSortMode != null && !extraSortMode.isEmpty()) {
      sortMode = extraSortMode;
    }

    View rootView = findViewById(android.R.id.content);
    actionButton = findViewById(R.id.btn_course_map_page_next);
    titleText = findViewById(R.id.tv_course_map_title);
    RecyclerView courseListView = findViewById(R.id.rv_course_list);
    loadingOverlay = findViewById(R.id.course_map_loading_overlay);
    loadingProgressBar = findViewById(R.id.pb_course_map_loading);
    loadingMessageText = findViewById(R.id.tv_course_map_loading);

    NavigationBarInsetHelper.applyBottomInset(rootView, actionButton);
    actionButton.setText("필터 다시 선택");
    actionButton.setOnClickListener(v -> finish());

    adapter = new CourseAdapter(courses, this::changeMapToCourse, this::openCourseDetail);
    courseListView.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    courseListView.setAdapter(adapter);

    titleText.setText("추천 코스");
    showLoading("필터를 바탕으로 추천 코스를 만들고 있습니다...");

    FragmentManager fragmentManager = getSupportFragmentManager();
    MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);
    if (mapFragment == null) {
      mapFragment = MapFragment.newInstance();
      fragmentManager.beginTransaction().add(R.id.map, mapFragment).commit();
    }
    mapFragment.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull NaverMap naverMap) {
    this.naverMap = naverMap;
    moveCameraToRequestedLocation();
    loadRecommendedCourses();
  }

  private void loadRecommendedCourses() {
    showLoading("필터를 바탕으로 추천 코스를 만들고 있습니다...");
    titleText.setText("추천 코스 생성 중...");
    courses.clear();
    adapter.notifyDataSetChanged();
    resetMapOverlays();

    CourseDatabase.databaseExecutor.execute(
        () -> {
          List<CourseDto> loadedCourses;
          try {
            loadedCourses =
                NearbyCourseEngine.recommendCourses(
                    latitude, longitude, selectedKeywords, courseOrder, sortMode, generationSeed);
          } catch (Exception ignored) {
            loadedCourses = Collections.emptyList();
          }

          List<CourseDto> finalCourses = loadedCourses;

          runOnUiThread(
              () -> {
                if (isFinishing() || isDestroyed()) {
                  return;
                }

                courses.clear();
                courses.addAll(finalCourses);
                adapter.notifyDataSetChanged();
                hideLoading();

                if (!courses.isEmpty()) {
                  titleText.setText("내 주변 추천 코스 " + courses.size() + "가지");
                  changeMapToCourse(courses.get(0));
                  return;
                }

                titleText.setText("추천 코스를 찾지 못했어요");
                moveCameraToRequestedLocation();
                Toast.makeText(
                        this,
                        "예시 코스는 더 이상 보여주지 않아요. 실제 주변 장소로 코스를 못 만들면 다시 선택해 주세요.",
                        Toast.LENGTH_SHORT)
                    .show();
              });
        });
  }

  private void changeMapToCourse(CourseDto course) {
    if (naverMap == null || course == null || course.places == null) {
      return;
    }

    resetMapOverlays();

    List<LatLng> pathCoords = new ArrayList<>();

    for (int i = 0; i < course.places.size(); i++) {
      PlaceDto place = course.places.get(i);
      LatLng position = new LatLng(place.latitude, place.longitude);

      Marker marker = new Marker();
      marker.setPosition(position);
      marker.setCaptionText((i + 1) + ". " + place.placeName);
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

    if (!course.places.isEmpty()) {
      PlaceDto firstPlace = course.places.get(0);
      CameraUpdate cameraUpdate =
          CameraUpdate.scrollTo(new LatLng(firstPlace.latitude, firstPlace.longitude))
              .animate(CameraAnimation.Easing);
      naverMap.moveCamera(cameraUpdate);
    }
  }

  private void moveCameraToRequestedLocation() {
    if (naverMap == null) {
      return;
    }

    LatLng requestedPosition = new LatLng(latitude, longitude);
    CameraUpdate cameraUpdate =
        CameraUpdate.toCameraPosition(new CameraPosition(requestedPosition, DEFAULT_ZOOM))
            .animate(CameraAnimation.Easing);
    naverMap.moveCamera(cameraUpdate);
  }

  private void resetMapOverlays() {
    for (Marker marker : activeMarkers) {
      marker.setMap(null);
    }
    activeMarkers.clear();

    if (currentPath != null) {
      currentPath.setMap(null);
      currentPath = null;
    }
  }

  private void showLoading(String message) {
    loadingOverlay.setVisibility(View.VISIBLE);
    loadingProgressBar.setVisibility(View.VISIBLE);
    loadingMessageText.setText(message);
    actionButton.setEnabled(false);
    actionButton.setAlpha(0.6f);
  }

  private void hideLoading() {
    loadingOverlay.setVisibility(View.GONE);
    actionButton.setEnabled(true);
    actionButton.setAlpha(1f);
  }

  private void openCourseDetail(CourseDto course) {
    if (course == null) {
      return;
    }

    Intent intent = new Intent(this, CourseDetailPageActivity.class);
    intent.putExtra(CourseContract.EXTRA_RECOMMENDED_COURSE, course);
    startActivity(intent);
  }

  private static class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private final List<CourseDto> items;
    private final OnItemClickListener previewListener;
    private final OnItemClickListener detailListener;

    interface OnItemClickListener {
      void onItemClick(CourseDto course);
    }

    CourseAdapter(
        List<CourseDto> items,
        OnItemClickListener previewListener,
        OnItemClickListener detailListener) {
      this.items = items;
      this.previewListener = previewListener;
      this.detailListener = detailListener;
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

      holder.thumbView.setBackgroundColor(COURSE_COLORS[position % COURSE_COLORS.length]);
      holder.titleView.setText(course.courseName);
      holder.badgeView.setText(
          course.badgeText == null || course.badgeText.isEmpty() ? "3곳" : course.badgeText);

      bindRoute(holder.routeLayout, holder.itemView, course.places);
      bindTags(holder.tagsLayout, holder.itemView, course.tags);
      holder.detailView.setVisibility(View.VISIBLE);
      holder.detailView.setOnClickListener(
          v -> {
            if (detailListener != null) {
              detailListener.onItemClick(course);
            }
          });

      holder.itemView.setOnClickListener(
          v -> {
            if (previewListener != null) {
              previewListener.onItemClick(course);
            }
          });
    }

    private void bindRoute(LinearLayout routeLayout, View rootView, List<PlaceDto> places) {
      routeLayout.removeAllViews();
      for (int i = 0; i < places.size(); i++) {
        TextView placeText = new TextView(rootView.getContext());
        placeText.setText(places.get(i).placeName);
        placeText.setTextSize(9);
        placeText.setTextColor(ContextCompat.getColor(rootView.getContext(), R.color.text_medium));
        routeLayout.addView(placeText);

        if (i < places.size() - 1) {
          TextView arrow = new TextView(rootView.getContext());
          arrow.setText("  >  ");
          arrow.setTextSize(9);
          arrow.setTextColor(ContextCompat.getColor(rootView.getContext(), R.color.text_light));
          routeLayout.addView(arrow);
        }
      }
    }

    private void bindTags(LinearLayout tagsLayout, View rootView, List<String> tags) {
      tagsLayout.removeAllViews();
      List<String> safeTags = tags == null ? Collections.emptyList() : tags;

      float density = rootView.getResources().getDisplayMetrics().density;
      int horizontalPadding = (int) (7 * density);
      int verticalPadding = (int) (2 * density);
      int marginEnd = (int) (4 * density);

      for (int i = 0; i < safeTags.size() && i < 3; i++) {
        TextView tagView = new TextView(rootView.getContext());
        tagView.setText(safeTags.get(i));
        tagView.setTextSize(9);
        tagView.setTextColor(ContextCompat.getColor(rootView.getContext(), R.color.rose));
        tagView.setBackground(
            ContextCompat.getDrawable(rootView.getContext(), R.drawable.shape_tag_rose));
        tagView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(marginEnd);
        tagsLayout.addView(tagView, params);
      }
    }

    @Override
    public int getItemCount() {
      return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
      private final View thumbView;
      private final TextView titleView;
      private final LinearLayout routeLayout;
      private final LinearLayout tagsLayout;
      private final TextView badgeView;
      private final TextView detailView;

      ViewHolder(View itemView) {
        super(itemView);
        thumbView = itemView.findViewById(R.id.cc_thumb);
        titleView = itemView.findViewById(R.id.cc_title);
        routeLayout = itemView.findViewById(R.id.cc_route);
        tagsLayout = itemView.findViewById(R.id.cc_tags);
        badgeView = itemView.findViewById(R.id.cc_rating);
        detailView = itemView.findViewById(R.id.cc_action);
      }
    }
  }
}
