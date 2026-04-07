package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.day2day.R;

public class FilteringActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_filtering);

    Button nextButton = findViewById(R.id.btn_filtering_next);

    nextButton.setOnClickListener(
        v -> startActivity(new Intent(FilteringActivity.this, CourseMapPageActivity.class)));
  }
}
