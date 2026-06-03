package com.example.day2day.data.remote;

import com.example.day2day.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherEngine {
  private final WeatherApiService apiService;

  public WeatherEngine() {
    Gson gson = new GsonBuilder().setLenient().create();

    OkHttpClient okHttpClient =
        new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    apiService = retrofit.create(WeatherApiService.class);
  }

  public interface FetchWeatherCallback {
    void onSuccess(WeatherResponse weather);

    void onFailure(String errorMessage);
  }

  public void fetchWeather(double lat, double lon, FetchWeatherCallback callback) {
    Call<WeatherResponse> call =
        apiService.getCurrentWeather(lat, lon, BuildConfig.WEATHER_API_KEY, "metric", "kr");
    call.enqueue(
        new Callback<WeatherResponse>() {
          @Override
          public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
              callback.onSuccess(response.body());
            } else {
              callback.onFailure("날씨 서버 오류: " + response.code());
            }
          }

          @Override
          public void onFailure(Call<WeatherResponse> call, Throwable t) {
            callback.onFailure("네트워크 오류: " + t.getMessage());
          }
        });
  }
}
