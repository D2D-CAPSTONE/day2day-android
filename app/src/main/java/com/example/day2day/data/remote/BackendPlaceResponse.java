package com.example.day2day.data.remote;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BackendPlaceResponse {
  @SerializedName("name")
  private String name;

  @SerializedName(
      value = "roadAddress",
      alternate = {"new_address", "address"})
  private String roadAddress;

  @SerializedName(
      value = "category",
      alternate = {"last_cate_name", "cate_name_depth2", "cate_name_depth3"})
  private Object category;

  @SerializedName(
      value = "imageUrl",
      alternate = {"img"})
  private String imageUrl;

  @SerializedName(
      value = "x",
      alternate = {"lon"})
  private String x;

  @SerializedName(
      value = "y",
      alternate = {"lat"})
  private String y;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRoadAddress() {
    return roadAddress;
  }

  public void setRoadAddress(String roadAddress) {
    this.roadAddress = roadAddress;
  }

  public String getCategory() {
    if (category == null) {
      return null;
    }
    if (category instanceof String) {
      return (String) category;
    } else if (category instanceof List) {
      List<?> categoryList = (List<?>) category;
      if (categoryList.isEmpty()) {
        return "";
      }
      return String.valueOf(categoryList.get(0));
    }
    return String.valueOf(category);
  }

  public void setCategory(Object category) {
    this.category = category;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getX() {
    return x;
  }

  public void setX(String x) {
    this.x = x;
  }

  public String getY() {
    return y;
  }

  public void setY(String y) {
    this.y = y;
  }
}
