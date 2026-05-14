package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.day2day.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilteringActivity extends AppCompatActivity {
  private static final double DEFAULT_LATITUDE = 37.5666102;
  private static final double DEFAULT_LONGITUDE = 126.9783881;

  private double initialLatitude = DEFAULT_LATITUDE;
  private double initialLongitude = DEFAULT_LONGITUDE;
  private double latitude = DEFAULT_LATITUDE;
  private double longitude = DEFAULT_LONGITUDE;

  private TextInputEditText locationInput;
  private TextView locationHintText;

  private final ActivityResultLauncher<Intent> startLocationPickerLauncher =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            if (result.getResultCode() != RESULT_OK || result.getData() == null) {
              return;
            }

            latitude =
                result.getData().getDoubleExtra(RecommendFlowContract.EXTRA_LATITUDE, latitude);
            longitude =
                result.getData().getDoubleExtra(RecommendFlowContract.EXTRA_LONGITUDE, longitude);
            configureLocationInput(true);
          });

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_filtering);

    initialLatitude =
        getIntent().getDoubleExtra(RecommendFlowContract.EXTRA_LATITUDE, DEFAULT_LATITUDE);
    initialLongitude =
        getIntent().getDoubleExtra(RecommendFlowContract.EXTRA_LONGITUDE, DEFAULT_LONGITUDE);
    latitude = initialLatitude;
    longitude = initialLongitude;

    TextView backButton = findViewById(R.id.tv_filtering_back);
    locationInput = findViewById(R.id.et_filtering_location);
    locationHintText = findViewById(R.id.tv_filtering_location_hint);
    Button useCurrentLocationButton = findViewById(R.id.btn_filtering_use_current_location);
    Button adjustOnMapButton = findViewById(R.id.btn_filtering_adjust_location_on_map);
    Button nextButton = findViewById(R.id.btn_filtering_next);

    backButton.setOnClickListener(v -> finish());
    configureLocationInput(false);
    locationInput.setOnClickListener(v -> openStartLocationPicker());
    useCurrentLocationButton.setOnClickListener(
        v -> {
          latitude = initialLatitude;
          longitude = initialLongitude;
          configureLocationInput(false);
        });
    adjustOnMapButton.setOnClickListener(v -> openStartLocationPicker());
    configureMoodChips();

    nextButton.setOnClickListener(
        v -> {
          Intent intent = new Intent(FilteringActivity.this, CourseMapPageActivity.class);
          intent.putExtra(RecommendFlowContract.EXTRA_LATITUDE, latitude);
          intent.putExtra(RecommendFlowContract.EXTRA_LONGITUDE, longitude);
          intent.putStringArrayListExtra(
              RecommendFlowContract.EXTRA_SELECTED_KEYWORDS,
              new ArrayList<>(collectSelectedKeywords()));
          intent.putExtra(RecommendFlowContract.EXTRA_SORT_MODE, resolveSortMode());
          intent.putExtra(RecommendFlowContract.EXTRA_GENERATION_SEED, System.currentTimeMillis());
          startActivity(intent);
        });
  }

  private void openStartLocationPicker() {
    Intent intent = new Intent(this, StartLocationPickerActivity.class);
    intent.putExtra(RecommendFlowContract.EXTRA_LATITUDE, latitude);
    intent.putExtra(RecommendFlowContract.EXTRA_LONGITUDE, longitude);
    startLocationPickerLauncher.launch(intent);
  }

  private void configureLocationInput(boolean isAdjustedOnMap) {
    locationInput.setText(String.format(Locale.KOREA, "위도 %.5f / 경도 %.5f", latitude, longitude));
    locationInput.setFocusable(false);
    locationInput.setClickable(true);
    locationInput.setCursorVisible(false);
    locationHintText.setText(
        isAdjustedOnMap ? "지도에서 조정한 시작 위치를 기준으로 추천해요." : "현재 위치 기준으로 추천해요. 필요하면 지도에서 직접 옮길 수 있어요.");
  }

  private void configureMoodChips() {
    ChipGroup chipGroup = findViewById(R.id.chip_group_mood);
    int[][] states = new int[][] {new int[] {android.R.attr.state_checked}, new int[] {}};
    ColorStateList backgroundColors =
        new ColorStateList(states, new int[] {Color.parseColor("#E8506A"), Color.WHITE});
    ColorStateList strokeColors =
        new ColorStateList(
            states, new int[] {Color.parseColor("#E8506A"), Color.parseColor("#D1D1D1")});
    ColorStateList textColors =
        new ColorStateList(states, new int[] {Color.WHITE, Color.parseColor("#2A1F2D")});

    for (int i = 0; i < chipGroup.getChildCount(); i++) {
      if (!(chipGroup.getChildAt(i) instanceof Chip)) {
        continue;
      }

      Chip chip = (Chip) chipGroup.getChildAt(i);
      chip.setCheckable(true);
      chip.setCheckedIconVisible(false);
      chip.setChipBackgroundColor(backgroundColors);
      chip.setChipStrokeColor(strokeColors);
      chip.setTextColor(textColors);
      chip.setChipStrokeWidth(1f);

      if (i == 4) {
        chip.setChecked(true);
      }
    }
  }

  private List<String> collectSelectedKeywords() {
    ChipGroup chipGroup = findViewById(R.id.chip_group_mood);
    List<String> selectedKeywords = new ArrayList<>();

    for (int i = 0; i < chipGroup.getChildCount(); i++) {
      if (!(chipGroup.getChildAt(i) instanceof Chip)) {
        continue;
      }

      Chip chip = (Chip) chipGroup.getChildAt(i);
      if (chip.isChecked()) {
        selectedKeywords.add(chip.getText().toString().trim());
      }
    }

    return selectedKeywords;
  }

  private String resolveSortMode() {
    int checkedId = ((RadioGroup) findViewById(R.id.rg_sort)).getCheckedRadioButtonId();
    if (checkedId == R.id.rb_distance) {
      return RecommendFlowContract.SORT_DISTANCE;
    }
    if (checkedId == R.id.rb_review) {
      return RecommendFlowContract.SORT_REVIEW;
    }
    return RecommendFlowContract.SORT_RECOMMENDED;
  }
}
