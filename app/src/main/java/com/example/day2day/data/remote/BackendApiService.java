package com.example.day2day.data.remote;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BackendApiService {

  @GET("/api/naver-map/search")
  Call<List<BackendPlaceResponse>> searchNaverMap(
      @Query("q") String query, @Query("page") Integer page);

  @GET("/api/naver-map/coordinate")
  Call<List<BackendPlaceResponse>> searchNaverMapByCoordinate(
      @Query("q") String query, @Query("x") String longitude, @Query("y") String latitude);

  @GET("/api/kakao-map/search")
  Call<List<BackendPlaceResponse>> searchKakaoMap(
      @Query("q") String query, @Query("page") Integer page);
}
