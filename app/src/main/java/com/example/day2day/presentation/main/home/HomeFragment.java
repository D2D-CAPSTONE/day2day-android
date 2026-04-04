package com.example.day2day.presentation.main.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.day2day.R;
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
