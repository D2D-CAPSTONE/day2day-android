package com.example.day2day.presentation.main.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import com.example.day2day.R;
import com.example.day2day.data.CourseContract;
import com.example.day2day.data.local.CourseDatabase;
import com.example.day2day.data.local.CourseSeedData;
import com.example.day2day.data.local.entity.Course;
import com.example.day2day.presentation.common.CourseCardHelper;
import com.example.day2day.presentation.recommend.flow.CourseDetailPageActivity;
import com.example.day2day.presentation.recommend.flow.MapPageActivity;
import com.example.day2day.presentation.recommend.flow.MapSelectionActivity;
import java.util.List;

public class HomeFragment extends Fragment {

  private static final int PAGE_SIZE = 10;
  private int currentOffset = 0;
  private boolean hasMore = false;
  private boolean isLoading = true;
  private LinearLayout courseList;
  private View loadMoreView;

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
              if (!v.canScrollVertically(1) && hasMore && !isLoading) {
                isLoading = true;
                loadMoreView.setVisibility(View.VISIBLE);
                v.post(() -> v.fullScroll(View.FOCUS_DOWN));
                loadNextPage();
              }
            });

    loadCoursesFromDatabase();
  }

  private void loadCoursesFromDatabase() {
    CourseDatabase database = CourseDatabase.getInstance(requireContext());
    CourseDatabase.databaseExecutor.execute(
        () -> {
          if (database.courseDao().getCourseCount() == 0) {
            // 실제 서비스 출시 때는 CourseSeedData.get...() 호출 부분을 ApiService.fetchPopularCourses() 같은 걸로 교체
            database.courseDao().insertCourses(CourseSeedData.getPopularCourses());
          }
          if (database.coursePlaceDao().getPlaceCount() == 0) {
            database.coursePlaceDao().insertPlaces(CourseSeedData.getCoursePlaces());
          }
          if (database.popularCourseDao().getPopularCourseCount() == 0) {
            database.popularCourseDao().insertPopularCourses(CourseSeedData.getPopularCourseIds());
          }

          List<Course> loaded = database.popularCourseDao().getPopularCoursesPaged(PAGE_SIZE, 0);
          if (!isAdded()) return;

          requireActivity()
              .runOnUiThread(
                  () -> {
                    if (!isAdded()) return;
                    courseList.removeAllViews();
                    currentOffset = loaded.size();
                    hasMore = loaded.size() == PAGE_SIZE;
                    isLoading = false;
                    appendCards(loaded);
                  });
        });
  }

  private void loadNextPage() {
    CourseDatabase database = CourseDatabase.getInstance(requireContext());
    CourseDatabase.databaseExecutor.execute(
        () -> {
          List<Course> loaded =
              database.popularCourseDao().getPopularCoursesPaged(PAGE_SIZE, currentOffset);
          if (!isAdded()) return;

          requireActivity()
              .runOnUiThread(
                  () -> {
                    if (!isAdded()) return;
                    currentOffset += loaded.size();
                    hasMore = loaded.size() == PAGE_SIZE;
                    isLoading = false;
                    loadMoreView.setVisibility(View.GONE);
                    appendCards(loaded);
                  });
        });
  }

  private void appendCards(List<Course> items) {
    for (Course item : items) {
      CourseCardHelper.addCard(
          requireContext(),
          item,
          courseList,
          v -> {
            Intent intent = new Intent(requireContext(), CourseDetailPageActivity.class);
            intent.putExtra(CourseContract.EXTRA_COURSE_ID, item.courseId);
            startActivity(intent);
          });
    }
  }
}
