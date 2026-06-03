package com.example.day2day.presentation.recommend.flow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.day2day.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.Locale;

public class FilteringActivity extends AppCompatActivity {

  private static final double DEFAULT_LATITUDE = 37.5666102;
  private static final double DEFAULT_LONGITUDE = 126.9783881;

  private static final int[] MOOD_CHIP_IDS = {
    R.id.chip_anniversary,
    R.id.chip_quiet,
    R.id.chip_budget,
    R.id.chip_active,
    R.id.chip_atmosphere,
    R.id.chip_rainy,
    R.id.chip_insta,
    R.id.chip_wide_seat,
    R.id.chip_pet
  };

  private TextInputEditText locationInput;
  private ChipGroup chipGroupMood;
  private RadioGroup sortRadioGroup;
  private FusedLocationProviderClient fusedLocationClient;

  private String districtName;
  private boolean hasCurrentLocation;
  private boolean useCoordinateSearch;
  private boolean isCustomCoordinate;
  private double currentLatitude = DEFAULT_LATITUDE;
  private double currentLongitude = DEFAULT_LONGITUDE;
  private double selectedLatitude = DEFAULT_LATITUDE;
  private double selectedLongitude = DEFAULT_LONGITUDE;

  private final ActivityResultLauncher<Intent> locationPickerLauncher =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            if (result.getResultCode() != RESULT_OK || result.getData() == null) {
              return;
            }

