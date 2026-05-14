package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.day2day.R;
import com.example.day2day.presentation.common.NavigationBarInsetHelper;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import java.util.Locale;

public class StartLocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {
  private static final double DEFAULT_LATITUDE = 37.5666102;
  private static final double DEFAULT_LONGITUDE = 126.9783881;
  private static final double DEFAULT_ZOOM = 16.0;

  private double initialLatitude = DEFAULT_LATITUDE;
  private double initialLongitude = DEFAULT_LONGITUDE;
  private double selectedLatitude = DEFAULT_LATITUDE;
  private double selectedLongitude = DEFAULT_LONGITUDE;

  private TextView coordinateTextView;
  private NaverMap naverMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_start_location_picker);

    initialLatitude =
        getIntent().getDoubleExtra(RecommendFlowContract.EXTRA_LATITUDE, DEFAULT_LATITUDE);
    initialLongitude =
        getIntent().getDoubleExtra(RecommendFlowContract.EXTRA_LONGITUDE, DEFAULT_LONGITUDE);
    selectedLatitude = initialLatitude;
    selectedLongitude = initialLongitude;

    TextView closeButton = findViewById(R.id.tv_start_location_close);
    TextView resetButton = findViewById(R.id.tv_start_location_reset);
    coordinateTextView = findViewById(R.id.tv_start_location_coordinates);
    Button confirmButton = findViewById(R.id.btn_start_location_confirm);

    NavigationBarInsetHelper.applyTopInset(
        findViewById(R.id.root_start_location_picker), findViewById(R.id.start_location_top_card));
    NavigationBarInsetHelper.applyBottomInset(
        findViewById(R.id.root_start_location_picker), confirmButton);

    closeButton.setOnClickListener(v -> finish());
    resetButton.setOnClickListener(
        v -> {
          selectedLatitude = initialLatitude;
          selectedLongitude = initialLongitude;
          moveCameraToSelectedLocation();
          updateCoordinateText();
        });
    confirmButton.setOnClickListener(v -> applySelectedLocation());

    updateCoordinateText();

    MapFragment mapFragment =
        (MapFragment) getSupportFragmentManager().findFragmentById(R.id.start_location_picker_map);
    if (mapFragment == null) {
      mapFragment = MapFragment.newInstance();
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.start_location_picker_map, mapFragment)
          .commit();
    }
    mapFragment.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull NaverMap naverMap) {
    this.naverMap = naverMap;
    naverMap.getUiSettings().setLocationButtonEnabled(false);
    naverMap.setOnMapClickListener(
        (pointF, latLng) -> {
          selectedLatitude = latLng.latitude;
          selectedLongitude = latLng.longitude;
          moveCameraToSelectedLocation();
        });
    naverMap.addOnCameraIdleListener(this::syncSelectedLocationFromCamera);

    moveCameraToSelectedLocation();
  }

  private void moveCameraToSelectedLocation() {
    if (naverMap == null) {
      return;
    }

    LatLng target = new LatLng(selectedLatitude, selectedLongitude);
    CameraPosition currentPosition = naverMap.getCameraPosition();
    double zoom = currentPosition == null ? DEFAULT_ZOOM : currentPosition.zoom;
    CameraUpdate cameraUpdate =
        CameraUpdate.toCameraPosition(new CameraPosition(target, zoom))
            .animate(CameraAnimation.Easing);
    naverMap.moveCamera(cameraUpdate);
  }

  private void syncSelectedLocationFromCamera() {
    if (naverMap == null) {
      return;
    }

    LatLng target = naverMap.getCameraPosition().target;
    selectedLatitude = target.latitude;
    selectedLongitude = target.longitude;
    updateCoordinateText();
  }

  private void updateCoordinateText() {
    coordinateTextView.setText(
        String.format(Locale.KOREA, "위도 %.5f / 경도 %.5f", selectedLatitude, selectedLongitude));
  }

  private void applySelectedLocation() {
    Intent result = new Intent();
    result.putExtra(RecommendFlowContract.EXTRA_LATITUDE, selectedLatitude);
    result.putExtra(RecommendFlowContract.EXTRA_LONGITUDE, selectedLongitude);
    setResult(RESULT_OK, result);
    finish();
  }
}
