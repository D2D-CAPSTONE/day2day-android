package com.example.day2day.presentation.main.home;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import com.example.day2day.R;
import com.example.day2day.presentation.common.NavigationBarInsetHelper;
import com.example.day2day.presentation.recommend.flow.CourseDetailPageActivity;
import com.example.day2day.presentation.recommend.flow.MapPageActivity;
import com.example.day2day.presentation.recommend.flow.MapSelectionActivity;

public class HomeFragment extends Fragment {
  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Button nearbyButton = view.findViewById(R.id.btn_nearby_course);
    Button locationButton = view.findViewById(R.id.btn_location_course);

    nearbyButton.setOnClickListener(
        v -> startActivity(new Intent(requireContext(), MapSelectionActivity.class)));
    locationButton.setOnClickListener(
        v -> startActivity(new Intent(requireContext(), MapPageActivity.class)));
  }
}
