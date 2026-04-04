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
import androidx.transition.TransitionManager;
import com.example.day2day.R;
import com.example.day2day.presentation.recommend.flow.MapPageActivity;
import com.example.day2day.presentation.recommend.flow.MapSelectionActivity;

public class HomeFragment extends Fragment {

  private static final int SCROLL_THRESHOLD_DP = 50;

  // {title, spots(comma), tags(comma), rating, thumbBgColor, iconColor}
  private static final String[][] COURSES = {
    {"홍대 감성 데이트 코스", "연남동 맛집,홍대 카페,클럽 거리", "기념일,힙한", "★ 4.8", "#fce8ec", "#e8506a"},
    {"한강 피크닉 코스", "뚝섬 한강공원,성수 카페,서울숲", "야외,맑은 날", "★ 4.6", "#fff5f7", "#fb8c9e"},
    {"경복궁 고즈넉 데이트", "경복궁,북촌 한옥마을,인사동", "조용한,문화", "★ 4.7", "#fdf8f0", "#c97a30"},
    {"성수동 힙스터 코스", "성수 브런치,대림창고,서울숲", "힙한,브런치", "★ 4.5", "#eff4ff", "#5078c8"},
    {"이태원 글로벌 데이트", "경리단길,이태원 바,N서울타워", "야경,글로벌", "★ 4.4", "#effaf4", "#2a9050"},
  };

  private View weatherFull;
  private View weatherMini;
  private View courseFindSection;
  private LinearLayout courseList;
  private View loadMore;
  private ViewGroup rootView;

  private int currentPage = 0;
  private static final int PER_PAGE = 3;
  private boolean isMini = false;

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
    rootView = (ViewGroup) view;

    // Styled app title: "day[to]day"
    TextView tvTitle = view.findViewById(R.id.tv_app_title);
    SpannableString title = new SpannableString("daytoday");
    int roseColor = requireContext().getColor(R.color.rose);
    int darkWithAlpha = (requireContext().getColor(R.color.text_dark) & 0x00FFFFFF) | 0xCC000000;
    title.setSpan(new ForegroundColorSpan(roseColor), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    title.setSpan(new ForegroundColorSpan(darkWithAlpha), 3, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    title.setSpan(new ForegroundColorSpan(roseColor), 5, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    tvTitle.setText(title);

    // Weather banner views
    weatherFull = view.findViewById(R.id.weather_full);
    weatherMini = view.findViewById(R.id.weather_mini);
    courseFindSection = view.findViewById(R.id.course_find_section);
    courseList = view.findViewById(R.id.course_list);
    loadMore = view.findViewById(R.id.load_more);

    // Course finder buttons
    view.findViewById(R.id.btn_nearby_course)
        .setOnClickListener(
            v -> startActivity(new Intent(requireContext(), MapSelectionActivity.class)));
    view.findViewById(R.id.btn_location_course)
        .setOnClickListener(
            v -> startActivity(new Intent(requireContext(), MapPageActivity.class)));

    // Scroll listener: collapse/expand weather banner
    NestedScrollView scrollBody = view.findViewById(R.id.scroll_body);
    float threshold =
        SCROLL_THRESHOLD_DP * requireContext().getResources().getDisplayMetrics().density;

    scrollBody.setOnScrollChangeListener(
        (NestedScrollView.OnScrollChangeListener)
            (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
              if (scrollY > threshold && !isMini) {
                isMini = true;
                TransitionManager.beginDelayedTransition(rootView);
                weatherFull.setVisibility(View.GONE);
                weatherMini.setVisibility(View.VISIBLE);
                courseFindSection.setVisibility(View.GONE);
              } else if (scrollY <= threshold && isMini) {
                isMini = false;
                TransitionManager.beginDelayedTransition(rootView);
                weatherFull.setVisibility(View.VISIBLE);
                weatherMini.setVisibility(View.GONE);
                courseFindSection.setVisibility(View.VISIBLE);
              }

              // Lazy load more courses near bottom
              View inner = v.getChildAt(0);
              if (inner != null
                  && scrollY + v.getMeasuredHeight() >= inner.getMeasuredHeight() - 100) {
                loadMoreCourses();
              }
            });

    // Initial course load
    loadMoreCourses();
  }

  private void loadMoreCourses() {
    if (loadMore == null || loadMore.getVisibility() == View.GONE) return;
    int start = currentPage * PER_PAGE;
    if (start >= COURSES.length) {
      loadMore.setVisibility(View.GONE);
      return;
    }
    int end = Math.min(start + PER_PAGE, COURSES.length);

    LayoutInflater inflater = LayoutInflater.from(requireContext());
    for (int i = start; i < end; i++) {
      View card = inflater.inflate(R.layout.item_course_card, courseList, false);
      bindCourseCard(card, COURSES[i]);
      courseList.addView(card);
    }
    currentPage++;

    if (currentPage * PER_PAGE >= COURSES.length) {
      loadMore.setVisibility(View.GONE);
    }
  }

  private void bindCourseCard(View card, String[] course) {
    ((TextView) card.findViewById(R.id.cc_title)).setText(course[0]);

    // Route spots
    LinearLayout routeLayout = card.findViewById(R.id.cc_route);
    String[] spots = course[1].split(",");
    for (int i = 0; i < spots.length; i++) {
      TextView spotView = new TextView(requireContext());
      spotView.setText(spots[i]);
      spotView.setTextColor(requireContext().getColor(R.color.text_medium));
      spotView.setTextSize(9f);
      routeLayout.addView(spotView);
      if (i < spots.length - 1) {
        TextView arrow = new TextView(requireContext());
        arrow.setText(" › ");
        arrow.setTextColor(requireContext().getColor(R.color.text_light));
        arrow.setTextSize(8f);
        routeLayout.addView(arrow);
      }
    }

    // Tags
    LinearLayout tagsLayout = card.findViewById(R.id.cc_tags);
    for (String tag : course[2].split(",")) {
      TextView tagView = new TextView(requireContext());
      tagView.setText(tag);
      tagView.setTextColor(requireContext().getColor(R.color.rose));
      tagView.setTextSize(9f);
      tagView.setBackgroundResource(R.drawable.shape_tag_rose);
      int hPad = dpToPx(7);
      int vPad = dpToPx(2);
      tagView.setPadding(hPad, vPad, hPad, vPad);
      LinearLayout.LayoutParams lp =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      lp.setMarginEnd(dpToPx(3));
      tagView.setLayoutParams(lp);
      tagsLayout.addView(tagView);
    }

    // Rating
    ((TextView) card.findViewById(R.id.cc_rating)).setText(course[3]);

    // Thumb background (rounded rect, per-course color)
    FrameLayout thumb = card.findViewById(R.id.cc_thumb);
    GradientDrawable thumbBg = new GradientDrawable();
    thumbBg.setShape(GradientDrawable.RECTANGLE);
    thumbBg.setCornerRadius(dpToPx(12));
    thumbBg.setColor(Color.parseColor(course[4]));
    thumb.setBackground(thumbBg);

    // Pin icon tint
    ImageView thumbIcon = card.findViewById(R.id.cc_thumb_icon);
    thumbIcon.setColorFilter(Color.parseColor(course[5]));
  }

  private int dpToPx(int dp) {
    return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
  }
}
