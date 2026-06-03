package com.example.day2day.data.remote;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class KakaoCoordinateResponse {

  @SerializedName("documents")
  private List<Document> documents;

  public List<BackendPlaceResponse> toBackendPlaces() {
    List<BackendPlaceResponse> places = new ArrayList<>();
    if (documents == null) {
      return places;
    }

    for (Document document : documents) {
      places.add(document.toBackendPlaceResponse());
    }
    return places;
  }

  static class Document {
    @SerializedName("place_name")
    private String placeName;

    @SerializedName("road_address_name")
    private String roadAddressName;

    @SerializedName("address_name")
    private String addressName;

    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("x")
    private String x;

    @SerializedName("y")
    private String y;

    private BackendPlaceResponse toBackendPlaceResponse() {
      BackendPlaceResponse place = new BackendPlaceResponse();
      place.setName(placeName);
      place.setRoadAddress(
          roadAddressName != null && !roadAddressName.isEmpty() ? roadAddressName : addressName);
      place.setCategory(categoryName);
      place.setX(x);
      place.setY(y);
      return place;
    }
  }
}
