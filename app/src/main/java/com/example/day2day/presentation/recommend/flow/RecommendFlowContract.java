package com.example.day2day.presentation.recommend.flow;

public final class RecommendFlowContract {

  public static final String EXTRA_LATITUDE = "recommend_latitude";
  public static final String EXTRA_LONGITUDE = "recommend_longitude";
  public static final String EXTRA_SELECTED_KEYWORDS = "recommend_selected_keywords";
  public static final String EXTRA_COURSE_ORDER = "recommend_course_order";
  public static final String EXTRA_SORT_MODE = "recommend_sort_mode";
  public static final String EXTRA_GENERATION_SEED = "recommend_generation_seed";

  public static final String SORT_RECOMMENDED = "recommended";
  public static final String SORT_DISTANCE = "distance";
  public static final String SORT_REVIEW = "review";

  public static final String COURSE_ORDER_ACTIVITY_FOOD_CAFE = "activity_food_cafe";
  public static final String COURSE_ORDER_ACTIVITY_CAFE_FOOD = "activity_cafe_food";
  public static final String COURSE_ORDER_FOOD_ACTIVITY_CAFE = "food_activity_cafe";
  public static final String COURSE_ORDER_FOOD_CAFE_ACTIVITY = "food_cafe_activity";
  public static final String COURSE_ORDER_CAFE_ACTIVITY_FOOD = "cafe_activity_food";
  public static final String COURSE_ORDER_CAFE_FOOD_ACTIVITY = "cafe_food_activity";

  private RecommendFlowContract() {}
}
