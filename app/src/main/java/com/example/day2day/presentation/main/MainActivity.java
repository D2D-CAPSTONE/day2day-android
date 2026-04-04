package com.example.day2day.presentation.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.day2day.R;
import com.example.day2day.presentation.main.favorites.FavoritesFragment;
import com.example.day2day.presentation.main.home.HomeFragment;
import com.example.day2day.presentation.main.record.RecordFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
    bottomNavigation.setOnItemSelectedListener(
        item -> {
          int itemId = item.getItemId();
          if (itemId == R.id.menu_home) {
            showFragment(new HomeFragment());
            return true;
          }
          if (itemId == R.id.menu_favorites) {
            showFragment(new FavoritesFragment());
            return true;
          }
          if (itemId == R.id.menu_record) {
            showFragment(new RecordFragment());
            return true;
          }
          return false;
        });

    if (savedInstanceState == null) {
      bottomNavigation.setSelectedItemId(R.id.menu_home);
    }
  }

  private void showFragment(Fragment fragment) {
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.main_fragment_container, fragment)
        .commit();
  }
}
