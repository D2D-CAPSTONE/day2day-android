package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.day2day.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class FilteringActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_filtering);

    TextView tvClose = findViewById(R.id.tv_filter_close);
    LinearLayout llReset = findViewById(R.id.ll_filter_reset);
    ChipGroup chipGroupMood = findViewById(R.id.chip_group_mood);
    RadioGroup rgSort = findViewById(R.id.rg_sort);
    View nextButton = findViewById(R.id.btn_filtering_next);

    tvClose.setOnClickListener(v -> finish());

    llReset.setOnClickListener(
        v -> {
          for (int i = 0; i < chipGroupMood.getChildCount(); i++) {
            View child = chipGroupMood.getChildAt(i);
            if (child instanceof Chip) {
              ((Chip) child).setChecked(false);
            }
          }
          rgSort.check(R.id.rb_recommended);
        });

    nextButton.setOnClickListener(
        v -> startActivity(new Intent(FilteringActivity.this, CourseMapPageActivity.class)));
  }
}
