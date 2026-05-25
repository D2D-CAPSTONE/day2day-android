package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import com.example.day2day.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;

public class FilteringActivity extends AppCompatActivity {

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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_filtering);

    TextInputEditText etLocation = findViewById(R.id.et_location);
    MaterialButton nextButton = findViewById(R.id.btn_filtering_next);
    View btnClose = findViewById(R.id.btn_close_filter);
    View btnReset = findViewById(R.id.btn_reset_filter);
    ChipGroup chipGroupMood = findViewById(R.id.chip_group_mood);
    RadioGroup rgSort = findViewById(R.id.rg_sort);

    btnClose.setOnClickListener(v -> finish());

    btnReset.setOnClickListener(v -> {
      for (int chipId : MOOD_CHIP_IDS) {
        Chip chip = chipGroupMood.findViewById(chipId);
        if (chip != null) chip.setChecked(false);
      }
      etLocation.setText("");
      rgSort.check(R.id.rb_recommended);
    });

    nextButton.setOnClickListener(v -> {
      // 위치 키워드
      String location = "연남동 데이트";
      if (etLocation.getText() != null && !etLocation.getText().toString().isEmpty()) {
        location = etLocation.getText().toString();
      }

      // 선택된 분위기 칩 수집
      ArrayList<String> selectedMoods = new ArrayList<>();
      for (int chipId : MOOD_CHIP_IDS) {
        Chip chip = chipGroupMood.findViewById(chipId);
        if (chip != null && chip.isChecked()) {
          selectedMoods.add(chip.getText().toString());
        }
      }

      // 정렬 기준
      String sortOrder;
      int checkedId = rgSort.getCheckedRadioButtonId();
      if (checkedId == R.id.rb_distance) {
        sortOrder = "distance";
      } else if (checkedId == R.id.rb_popular) {
        sortOrder = "popular";
      } else {
        sortOrder = "recommended";
      }

      Intent intent = new Intent(FilteringActivity.this, CourseMapPageActivity.class);
      intent.putExtra("FILTER_KEYWORD", location);
      intent.putStringArrayListExtra("FILTER_MOODS", selectedMoods);
      intent.putExtra("FILTER_SORT", sortOrder);
      startActivity(intent);
    });
  }
}
