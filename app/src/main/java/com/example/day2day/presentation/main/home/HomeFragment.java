package com.example.day2day.presentation.main.home;

import android.content.Intent;
import android.graphics.Color;
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
import com.example.day2day.presentation.recommend.flow.CourseDetailPageActivity;
import com.example.day2day.presentation.recommend.flow.MapPageActivity;
import com.example.day2day.presentation.recommend.flow.MapSelectionActivity;

public class HomeFragment extends Fragment {

  private static final int PAGE_SIZE = 10;
  private int currentIndex = 0;
  private LinearLayout courseList;
  private View loadMoreView;

  private static class CourseItem {
    String title;
    String[] route;
    String[] tags;
    String rating;
    int thumbColor;

    CourseItem(String title, String[] route, String[] tags, String rating, int thumbColor) {
      this.title = title;
      this.route = route;
      this.tags = tags;
      this.rating = rating;
      this.thumbColor = thumbColor;
    }
  }

  private static final CourseItem[] DUMMY_COURSES = {
    new CourseItem(
        "홍대 감성 데이트 코스",
        new String[] {"홍대", "연남동", "망원"},
        new String[] {"#감성", "#데이트"},
        "★ 4.8",
        Color.parseColor("#FFD6D6")),
    new CourseItem(
        "한강 피크닉 코스",
        new String[] {"여의도", "한강공원", "노을공원"},
        new String[] {"#야외", "#피크닉"},
        "★ 4.6",
        Color.parseColor("#D6EAF8")),
    new CourseItem(
        "성수 카페 투어 코스",
        new String[] {"성수역", "서울숲", "뚝섬"},
        new String[] {"#카페", "#힐링"},
        "★ 4.7",
        Color.parseColor("#D5F5E3")),
    new CourseItem(
        "북촌 한옥마을 코스",
        new String[] {"경복궁", "북촌", "인사동"},
        new String[] {"#역사", "#전통"},
        "★ 4.5",
        Color.parseColor("#FEF9E7")),
    new CourseItem(
        "강남 쇼핑 데이트",
        new String[] {"코엑스", "청담", "압구정"},
        new String[] {"#쇼핑", "#럭셔리"},
        "★ 4.4",
        Color.parseColor("#F5EEF8")),
    new CourseItem(
        "을지로 힙스터 코스",
        new String[] {"을지로3가", "을지로4가", "황학동"},
        new String[] {"#힙", "#복고"},
        "★ 4.6",
        Color.parseColor("#D6DBDF")),
    new CourseItem(
        "남산 야경 데이트",
        new String[] {"명동", "남산타워", "이태원"},
        new String[] {"#야경", "#데이트"},
        "★ 4.9",
        Color.parseColor("#D7BDE2")),
    new CourseItem(
        "익선동 복고 감성 코스",
        new String[] {"익선동", "종로3가", "낙원상가"},
        new String[] {"#복고", "#감성"},
        "★ 4.5",
        Color.parseColor("#FDEBD0")),
    new CourseItem(
        "연남동 브런치 코스",
        new String[] {"연남동", "경의선숲길", "홍대"},
        new String[] {"#브런치", "#카페"},
        "★ 4.7",
        Color.parseColor("#D5F5E3")),
    new CourseItem(
        "서촌 골목 투어",
        new String[] {"경복궁역", "서촌", "통인시장"},
        new String[] {"#골목", "#전통"},
        "★ 4.4",
        Color.parseColor("#FEF9E7")),
    new CourseItem(
        "동대문 쇼핑 코스",
        new String[] {"동대문", "DDP", "신당동"},
        new String[] {"#쇼핑", "#패션"},
        "★ 4.3",
        Color.parseColor("#EAF2FF")),
    new CourseItem(
        "가로수길 브랜드 투어",
        new String[] {"신사역", "가로수길", "세로수길"},
        new String[] {"#쇼핑", "#트렌디"},
        "★ 4.5",
        Color.parseColor("#F5EEF8")),
    new CourseItem(
        "합정 카페 거리 코스",
        new String[] {"합정역", "망원동", "당인리"},
        new String[] {"#카페", "#감성"},
        "★ 4.6",
        Color.parseColor("#FFD6D6")),
    new CourseItem(
        "상암 하늘공원 코스",
        new String[] {"상암DMC", "하늘공원", "노을공원"},
        new String[] {"#야외", "#힐링"},
        "★ 4.5",
        Color.parseColor("#D5F5E3")),
    new CourseItem(
        "뚝섬 한강 자전거 코스",
        new String[] {"뚝섬한강공원", "자양동", "건대입구"},
        new String[] {"#자전거", "#야외"},
        "★ 4.4",
        Color.parseColor("#D6EAF8")),
    new CourseItem(
        "경리단길 레스토랑 투어",
        new String[] {"이태원역", "경리단길", "녹사평"},
        new String[] {"#맛집", "#이국적"},
        "★ 4.7",
        Color.parseColor("#FDEBD0")),
    new CourseItem(
        "낙산공원 야경 코스",
        new String[] {"혜화역", "낙산공원", "이화마을"},
        new String[] {"#야경", "#산책"},
        "★ 4.6",
        Color.parseColor("#D7BDE2")),
    new CourseItem(
        "광화문 역사 탐방",
        new String[] {"광화문", "청계천", "종각"},
        new String[] {"#역사", "#문화"},
        "★ 4.3",
        Color.parseColor("#FEF9E7")),
    new CourseItem(
        "건대 먹거리 코스",
        new String[] {"건대입구", "자양동", "구의역"},
        new String[] {"#맛집", "#야식"},
        "★ 4.5",
        Color.parseColor("#FFD6D6")),
    new CourseItem(
        "부암동 카페 코스",
        new String[] {"경복궁역", "부암동", "세검정"},
        new String[] {"#카페", "#한적"},
        "★ 4.8",
        Color.parseColor("#D5F5E3")),
    new CourseItem(
        "명동 뷰티 쇼핑",
        new String[] {"명동역", "명동거리", "남대문"},
        new String[] {"#뷰티", "#쇼핑"},
        "★ 4.2",
        Color.parseColor("#F9EBEA")),
    new CourseItem(
        "왕십리 맛집 투어",
        new String[] {"왕십리역", "행당동", "마장동"},
        new String[] {"#맛집", "#로컬"},
        "★ 4.4",
        Color.parseColor("#EAFAF1")),
    new CourseItem(
        "서울숲 피크닉 코스",
        new String[] {"서울숲역", "서울숲공원", "성수동"},
        new String[] {"#피크닉", "#힐링"},
        "★ 4.7",
        Color.parseColor("#D6EAF8")),
    new CourseItem(
        "신촌 대학가 코스",
        new String[] {"신촌역", "이화여대", "연세로"},
        new String[] {"#젊음", "#활기"},
        "★ 4.3",
        Color.parseColor("#FEF9E7")),
    new CourseItem(
        "잠실 롯데월드 데이트",
        new String[] {"잠실역", "롯데월드", "석촌호수"},
        new String[] {"#놀이공원", "#데이트"},
        "★ 4.6",
        Color.parseColor("#FFD6D6")),
    new CourseItem(
        "종로 전통시장 투어",
        new String[] {"종로5가", "광장시장", "방산시장"},
        new String[] {"#시장", "#먹거리"},
        "★ 4.5",
        Color.parseColor("#FDEBD0")),
    new CourseItem(
        "마포 힙플레이스 코스",
        new String[] {"마포역", "공덕동", "아현동"},
        new String[] {"#힙", "#로컬"},
        "★ 4.4",
        Color.parseColor("#D6DBDF")),
    new CourseItem(
        "도봉산 등산 코스",
        new String[] {"도봉산역", "도봉산", "망월사"},
        new String[] {"#등산", "#자연"},
        "★ 4.6",
        Color.parseColor("#D5F5E3")),
    new CourseItem(
        "압구정 로데오 코스",
        new String[] {"압구정역", "로데오거리", "청담동"},
        new String[] {"#쇼핑", "#감성"},
        "★ 4.5",
        Color.parseColor("#F5EEF8")),
    new CourseItem(
        "망원동 감성 투어",
        new String[] {"망원역", "망원시장", "합정"},
        new String[] {"#감성", "#로컬"},
        "★ 4.7",
        Color.parseColor("#D7BDE2")),
    new CourseItem(
        "노량진 수산시장 코스",
        new String[] {"노량진역", "수산시장", "한강대교"},
        new String[] {"#해산물", "#맛집"},
        "★ 4.3",
        Color.parseColor("#EAF2FF")),
  };

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
            v -> startActivity(new Intent(requireContext(), MapSelectionActivity.class)));
    view.findViewById(R.id.btn_location_course)
        .setOnClickListener(
            v -> startActivity(new Intent(requireContext(), MapPageActivity.class)));

    renderCourseCards(view);
  }

  private void renderCourseCards(View view) {
    courseList = view.findViewById(R.id.course_list);
    loadMoreView = view.findViewById(R.id.load_more);
    loadMoreView.setVisibility(View.GONE);

    appendCourses();

    NestedScrollView scrollBody = view.findViewById(R.id.scroll_body);
    scrollBody.setOnScrollChangeListener(
        (NestedScrollView.OnScrollChangeListener)
            (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
              if (!v.canScrollVertically(1) && currentIndex < DUMMY_COURSES.length) {
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
  }

  private void appendCourses() {
    LayoutInflater inflater = LayoutInflater.from(requireContext());
    float density = getResources().getDisplayMetrics().density;
    int end = Math.min(currentIndex + PAGE_SIZE, DUMMY_COURSES.length);

    for (int i = currentIndex; i < end; i++) {
      CourseItem item = DUMMY_COURSES[i];
      View card = inflater.inflate(R.layout.item_course_card, courseList, false);

      card.findViewById(R.id.cc_thumb).setBackgroundColor(item.thumbColor);
      ((TextView) card.findViewById(R.id.cc_title)).setText(item.title);
      ((TextView) card.findViewById(R.id.cc_rating)).setText(item.rating);

      LinearLayout routeLayout = card.findViewById(R.id.cc_route);
      for (int j = 0; j < item.route.length; j++) {
        TextView stop = new TextView(requireContext());
        stop.setText(item.route[j]);
        stop.setTextSize(9);
        stop.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_medium));
        routeLayout.addView(stop);
        if (j < item.route.length - 1) {
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
      for (String tag : item.tags) {
        TextView tagView = new TextView(requireContext());
        tagView.setText(tag);
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
          v -> startActivity(new Intent(requireContext(), CourseDetailPageActivity.class)));

      courseList.addView(card);
    }

    currentIndex = end;
  }
}
