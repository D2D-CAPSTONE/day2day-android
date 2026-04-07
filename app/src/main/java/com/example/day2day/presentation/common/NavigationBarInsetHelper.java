package com.example.day2day.presentation.common;

import android.view.View;
import android.view.ViewGroup;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class NavigationBarInsetHelper {
  private NavigationBarInsetHelper() {}

  public static void applyTopInset(View rootView, View targetView) {
    final int basePadding = targetView.getPaddingTop();
    ViewCompat.setOnApplyWindowInsetsListener(
        rootView,
        (view, windowInsets) -> {
          Insets statusBar = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
          targetView.setPadding(
              targetView.getPaddingLeft(),
              basePadding + statusBar.top,
              targetView.getPaddingRight(),
              targetView.getPaddingBottom());
          return windowInsets;
        });
    ViewCompat.requestApplyInsets(rootView);
  }

  public static void applyBottomInset(View rootView, View targetView) {
    ViewGroup.MarginLayoutParams layoutParams =
        (ViewGroup.MarginLayoutParams) targetView.getLayoutParams();
    final int baseBottomMargin = layoutParams.bottomMargin;

    ViewCompat.setOnApplyWindowInsetsListener(
        rootView,
        (view, windowInsets) -> {
          Insets navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
          ViewGroup.MarginLayoutParams params =
              (ViewGroup.MarginLayoutParams) targetView.getLayoutParams();
          params.bottomMargin = baseBottomMargin + navigationBars.bottom;
          targetView.setLayoutParams(params);
          return windowInsets;
        });
    ViewCompat.requestApplyInsets(rootView);
  }
}
