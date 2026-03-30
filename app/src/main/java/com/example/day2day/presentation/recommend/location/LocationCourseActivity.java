package com.example.day2day.presentation.recommend.location;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.day2day.R;
import com.example.day2day.presentation.main.MainActivity;

public class LocationCourseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_course);

        Button goMainButton = findViewById(R.id.btn_location_go_main);
        goMainButton.setOnClickListener(v -> {
            Intent intent = new Intent(LocationCourseActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
