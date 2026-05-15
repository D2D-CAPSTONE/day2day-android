package com.example.day2day.presentation.recommend.flow;

import android.net.Uri;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class NearbyCourseEngine {
  private static final String BASE_URL = "http://34.47.126.220";
  private static final int CONNECT_TIMEOUT_MS = 5000;
  private static final int READ_TIMEOUT_MS = 7000;
  private static final int TARGET_COURSE_COUNT = 5;
  private static final int MAX_GENERATED_COURSES = 10;
  private static final int ROTATION_WINDOW = 8;
  private static final int KAKAO_MAX_PAGE = 3;
  private static final int ACTIVITY_POOL_TARGET = 14;
  private static final int FOOD_POOL_TARGET = 24;
  private static final int CAFE_POOL_TARGET = 24;
  private static final int[] ACTIVITY_RADIUS_STEPS = {600, 900, 1300, 1800};
  private static final int[] FOOD_RADIUS_STEPS = {500, 900, 1400, 2200};
  private static final int[] CAFE_RADIUS_STEPS = {400, 800, 1200, 1800};

  private NearbyCourseEngine() {}

  public static List<CourseDto> recommendCourses(
      double latitude,
      double longitude,
      List<String> selectedKeywords,
      String courseOrder,
      String sortMode,
      long generationSeed)
      throws IOException, JSONException {
    SearchSession session = new SearchSession();
    List<String> safeKeywords = sanitizeKeywords(selectedKeywords);
    CategoryType[] selectedOrder = resolveCourseOrder(courseOrder);

    List<PlaceCandidate> activityPool =
        loadCategoryPool(
            session,
            buildActivityQueries(safeKeywords, generationSeed),
            latitude,
            longitude,
            ACTIVITY_RADIUS_STEPS,
            CategoryType.ACTIVITY,
            ACTIVITY_POOL_TARGET,
            generationSeed);

    List<PlaceCandidate> foodPool =
        loadCategoryPool(
            session,
            buildFoodQueries(safeKeywords, generationSeed + 17L),
            latitude,
            longitude,
            FOOD_RADIUS_STEPS,
            CategoryType.FOOD,
            FOOD_POOL_TARGET,
            generationSeed + 17L);

    List<PlaceCandidate> cafePool =
        loadCategoryPool(
            session,
            buildCafeQueries(safeKeywords, generationSeed + 41L),
            latitude,
            longitude,
            CAFE_RADIUS_STEPS,
            CategoryType.CAFE,
            CAFE_POOL_TARGET,
            generationSeed + 41L);

    if (activityPool.isEmpty() || foodPool.isEmpty() || cafePool.isEmpty()) {
      return Collections.emptyList();
    }

    List<CourseCandidate> candidates = new ArrayList<>();
    Set<String> usedCourseKeys = new LinkedHashSet<>();
    Map<String, Integer> stopUsage = new HashMap<>();

    buildCoursePass(
        candidates,
        usedCourseKeys,
        stopUsage,
        activityPool,
        foodPool,
        cafePool,
        selectedOrder,
        safeKeywords,
        generationSeed,
        false);

    if (candidates.size() < TARGET_COURSE_COUNT) {
      buildCoursePass(
          candidates,
          usedCourseKeys,
          stopUsage,
          activityPool,
          foodPool,
          cafePool,
          selectedOrder,
          safeKeywords,
          generationSeed + 97L,
          true);
    }

    if (candidates.isEmpty()) {
      return Collections.emptyList();
    }

    sortCourseCandidates(candidates, sortMode);

    List<CourseDto> result = new ArrayList<>();
    for (int i = 0; i < candidates.size() && i < TARGET_COURSE_COUNT; i++) {
      result.add(candidates.get(i).course);
    }
    return result;
  }

  private static void buildCoursePass(
      List<CourseCandidate> courses,
      Set<String> usedCourseKeys,
      Map<String, Integer> stopUsage,
      List<PlaceCandidate> activityPool,
      List<PlaceCandidate> foodPool,
      List<PlaceCandidate> cafePool,
      CategoryType[] courseOrder,
      List<String> selectedKeywords,
      long seed,
      boolean expandedDistance) {
    List<PlaceCandidate> firstPool =
        getPoolForType(courseOrder[0], activityPool, foodPool, cafePool);
    List<PlaceCandidate> secondPool =
        getPoolForType(courseOrder[1], activityPool, foodPool, cafePool);
    List<PlaceCandidate> thirdPool =
        getPoolForType(courseOrder[2], activityPool, foodPool, cafePool);
    List<PlaceCandidate> rotatedFirstPlaces = rotateWithinWindow(firstPool, seed, ROTATION_WINDOW);

    int firstToSecondLimit =
        resolveLegDistanceLimit(courseOrder[0], courseOrder[1], expandedDistance);
    int secondToThirdLimit =
        resolveLegDistanceLimit(courseOrder[1], courseOrder[2], expandedDistance);

    for (int i = 0; i < rotatedFirstPlaces.size() && courses.size() < MAX_GENERATED_COURSES; i++) {
      PlaceCandidate firstPlace = rotatedFirstPlaces.get(i);
      Set<String> usedMergeKeys = new HashSet<>();
      usedMergeKeys.add(firstPlace.mergeKey);

      PlaceCandidate secondPlace =
          selectNextPlace(
              firstPlace,
              secondPool,
              usedMergeKeys,
              stopUsage,
              seed + (31L * i),
              firstToSecondLimit);
      if (secondPlace == null) {
        continue;
      }

      usedMergeKeys.add(secondPlace.mergeKey);
      PlaceCandidate thirdPlace =
          selectNextPlace(
              secondPlace,
              thirdPool,
              usedMergeKeys,
              stopUsage,
              seed + (53L * i),
              secondToThirdLimit);
      if (thirdPlace == null) {
        continue;
      }

      String courseKey =
          firstPlace.mergeKey + "|" + secondPlace.mergeKey + "|" + thirdPlace.mergeKey;
      if (!usedCourseKeys.add(courseKey)) {
        continue;
      }

      int legOneDistance =
          roundDistance(
              distanceBetween(
                  firstPlace.latitude,
                  firstPlace.longitude,
                  secondPlace.latitude,
                  secondPlace.longitude));
      int legTwoDistance =
          roundDistance(
              distanceBetween(
                  secondPlace.latitude,
                  secondPlace.longitude,
                  thirdPlace.latitude,
                  thirdPlace.longitude));
      int totalDistance = firstPlace.userDistanceMeters + legOneDistance + legTwoDistance;

      courses.add(
          new CourseCandidate(
              new CourseDto(
                  buildCourseName(firstPlace),
                  Arrays.asList(
                      firstPlace.toPlaceDto(), secondPlace.toPlaceDto(), thirdPlace.toPlaceDto()),
                  buildCourseTags(
                      selectedKeywords,
                      Arrays.asList(firstPlace, secondPlace, thirdPlace),
                      courseOrder),
                  formatDistance(totalDistance)),
              firstPlace.userDistanceMeters,
              totalDistance,
              usesNaver(firstPlace, secondPlace, thirdPlace)));

      incrementUsage(stopUsage, firstPlace.uniqueKey);
      incrementUsage(stopUsage, secondPlace.uniqueKey);
      incrementUsage(stopUsage, thirdPlace.uniqueKey);
    }
  }

  private static PlaceCandidate selectNextPlace(
      PlaceCandidate anchor,
      List<PlaceCandidate> pool,
      Set<String> usedMergeKeys,
      Map<String, Integer> stopUsage,
      long seed,
      int maxLegDistanceMeters) {
    List<PlaceOption> options = new ArrayList<>();

    for (PlaceCandidate candidate : pool) {
      if (usedMergeKeys.contains(candidate.mergeKey)) {
        continue;
      }

      int legDistance =
          roundDistance(
              distanceBetween(
                  anchor.latitude, anchor.longitude, candidate.latitude, candidate.longitude));
      if (legDistance > maxLegDistanceMeters) {
        continue;
      }

      options.add(
          new PlaceOption(candidate, legDistance, stopUsage.getOrDefault(candidate.uniqueKey, 0)));
    }

    if (options.isEmpty()) {
      return null;
    }

    options.sort(
        Comparator.comparingInt((PlaceOption option) -> option.usageCount)
            .thenComparingInt(option -> option.legDistanceMeters)
            .thenComparingInt(option -> option.place.userDistanceMeters));

    int window = Math.min(ROTATION_WINDOW, options.size());
    int rotatedStart = positiveMod(seed, window);

    for (int offset = 0; offset < options.size(); offset++) {
      int index = offset;
      if (offset < window) {
        index = (rotatedStart + offset) % window;
      }
      PlaceOption option = options.get(index);
      if (option.usageCount <= 1 || offset >= window - 1) {
        return option.place;
      }
    }

    return options.get(0).place;
  }

  private static List<PlaceCandidate> loadCategoryPool(
      SearchSession session,
      List<String> queries,
      double latitude,
      double longitude,
      int[] radiusSteps,
      CategoryType categoryType,
      int desiredCount,
      long seed)
      throws IOException, JSONException {
    int maxRadius = radiusSteps[radiusSteps.length - 1];
    LinkedHashMap<String, PlaceCandidate> merged = new LinkedHashMap<>();

    for (String query : queries) {
      List<PlaceCandidate> rawResults;
      try {
        rawResults = searchPlaces(session, query, latitude, longitude, maxRadius);
      } catch (IOException | JSONException ignored) {
        continue;
      }

      for (PlaceCandidate candidate : rawResults) {
        if (!matchesCategory(candidate, categoryType)) {
          continue;
        }

        candidate.userDistanceMeters =
            roundDistance(
                distanceBetween(latitude, longitude, candidate.latitude, candidate.longitude));

        PlaceCandidate current = merged.get(candidate.mergeKey);
        if (current == null || shouldReplace(current, candidate)) {
          merged.put(candidate.mergeKey, candidate);
        }
      }

      if (merged.size() >= desiredCount * 2) {
        break;
      }
    }

    List<PlaceCandidate> sorted = new ArrayList<>(merged.values());
    sorted.sort(
        Comparator.comparingInt((PlaceCandidate candidate) -> candidate.userDistanceMeters)
            .thenComparing(candidate -> candidate.placeName));

    if (sorted.isEmpty()) {
      return sorted;
    }

    List<PlaceCandidate> filtered = new ArrayList<>();
    for (int radius : radiusSteps) {
      filtered = takeWithinRadius(sorted, radius);
      if (filtered.size() >= desiredCount) {
        break;
      }
    }

    if (filtered.isEmpty()) {
      filtered = sorted;
    }

    List<PlaceCandidate> trimmed = new ArrayList<>();
    for (int i = 0; i < filtered.size() && i < desiredCount; i++) {
      trimmed.add(filtered.get(i));
    }

    return rotateWithinWindow(trimmed, seed, ROTATION_WINDOW);
  }

  private static List<PlaceCandidate> searchPlaces(
      SearchSession session, String query, double latitude, double longitude, int kakaoRadius)
      throws IOException, JSONException {
    String cacheKey =
        String.format(Locale.US, "%s|%.5f|%.5f|%d", query, latitude, longitude, kakaoRadius);
    List<PlaceCandidate> cached = session.searchCache.get(cacheKey);
    if (cached != null) {
      return new ArrayList<>(cached);
    }

    LinkedHashMap<String, PlaceCandidate> merged = new LinkedHashMap<>();
    addCandidates(
        merged, fetchKakaoCoordinatePlaces(session, query, latitude, longitude, kakaoRadius));

    List<PlaceCandidate> result = new ArrayList<>(merged.values());
    result.sort(
        Comparator.comparingDouble(
                (PlaceCandidate candidate) ->
                    distanceBetween(latitude, longitude, candidate.latitude, candidate.longitude))
            .thenComparing(candidate -> candidate.placeName));

    session.searchCache.put(cacheKey, new ArrayList<>(result));
    return result;
  }

  private static List<PlaceCandidate> fetchKakaoCoordinatePlaces(
      SearchSession session, String query, double latitude, double longitude, int radius)
      throws IOException, JSONException {
    List<PlaceCandidate> result = new ArrayList<>();

    for (int page = 1; page <= KAKAO_MAX_PAGE; page++) {
      String response =
          getResponse(
              session,
              "/api/kakao-map/coordinate",
              buildParams(query, longitude, latitude, radius, page, true));
      JSONObject body = new JSONObject(response);
      JSONArray documents = body.optJSONArray("documents");
      if (documents == null || documents.length() == 0) {
        break;
      }

      for (int i = 0; i < documents.length(); i++) {
        JSONObject item = documents.optJSONObject(i);
        if (item == null) {
          continue;
        }

        PlaceCandidate candidate = parseKakaoPlace(item);
        if (candidate != null) {
          result.add(candidate);
        }
      }

      JSONObject meta = body.optJSONObject("meta");
      if (meta != null && meta.optBoolean("is_end", false)) {
        break;
      }
    }

    return result;
  }

  private static PlaceCandidate parseKakaoPlace(JSONObject item) {
    double longitude = parseDouble(item.optString("x", ""));
    double latitude = parseDouble(item.optString("y", ""));
    if (latitude == 0d || longitude == 0d) {
      return null;
    }

    String placeName = cleanText(item.optString("place_name", ""));
    if (placeName.isEmpty()) {
      return null;
    }

    String categoryName = cleanText(item.optString("category_name", ""));
    if (categoryName.isEmpty()) {
      categoryName = cleanText(item.optString("category_group_name", ""));
    }

    String address = cleanText(item.optString("road_address_name", ""));
    if (address.isEmpty()) {
      address = cleanText(item.optString("address_name", ""));
    }

    String providerId = cleanText(item.optString("id", ""));
    String categoryGroupCode = cleanText(item.optString("category_group_code", ""));
    return new PlaceCandidate(
        providerId.isEmpty()
            ? buildFallbackId("kakao", placeName, latitude, longitude)
            : providerId,
        "kakao",
        placeName,
        latitude,
        longitude,
        categoryName,
        categoryGroupCode,
        address,
        parseDistanceMeters(item.optString("distance", "")),
        cleanText(item.optString("place_url", "")));
  }

  private static boolean matchesCategory(PlaceCandidate candidate, CategoryType categoryType) {
    if (isExcludedPlace(candidate)) {
      return false;
    }

    boolean isCafe = isCafePlace(candidate);
    boolean isFood = isFoodPlace(candidate);

    if (categoryType == CategoryType.CAFE) {
      return isCafe;
    }
    if (categoryType == CategoryType.FOOD) {
      return isFood && !isCafe;
    }
    if (isCafe || isFood) {
      return false;
    }

    if ("AT4".equals(candidate.categoryGroupCode) || "CT1".equals(candidate.categoryGroupCode)) {
      return true;
    }

    String lowerCategory = candidate.categoryName.toLowerCase(Locale.KOREA);
    String lowerName = candidate.placeName.toLowerCase(Locale.KOREA);
    return containsAny(
            lowerCategory,
            "놀거리",
            "전시",
            "문화",
            "체험",
            "소품",
            "팝업",
            "공방",
            "오락",
            "박물관",
            "미술관",
            "관광",
            "공원",
            "테마")
        || containsAny(lowerName, "전시", "소품", "공방", "놀", "팝업", "뮤지엄", "갤러리", "체험");
  }

  private static boolean isFoodPlace(PlaceCandidate candidate) {
    if ("FD6".equals(candidate.categoryGroupCode)) {
      return true;
    }

    String text = (candidate.categoryName + " " + candidate.placeName).toLowerCase(Locale.KOREA);
    return containsAny(text, "음식점", "식당", "맛집", "한식", "양식", "일식", "중식", "주점", "브런치", "레스토랑")
        && !text.contains("카페");
  }

  private static boolean isCafePlace(PlaceCandidate candidate) {
    if ("CE7".equals(candidate.categoryGroupCode)) {
      return true;
    }

    String text = (candidate.categoryName + " " + candidate.placeName).toLowerCase(Locale.KOREA);
    return containsAny(text, "카페", "디저트", "커피", "베이커리", "티룸");
  }

  private static boolean isExcludedPlace(PlaceCandidate candidate) {
    String text =
        (candidate.placeName + " " + candidate.categoryName + " " + candidate.address)
            .toLowerCase(Locale.KOREA);

    return containsAny(
        text,
        "\uD559\uC6D0",
        "\uAD50\uC2B5\uC18C",
        "\uAD50\uC2B5",
        "\uC2A4\uD130\uB514\uCE74\uD398",
        "\uC2A4\uD130\uB514\uB8F8",
        "\uB3C5\uC11C\uC2E4",
        "\uACF5\uBD80\uBC29",
        "\uAD50\uC721",
        "\uBCF4\uC2B5",
        "\uC785\uC2DC",
        "\uC5B4\uD559",
        "\uD559\uC2B5\uAD00",
        "\uD559\uC2B5");
  }

  private static List<String> buildActivityQueries(List<String> keywords, long seed) {
    LinkedHashSet<String> queries = new LinkedHashSet<>();
    addMoodQueries(queries, keywords, "놀거리", "전시", "소품샵");
    for (String keyword : keywords) {
      String cleanKeyword = cleanText(keyword);
      if (cleanKeyword.contains("활동") || cleanKeyword.contains("액티브")) {
        queries.add(cleanKeyword + " 공방");
      }
    }
    queries.add("놀거리");
    queries.add("전시");
    queries.add("소품샵");
    queries.add("공방");
    queries.add("팝업");
    queries.add("체험");
    return rotateStrings(new ArrayList<>(queries), seed);
  }

  private static List<String> buildFoodQueries(List<String> keywords, long seed) {
    LinkedHashSet<String> queries = new LinkedHashSet<>();
    addMoodQueries(queries, keywords, "맛집", "음식점", "브런치");
    queries.add("맛집");
    queries.add("음식점");
    queries.add("브런치");
    return rotateStrings(new ArrayList<>(queries), seed);
  }

  private static List<String> buildCafeQueries(List<String> keywords, long seed) {
    LinkedHashSet<String> queries = new LinkedHashSet<>();
    addMoodQueries(queries, keywords, "카페", "디저트 카페", "감성 카페");
    queries.add("카페");
    queries.add("디저트 카페");
    queries.add("감성 카페");
    return rotateStrings(new ArrayList<>(queries), seed);
  }

  private static void addMoodQueries(
      Set<String> queries,
      List<String> keywords,
      String primary,
      String secondary,
      String tertiary) {
    for (String keyword : keywords) {
      String cleanKeyword = cleanText(keyword);
      if (cleanKeyword.isEmpty()) {
        continue;
      }
      queries.add(cleanKeyword + " " + primary);

      if (cleanKeyword.contains("분위기") || cleanKeyword.contains("감성")) {
        queries.add(cleanKeyword + " " + tertiary);
      }
      if (cleanKeyword.contains("조용")) {
        queries.add(cleanKeyword + " " + secondary);
      }
      if (cleanKeyword.contains("가성비")) {
        queries.add(cleanKeyword + " " + primary);
      }
      if (cleanKeyword.contains("비")) {
        queries.add("실내 " + primary);
      }
      if (cleanKeyword.contains("반려")) {
        queries.add("애견 동반 " + primary);
      }
    }
  }

  private static List<CourseCandidate> sortCourseCandidates(
      List<CourseCandidate> candidates, String sortMode) {
    if (RecommendFlowContract.SORT_DISTANCE.equals(sortMode)) {
      candidates.sort(
          Comparator.comparingInt((CourseCandidate candidate) -> candidate.totalDistanceMeters)
              .thenComparingInt(candidate -> candidate.startDistanceMeters));
      return candidates;
    }

    if (RecommendFlowContract.SORT_REVIEW.equals(sortMode)) {
      candidates.sort(
          Comparator.comparing((CourseCandidate candidate) -> !candidate.includesNaver)
              .thenComparingInt(candidate -> candidate.totalDistanceMeters)
              .thenComparingInt(candidate -> candidate.startDistanceMeters));
      return candidates;
    }

    candidates.sort(
        Comparator.comparingInt(
                (CourseCandidate candidate) ->
                    candidate.startDistanceMeters + (candidate.totalDistanceMeters / 2))
            .thenComparing((CourseCandidate candidate) -> !candidate.includesNaver)
            .thenComparing(candidate -> candidate.course.courseName));
    return candidates;
  }

  private static String buildCourseName(PlaceCandidate firstPlace) {
    String placeName = firstPlace.placeName;
    if (placeName.length() > 12) {
      placeName = placeName.substring(0, 12).trim();
    }
    return placeName + " 근처 코스";
  }

  private static List<String> buildCourseTags(
      List<String> selectedKeywords, PlaceCandidate activity) {
    LinkedHashSet<String> tags = new LinkedHashSet<>();

    for (String keyword : selectedKeywords) {
      String cleanKeyword = cleanText(keyword);
      if (!cleanKeyword.isEmpty()) {
        tags.add(cleanKeyword);
      }
      if (tags.size() >= 2) {
        break;
      }
    }

    if (tags.size() < 3 && !activity.categoryName.isEmpty()) {
      tags.add(compactCategoryTag(activity.categoryName));
    }
    if (tags.size() < 3) {
      tags.add("맛집");
    }
    if (tags.size() < 3) {
      tags.add("카페");
    }

    return new ArrayList<>(tags);
  }

  private static List<String> buildCourseTags(
      List<String> selectedKeywords,
      List<PlaceCandidate> orderedPlaces,
      CategoryType[] courseOrder) {
    LinkedHashSet<String> tags = new LinkedHashSet<>();

    for (String keyword : selectedKeywords) {
      String cleanKeyword = cleanText(keyword);
      if (!cleanKeyword.isEmpty()) {
        tags.add(cleanKeyword);
      }
      if (tags.size() >= 2) {
        break;
      }
    }

    for (PlaceCandidate place : orderedPlaces) {
      if (tags.size() >= 3) {
        break;
      }
      if (!place.categoryName.isEmpty()) {
        tags.add(compactCategoryTag(place.categoryName));
      }
    }

    for (CategoryType type : courseOrder) {
      if (tags.size() >= 3) {
        break;
      }
      tags.add(defaultTagForType(type));
    }

    return new ArrayList<>(tags);
  }

  private static String defaultTagForType(CategoryType type) {
    if (type == CategoryType.FOOD) {
      return "맛집";
    }
    if (type == CategoryType.CAFE) {
      return "카페";
    }
    return "놀거리";
  }

  private static CategoryType[] resolveCourseOrder(String courseOrder) {
    if (RecommendFlowContract.COURSE_ORDER_ACTIVITY_CAFE_FOOD.equals(courseOrder)) {
      return new CategoryType[] {CategoryType.ACTIVITY, CategoryType.CAFE, CategoryType.FOOD};
    }
    if (RecommendFlowContract.COURSE_ORDER_FOOD_ACTIVITY_CAFE.equals(courseOrder)) {
      return new CategoryType[] {CategoryType.FOOD, CategoryType.ACTIVITY, CategoryType.CAFE};
    }
    if (RecommendFlowContract.COURSE_ORDER_FOOD_CAFE_ACTIVITY.equals(courseOrder)) {
      return new CategoryType[] {CategoryType.FOOD, CategoryType.CAFE, CategoryType.ACTIVITY};
    }
    if (RecommendFlowContract.COURSE_ORDER_CAFE_ACTIVITY_FOOD.equals(courseOrder)) {
      return new CategoryType[] {CategoryType.CAFE, CategoryType.ACTIVITY, CategoryType.FOOD};
    }
    if (RecommendFlowContract.COURSE_ORDER_CAFE_FOOD_ACTIVITY.equals(courseOrder)) {
      return new CategoryType[] {CategoryType.CAFE, CategoryType.FOOD, CategoryType.ACTIVITY};
    }
    return new CategoryType[] {CategoryType.ACTIVITY, CategoryType.FOOD, CategoryType.CAFE};
  }

  private static List<PlaceCandidate> getPoolForType(
      CategoryType type,
      List<PlaceCandidate> activityPool,
      List<PlaceCandidate> foodPool,
      List<PlaceCandidate> cafePool) {
    if (type == CategoryType.FOOD) {
      return foodPool;
    }
    if (type == CategoryType.CAFE) {
      return cafePool;
    }
    return activityPool;
  }

  private static int resolveLegDistanceLimit(
      CategoryType from, CategoryType to, boolean expandedDistance) {
    if (areSamePair(from, to, CategoryType.ACTIVITY, CategoryType.FOOD)) {
      return expandedDistance ? 1200 : 850;
    }
    if (areSamePair(from, to, CategoryType.FOOD, CategoryType.CAFE)) {
      return expandedDistance ? 900 : 650;
    }
    return expandedDistance ? 1100 : 800;
  }

  private static boolean areSamePair(
      CategoryType first, CategoryType second, CategoryType expectedA, CategoryType expectedB) {
    return (first == expectedA && second == expectedB)
        || (first == expectedB && second == expectedA);
  }

  private static String compactCategoryTag(String categoryName) {
    String[] segments = categoryName.split(">");
    String value = segments[segments.length - 1].trim();
    if (value.isEmpty()) {
      return "놀거리";
    }
    return value.length() > 8 ? value.substring(0, 8).trim() : value;
  }

  private static boolean usesNaver(PlaceCandidate... places) {
    return false;
  }

  private static boolean shouldReplace(PlaceCandidate current, PlaceCandidate candidate) {
    if (candidate.userDistanceMeters != current.userDistanceMeters) {
      return candidate.userDistanceMeters < current.userDistanceMeters;
    }
    return "kakao".equals(candidate.provider) && "naver".equals(current.provider);
  }

  private static List<PlaceCandidate> takeWithinRadius(
      List<PlaceCandidate> candidates, int radius) {
    List<PlaceCandidate> result = new ArrayList<>();
    for (PlaceCandidate candidate : candidates) {
      if (candidate.userDistanceMeters <= radius) {
        result.add(candidate);
      }
    }
    return result;
  }

  private static List<PlaceCandidate> rotateWithinWindow(
      List<PlaceCandidate> source, long seed, int windowSize) {
    if (source.size() <= 1) {
      return new ArrayList<>(source);
    }

    List<PlaceCandidate> result = new ArrayList<>(source);
    int window = Math.min(windowSize, result.size());
    int rotatedStart = positiveMod(seed, window);
    List<PlaceCandidate> head = new ArrayList<>(result.subList(0, window));
    List<PlaceCandidate> rotated = new ArrayList<>(result.size());

    for (int i = 0; i < window; i++) {
      rotated.add(head.get((rotatedStart + i) % window));
    }
    for (int i = window; i < result.size(); i++) {
      rotated.add(result.get(i));
    }
    return rotated;
  }

  private static List<String> rotateStrings(List<String> source, long seed) {
    if (source.isEmpty()) {
      return source;
    }

    int window = Math.min(ROTATION_WINDOW, source.size());
    int start = positiveMod(seed, window);
    List<String> result = new ArrayList<>(source.size());

    for (int i = 0; i < window; i++) {
      result.add(source.get((start + i) % window));
    }
    for (int i = window; i < source.size(); i++) {
      result.add(source.get(i));
    }
    return result;
  }

  private static void addCandidates(
      LinkedHashMap<String, PlaceCandidate> merged, List<PlaceCandidate> candidates) {
    for (PlaceCandidate candidate : candidates) {
      PlaceCandidate current = merged.get(candidate.mergeKey);
      if (current == null || candidate.distanceMeters < current.distanceMeters) {
        merged.put(candidate.mergeKey, candidate);
      }
    }
  }

  private static Map<String, String> buildParams(
      String query,
      double longitude,
      double latitude,
      int radius,
      int page,
      boolean includeRadius) {
    Map<String, String> params = new LinkedHashMap<>();
    params.put("query", query);
    params.put("x", String.format(Locale.US, "%.7f", longitude));
    params.put("y", String.format(Locale.US, "%.7f", latitude));
    if (includeRadius) {
      params.put("radius", String.valueOf(radius));
    }
    params.put("page", String.valueOf(page));
    return params;
  }

  private static String getResponse(SearchSession session, String path, Map<String, String> params)
      throws IOException {
    String url = buildUrl(path, params);
    String cached = session.responseCache.get(url);
    if (cached != null) {
      return cached;
    }

    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestMethod("GET");
    connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
    connection.setReadTimeout(READ_TIMEOUT_MS);
    connection.setRequestProperty("Accept", "application/json");

    int responseCode = connection.getResponseCode();
    InputStream stream =
        responseCode >= 200 && responseCode < 300
            ? connection.getInputStream()
            : connection.getErrorStream();
    String body = readStream(stream);
    connection.disconnect();

    if (responseCode < 200 || responseCode >= 300) {
      throw new IOException("Request failed: " + responseCode + " " + url);
    }

    session.responseCache.put(url, body);
    return body;
  }

  private static String buildUrl(String path, Map<String, String> params) {
    Uri.Builder builder = Uri.parse(BASE_URL + path).buildUpon();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      builder.appendQueryParameter(entry.getKey(), entry.getValue());
    }
    return builder.build().toString();
  }

  private static String readStream(InputStream stream) throws IOException {
    if (stream == null) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        builder.append(line);
      }
    }
    return builder.toString();
  }

  private static List<String> sanitizeKeywords(List<String> selectedKeywords) {
    if (selectedKeywords == null || selectedKeywords.isEmpty()) {
      return Collections.emptyList();
    }

    LinkedHashSet<String> keywords = new LinkedHashSet<>();
    for (String keyword : selectedKeywords) {
      String cleanKeyword = cleanText(keyword);
      if (!cleanKeyword.isEmpty()) {
        keywords.add(cleanKeyword);
      }
    }
    return new ArrayList<>(keywords);
  }

  private static String cleanText(String value) {
    return value == null ? "" : value.replace("&gt;", ">").trim();
  }

  private static boolean containsAny(String value, String... needles) {
    for (String needle : needles) {
      if (value.contains(needle.toLowerCase(Locale.KOREA))) {
        return true;
      }
    }
    return false;
  }

  private static int parseDistanceMeters(String raw) {
    String value = cleanText(raw).toLowerCase(Locale.US);
    if (value.isEmpty()) {
      return Integer.MAX_VALUE;
    }

    try {
      if (value.endsWith("km")) {
        double kilometers = Double.parseDouble(value.replace("km", "").trim());
        return (int) Math.round(kilometers * 1000d);
      }
      return (int) Math.round(Double.parseDouble(value.replace("m", "").trim()));
    } catch (NumberFormatException ignored) {
      return Integer.MAX_VALUE;
    }
  }

  private static double parseDouble(String value) {
    try {
      return Double.parseDouble(cleanText(value));
    } catch (NumberFormatException ignored) {
      return 0d;
    }
  }

  private static int roundDistance(double meters) {
    return (int) Math.round(meters);
  }

  private static void incrementUsage(Map<String, Integer> usageMap, String key) {
    usageMap.put(key, usageMap.getOrDefault(key, 0) + 1);
  }

  private static int positiveMod(long value, int mod) {
    if (mod <= 0) {
      return 0;
    }
    long result = value % mod;
    return (int) (result < 0 ? result + mod : result);
  }

  private static double distanceBetween(
      double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
    double earthRadius = 6371000d;
    double dLat = Math.toRadians(endLatitude - startLatitude);
    double dLng = Math.toRadians(endLongitude - startLongitude);
    double a =
        Math.sin(dLat / 2d) * Math.sin(dLat / 2d)
            + Math.cos(Math.toRadians(startLatitude))
                * Math.cos(Math.toRadians(endLatitude))
                * Math.sin(dLng / 2d)
                * Math.sin(dLng / 2d);
    double c = 2d * Math.atan2(Math.sqrt(a), Math.sqrt(1d - a));
    return earthRadius * c;
  }

  private static String formatDistance(int meters) {
    if (meters < 1000) {
      return meters + "m";
    }
    return String.format(Locale.KOREA, "%.1fkm", meters / 1000d);
  }

  private static String buildFallbackId(
      String provider, String placeName, double latitude, double longitude) {
    return provider
        + "_"
        + normalizeName(placeName)
        + "_"
        + String.format(Locale.US, "%.4f_%.4f", latitude, longitude);
  }

  private static String normalizeName(String placeName) {
    return cleanText(placeName).replaceAll("\\s+", "").toLowerCase(Locale.KOREA);
  }

  private enum CategoryType {
    ACTIVITY,
    FOOD,
    CAFE
  }

  private static final class SearchSession {
    private final Map<String, String> responseCache = new HashMap<>();
    private final Map<String, List<PlaceCandidate>> searchCache = new HashMap<>();
  }

  private static final class PlaceCandidate {
    private final String provider;
    private final String uniqueKey;
    private final String mergeKey;
    private final String placeName;
    private final double latitude;
    private final double longitude;
    private final String categoryName;
    private final String categoryGroupCode;
    private final String address;
    private final int distanceMeters;
    private final String placeUrl;
    private int userDistanceMeters;

    private PlaceCandidate(
        String providerId,
        String provider,
        String placeName,
        double latitude,
        double longitude,
        String categoryName,
        String categoryGroupCode,
        String address,
        int distanceMeters,
        String placeUrl) {
      this.provider = provider;
      this.uniqueKey = provider + ":" + providerId;
      this.mergeKey =
          normalizeName(placeName)
              + "|"
              + String.format(Locale.US, "%.4f", latitude)
              + "|"
              + String.format(Locale.US, "%.4f", longitude);
      this.placeName = placeName;
      this.latitude = latitude;
      this.longitude = longitude;
      this.categoryName = categoryName;
      this.categoryGroupCode = categoryGroupCode;
      this.address = address;
      this.distanceMeters = distanceMeters == Integer.MAX_VALUE ? 999999 : distanceMeters;
      this.placeUrl = placeUrl;
      this.userDistanceMeters = this.distanceMeters;
    }

    private PlaceDto toPlaceDto() {
      return new PlaceDto(
          placeName, latitude, longitude, categoryName, address, userDistanceMeters, placeUrl);
    }
  }

  private static final class PlaceOption {
    private final PlaceCandidate place;
    private final int legDistanceMeters;
    private final int usageCount;

    private PlaceOption(PlaceCandidate place, int legDistanceMeters, int usageCount) {
      this.place = place;
      this.legDistanceMeters = legDistanceMeters;
      this.usageCount = usageCount;
    }
  }

  private static final class CourseCandidate {
    private final CourseDto course;
    private final int startDistanceMeters;
    private final int totalDistanceMeters;
    private final boolean includesNaver;

    private CourseCandidate(
        CourseDto course, int startDistanceMeters, int totalDistanceMeters, boolean includesNaver) {
      this.course = course;
      this.startDistanceMeters = startDistanceMeters;
      this.totalDistanceMeters = totalDistanceMeters;
      this.includesNaver = includesNaver;
    }
  }
}
