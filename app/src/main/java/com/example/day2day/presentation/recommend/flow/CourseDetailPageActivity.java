package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.day2day.R;
import com.example.day2day.presentation.common.NavigationBarInsetHelper;
import com.example.day2day.presentation.main.MainActivity;

public class CourseDetailPageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail_page);

        View rootView = findViewById(android.R.id.content);
        Button goHomeButton = findViewById(R.id.btn_course_detail_go_home);
        NavigationBarInsetHelper.applyBottomInset(rootView, goHomeButton);

        goHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(CourseDetailPageActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
