package com.example.day2day.data.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NearbyCourseEngine {

  private final BackendApiService apiService;

  public NearbyCourseEngine() {
    Gson gson = new GsonBuilder().setLenient().create();

    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl("http://34.47.126.220/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    apiService = retrofit.create(BackendApiService.class);
  }

  public interface FetchPlacesCallback {
    void onSuccess(List<BackendPlaceResponse> places);

    void onFailure(String errorMessage);
  }

  public void fetchNearbyPlaces(String keyword, FetchPlacesCallback callback) {
    Call<List<BackendPlaceResponse>> call = apiService.searchNaverMap(keyword, 1);

    call.enqueue(
        new Callback<List<BackendPlaceResponse>>() {
          @Override
          public void onResponse(
              Call<List<BackendPlaceResponse>> call,
              Response<List<BackendPlaceResponse>> response) {
            if (response.isSuccessful() && response.body() != null) {
              callback.onSuccess(response.body());
            } else {
              callback.onFailure("Fetcher 서버 연동 실패: " + response.code());
            }
          }

          @Override
          public void onFailure(Call<List<BackendPlaceResponse>> call, Throwable t) {
            callback.onFailure("네트워크 오류 발생: " + t.getMessage());
          }
        });
  }
}
