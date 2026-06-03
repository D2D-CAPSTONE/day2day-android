package com.example.day2day.data.remote;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {
  @SerializedName("name")
  private String name;

  @SerializedName("main")
  private Main main;

  @SerializedName("weather")
  private List<Weather> weather;

  public String getName() {
    return name;
  }

  public double getTemp() {
    return main != null ? main.temp : 0;
  }

  public String getWeatherMain() {
    return (weather != null && !weather.isEmpty()) ? weather.get(0).main : null;
  }

  public String getDescription() {
    return (weather != null && !weather.isEmpty()) ? weather.get(0).description : null;
  }

  public static class Main {
    @SerializedName("temp")
    double temp;
  }

  public static class Weather {
    @SerializedName("main")
    String main;

    @SerializedName("description")
    String description;
  }
}
