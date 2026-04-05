package com.example.day2day.presentation.start;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.example.day2day.R;
import com.example.day2day.presentation.main.MainActivity;

public class StartActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Splash theme uses fullscreen. Ensure status bar is visible again from here.
    WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
    WindowInsetsControllerCompat controller =
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
    controller.show(WindowInsetsCompat.Type.statusBars());
    controller.show(WindowInsetsCompat.Type.navigationBars());

    setContentView(R.layout.activity_start);

    View rootView = findViewById(android.R.id.content);
    Button goMainButton = findViewById(R.id.btn_go_main);
    ViewGroup.MarginLayoutParams buttonLayoutParams =
        (ViewGroup.MarginLayoutParams) goMainButton.getLayoutParams();
    final int baseBottomMargin = buttonLayoutParams.bottomMargin;

    ViewCompat.setOnApplyWindowInsetsListener(
        rootView,
        (view, windowInsets) -> {
          Insets navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
          ViewGroup.MarginLayoutParams params =
              (ViewGroup.MarginLayoutParams) goMainButton.getLayoutParams();
          params.bottomMargin = baseBottomMargin + navigationBars.bottom;
          goMainButton.setLayoutParams(params);
          return windowInsets;
        });
    ViewCompat.requestApplyInsets(rootView);

    goMainButton.setOnClickListener(
        v -> startActivity(new Intent(StartActivity.this, MainActivity.class)));
  }
}
