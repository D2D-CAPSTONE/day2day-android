package com.example.day2day.data.remote;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NearbyCourseEngine {

  private static final String TAG = "NearbyCourseEngine";
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
              Log.w(TAG, "네이버 API 실패 (" + response.code() + "). 카카오 API로 재시도합니다.");
              fetchWithKakao(keyword, callback);
            }
          }

          @Override
          public void onFailure(Call<List<BackendPlaceResponse>> call, Throwable t) {
            Log.w(TAG, "네이버 API 네트워크 오류. 카카오 API로 재시도합니다.", t);
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
              callback.onFailure("모든 지도 API 연동 실패: " + response.code());
            }
          }

          @Override
          public void onFailure(Call<List<BackendPlaceResponse>> call, Throwable t) {
            callback.onFailure("모든 지도 API 네트워크 오류: " + t.getMessage());
          }
        });
  }

  public void fetchNearbyPlacesByCoordinate(
      String keyword, String longitude, String latitude, FetchPlacesCallback callback) {
    Call<List<BackendPlaceResponse>> call =
        apiService.searchNaverMapByCoordinate(keyword, longitude, latitude);
    call.enqueue(
        new Callback<List<BackendPlaceResponse>>() {
          @Override
          public void onResponse(
              Call<List<BackendPlaceResponse>> call,
              Response<List<BackendPlaceResponse>> response) {
            if (response.isSuccessful() && response.body() != null) {
              callback.onSuccess(response.body());
            } else {
              callback.onFailure("좌표 기반 검색 실패: " + response.code());
            }
          }

          @Override
          public void onFailure(Call<List<BackendPlaceResponse>> call, Throwable t) {
            callback.onFailure("좌표 기반 검색 네트워크 오류: " + t.getMessage());
          }
        });
  }
}
