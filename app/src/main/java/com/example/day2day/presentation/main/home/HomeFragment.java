package com.example.day2day.presentation.main.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import com.example.day2day.R;
import com.example.day2day.data.CourseContract;
import com.example.day2day.data.local.CourseDatabase;
import com.example.day2day.data.local.CourseSeedData;
import com.example.day2day.data.local.entity.Course;
import com.example.day2day.presentation.recommend.flow.CourseDetailPageActivity;
import com.example.day2day.presentation.recommend.flow.MapPageActivity;
import com.example.day2day.presentation.recommend.flow.MapSelectionActivity;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

  private static final int PAGE_SIZE = 10;
  private int currentIndex = 0;
  private LinearLayout courseList;
  private View loadMoreView;
  private final List<Course> courses = new ArrayList<>();

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

    view.findViewById(R.id.btn_nearby_course)
        .setOnClickListener(
            v -> startActivity(new Intent(requireContext(), MapPageActivity.class)));
    view.findViewById(R.id.btn_location_course)
        .setOnClickListener(
            v -> startActivity(new Intent(requireContext(), MapSelectionActivity.class)));

    renderCourseCards(view);
  }

  private void renderCourseCards(View view) {
    courseList = view.findViewById(R.id.course_list);
    loadMoreView = view.findViewById(R.id.load_more);
    loadMoreView.setVisibility(View.GONE);

    NestedScrollView scrollBody = view.findViewById(R.id.scroll_body);
    scrollBody.setOnScrollChangeListener(
        (NestedScrollView.OnScrollChangeListener)
            (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
              if (!v.canScrollVertically(1) && currentIndex < courses.size()) {
                loadMoreView.setVisibility(View.VISIBLE);
                v.post(() -> v.fullScroll(View.FOCUS_DOWN));
                v.postDelayed(
                    () -> {
                      appendCourses();
                      loadMoreView.setVisibility(View.GONE);
                    },
                    700);
              }
            });

    loadCoursesFromDatabase();
  }

  private void loadCoursesFromDatabase() {
    CourseDatabase database = CourseDatabase.getInstance(requireContext());
    CourseDatabase.databaseExecutor.execute(
        () -> {
          if (database.courseDao().getCourseCount() == 0) {
            database.courseDao().insertCourses(CourseSeedData.getPopularCourses());
          }

          List<Course> loadedCourses = database.courseDao().getAllCourses();
          if (!isAdded()) {
            return;
          }

          requireActivity()
              .runOnUiThread(
                  () -> {
                    if (!isAdded()) {
                      return;
                    }
                    courses.clear();
                    courses.addAll(loadedCourses);
                    currentIndex = 0;
                    courseList.removeAllViews();
                    appendCourses();
                  });
        });
  }

  private void appendCourses() {
    LayoutInflater inflater = LayoutInflater.from(requireContext());
    float density = getResources().getDisplayMetrics().density;
    int end = Math.min(currentIndex + PAGE_SIZE, courses.size());

    for (int i = currentIndex; i < end; i++) {
      Course item = courses.get(i);
      View card = inflater.inflate(R.layout.item_course_card, courseList, false);

      card.findViewById(R.id.cc_thumb).setBackgroundColor(item.thumbColor);
      ((TextView) card.findViewById(R.id.cc_title)).setText(item.title);
      ((TextView) card.findViewById(R.id.cc_rating)).setText(item.ratingText);

      LinearLayout routeLayout = card.findViewById(R.id.cc_route);
      String[] routeItems = item.routeText.split(" > ");
      for (int j = 0; j < routeItems.length; j++) {
        TextView stop = new TextView(requireContext());
        stop.setText(routeItems[j]);
        stop.setTextSize(9);
        stop.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_medium));
        routeLayout.addView(stop);
        if (j < routeItems.length - 1) {
          TextView arrow = new TextView(requireContext());
          arrow.setText(" › ");
          arrow.setTextSize(9);
          arrow.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_light));
          routeLayout.addView(arrow);
        }
      }

      LinearLayout tagsLayout = card.findViewById(R.id.cc_tags);
      int px7 = (int) (7 * density);
      int px2 = (int) (2 * density);
      int px4 = (int) (4 * density);
      for (String tag : item.tagsText.split(",")) {
        TextView tagView = new TextView(requireContext());
        tagView.setText(tag.trim());
        tagView.setTextSize(9);
        tagView.setTextColor(ContextCompat.getColor(requireContext(), R.color.rose));
        tagView.setBackground(
            ContextCompat.getDrawable(requireContext(), R.drawable.shape_tag_rose));
        tagView.setPadding(px7, px2, px7, px2);
        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(px4);
        tagsLayout.addView(tagView, params);
      }

      card.setOnClickListener(
          v -> {
            Intent intent = new Intent(requireContext(), CourseDetailPageActivity.class);
            intent.putExtra(CourseContract.EXTRA_COURSE_ID, item.courseId);
            startActivity(intent);
          });

      courseList.addView(card);
    }

    currentIndex = end;
  }
}
