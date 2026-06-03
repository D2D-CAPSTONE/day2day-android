package com.example.day2day.presentation.recommend.flow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.example.day2day.R;
import com.example.day2day.presentation.common.NavigationBarInsetHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.util.FusedLocationSource;

public class MapPageActivity extends AppCompatActivity implements OnMapReadyCallback {
  private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
  private static final double DEFAULT_LATITUDE = 37.5666102;
  private static final double DEFAULT_LONGITUDE = 126.9783881;
  private static final long MIN_LOADING_VISIBLE_MS = 900L;

  private FusedLocationSource locationSource;
  private FusedLocationProviderClient fusedLocationClient;
  private NaverMap naverMap;
  private Button nextButton;
  private View loadingOverlay;
  private ProgressBar loadingProgressBar;
  private TextView loadingTextView;
  private double currentLatitude = DEFAULT_LATITUDE;
  private double currentLongitude = DEFAULT_LONGITUDE;
  private boolean hasResolvedCurrentLocation;
  private boolean isPermissionRequestInFlight;
  private long loadingShownAtMs;
  private long loadingToken;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map_page);

    View rootView = findViewById(R.id.root_map_page);
    nextButton = findViewById(R.id.btn_map_page_next);
    loadingOverlay = findViewById(R.id.map_page_loading_overlay);
    loadingProgressBar = findViewById(R.id.pb_map_page_loading);
    loadingTextView = findViewById(R.id.tv_map_page_loading);

    NavigationBarInsetHelper.applyBottomInset(rootView, nextButton);
    nextButton.setText("필터 고르기");
    nextButton.setOnClickListener(
        v -> {
          Intent intent = new Intent(MapPageActivity.this, FilteringActivity.class);
          intent.putExtra(RecommendFlowContract.EXTRA_LATITUDE, currentLatitude);
          intent.putExtra(RecommendFlowContract.EXTRA_LONGITUDE, currentLongitude);
          startActivity(intent);
        });

    locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    showLoading("현재 위치를 불러오는 중입니다...");
    requestCurrentLocation();

    MapFragment mapFragment =
        (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_page_map);
    if (mapFragment == null) {
      mapFragment = MapFragment.newInstance();
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.map_page_map, mapFragment)
          .commit();
    }
    mapFragment.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull NaverMap naverMap) {
    this.naverMap = naverMap;

    naverMap.setLocationSource(locationSource);
    naverMap.getUiSettings().setLocationButtonEnabled(true);
    naverMap.getLocationOverlay().setVisible(true);
    naverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);

    if (hasResolvedCurrentLocation) {
      moveCameraToCurrentLocation();
      hideLoading();
      return;
    }

    requestCurrentLocation();
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    isPermissionRequestInFlight = false;

    if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
      if (!locationSource.isActivated() && naverMap != null) {
        naverMap.setLocationTrackingMode(LocationTrackingMode.None);
      }

      if (hasLocationPermission()) {
        requestCurrentLocation();
      } else {
        showLocationUnavailable("위치 권한이 필요합니다. 권한을 허용해 주세요.");
      }
    }
  }

  private void requestCurrentLocation() {
    if (!hasLocationPermission()) {
      requestLocationPermission();
      return;
    }

    showLoading("현재 위치를 불러오는 중입니다...");
    requestPreciseCurrentLocation();
  }

  private boolean hasLocationPermission() {
    return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED;
  }

  private void requestLocationPermission() {
    if (isPermissionRequestInFlight) {
      return;
    }

    isPermissionRequestInFlight = true;
    showLoading("현재 위치 권한을 확인하는 중입니다...");
    ActivityCompat.requestPermissions(
        this,
        new String[] {
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        },
        LOCATION_PERMISSION_REQUEST_CODE);
  }

  @SuppressLint("MissingPermission")
  private void requestPreciseCurrentLocation() {
    CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    fusedLocationClient
        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
        .addOnSuccessListener(
            location -> {
              if (location != null) {
                updateCurrentLocation(location);
                return;
              }
              requestLastKnownLocation();
            })
        .addOnFailureListener(ignored -> requestLastKnownLocation());
  }

  @SuppressLint("MissingPermission")
  private void requestLastKnownLocation() {
    fusedLocationClient
        .getLastLocation()
        .addOnSuccessListener(
            location -> {
              if (location != null) {
                updateCurrentLocation(location);
                return;
              }
              showLocationUnavailable("현재 위치를 아직 찾지 못했어요. 잠시 후 다시 시도해 주세요.");
            })
        .addOnFailureListener(
            ignored -> showLocationUnavailable("현재 위치를 아직 찾지 못했어요. 잠시 후 다시 시도해 주세요."));
  }

  private void updateCurrentLocation(Location location) {
    currentLatitude = location.getLatitude();
    currentLongitude = location.getLongitude();
    hasResolvedCurrentLocation = true;

    if (naverMap == null) {
      showLoading("지도를 준비하는 중입니다...");
      return;
    }

    moveCameraToCurrentLocation();
    hideLoading();
  }

  private void moveCameraToCurrentLocation() {
    if (naverMap == null) {
      return;
    }

    LatLng currentPosition = new LatLng(currentLatitude, currentLongitude);
    CameraUpdate cameraUpdate =
        CameraUpdate.scrollTo(currentPosition).animate(CameraAnimation.Easing);
    naverMap.moveCamera(cameraUpdate);
    naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
  }

  private void showLoading(String message) {
    loadingToken++;
    loadingShownAtMs = SystemClock.elapsedRealtime();
    loadingOverlay.setVisibility(View.VISIBLE);
    loadingProgressBar.setVisibility(View.VISIBLE);
    loadingTextView.setText(message);
    nextButton.setEnabled(false);
    nextButton.setAlpha(0.6f);
  }

  private void hideLoading() {
    long currentToken = loadingToken;
    long elapsed = SystemClock.elapsedRealtime() - loadingShownAtMs;
    long delayMs = Math.max(0L, MIN_LOADING_VISIBLE_MS - elapsed);

    if (delayMs == 0L) {
      performHideLoading(currentToken);
      return;
    }

    loadingOverlay.postDelayed(() -> performHideLoading(currentToken), delayMs);
  }

  private void performHideLoading(long tokenAtRequest) {
    if (tokenAtRequest != loadingToken) {
      return;
    }

    loadingOverlay.setVisibility(View.GONE);
    nextButton.setEnabled(true);
    nextButton.setAlpha(1f);
  }

  private void showLocationUnavailable(String message) {
    loadingToken++;
    loadingOverlay.setVisibility(View.GONE);
    nextButton.setEnabled(false);
    nextButton.setAlpha(0.6f);
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }
}
