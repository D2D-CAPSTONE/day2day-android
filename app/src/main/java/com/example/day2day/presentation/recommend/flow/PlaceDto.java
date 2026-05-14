package com.example.day2day.presentation.recommend.flow;

public class PlaceDto {
  public String placeName;
  public double latitude;
  public double longitude;
  public String categoryName;
  public String address;
  public int distanceMeters;
  public String placeUrl;

  public PlaceDto(String placeName, double latitude, double longitude) {
    this(placeName, latitude, longitude, "", "", 0, "");
  }

  public PlaceDto(
      String placeName,
      double latitude,
      double longitude,
      String categoryName,
      String address,
      int distanceMeters,
      String placeUrl) {
    this.placeName = placeName;
    this.latitude = latitude;
    this.longitude = longitude;
    this.categoryName = categoryName;
    this.address = address;
    this.distanceMeters = distanceMeters;
    this.placeUrl = placeUrl;
  }
}