            selectedLatitude =
                result
                    .getData()
                    .getDoubleExtra(RecommendFlowContract.EXTRA_LATITUDE, selectedLatitude);
            selectedLongitude =
                result
                    .getData()
                    .getDoubleExtra(RecommendFlowContract.EXTRA_LONGITUDE, selectedLongitude);
            useCoordinateSearch = true;
            isCustomCoordinate = true;
            updateLocationUi();
          });

  private final ActivityResultLauncher<String[]> locationPermissionLauncher =
      registerForActivityResult(
          new ActivityResultContracts.RequestMultiplePermissions(),
          result -> {
            boolean granted =
                Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                    || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
            if (granted) {
              fetchCurrentLocationForFilter();
            } else {
              Toast.makeText(this, "현재 위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
          });

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_filtering);

    locationInput = findViewById(R.id.et_location);
    chipGroupMood = findViewById(R.id.chip_group_mood);
    sortRadioGroup = findViewById(R.id.rg_sort);
    applyMoodChipStateColors();
    TextInputLayout locationInputLayout = findViewById(R.id.til_location);
    MaterialButton nextButton = findViewById(R.id.btn_filtering_next);
    MaterialButton useCurrentLocationButton = findViewById(R.id.btn_use_current_location);
    MaterialButton adjustLocationButton = findViewById(R.id.btn_adjust_location_on_map);

    findViewById(R.id.btn_close_filter).setOnClickListener(v -> finish());
    findViewById(R.id.btn_reset_filter).setOnClickListener(v -> resetFilters());

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    districtName = getIntent().getStringExtra("district_name");

    if (getIntent().hasExtra(RecommendFlowContract.EXTRA_LATITUDE)
        && getIntent().hasExtra(RecommendFlowContract.EXTRA_LONGITUDE)) {
      currentLatitude =
          getIntent().getDoubleExtra(RecommendFlowContract.EXTRA_LATITUDE, DEFAULT_LATITUDE);
      currentLongitude =
          getIntent().getDoubleExtra(RecommendFlowContract.EXTRA_LONGITUDE, DEFAULT_LONGITUDE);
      selectedLatitude = currentLatitude;
      selectedLongitude = currentLongitude;
      hasCurrentLocation = true;
      useCoordinateSearch = true;
    }

    updateLocationUi();

    useCurrentLocationButton.setOnClickListener(v -> useCurrentLocation());
    adjustLocationButton.setOnClickListener(v -> openStartLocationPicker());
    locationInputLayout.setEndIconOnClickListener(v -> openStartLocationPicker());

    nextButton.setOnClickListener(
        v -> {
          ArrayList<String> selectedMoods = collectSelectedMoods();

          Intent intent = new Intent(FilteringActivity.this, CourseMapPageActivity.class);
          intent.putExtra("FILTER_KEYWORD", resolveSearchKeyword());
          intent.putExtra(RecommendFlowContract.EXTRA_LOCATION_LABEL, resolveLocationLabel());
          intent.putStringArrayListExtra("FILTER_MOODS", selectedMoods);
          intent.putExtra("FILTER_SORT", resolveSortOrder());
          intent.putExtra(RecommendFlowContract.EXTRA_USE_COORDINATE_SEARCH, useCoordinateSearch);

          if (useCoordinateSearch) {
            intent.putExtra(RecommendFlowContract.EXTRA_LATITUDE, selectedLatitude);
            intent.putExtra(RecommendFlowContract.EXTRA_LONGITUDE, selectedLongitude);
          }

          startActivity(intent);
        });
  }

  private void resetFilters() {
    for (int chipId : MOOD_CHIP_IDS) {
      Chip chip = chipGroupMood.findViewById(chipId);
      if (chip != null) {
        chip.setChecked(false);
      }
    }

    sortRadioGroup.check(R.id.rb_recommended);

    if (hasCurrentLocation) {
      selectedLatitude = currentLatitude;
      selectedLongitude = currentLongitude;
      useCoordinateSearch = true;
      isCustomCoordinate = false;
    } else {
      useCoordinateSearch = false;
      isCustomCoordinate = false;
    }

    updateLocationUi();
  }

  private void applyMoodChipStateColors() {
    int[][] states = {new int[] {android.R.attr.state_checked}, new int[] {}};
    ColorStateList backgroundColors =
        new ColorStateList(
            states,
            new int[] {
              ContextCompat.getColor(this, R.color.rose_light),
              ContextCompat.getColor(this, R.color.white)
            });
    ColorStateList textColors =
        new ColorStateList(
            states,
            new int[] {
              ContextCompat.getColor(this, R.color.rose),
              ContextCompat.getColor(this, R.color.text_dark)
            });
    ColorStateList strokeColors =
        new ColorStateList(
            states,
            new int[] {
              ContextCompat.getColor(this, R.color.rose),
              ContextCompat.getColor(this, R.color.border_color)
            });

    for (int chipId : MOOD_CHIP_IDS) {
      Chip chip = chipGroupMood.findViewById(chipId);
      if (chip == null) {
        continue;
      }
      chip.setChipBackgroundColor(backgroundColors);
      chip.setTextColor(textColors);
      chip.setChipStrokeColor(strokeColors);
    }
  }

  private void useCurrentLocation() {
    if (hasCurrentLocation) {
      selectedLatitude = currentLatitude;
      selectedLongitude = currentLongitude;
      useCoordinateSearch = true;
      isCustomCoordinate = false;
      updateLocationUi();
      return;
    }

    if (hasLocationPermission()) {
      fetchCurrentLocationForFilter();
      return;
    }

    locationPermissionLauncher.launch(
        new String[] {
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        });
  }

  private void openStartLocationPicker() {
    Intent intent = new Intent(this, StartLocationPickerActivity.class);
    double baseLatitude = useCoordinateSearch ? selectedLatitude : currentLatitude;
    double baseLongitude = useCoordinateSearch ? selectedLongitude : currentLongitude;
    intent.putExtra(RecommendFlowContract.EXTRA_LATITUDE, baseLatitude);
    intent.putExtra(RecommendFlowContract.EXTRA_LONGITUDE, baseLongitude);
    locationPickerLauncher.launch(intent);
  }

  private void updateLocationUi() {
    if (useCoordinateSearch) {
      locationInput.setFocusable(false);
      locationInput.setFocusableInTouchMode(false);
      locationInput.setCursorVisible(false);
      locationInput.setText(
          String.format(Locale.KOREA, "위도 %.5f / 경도 %.5f", selectedLatitude, selectedLongitude));
      return;
    }

    locationInput.setFocusable(true);
    locationInput.setFocusableInTouchMode(true);
    locationInput.setCursorVisible(true);

    if (districtName != null && !districtName.isEmpty()) {
      locationInput.setText("서울시 " + districtName);
      return;
    }

    locationInput.setText("");
  }

  private String resolveSearchKeyword() {
    if (useCoordinateSearch) {
      return resolveLocationLabel();
    }

    if (locationInput.getText() != null) {
      String value = locationInput.getText().toString().trim();
      if (!value.isEmpty()) {
        return value;
      }
    }

    if (districtName != null && !districtName.isEmpty()) {
      return "서울시 " + districtName;
    }

    return "서울 데이트";
  }

  private String resolveLocationLabel() {
    if (!useCoordinateSearch) {
      return resolveSearchKeyword();
    }
    return isCustomCoordinate ? "선택한 위치" : "현재 위치";
  }

  private ArrayList<String> collectSelectedMoods() {
    ArrayList<String> selectedMoods = new ArrayList<>();
    for (int chipId : MOOD_CHIP_IDS) {
      Chip chip = chipGroupMood.findViewById(chipId);
      if (chip != null && chip.isChecked()) {
        selectedMoods.add(chip.getText().toString());
      }
    }
    return selectedMoods;
  }

  private String resolveSortOrder() {
    int checkedId = sortRadioGroup.getCheckedRadioButtonId();
    if (checkedId == R.id.rb_distance) {
      return "distance";
    }
    if (checkedId == R.id.rb_popular) {
      return "popular";
    }
    return "recommended";
  }

  private boolean hasLocationPermission() {
    return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED;
  }

  @SuppressLint("MissingPermission")
  private void fetchCurrentLocationForFilter() {
    CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    fusedLocationClient
        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
        .addOnSuccessListener(
            location -> {
              if (location != null) {
                applyCurrentLocation(location.getLatitude(), location.getLongitude());
                return;
              }
              fetchLastKnownLocation();
            })
        .addOnFailureListener(ignored -> fetchLastKnownLocation());
  }

  @SuppressLint("MissingPermission")
  private void fetchLastKnownLocation() {
    fusedLocationClient
        .getLastLocation()
        .addOnSuccessListener(
            location -> {
              if (location == null) {
                Toast.makeText(this, "현재 위치를 찾지 못했어요.", Toast.LENGTH_SHORT).show();
                return;
              }
              applyCurrentLocation(location.getLatitude(), location.getLongitude());
            })
        .addOnFailureListener(
            ignored -> Toast.makeText(this, "현재 위치를 찾지 못했어요.", Toast.LENGTH_SHORT).show());
  }

  private void applyCurrentLocation(double latitude, double longitude) {
    currentLatitude = latitude;
    currentLongitude = longitude;
    selectedLatitude = latitude;
    selectedLongitude = longitude;
    hasCurrentLocation = true;
    useCoordinateSearch = true;
    isCustomCoordinate = false;
    updateLocationUi();
  }
}
