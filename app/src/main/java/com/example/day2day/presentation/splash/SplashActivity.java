package com.example.day2day.presentation.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.day2day.R;
import com.example.day2day.presentation.start.StartActivity;

public class SplashActivity extends AppCompatActivity {
  private final Handler handler = new Handler(Looper.getMainLooper());
  // 지정한 시간 뒤의 할 일 정의
  private final Runnable navigationRunnable =
      new Runnable() {
        @Override
        public void run() {
          Intent intent = new Intent(SplashActivity.this, StartActivity.class);
          startActivity(intent);
          finish();
        }
      };

  // 강제종료 돼도 데이터 유지
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    handler.postDelayed(navigationRunnable, 2000);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    handler.removeCallbacks(navigationRunnable);
  }
}
