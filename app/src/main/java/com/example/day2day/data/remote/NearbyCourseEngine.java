package com.example.day2day.data.remote;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NearbyCourseEngine {

  private static final String TAG = "NearbyCourseEngine";
  private static final int DEFAULT_COORDINATE_RADIUS_METERS = 2000;

  private final BackendApiService apiService;

  public NearbyCourseEngine() {
    Gson gson = new GsonBuilder().setLenient().create();

    OkHttpClient okHttpClient =
        new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl("http://34.47.126.220/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    apiService = retrofit.create(BackendApiService.class);
  }

  public interface FetchPlacesCallback {
    void onSuccess(List<BackendPlaceResponse> places);

    void onFailure(String errorMessage);
  }

  public void fetchNearbyPlaces(String keyword, FetchPlacesCallback callback) {
    Call<List<BackendPlaceResponse>> naverCall = apiService.searchNaverMap(keyword, 1);

    naverCall.enqueue(
        new Callback<List<BackendPlaceResponse>>() {
          @Override
          public void onResponse(
              Call<List<BackendPlaceResponse>> call,
              Response<List<BackendPlaceResponse>> response) {
            if (response.isSuccessful() && response.body() != null) {
              callback.onSuccess(response.body());
            } else {
              Log.w(TAG, "Naver search failed (" + response.code() + "). Falling back to Kakao.");
              fetchWithKakao(keyword, callback);
            }
          }

          @Override
          public void onFailure(Call<List<BackendPlaceResponse>> call, Throwable t) {
            Log.w(TAG, "Naver search network error. Falling back to Kakao.", t);
            fetchWithKakao(keyword, callback);
          }
        });
  }

  private void fetchWithKakao(String keyword, FetchPlacesCallback callback) {
    Call<List<BackendPlaceResponse>> kakaoCall = apiService.searchKakaoMap(keyword, 1);
    kakaoCall.enqueue(
        new Callback<List<BackendPlaceResponse>>() {
          @Override
          public void onResponse(
              Call<List<BackendPlaceResponse>> call,
              Response<List<BackendPlaceResponse>> response) {
            if (response.isSuccessful() && response.body() != null) {
              callback.onSuccess(response.body());
            } else {
              callback.onFailure("All place search APIs failed: " + response.code());
            }
          }

          @Override
          public void onFailure(Call<List<BackendPlaceResponse>> call, Throwable t) {
            callback.onFailure("All place search APIs network error: " + t.getMessage());
          }
        });
  }

  public void fetchNearbyPlacesByCoordinate(
      String keyword, String longitude, String latitude, FetchPlacesCallback callback) {
    Call<List<BackendPlaceResponse>> naverCall =
        apiService.searchNaverMapByCoordinate(keyword, longitude, latitude);
    naverCall.enqueue(
        new Callback<List<BackendPlaceResponse>>() {
          @Override
          public void onResponse(
              Call<List<BackendPlaceResponse>> call,
              Response<List<BackendPlaceResponse>> response) {
            if (response.isSuccessful() && response.body() != null) {
              callback.onSuccess(response.body());
            } else {
              Log.w(
                  TAG,
                  "Naver coordinate search failed ("
                      + response.code()
                      + "). Falling back to Kakao coordinate search.");
              fetchCoordinateWithKakao(keyword, longitude, latitude, callback);
            }
          }

          @Override
          public void onFailure(Call<List<BackendPlaceResponse>> call, Throwable t) {
            Log.w(TAG, "Naver coordinate search network error. Falling back to Kakao.", t);
            fetchCoordinateWithKakao(keyword, longitude, latitude, callback);
          }
        });
  }

  private void fetchCoordinateWithKakao(
      String keyword, String longitude, String latitude, FetchPlacesCallback callback) {
    Call<KakaoCoordinateResponse> kakaoCall =
        apiService.searchKakaoMapByCoordinate(
            keyword, longitude, latitude, DEFAULT_COORDINATE_RADIUS_METERS);

    kakaoCall.enqueue(
        new Callback<KakaoCoordinateResponse>() {
          @Override
          public void onResponse(
              Call<KakaoCoordinateResponse> call, Response<KakaoCoordinateResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
              callback.onSuccess(response.body().toBackendPlaces());
            } else {
              callback.onFailure("Coordinate place search failed: " + response.code());
            }
          }

          @Override
          public void onFailure(Call<KakaoCoordinateResponse> call, Throwable t) {
            callback.onFailure("Coordinate place search network error: " + t.getMessage());
          }
        });
  }

  public String resolveImageUrl(String placeName, double latitude, double longitude) {
    try {
      Response<List<BackendPlaceResponse>> response =
          apiService.searchKakaoMap(placeName, 1).execute();
      if (!response.isSuccessful() || response.body() == null) {
        return null;
      }
      return pickBestImageUrl(response.body(), placeName, latitude, longitude);
    } catch (IOException e) {
      Log.w(TAG, "Failed to resolve image for place: " + placeName, e);
      return null;
    }
  }

  private String pickBestImageUrl(
      List<BackendPlaceResponse> candidates, String placeName, double latitude, double longitude) {
    String normalizedTarget = normalizePlaceName(placeName);
    BackendPlaceResponse bestExact = null;
    double bestExactDistance = Double.MAX_VALUE;
    BackendPlaceResponse bestSimilar = null;
    double bestSimilarDistance = Double.MAX_VALUE;
    BackendPlaceResponse bestAny = null;
    double bestAnyDistance = Double.MAX_VALUE;

    for (BackendPlaceResponse candidate : candidates) {
      String imageUrl = candidate.getImageUrl();
      if (imageUrl == null || imageUrl.isEmpty()) {
        continue;
      }

      double candidateDistance = distanceFromPlace(latitude, longitude, candidate);
      String normalizedCandidate = normalizePlaceName(candidate.getName());

      if (normalizedCandidate.equals(normalizedTarget)) {
        if (candidateDistance < bestExactDistance) {
          bestExact = candidate;
          bestExactDistance = candidateDistance;
        }
        continue;
      }

      if (normalizedCandidate.contains(normalizedTarget)
          || normalizedTarget.contains(normalizedCandidate)) {
        if (candidateDistance < bestSimilarDistance) {
          bestSimilar = candidate;
          bestSimilarDistance = candidateDistance;
        }
      }

      if (candidateDistance < bestAnyDistance) {
        bestAny = candidate;
        bestAnyDistance = candidateDistance;
      }
    }

    if (bestExact != null) {
      return bestExact.getImageUrl();
    }
    if (bestSimilar != null) {
      return bestSimilar.getImageUrl();
    }
    return bestAny != null ? bestAny.getImageUrl() : null;
  }

  private String normalizePlaceName(String placeName) {
    if (placeName == null) {
      return "";
    }
    return placeName.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
  }

  private double distanceFromPlace(
      double targetLatitude, double targetLongitude, BackendPlaceResponse candidate) {
    try {
      double candidateLongitude = Double.parseDouble(candidate.getX());
      double candidateLatitude = Double.parseDouble(candidate.getY());
      return calculateDistanceMeters(
          targetLatitude, targetLongitude, candidateLatitude, candidateLongitude);
    } catch (Exception ignored) {
      return Double.MAX_VALUE;
    }
  }

  private double calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
    final int earthRadiusMeters = 6371000;
    double phi1 = Math.toRadians(lat1);
    double phi2 = Math.toRadians(lat2);
    double deltaPhi = Math.toRadians(lat2 - lat1);
    double deltaLambda = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2)
            + Math.cos(phi1)
                * Math.cos(phi2)
                * Math.sin(deltaLambda / 2)
                * Math.sin(deltaLambda / 2);
    return earthRadiusMeters * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }
}
