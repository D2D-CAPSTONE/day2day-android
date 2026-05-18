package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.day2day.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class FilteringActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_filtering);

    Button nextButton = findViewById(R.id.btn_filtering_next);

    nextButton.setOnClickListener(
        v -> {
          Intent intent = new Intent(FilteringActivity.this, CourseMapPageActivity.class);
          String keyword = "연남동 데이트"; // 기본값

          ViewGroup scrollContent =
              (ViewGroup) ((ViewGroup) findViewById(R.id.filtering_scroll)).getChildAt(0);
          if (scrollContent != null && scrollContent.getChildCount() > 1) {
            try {
              ViewGroup cardView = (ViewGroup) scrollContent.getChildAt(1);
              TextInputLayout textInputLayout = (TextInputLayout) cardView.getChildAt(0);
              TextInputEditText editText = (TextInputEditText) textInputLayout.getEditText();
              if (editText != null
                  && editText.getText() != null
                  && !editText.getText().toString().isEmpty()) {
                keyword = editText.getText().toString();
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          intent.putExtra("FILTER_KEYWORD", keyword);
          startActivity(intent);
        });
  }
}
