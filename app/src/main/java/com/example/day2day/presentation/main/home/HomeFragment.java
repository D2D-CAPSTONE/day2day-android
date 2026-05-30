package com.example.day2day.presentation.main.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import com.example.day2day.R;
import com.example.day2day.data.CourseContract;
import com.example.day2day.data.local.CourseDatabase;
import com.example.day2day.data.local.CourseSeedData;
import com.example.day2day.data.local.entity.Course;
import com.example.day2day.data.remote.WeatherEngine;
import com.example.day2day.data.remote.WeatherResponse;
import com.example.day2day.presentation.common.CourseCardHelper;
import com.example.day2day.presentation.recommend.flow.CourseDetailPageActivity;
import com.example.day2day.presentation.recommend.flow.MapPageActivity;
import com.example.day2day.presentation.recommend.flow.MapSelectionActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

  private static final int PAGE_SIZE = 10;
  private int currentOffset = 0;
  private boolean hasMore = false;
  private boolean isLoading = true;
  private LinearLayout courseList;
  private View loadMoreView;

  private FusedLocationProviderClient fusedLocationClient;
  private FrameLayout weatherBanner;
  private ImageView ivWeatherIcon;
  private ImageView ivWeatherIconMini;
  private TextView tvWeatherTemp;
  private TextView tvWeatherTempMini;
  private TextView tvWeatherDesc;
  private TextView tvWeatherDescMini;
  private TextView tvWeatherLocation;
  private TextView tvWeatherTag1;
  private TextView tvWeatherTag2;
  private TextView tvWeatherTag3;
  private TextView tvWeatherTag4;

  private final ActivityResultLauncher<String[]> locationPermissionLauncher =
      registerForActivityResult(
          new ActivityResultContracts.RequestMultiplePermissions(),
          result -> {
            if (!isAdded()) return;
            boolean granted =
                Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                    || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
            if (granted) {
              fetchLocation();
            } else {
              fetchWeatherForCoords(37.5665, 126.9780, "서울");
            }
          });

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    view.findViewById(R.id.btn_nearby_course)
        .setOnClickListener(
            v -> startActivity(new Intent(requireContext(), MapPageActivity.class)));
    view.findViewById(R.id.btn_location_course)
        .setOnClickListener(
            v -> startActivity(new Intent(requireContext(), MapSelectionActivity.class)));

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    bindWeatherViews(view);
    loadWeather();
    bindDebugWeatherButtons(view);
    renderCourseCards(view);
  }

  private void bindWeatherViews(View view) {
    weatherBanner = view.findViewById(R.id.weather_banner);
    ivWeatherIcon = view.findViewById(R.id.iv_weather_icon);
    ivWeatherIconMini = view.findViewById(R.id.iv_weather_icon_mini);
    tvWeatherTemp = view.findViewById(R.id.tv_weather_temp);
    tvWeatherTempMini = view.findViewById(R.id.tv_weather_temp_mini);
    tvWeatherDesc = view.findViewById(R.id.tv_weather_desc);
    tvWeatherDescMini = view.findViewById(R.id.tv_weather_desc_mini);
    tvWeatherLocation = view.findViewById(R.id.tv_weather_location);
    tvWeatherTag1 = view.findViewById(R.id.tv_weather_tag1);
    tvWeatherTag2 = view.findViewById(R.id.tv_weather_tag2);
    tvWeatherTag3 = view.findViewById(R.id.tv_weather_tag3);
    tvWeatherTag4 = view.findViewById(R.id.tv_weather_tag4);
  }

  private void loadWeather() {
    boolean hasFine =
        ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED;
    boolean hasCoarse =
        ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED;
    if (hasFine || hasCoarse) {
      fetchLocation();
    } else {
      locationPermissionLauncher.launch(
          new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
          });
    }
  }

  @SuppressLint("MissingPermission")
  private void fetchLocation() {
    fusedLocationClient
        .getLastLocation()
        .addOnSuccessListener(
            location -> {
              if (!isAdded()) return;
              if (location != null) {
                fetchKoreanCityAndWeather(location.getLatitude(), location.getLongitude());
              } else {
                fetchWeatherForCoords(37.5665, 126.9780, "서울");
              }
            })
        .addOnFailureListener(
            e -> {
              if (isAdded()) fetchWeatherForCoords(37.5665, 126.9780, "서울");
            });
  }

  private void fetchKoreanCityAndWeather(double lat, double lon) {
    CourseDatabase.databaseExecutor.execute(
        () -> {
          String city = "서울";
          try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.KOREAN);
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
              String locality = addresses.get(0).getLocality();
              if (locality != null) city = locality;
            }
          } catch (IOException e) {
            android.util.Log.w("HomeFragment", "Geocoder 실패: " + e.getMessage());
          }
          final String koreanCity = city;
          if (isAdded()) fetchWeatherForCoords(lat, lon, koreanCity);
        });
  }

  private void fetchWeatherForCoords(double lat, double lon, String koreanCity) {
    new WeatherEngine()
        .fetchWeather(
            lat,
            lon,
            new WeatherEngine.FetchWeatherCallback() {
              @Override
              public void onSuccess(WeatherResponse weather) {
                if (!isAdded()) return;
                requireActivity()
                    .runOnUiThread(
                        () -> {
                          if (!isAdded()) return;
                          updateWeatherBanner(weather, koreanCity);
                        });
              }

              @Override
              public void onFailure(String error) {
                android.util.Log.w("HomeFragment", "날씨 로드 실패: " + error);
              }
            });
  }

  private void updateWeatherBanner(WeatherResponse weather, String koreanCity) {
    String temp = Math.round(weather.getTemp()) + "°";
    String desc =
        weather.getDescription() != null ? weather.getDescription() : weather.getWeatherMain();
    String weatherMain = weather.getWeatherMain();

    if (tvWeatherTemp != null) tvWeatherTemp.setText(temp);
    if (tvWeatherTempMini != null) tvWeatherTempMini.setText(temp);
    if (tvWeatherDesc != null) tvWeatherDesc.setText(desc);
    if (tvWeatherLocation != null) tvWeatherLocation.setText(koreanCity);
    if (tvWeatherDescMini != null) tvWeatherDescMini.setText(desc + " · " + koreanCity);

    updateWeatherTags(weatherMain);
    updateWeatherVisuals(weatherMain);
  }

  private void bindDebugWeatherButtons(View view) {
    view.findViewById(R.id.dbg_btn_clear).setOnClickListener(v -> applyDebugWeather("Clear"));
    view.findViewById(R.id.dbg_btn_clouds).setOnClickListener(v -> applyDebugWeather("Clouds"));
    view.findViewById(R.id.dbg_btn_rain).setOnClickListener(v -> applyDebugWeather("Rain"));
    view.findViewById(R.id.dbg_btn_snow).setOnClickListener(v -> applyDebugWeather("Snow"));
    view.findViewById(R.id.dbg_btn_storm)
        .setOnClickListener(v -> applyDebugWeather("Thunderstorm"));
  }

  private void applyDebugWeather(String weatherMain) {
    updateWeatherTags(weatherMain);
    updateWeatherVisuals(weatherMain);
  }

  private void updateWeatherTags(String weatherMain) {
    if (weatherMain == null) weatherMain = "";
    String[] tags;
    switch (weatherMain) {
      case "Clear":
        tags = new String[] {"활동적인", "인스타 감성", "분위기 좋은", "가성비"};
        break;
      case "Clouds":
        tags = new String[] {"분위기 좋은", "조용한", "인스타 감성", "가성비"};
        break;
      case "Rain":
      case "Drizzle":
        tags = new String[] {"비 오는 날", "분위기 좋은", "조용한", "인스타 감성"};
        break;
      case "Snow":
        tags = new String[] {"분위기 좋은", "기념일", "조용한", "인스타 감성"};
        break;
      case "Thunderstorm":
        tags = new String[] {"비 오는 날", "조용한", "분위기 좋은", "인스타 감성"};
        break;
      default:
        tags = new String[] {"분위기 좋은", "조용한", "인스타 감성", "가성비"};
    }
    if (tvWeatherTag1 != null) tvWeatherTag1.setText(tags[0]);
    if (tvWeatherTag2 != null) tvWeatherTag2.setText(tags[1]);
    if (tvWeatherTag3 != null) tvWeatherTag3.setText(tags[2]);
    if (tvWeatherTag4 != null) tvWeatherTag4.setText(tags[3]);
  }

  private void updateWeatherVisuals(String weatherMain) {
    if (weatherMain == null) weatherMain = "";
    int bannerBg, iconRes, iconMiniRes;
    switch (weatherMain) {
      case "Clear":
        bannerBg = R.drawable.bg_weather_banner;
        iconRes = R.drawable.ic_sun;
        iconMiniRes = R.drawable.ic_sun_small;
        break;
      case "Clouds":
        bannerBg = R.drawable.bg_weather_cloudy;
        iconRes = R.drawable.ic_cloud;
        iconMiniRes = R.drawable.ic_cloud_small;
        break;
      case "Rain":
      case "Drizzle":
        bannerBg = R.drawable.bg_weather_rainy;
        iconRes = R.drawable.ic_rain;
        iconMiniRes = R.drawable.ic_rain_small;
        break;
      case "Snow":
        bannerBg = R.drawable.bg_weather_snowy;
        iconRes = R.drawable.ic_snow;
        iconMiniRes = R.drawable.ic_snow_small;
        break;
      case "Thunderstorm":
        bannerBg = R.drawable.bg_weather_stormy;
        iconRes = R.drawable.ic_storm;
        iconMiniRes = R.drawable.ic_storm_small;
        break;
      default:
        bannerBg = R.drawable.bg_weather_banner;
        iconRes = R.drawable.ic_sun;
        iconMiniRes = R.drawable.ic_sun_small;
    }
    if (weatherBanner != null) weatherBanner.setBackgroundResource(bannerBg);
    if (ivWeatherIcon != null) ivWeatherIcon.setImageResource(iconRes);
    if (ivWeatherIconMini != null) ivWeatherIconMini.setImageResource(iconMiniRes);
  }

  private void renderCourseCards(View view) {
    courseList = view.findViewById(R.id.course_list);
    loadMoreView = view.findViewById(R.id.load_more);
    loadMoreView.setVisibility(View.GONE);

    NestedScrollView scrollBody = view.findViewById(R.id.scroll_body);
    scrollBody.setOnScrollChangeListener(
        (NestedScrollView.OnScrollChangeListener)
            (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
              if (!v.canScrollVertically(1) && hasMore && !isLoading) {
                isLoading = true;
                loadMoreView.setVisibility(View.VISIBLE);
                v.post(() -> v.fullScroll(View.FOCUS_DOWN));
                loadNextPage();
              }
            });

    loadCoursesFromDatabase();
  }

  private void loadCoursesFromDatabase() {
    CourseDatabase database = CourseDatabase.getInstance(requireContext());
    CourseDatabase.databaseExecutor.execute(
        () -> {
          if (database.courseDao().getCourseCount() == 0) {
            database.courseDao().insertCourses(CourseSeedData.getPopularCourses());
          }
          if (database.coursePlaceDao().getPlaceCount() == 0) {
            database.coursePlaceDao().insertPlaces(CourseSeedData.getCoursePlaces());
          }
          if (database.popularCourseDao().getPopularCourseCount() == 0) {
            database.popularCourseDao().insertPopularCourses(CourseSeedData.getPopularCourseIds());
          }

          List<Course> loaded = database.popularCourseDao().getPopularCoursesPaged(PAGE_SIZE, 0);
          if (!isAdded()) return;

          requireActivity()
              .runOnUiThread(
                  () -> {
                    if (!isAdded()) return;
                    courseList.removeAllViews();
                    currentOffset = loaded.size();
                    hasMore = loaded.size() == PAGE_SIZE;
                    isLoading = false;
                    appendCards(loaded);
                  });
        });
  }

  private void loadNextPage() {
    CourseDatabase database = CourseDatabase.getInstance(requireContext());
    CourseDatabase.databaseExecutor.execute(
        () -> {
          List<Course> loaded =
              database.popularCourseDao().getPopularCoursesPaged(PAGE_SIZE, currentOffset);
          if (!isAdded()) return;

          requireActivity()
              .runOnUiThread(
                  () -> {
                    if (!isAdded()) return;
                    currentOffset += loaded.size();
                    hasMore = loaded.size() == PAGE_SIZE;
                    isLoading = false;
                    loadMoreView.setVisibility(View.GONE);
                    appendCards(loaded);
                  });
        });
  }

  private void appendCards(List<Course> items) {
    for (Course item : items) {
      CourseCardHelper.addCard(
          requireContext(),
          item,
          courseList,
          v -> {
            Intent intent = new Intent(requireContext(), CourseDetailPageActivity.class);
            intent.putExtra(CourseContract.EXTRA_COURSE_ID, item.courseId);
            startActivity(intent);
          });
    }
  }
}
