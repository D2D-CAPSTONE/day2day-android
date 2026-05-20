package com.example.day2day.presentation.recommend.flow;

public class PlaceDto {
  public String placeName;
  public double latitude;
  public double longitude;

  public PlaceDto(String placeName, double latitude, double longitude) {
    this.placeName = placeName;
    this.latitude = latitude;
    this.longitude = longitude;
  }
}
