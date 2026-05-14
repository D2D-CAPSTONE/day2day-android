package com.example.day2day.presentation.recommend.flow;

public final class RecommendFlowContract {

  public static final String EXTRA_LATITUDE = "recommend_latitude";
  public static final String EXTRA_LONGITUDE = "recommend_longitude";
  public static final String EXTRA_SELECTED_KEYWORDS = "recommend_selected_keywords";
  public static final String EXTRA_SORT_MODE = "recommend_sort_mode";
  public static final String EXTRA_GENERATION_SEED = "recommend_generation_seed";

  public static final String SORT_RECOMMENDED = "recommended";
  public static final String SORT_DISTANCE = "distance";
  public static final String SORT_REVIEW = "review";

  private RecommendFlowContract() {}
}
