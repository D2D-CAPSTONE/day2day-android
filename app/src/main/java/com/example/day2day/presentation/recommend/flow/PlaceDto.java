package com.example.day2day.presentation.recommend.flow;

import java.io.Serializable;

public class PlaceDto implements Serializable {
  public String placeName;
  public double latitude;
  public double longitude;

  public PlaceDto(String placeName, double latitude, double longitude) {
    this.placeName = placeName;
    this.latitude = latitude;
    this.longitude = longitude;
  }
}
