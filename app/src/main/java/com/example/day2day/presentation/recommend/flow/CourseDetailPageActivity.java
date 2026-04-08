package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.day2day.R;

public class CourseDetailPageActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_course_detail_page);

    // daytoday 상단바 제거
    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }

    View goCourseMapButton = findViewById(R.id.btn_course_detail_go_course_map);

    goCourseMapButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(CourseDetailPageActivity.this, CourseMapPageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
          }
        });
  }
}
