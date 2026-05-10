package com.example.day2day.presentation.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.day2day.R;
import com.example.day2day.presentation.main.MainActivity;

public class SplashActivity extends AppCompatActivity {
  private final Handler handler = new Handler(Looper.getMainLooper());
  // 지정한 시간 뒤의 할 일 정의
  private final Runnable navigationRunnable =
      new Runnable() {
        @Override
        public void run() {
          Intent intent = new Intent(SplashActivity.this, MainActivity.class);
          startActivity(intent);
          finish();
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    handler.postDelayed(navigationRunnable, 2000);
  }

  // 콜백 정리
  @Override
  protected void onDestroy() {
    super.onDestroy();
    handler.removeCallbacks(navigationRunnable);
  }
}
