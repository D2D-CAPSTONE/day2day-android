package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.day2day.R;
import com.example.day2day.presentation.common.NavigationBarInsetHelper;

public class FilteringActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtering);

        View rootView = findViewById(android.R.id.content);
        Button nextButton = findViewById(R.id.btn_filtering_next);
        NavigationBarInsetHelper.applyBottomInset(rootView, nextButton);

        nextButton.setOnClickListener(v ->
                startActivity(new Intent(FilteringActivity.this, CourseMapPageActivity.class)));
    }
}
