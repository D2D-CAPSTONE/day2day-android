package com.example.day2day.presentation.main.record;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.day2day.R;
import com.example.day2day.data.CourseContract;
import com.example.day2day.data.local.CourseDatabase;
import com.example.day2day.data.local.CourseSeedData;
import com.example.day2day.data.local.entity.Course;
import com.example.day2day.data.local.entity.Record;
import com.example.day2day.presentation.common.CourseCardHelper;
import com.example.day2day.presentation.recommend.flow.CourseDetailPageActivity;
import java.util.ArrayList;
import java.util.List;

public class RecordFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_record, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    loadRecords(view);
  }

  private void loadRecords(View view) {
    LinearLayout list = view.findViewById(R.id.record_list);
    TextView empty = view.findViewById(R.id.record_empty);

    CourseDatabase db = CourseDatabase.getInstance(requireContext());
    // DB 조회는 시간이 걸려 메인 스레드에서 하면 앱이 멈추므로 백그라운드 스레드에서 실행한다
    CourseDatabase.databaseExecutor.execute(
        () -> {
          if (db.courseDao().getCourseCount() == 0) {
            db.courseDao().insertCourses(CourseSeedData.getPopularCourses());
          }
          if (db.recordDao().getAllRecords().isEmpty()) {
            long now = System.currentTimeMillis();
            db.recordDao()
                .insertRecord(new Record("popular_001", now - 86400000L * 3, "홍대 데이트 다녀옴"));
            db.recordDao()
                .insertRecord(new Record("popular_002", now - 86400000L * 7, "한강 피크닉 최고"));
            db.recordDao()
                .insertRecord(new Record("popular_007", now - 86400000L * 14, "남산 야경 진짜 예뻤음"));
          }

          List<Record> records = db.recordDao().getAllRecords();
          List<Course> courses = new ArrayList<>();
          for (Record record : records) {
            Course course = db.courseDao().getCourseById(record.courseId);
            if (course != null) {
              courses.add(course);
            }
          }

          // 백그라운드 작업 중 사용자가 탭을 전환하면 Fragment가 분리될 수 있어 확인한다
          if (!isAdded()) return;
          // View 갱신은 반드시 메인 스레드에서만 가능하므로 다시 메인 스레드로 전환한다
          requireActivity()
              .runOnUiThread(
                  () -> {
                    // runOnUiThread 진입 직전에도 한 번 더 확인한다
                    if (!isAdded()) return;
                    if (courses.isEmpty()) {
                      empty.setVisibility(View.VISIBLE);
                      return;
                    }
                    for (Course course : courses) {
                      CourseCardHelper.addCard(
                          requireContext(),
                          course,
                          list,
                          v -> {
                            Intent intent =
                                new Intent(requireContext(), CourseDetailPageActivity.class);
                            intent.putExtra(CourseContract.EXTRA_COURSE_ID, course.courseId);
                            startActivity(intent);
                          });
                    }
                  });
        });
  }
}
