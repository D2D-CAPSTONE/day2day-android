package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.day2day.R;
import com.example.day2day.presentation.common.NavigationBarInsetHelper;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.util.FusedLocationSource;

public class MapPageActivity extends AppCompatActivity implements OnMapReadyCallback {
  private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

  private FusedLocationSource locationSource;
  private NaverMap naverMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map_page);

    View rootView = findViewById(R.id.root_map_page);
    Button nextButton = findViewById(R.id.btn_map_page_next);
    NavigationBarInsetHelper.applyBottomInset(rootView, nextButton);

    nextButton.setOnClickListener(
        v -> startActivity(new Intent(MapPageActivity.this, FilteringActivity.class)));

    locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

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
    naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
      if (!locationSource.isActivated() && naverMap != null) {
        naverMap.setLocationTrackingMode(LocationTrackingMode.None);
      }
    }
  }
}
