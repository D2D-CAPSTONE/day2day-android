package com.example.day2day.presentation.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.example.day2day.R;
import com.example.day2day.data.local.entity.Course;

public final class CourseCardHelper {
  private CourseCardHelper() {}

  // onClick: 카드를 사용하는 쪽(Fragment)이 클릭 시 동작을 직접 정해서 넘겨준다
  public static void addCard(
      Context context, Course course, LinearLayout container, View.OnClickListener onClick) {
    float density = context.getResources().getDisplayMetrics().density;
    View card = LayoutInflater.from(context).inflate(R.layout.item_course_card, container, false);

    card.findViewById(R.id.cc_thumb).setBackgroundColor(course.thumbColor);
    ((TextView) card.findViewById(R.id.cc_title)).setText(course.title);
    ((TextView) card.findViewById(R.id.cc_rating)).setText(course.ratingText);

    LinearLayout routeLayout = card.findViewById(R.id.cc_route);
    String[] routeItems = course.routeText.split(" > ");
    for (int j = 0; j < routeItems.length; j++) {
      TextView stop = new TextView(context);
      stop.setText(routeItems[j]);
      stop.setTextSize(9);
      stop.setTextColor(ContextCompat.getColor(context, R.color.text_medium));
      routeLayout.addView(stop);
      if (j < routeItems.length - 1) {
        TextView arrow = new TextView(context);
        arrow.setText(" › ");
        arrow.setTextSize(9);
        arrow.setTextColor(ContextCompat.getColor(context, R.color.text_light));
        routeLayout.addView(arrow);
      }
    }

    LinearLayout tagsLayout = card.findViewById(R.id.cc_tags);
    int px7 = (int) (7 * density);
    int px2 = (int) (2 * density);
    int px4 = (int) (4 * density);
    for (String tag : course.tagsText.split(",")) {
      TextView tagView = new TextView(context);
      tagView.setText(tag.trim());
      tagView.setTextSize(9);
      tagView.setTextColor(ContextCompat.getColor(context, R.color.rose));
      tagView.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_tag_rose));
      tagView.setPadding(px7, px2, px7, px2);
      LinearLayout.LayoutParams params =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMarginEnd(px4);
      tagsLayout.addView(tagView, params);
    }

    card.setOnClickListener(onClick);
    container.addView(card);
  }
}
