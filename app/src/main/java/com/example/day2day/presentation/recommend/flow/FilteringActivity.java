package com.example.day2day.presentation.recommend.flow;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.day2day.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FilteringActivity extends AppCompatActivity {
  private static final double DEFAULT_LATITUDE = 37.5666102;
  private static final double DEFAULT_LONGITUDE = 126.9783881;
  private static final String ORDER_ACTIVITY = "activity";
  private static final String ORDER_FOOD = "food";
  private static final String ORDER_CAFE = "cafe";

  private final ArrayList<CourseOrderItem> courseOrderItems = new ArrayList<>();

  private double initialLatitude = DEFAULT_LATITUDE;
  private double initialLongitude = DEFAULT_LONGITUDE;
  private double latitude = DEFAULT_LATITUDE;
  private double longitude = DEFAULT_LONGITUDE;
  private boolean isLocationAdjusted;

  private TextInputEditText locationInput;
  private TextView moodSummaryText;
  private TextView selectionSummaryText;
  private ChipGroup moodChipGroup;
  private RadioGroup sortRadioGroup;
  private CourseOrderAdapter courseOrderAdapter;

  private final ActivityResultLauncher<Intent> startLocationPickerLauncher =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            if (result.getResultCode() != RESULT_OK || result.getData() == null) {
              return;
            }

            latitude =
                result.getData().getDoubleExtra(RecommendFlowContract.EXTRA_LATITUDE, latitude);
            longitude =
                result.getData().getDoubleExtra(RecommendFlowContract.EXTRA_LONGITUDE, longitude);
            isLocationAdjusted = true;
            updateLocationUi();
            updateSelectionSummary();
          });

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_filtering);

    initialLatitude =
        getIntent().getDoubleExtra(RecommendFlowContract.EXTRA_LATITUDE, DEFAULT_LATITUDE);
    initialLongitude =
        getIntent().getDoubleExtra(RecommendFlowContract.EXTRA_LONGITUDE, DEFAULT_LONGITUDE);
    latitude = initialLatitude;
    longitude = initialLongitude;

    TextView backButton = findViewById(R.id.tv_filtering_back);
    TextView resetButton = findViewById(R.id.tv_filtering_reset);
    locationInput = findViewById(R.id.et_filtering_location);
    moodSummaryText = findViewById(R.id.tv_filtering_mood_summary);
    selectionSummaryText = findViewById(R.id.tv_filtering_selection_summary);
    moodChipGroup = findViewById(R.id.chip_group_mood);
    sortRadioGroup = findViewById(R.id.rg_sort);
    TextInputLayout locationInputLayout = findViewById(R.id.til_filtering_location);
    Button useCurrentLocationButton = findViewById(R.id.btn_filtering_use_current_location);
    Button adjustOnMapButton = findViewById(R.id.btn_filtering_adjust_location_on_map);
    Button nextButton = findViewById(R.id.btn_filtering_next);
    RecyclerView courseOrderRecyclerView = findViewById(R.id.rv_course_order);

    backButton.setOnClickListener(v -> finish());
    resetButton.setOnClickListener(v -> resetFilters());
    locationInput.setOnClickListener(v -> openStartLocationPicker());
    locationInputLayout.setEndIconOnClickListener(v -> openStartLocationPicker());
    useCurrentLocationButton.setOnClickListener(
        v -> {
          latitude = initialLatitude;
          longitude = initialLongitude;
          isLocationAdjusted = false;
          updateLocationUi();
          updateSelectionSummary();
        });
    adjustOnMapButton.setOnClickListener(v -> openStartLocationPicker());
    sortRadioGroup.setOnCheckedChangeListener((group, checkedId) -> updateSelectionSummary());

    configureMoodChips();
    configureCourseOrderList(courseOrderRecyclerView);
    resetCourseOrder();
    updateLocationUi();
    updateMoodSummary();
    updateSelectionSummary();

    nextButton.setOnClickListener(
        v -> {
          Intent intent = new Intent(FilteringActivity.this, CourseMapPageActivity.class);
          intent.putExtra(RecommendFlowContract.EXTRA_LATITUDE, latitude);
          intent.putExtra(RecommendFlowContract.EXTRA_LONGITUDE, longitude);
          intent.putStringArrayListExtra(
              RecommendFlowContract.EXTRA_SELECTED_KEYWORDS,
              new ArrayList<>(collectSelectedKeywords()));
          intent.putExtra(RecommendFlowContract.EXTRA_COURSE_ORDER, resolveCourseOrder());
          intent.putExtra(RecommendFlowContract.EXTRA_SORT_MODE, resolveSortMode());
          intent.putExtra(RecommendFlowContract.EXTRA_GENERATION_SEED, System.currentTimeMillis());
          startActivity(intent);
        });
  }

  private void configureCourseOrderList(RecyclerView recyclerView) {
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setNestedScrollingEnabled(false);

    courseOrderAdapter = new CourseOrderAdapter(courseOrderItems);
    recyclerView.setAdapter(courseOrderAdapter);

    ItemTouchHelper itemTouchHelper =
        new ItemTouchHelper(
            new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
              @Override
              public boolean onMove(
                  @NonNull RecyclerView recyclerView,
                  @NonNull RecyclerView.ViewHolder viewHolder,
                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getBindingAdapterPosition();
                int toPosition = target.getBindingAdapterPosition();
                if (fromPosition == RecyclerView.NO_POSITION
                    || toPosition == RecyclerView.NO_POSITION) {
                  return false;
                }

                Collections.swap(courseOrderItems, fromPosition, toPosition);
                courseOrderAdapter.notifyItemMoved(fromPosition, toPosition);
                updateSelectionSummary();
                return true;
              }

              @Override
              public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

              @Override
              public boolean isLongPressDragEnabled() {
                return true;
              }

              @Override
              public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (viewHolder instanceof CourseOrderAdapter.CourseOrderViewHolder) {
                  ((CourseOrderAdapter.CourseOrderViewHolder) viewHolder)
                      .setDragging(actionState == ItemTouchHelper.ACTION_STATE_DRAG);
                }
              }

              @Override
              public void clearView(
                  @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if (viewHolder instanceof CourseOrderAdapter.CourseOrderViewHolder) {
                  ((CourseOrderAdapter.CourseOrderViewHolder) viewHolder).setDragging(false);
                }
                courseOrderAdapter.notifyDataSetChanged();
                updateSelectionSummary();
              }
            });
    itemTouchHelper.attachToRecyclerView(recyclerView);
  }

  private void openStartLocationPicker() {
    Intent intent = new Intent(this, StartLocationPickerActivity.class);
    intent.putExtra(RecommendFlowContract.EXTRA_LATITUDE, latitude);
    intent.putExtra(RecommendFlowContract.EXTRA_LONGITUDE, longitude);
    startLocationPickerLauncher.launch(intent);
  }

  private void resetFilters() {
    latitude = initialLatitude;
    longitude = initialLongitude;
    isLocationAdjusted = false;
    clearMoodSelection();
    resetCourseOrder();
    sortRadioGroup.check(R.id.rb_recommended);
    updateLocationUi();
    updateMoodSummary();
    updateSelectionSummary();
  }

  private void resetCourseOrder() {
    courseOrderItems.clear();
    courseOrderItems.add(new CourseOrderItem(ORDER_ACTIVITY, "놀거리"));
    courseOrderItems.add(new CourseOrderItem(ORDER_FOOD, "음식점"));
    courseOrderItems.add(new CourseOrderItem(ORDER_CAFE, "카페"));
    if (courseOrderAdapter != null) {
      courseOrderAdapter.notifyDataSetChanged();
    }
  }

  private void updateLocationUi() {
    locationInput.setText(String.format(Locale.KOREA, "위도 %.5f / 경도 %.5f", latitude, longitude));
    locationInput.setFocusable(false);
    locationInput.setClickable(true);
    locationInput.setCursorVisible(false);
  }

  private void configureMoodChips() {
    int[][] states = new int[][] {new int[] {android.R.attr.state_checked}, new int[] {}};
    ColorStateList backgroundColors =
        new ColorStateList(states, new int[] {Color.parseColor("#E8506A"), Color.WHITE});
    ColorStateList strokeColors =
        new ColorStateList(
            states, new int[] {Color.parseColor("#E8506A"), Color.parseColor("#D1D1D1")});
    ColorStateList textColors =
        new ColorStateList(states, new int[] {Color.WHITE, Color.parseColor("#2A1F2D")});

    for (int i = 0; i < moodChipGroup.getChildCount(); i++) {
      if (!(moodChipGroup.getChildAt(i) instanceof Chip)) {
        continue;
      }

      Chip chip = (Chip) moodChipGroup.getChildAt(i);
      chip.setCheckable(true);
      chip.setChecked(false);
      chip.setCheckedIconVisible(false);
      chip.setChipBackgroundColor(backgroundColors);
      chip.setChipStrokeColor(strokeColors);
      chip.setTextColor(textColors);
      chip.setChipStrokeWidth(1f);
      chip.setOnCheckedChangeListener(
          (buttonView, isChecked) -> {
            updateMoodSummary();
            updateSelectionSummary();
          });
    }
  }

  private void clearMoodSelection() {
    for (int i = 0; i < moodChipGroup.getChildCount(); i++) {
      if (moodChipGroup.getChildAt(i) instanceof Chip) {
        ((Chip) moodChipGroup.getChildAt(i)).setChecked(false);
      }
    }
  }

  private void updateMoodSummary() {
    List<String> selectedKeywords = collectSelectedKeywords();
    if (selectedKeywords.isEmpty()) {
      moodSummaryText.setText("분위기 제한 없이 추천해요.");
      return;
    }

    moodSummaryText.setText(
        String.format(
            Locale.KOREA,
            "%d개 선택: %s",
            selectedKeywords.size(),
            TextUtils.join(", ", selectedKeywords)));
  }

  private void updateSelectionSummary() {
    List<String> selectedKeywords = collectSelectedKeywords();
    String locationLabel = isLocationAdjusted ? "지도에서 고른 위치" : "현재 위치";
    String moodLabel =
        selectedKeywords.isEmpty()
            ? "분위기 제한 없음"
            : String.format(Locale.KOREA, "분위기 %d개", selectedKeywords.size());
    selectionSummaryText.setText(
        String.format(
            Locale.KOREA,
            "%s · %s · %s · %s",
            locationLabel,
            moodLabel,
            resolveCourseOrderLabel(),
            resolveSortLabel()));
  }

  private List<String> collectSelectedKeywords() {
    List<String> selectedKeywords = new ArrayList<>();

    for (int i = 0; i < moodChipGroup.getChildCount(); i++) {
      if (!(moodChipGroup.getChildAt(i) instanceof Chip)) {
        continue;
      }

      Chip chip = (Chip) moodChipGroup.getChildAt(i);
      if (chip.isChecked()) {
        selectedKeywords.add(chip.getText().toString().trim());
      }
    }

    return selectedKeywords;
  }

  private String resolveCourseOrder() {
    List<String> orderKeys = new ArrayList<>();
    for (CourseOrderItem item : courseOrderItems) {
      orderKeys.add(item.key);
    }
    return TextUtils.join("_", orderKeys);
  }

  private String resolveCourseOrderLabel() {
    List<String> labels = new ArrayList<>();
    for (CourseOrderItem item : courseOrderItems) {
      labels.add(item.label);
    }
    return TextUtils.join(" → ", labels);
  }

  private String resolveSortMode() {
    int checkedId = sortRadioGroup.getCheckedRadioButtonId();
    if (checkedId == R.id.rb_distance) {
      return RecommendFlowContract.SORT_DISTANCE;
    }
    if (checkedId == R.id.rb_review) {
      return RecommendFlowContract.SORT_REVIEW;
    }
    return RecommendFlowContract.SORT_RECOMMENDED;
  }

  private String resolveSortLabel() {
    int checkedId = sortRadioGroup.getCheckedRadioButtonId();
    if (checkedId == R.id.rb_distance) {
      return "가까운 순";
    }
    if (checkedId == R.id.rb_review) {
      return "리뷰 많은 순";
    }
    return "추천순";
  }

  private static final class CourseOrderItem {
    private final String key;
    private final String label;

    private CourseOrderItem(String key, String label) {
      this.key = key;
      this.label = label;
    }
  }

  private static final class CourseOrderAdapter
      extends RecyclerView.Adapter<CourseOrderAdapter.CourseOrderViewHolder> {
    private final List<CourseOrderItem> items;

    private CourseOrderAdapter(List<CourseOrderItem> items) {
      this.items = items;
    }

    @NonNull
    @Override
    public CourseOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view =
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.item_course_order, parent, false);
      return new CourseOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseOrderViewHolder holder, int position) {
      CourseOrderItem item = items.get(position);
      holder.bind(position, item);
    }

    @Override
    public int getItemCount() {
      return items.size();
    }

    static final class CourseOrderViewHolder extends RecyclerView.ViewHolder {
      private final MaterialCardView cardView;
      private final TextView rankTextView;
      private final TextView labelTextView;

      private CourseOrderViewHolder(@NonNull View itemView) {
        super(itemView);
        cardView = (MaterialCardView) itemView;
        rankTextView = itemView.findViewById(R.id.tv_course_order_rank);
        labelTextView = itemView.findViewById(R.id.tv_course_order_label);
      }

      private void bind(int position, CourseOrderItem item) {
        rankTextView.setText(String.valueOf(position + 1));
        labelTextView.setText(item.label);
        setDragging(false);
      }

      private void setDragging(boolean dragging) {
        cardView.setCardElevation(dragging ? 8f : 0f);
        cardView.setScaleX(dragging ? 1.02f : 1f);
        cardView.setScaleY(dragging ? 1.02f : 1f);
      }
    }
  }
}
