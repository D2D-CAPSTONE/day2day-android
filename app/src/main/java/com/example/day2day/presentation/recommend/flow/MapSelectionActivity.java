package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.example.day2day.R;

public class MapSelectionActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_map_selection);

    // Use the actual layout root for stable inset dispatch.
    View rootView = findViewById(R.id.root);
    Button nextButton = findViewById(R.id.btn_map_selection_confirm);
    Toolbar toolbar = findViewById(R.id.toolbar);

    // 상태바/노치(상단) + 네비게이션바/제스처바(하단) 인셋을 반영해서
    // 상단 아이콘(유심/카메라홀 등)에 툴바가 가려지지 않게 처리.
    ViewGroup.MarginLayoutParams nextLp =
        (ViewGroup.MarginLayoutParams) nextButton.getLayoutParams();
    final int baseBottomMargin = nextLp.bottomMargin;
    final int baseToolbarPaddingTop = toolbar.getPaddingTop();

    // Splash fullscreen flag can leak into next activities on some devices; force clear it here.
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
    rootView.post(
        () -> {
          WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(rootView);
          if (controller != null) {
            controller.show(WindowInsetsCompat.Type.statusBars());
            controller.show(WindowInsetsCompat.Type.navigationBars());
            // 밝은 상단 배경에서 상태바 아이콘(시간/배터리/통신)이 보이도록 어두운 아이콘 사용.
            controller.setAppearanceLightStatusBars(true);
          }
        });

    ViewCompat.setOnApplyWindowInsetsListener(
        rootView,
        (view, windowInsets) -> {
          Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
          Insets navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());

          // Top: Toolbar 내용을 status bar 아래로 내림
          toolbar.setPadding(
              toolbar.getPaddingLeft(),
              baseToolbarPaddingTop + statusBars.top,
              toolbar.getPaddingRight(),
              toolbar.getPaddingBottom());

          // Bottom: 하단 버튼을 제스처/네비게이션바 위로 올림
          ViewGroup.MarginLayoutParams params =
              (ViewGroup.MarginLayoutParams) nextButton.getLayoutParams();
          params.bottomMargin = baseBottomMargin + navigationBars.bottom;
          nextButton.setLayoutParams(params);

          return windowInsets;
        });
    ViewCompat.requestApplyInsets(rootView);

    nextButton.setOnClickListener(
        v -> startActivity(new Intent(MapSelectionActivity.this, MapPageActivity.class)));
  }
}
