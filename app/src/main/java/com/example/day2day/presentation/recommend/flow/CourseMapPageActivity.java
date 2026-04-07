package com.example.day2day.presentation.recommend.flow;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.day2day.R;
import com.example.day2day.presentation.main.favorites.FavoritesFragment;
import com.example.day2day.presentation.main.home.HomeFragment;
import com.example.day2day.presentation.main.record.RecordFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;

public class CourseMapPageActivity extends AppCompatActivity implements OnMapReadyCallback {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_course_map_page);

    String realPackageName = getApplicationContext().getPackageName();
    Log.d("MAP_DEBUG", "네이버로 날아가는 진짜 패키지명: " + realPackageName);

    BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
    if (bottomNavigation != null) {
      bottomNavigation.setOnItemSelectedListener(
          item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home) {
              showFragment(new HomeFragment());
              return true;
            } else if (itemId == R.id.menu_favorites) {
              showFragment(new FavoritesFragment());
              return true;
            } else if (itemId == R.id.menu_record) {
              showFragment(new RecordFragment());
              return true;
            }
            return false;
          });
    }
  }

  // MainActivity에 있던 프래그먼트 교체 메서드를 그대로 가져옴
  private void showFragment(Fragment fragment) {
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.course_map_fragment_container, fragment) // 아까 만든 빈 틀 ID
        .addToBackStack(null) // 뒤로가기 버튼 눌렀을 때 지도 화면으로 돌아오기 위함
        .commit();
  }

  @Override
  public void onMapReady(@NonNull NaverMap naverMap) {}
}
