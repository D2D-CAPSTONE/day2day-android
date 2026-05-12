package com.example.day2day.presentation.recommend.flow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.day2day.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapSelectionActivity extends AppCompatActivity {

  private SeoulMapView.District selectedDistrict = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map_selection);

    SeoulMapView mapView = findViewById(R.id.seoul_map);
    TextView tvSelected = findViewById(R.id.tv_selected_name);
    Button btnConfirm = findViewById(R.id.btn_map_selection_confirm);

    TextView tvBack = findViewById(R.id.tv_back);
    TextView tvClose = findViewById(R.id.tv_close);
    TextView tvReset = findViewById(R.id.tv_reset);

    // 구를 탭했을 때
    mapView.setOnDistrictSelectedListener(
        district -> {
          selectedDistrict = district;
          tvSelected.setText(district.koreanName);
          btnConfirm.setEnabled(true);
          btnConfirm.setText(district.koreanName + " 코스 추천받기");
        });

    // 확인 버튼
    btnConfirm.setOnClickListener(
        v -> {
          if (selectedDistrict == null) return;
          // TODO: 다음 화면으로 이동
          // Intent intent = new Intent(this, CourseMapPageActivity.class);
          // intent.putExtra("district_key", selectedDistrict.key);
          // intent.putExtra("district_name", selectedDistrict.koreanName);
          // startActivity(intent);
        });

    // 뒤로/닫기
    tvBack.setOnClickListener(v -> finish());
    tvClose.setOnClickListener(v -> finish());

    // 리셋
    tvReset.setOnClickListener(
        v -> {
          selectedDistrict = null;
          mapView.setSelectedKey(null);
          tvSelected.setText("-");
          btnConfirm.setEnabled(false);
          btnConfirm.setText("지역을 선택해주세요");
        });
  }

  // ==========================================================================================
  // 서울 25개 구를 그리고 탭 이벤트를 받는 커스텀 View (내부 정적 클래스)
  // 레이아웃 XML에서는 다음 경로로 참조됩니다:
  //   <com.example.day2day.presentation.recommend.flow.MapSelectionActivity$SeoulMapView .../>
  // ==========================================================================================
  public static class SeoulMapView extends View {

    public static class District {
      public final String key;
      public final String koreanName;
      public final String pathData;

      public District(String key, String koreanName, String pathData) {
        this.key = key;
        this.koreanName = koreanName;
        this.pathData = pathData;
      }
    }

    public interface OnDistrictSelectedListener {
      void onDistrictSelected(District district);
    }

    private OnDistrictSelectedListener listener;
    private String selectedKey = null;

    private static final float VIEWBOX_WIDTH = 1000f;
    private static final float VIEWBOX_HEIGHT = 800f;

    private final List<District> districts = getAllDistricts();
    private final List<Path> paths = new ArrayList<>();

    private final Matrix transformMatrix = new Matrix();
    private final Matrix inverseMatrix = new Matrix();

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaintSelected = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float[] touchPoint = new float[2];
    private final Region tempRegion = new Region();
    private final RectF tempBounds = new RectF();
    private final Rect clipRect = new Rect();

    public SeoulMapView(Context context) {
      super(context);
      init();
    }

    public SeoulMapView(Context context, @Nullable AttributeSet attrs) {
      super(context, attrs);
      init();
    }

    public SeoulMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
      init();
    }

    private void init() {
      fillPaint.setStyle(Paint.Style.FILL);
      fillPaint.setColor(Color.WHITE);

      selectedFillPaint.setStyle(Paint.Style.FILL);
      selectedFillPaint.setColor(Color.parseColor("#FFE8506A"));

      strokePaint.setStyle(Paint.Style.STROKE);
      strokePaint.setColor(Color.parseColor("#FFE8506A"));
      strokePaint.setStrokeWidth(3f);
      strokePaint.setStrokeJoin(Paint.Join.ROUND);

      labelPaint.setColor(Color.parseColor("#FF2A1F2D"));
      labelPaint.setTextSize(22f);
      labelPaint.setTextAlign(Paint.Align.CENTER);
      labelPaint.setFakeBoldText(true);

      labelPaintSelected.setColor(Color.WHITE);
      labelPaintSelected.setTextSize(22f);
      labelPaintSelected.setTextAlign(Paint.Align.CENTER);
      labelPaintSelected.setFakeBoldText(true);

      for (District d : districts) {
        paths.add(parseSvgPath(d.pathData));
      }
    }

    public void setOnDistrictSelectedListener(OnDistrictSelectedListener l) {
      this.listener = l;
    }

    public void setSelectedKey(@Nullable String key) {
      this.selectedKey = key;
      invalidate();
    }

    @Nullable
    public String getSelectedKey() {
      return selectedKey;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
      super.onSizeChanged(w, h, oldw, oldh);
      recalcMatrix(w, h);
    }

    private void recalcMatrix(int w, int h) {
      if (w == 0 || h == 0) return;
      float scale = Math.min(w / VIEWBOX_WIDTH, h / VIEWBOX_HEIGHT);
      float dx = (w - VIEWBOX_WIDTH * scale) / 2f;
      float dy = (h - VIEWBOX_HEIGHT * scale) / 2f;
      transformMatrix.reset();
      transformMatrix.postScale(scale, scale);
      transformMatrix.postTranslate(dx, dy);
      transformMatrix.invert(inverseMatrix);
    }

    @Override
    protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      canvas.save();
      canvas.concat(transformMatrix);

      int selectedIndex = -1;
      for (int i = 0; i < districts.size(); i++) {
        if (districts.get(i).key.equals(selectedKey)) {
          selectedIndex = i;
          continue;
        }
        drawDistrict(canvas, districts.get(i), paths.get(i), false);
      }
      if (selectedIndex >= 0) {
        drawDistrict(canvas, districts.get(selectedIndex), paths.get(selectedIndex), true);
      }

      canvas.restore();
    }

    private void drawDistrict(Canvas canvas, District district, Path path, boolean isSelected) {
      canvas.drawPath(path, isSelected ? selectedFillPaint : fillPaint);
      canvas.drawPath(path, strokePaint);

      path.computeBounds(tempBounds, true);
      float cx = tempBounds.centerX();
      float cy = tempBounds.centerY() + 6f;
      canvas.drawText(district.koreanName, cx, cy, isSelected ? labelPaintSelected : labelPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      if (event.getAction() == MotionEvent.ACTION_DOWN) {
        return true;
      }
      if (event.getAction() != MotionEvent.ACTION_UP) {
        return super.onTouchEvent(event);
      }

      touchPoint[0] = event.getX();
      touchPoint[1] = event.getY();
      inverseMatrix.mapPoints(touchPoint);

      float tx = touchPoint[0];
      float ty = touchPoint[1];

      for (int i = paths.size() - 1; i >= 0; i--) {
        if (pathContains(paths.get(i), tx, ty)) {
          District d = districts.get(i);
          setSelectedKey(d.key);
          if (listener != null) {
            listener.onDistrictSelected(d);
          }
          performClick();
          return true;
        }
      }
      return true;
    }

    @Override
    public boolean performClick() {
      super.performClick();
      return true;
    }

    private boolean pathContains(Path path, float x, float y) {
      path.computeBounds(tempBounds, true);
      clipRect.set(
          (int) tempBounds.left,
          (int) tempBounds.top,
          (int) tempBounds.right + 1,
          (int) tempBounds.bottom + 1);
      tempRegion.setPath(path, new Region(clipRect));
      return tempRegion.contains((int) x, (int) y);
    }

    private Path parseSvgPath(String data) {
      Path path = new Path();
      // 명령 문자(M, L, Z) 앞뒤에 공백을 강제로 삽입해서 토큰 분리를 쉽게 만든다.
      // 예: "M80 360" -> " M 80 360 "
      String normalized =
          data.replace(",", " ").replaceAll("([MLZmlz])", " $1 ").trim().replaceAll("\\s+", " ");

      String[] tokens = normalized.split(" ");
      int i = 0;
      String cmd = "";
      while (i < tokens.length) {
        String t = tokens[i];
        if (t.isEmpty()) {
          i++;
          continue;
        }
        if (t.equalsIgnoreCase("M") || t.equalsIgnoreCase("L") || t.equalsIgnoreCase("Z")) {
          cmd = t.toUpperCase();
          if (cmd.equals("Z")) {
            path.close();
          }
          i++;
        } else {
          // 숫자 좌표
          try {
            float x = Float.parseFloat(t);
            float y = Float.parseFloat(tokens[i + 1]);
            if (cmd.equals("M")) {
              path.moveTo(x, y);
              cmd = "L"; // M 다음 연속 좌표는 L로 처리
            } else if (cmd.equals("L")) {
              path.lineTo(x, y);
            }
            i += 2;
          } catch (NumberFormatException e) {
            // 파싱 실패 토큰은 건너뜀
            i++;
          }
        }
      }
      return path;
    }

    private static List<District> getAllDistricts() {
      return Arrays.asList(
          new District(
              "gangdong",
              "강동구",
              "M818.9 410.6 L827.1 411.3 L839.9 401.7 L910.4 379.9 L937.3 366.6 L965.4 363.9 L961.7 371.7 L976.9 409.2 L975.6 427.8 L979.6 430.5 L980.0 444.9 L970.2 442.1 L940.7 447.5 L933.8 445.8 L911.7 471.1 L911.0 481.8 L898.3 498.4 L893.9 498.6 L891.2 510.2 L832.7 484.6 L842.1 460.7 L810.8 449.3 L820.7 426.7 L818.9 410.6 Z"),
          new District(
              "songpa",
              "송파구",
              "M713.0 491.5 L737.4 495.0 L786.0 485.6 L810.8 449.3 L831.3 455.2 L842.1 460.7 L832.7 484.6 L891.2 510.2 L880.5 529.4 L883.2 536.6 L898.2 541.6 L903.8 538.7 L929.7 548.5 L926.6 561.8 L919.2 574.5 L907.7 577.4 L902.1 598.5 L895.4 602.7 L885.3 603.0 L878.3 610.1 L859.3 605.6 L859.9 614.0 L870.0 619.6 L854.8 622.7 L846.6 619.0 L846.6 624.7 L840.0 628.9 L836.0 617.0 L815.7 589.4 L817.0 586.8 L801.9 568.8 L781.4 558.6 L730.0 545.8 L719.6 537.5 L712.0 501.0 L713.0 491.5 Z"),
          new District(
              "gangnam",
              "강남구",
              "M689.2 482.3 L713.0 491.5 L712.0 501.0 L719.6 537.5 L730.0 545.8 L781.4 558.6 L809.6 575.4 L817.0 586.8 L815.7 589.4 L840.0 624.2 L840.0 628.9 L828.9 635.9 L827.3 644.1 L816.4 637.3 L803.3 635.0 L796.9 641.7 L784.7 643.6 L777.4 632.5 L762.0 621.7 L753.2 611.3 L752.8 605.3 L729.0 611.3 L726.1 606.9 L720.5 606.1 L718.3 609.0 L702.9 611.4 L700.7 620.3 L682.9 620.8 L662.7 601.2 L654.3 582.2 L637.7 584.5 L606.5 519.8 L598.6 496.6 L586.6 485.2 L616.4 462.7 L627.9 459.9 L664.8 474.5 L667.1 470.6 L689.2 482.3 Z"),
          new District(
              "seocho",
              "서초구",
              "M586.6 485.2 L598.6 496.6 L606.5 519.8 L637.7 584.5 L654.3 582.2 L662.7 601.2 L682.9 620.8 L700.7 620.3 L702.9 611.4 L718.3 609.0 L720.5 606.1 L726.1 606.9 L729.0 611.3 L752.8 605.3 L753.2 611.3 L762.0 621.7 L777.4 632.5 L780.4 637.6 L776.5 643.6 L777.6 648.8 L762.1 665.7 L762.1 673.5 L752.4 677.6 L748.2 684.0 L723.8 682.6 L724.5 686.7 L730.5 689.5 L723.4 695.9 L722.9 707.5 L710.1 712.0 L692.2 709.4 L679.0 713.0 L668.4 707.8 L665.8 701.7 L650.7 690.2 L641.7 688.7 L640.6 684.0 L647.1 675.9 L646.1 665.2 L642.3 660.2 L644.4 653.2 L636.5 637.5 L639.1 632.0 L631.1 627.6 L619.3 644.3 L596.5 651.5 L584.8 651.4 L571.5 634.8 L567.2 624.2 L550.8 625.4 L552.1 635.8 L543.8 637.6 L533.3 645.1 L530.6 639.9 L532.2 628.2 L519.3 611.5 L517.1 602.6 L520.2 557.4 L525.1 549.1 L514.2 539.9 L513.8 521.3 L519.2 518.1 L535.0 519.9 L554.8 510.7 L573.3 499.6 L586.6 485.2 Z"),
          new District(
              "gwanak",
              "관악구",
              "M517.1 602.6 L519.3 611.5 L532.2 628.2 L530.6 639.9 L533.3 645.1 L519.8 648.4 L515.4 656.9 L499.7 669.3 L477.7 673.9 L474.7 684.3 L427.0 692.3 L420.4 696.0 L418.2 686.0 L401.1 673.6 L401.1 668.0 L383.1 653.6 L381.4 648.3 L363.6 646.1 L359.4 631.7 L361.5 628.7 L356.7 625.8 L348.8 611.9 L354.5 609.6 L357.4 601.9 L351.7 599.1 L352.2 592.8 L331.5 596.5 L328.7 594.2 L337.3 583.5 L360.3 578.4 L369.1 573.4 L385.7 572.1 L393.5 560.6 L413.3 567.1 L425.5 567.0 L438.1 562.7 L453.5 569.7 L470.4 564.2 L473.0 578.4 L470.7 587.0 L491.7 605.6 L517.1 602.6 Z"),
          new District(
              "dongjak",
              "동작구",
              "M513.8 521.3 L514.2 539.9 L525.1 549.1 L520.2 557.4 L517.1 602.6 L491.7 605.6 L470.7 587.0 L473.0 578.4 L470.4 564.2 L453.5 569.7 L438.1 562.7 L425.5 567.0 L413.3 567.1 L393.5 560.6 L385.7 572.1 L369.1 573.4 L360.3 578.4 L337.3 583.5 L358.7 557.4 L375.1 554.4 L388.2 520.0 L392.1 519.2 L389.6 512.1 L404.4 513.2 L445.6 503.1 L461.6 514.0 L478.2 519.7 L513.8 521.3 Z"),
          new District(
              "yeongdeungpo",
              "영등포구",
              "M306.4 433.9 L344.2 452.7 L409.6 465.9 L437.1 487.0 L445.6 503.1 L404.4 513.2 L389.6 512.1 L392.1 519.2 L388.2 520.0 L375.1 554.4 L358.7 557.4 L337.3 583.5 L331.1 582.0 L321.9 574.2 L315.5 556.6 L315.8 531.9 L308.0 522.5 L282.8 510.6 L285.2 491.2 L303.5 478.6 L307.1 479.4 L310.5 473.0 L310.0 466.9 L296.1 442.8 L306.4 433.9 Z"),
          new District(
              "geumcheon",
              "금천구",
              "M328.7 594.2 L331.5 596.5 L352.2 592.8 L351.7 599.1 L357.4 601.9 L354.5 609.6 L348.8 611.9 L356.7 625.8 L361.5 628.7 L359.4 631.7 L363.6 646.1 L381.4 648.3 L383.1 653.6 L395.9 663.4 L383.8 671.6 L375.6 683.8 L371.1 687.0 L362.8 686.7 L350.0 700.9 L336.3 700.3 L336.2 696.1 L327.0 688.3 L325.9 676.1 L319.8 673.6 L320.8 667.6 L316.3 657.7 L305.8 657.5 L304.8 651.9 L298.3 649.1 L298.2 642.1 L303.9 635.2 L283.8 611.5 L269.0 584.2 L281.1 579.8 L304.9 595.8 L318.0 598.4 L327.5 597.4 L328.7 594.2 Z"),
          new District(
              "guro",
              "구로구",
              "M157.4 530.0 L167.3 530.2 L184.0 543.4 L193.7 539.9 L192.4 536.4 L203.3 536.0 L204.2 530.7 L221.9 524.8 L228.0 527.9 L233.9 526.2 L238.4 533.7 L243.8 533.7 L245.4 537.0 L258.8 538.0 L266.2 536.7 L282.8 510.6 L308.0 522.5 L315.8 531.9 L315.5 556.6 L321.9 574.2 L337.3 583.5 L327.5 597.4 L304.9 595.8 L285.6 581.3 L272.0 582.6 L277.6 576.7 L274.8 574.2 L267.8 575.8 L267.8 571.9 L256.0 563.3 L241.0 572.4 L232.9 581.4 L226.9 583.0 L219.5 591.2 L208.5 590.2 L205.9 592.7 L205.0 608.1 L181.8 605.7 L173.4 600.5 L151.2 604.7 L145.7 599.6 L144.8 585.4 L154.4 577.1 L152.6 572.1 L133.6 564.5 L129.7 557.9 L130.5 553.6 L143.5 551.9 L150.0 544.3 L152.4 531.2 L157.4 530.0 Z"),
          new District(
              "gangseo",
              "강서구",
              "M233.0 377.8 L234.5 389.3 L252.6 397.9 L306.4 433.9 L296.1 442.8 L298.2 449.0 L295.9 449.0 L285.7 439.2 L270.3 441.4 L248.0 431.2 L243.1 446.9 L246.7 455.1 L247.4 480.7 L193.4 488.2 L179.6 465.4 L181.0 462.5 L176.5 453.1 L170.3 453.3 L164.9 444.9 L162.0 452.9 L151.3 455.7 L134.1 455.8 L124.3 453.6 L121.9 450.1 L108.2 450.7 L100.0 456.3 L97.8 462.3 L88.6 466.9 L86.1 458.6 L88.7 453.9 L81.7 453.1 L81.6 448.5 L70.0 442.7 L54.4 442.9 L44.2 436.8 L34.3 437.3 L20.0 421.4 L28.6 421.1 L47.0 406.8 L47.3 401.9 L42.3 394.6 L55.6 392.7 L60.6 387.9 L61.2 380.0 L76.7 371.2 L85.1 371.7 L86.2 358.6 L91.1 358.3 L97.8 346.6 L102.3 344.8 L93.7 323.1 L104.7 308.9 L113.1 311.1 L147.4 340.6 L217.4 375.6 L233.0 377.8 Z"),
          new District(
              "yangcheon",
              "양천구",
              "M151.3 455.7 L162.0 452.9 L164.9 444.9 L170.3 453.3 L176.5 453.1 L181.0 462.5 L179.6 465.4 L193.4 488.2 L247.4 480.7 L246.7 455.1 L243.1 446.9 L248.0 431.2 L270.3 441.4 L285.7 439.2 L295.9 449.0 L300.8 449.5 L310.5 473.0 L307.1 479.4 L303.5 478.6 L285.2 491.2 L280.5 516.3 L266.2 536.7 L245.4 537.0 L243.8 533.7 L238.4 533.7 L233.9 526.2 L228.0 527.9 L221.9 524.8 L204.2 530.7 L203.3 536.0 L192.4 536.4 L193.7 539.9 L184.0 543.4 L167.3 530.2 L157.4 530.0 L153.7 511.9 L159.1 503.8 L158.5 496.7 L165.8 487.8 L163.1 481.1 L159.2 480.5 L150.5 469.2 L151.3 455.7 Z"),
          new District(
              "mapo",
              "마포구",
              "M337.1 372.6 L333.6 375.9 L340.9 380.7 L394.7 403.7 L391.0 414.1 L414.6 422.6 L424.1 419.6 L464.6 419.1 L471.0 414.0 L474.7 419.6 L471.5 422.1 L473.1 430.9 L476.7 435.0 L464.2 444.8 L461.1 453.2 L447.7 465.8 L433.4 468.9 L429.9 481.6 L409.6 465.9 L331.6 447.8 L232.2 386.0 L233.2 375.6 L246.2 373.2 L250.9 375.9 L256.5 371.5 L274.6 366.4 L276.4 356.0 L289.2 340.4 L333.7 374.9 L337.1 372.6 Z"),
          new District(
              "seodaemun",
              "서대문구",
              "M445.5 301.5 L452.8 309.0 L451.0 318.7 L454.1 322.9 L461.1 323.6 L459.2 330.7 L463.9 342.6 L460.0 354.7 L466.9 359.8 L452.8 368.1 L489.6 406.8 L464.6 419.1 L424.1 419.6 L414.6 422.6 L391.0 414.1 L394.7 403.7 L388.9 399.3 L375.4 396.0 L333.6 375.9 L358.7 351.7 L367.1 352.7 L366.2 358.3 L381.2 356.3 L384.7 349.0 L394.5 346.3 L396.4 336.2 L407.2 327.9 L423.3 322.7 L427.3 308.5 L433.0 308.6 L438.0 301.6 L445.5 301.5 Z"),
          new District(
              "eunpyeong",
              "은평구",
              "M494.6 245.5 L488.8 250.3 L466.6 254.3 L460.2 261.4 L449.6 262.7 L448.9 270.1 L443.2 279.0 L446.9 291.9 L445.5 301.5 L438.0 301.6 L433.0 308.6 L427.3 308.5 L423.3 322.7 L407.2 327.9 L396.4 336.2 L394.5 346.3 L384.7 349.0 L381.2 356.3 L366.2 358.3 L367.1 352.7 L358.7 351.7 L333.7 374.9 L289.2 340.4 L295.7 333.8 L300.7 333.9 L296.5 340.1 L300.7 345.8 L323.0 345.7 L329.6 342.9 L327.6 336.5 L334.2 330.9 L330.5 312.4 L334.7 310.8 L332.1 290.3 L337.7 276.5 L346.6 269.3 L344.7 264.5 L350.8 254.4 L344.9 245.0 L354.8 237.3 L353.4 230.9 L358.5 218.0 L347.5 213.3 L349.1 210.7 L361.8 217.0 L378.7 215.1 L396.2 205.0 L409.1 203.6 L414.9 200.5 L422.6 189.7 L438.5 184.4 L447.5 193.5 L454.7 192.8 L460.2 198.8 L462.2 206.8 L467.9 208.7 L472.5 219.8 L477.7 220.8 L487.9 230.9 L487.7 235.8 L494.6 245.5 Z"),
          new District(
              "nowon",
              "노원구",
              "M801.9 271.8 L780.0 280.4 L756.4 273.1 L723.2 283.8 L701.7 286.6 L654.6 247.8 L660.2 242.9 L674.5 216.8 L685.4 226.1 L687.6 214.1 L683.5 191.7 L671.4 161.1 L678.5 125.0 L675.9 119.9 L684.7 114.7 L708.8 113.5 L718.6 103.1 L736.8 98.5 L747.0 98.5 L757.3 112.4 L777.3 115.0 L780.3 122.9 L770.1 137.9 L778.7 152.6 L779.5 158.5 L775.3 167.6 L780.5 177.7 L779.1 183.0 L757.5 192.3 L772.4 193.7 L775.2 198.5 L771.2 205.1 L776.8 215.1 L807.3 217.2 L816.3 227.3 L817.3 238.8 L814.0 249.4 L800.9 257.9 L796.9 267.1 L801.9 271.8 Z"),
          new District(
              "dobong",
              "도봉구",
              "M675.9 119.9 L678.5 125.0 L671.4 161.1 L683.5 191.7 L687.6 214.1 L685.4 226.1 L674.5 216.8 L660.2 242.9 L654.6 247.8 L644.2 235.3 L638.5 232.8 L634.3 224.1 L615.5 210.7 L589.8 203.9 L588.2 199.8 L595.3 166.6 L601.9 159.1 L586.7 137.6 L579.2 136.7 L579.8 102.3 L586.8 94.2 L592.3 94.1 L595.9 87.7 L603.7 87.0 L616.5 91.4 L623.8 88.9 L633.8 109.0 L655.1 100.8 L664.1 107.6 L671.8 104.0 L675.9 119.9 Z"),
          new District(
              "gangbuk",
              "강북구",
              "M578.4 125.3 L579.2 136.7 L586.7 137.6 L601.9 159.1 L595.3 166.6 L588.2 199.8 L589.8 203.9 L615.5 210.7 L634.3 224.1 L638.5 232.8 L674.1 263.6 L643.9 290.9 L629.2 298.6 L628.7 291.3 L610.4 293.8 L603.6 288.1 L584.0 281.9 L576.6 271.4 L577.5 264.8 L559.4 260.5 L545.7 250.8 L543.8 246.1 L539.4 246.2 L524.1 235.7 L527.6 227.4 L521.0 219.6 L524.9 214.1 L512.7 193.7 L513.4 189.8 L525.0 183.5 L532.3 173.2 L543.0 170.4 L545.9 153.9 L540.4 137.3 L554.6 127.4 L578.4 125.3 Z"),
          new District(
              "seongbuk",
              "성북구",
              "M502.2 247.6 L524.1 235.7 L539.4 246.2 L543.8 246.1 L545.7 250.8 L559.4 260.5 L577.5 264.8 L576.6 271.4 L584.0 281.9 L603.6 288.1 L610.4 293.8 L628.7 291.3 L629.2 298.6 L643.9 290.9 L674.1 263.6 L701.7 286.6 L723.2 283.8 L723.9 304.0 L721.1 298.2 L716.8 298.2 L717.1 303.8 L702.0 307.8 L695.1 317.3 L678.8 319.7 L674.7 317.1 L668.7 328.1 L652.5 330.7 L649.0 339.6 L612.5 370.0 L601.3 368.8 L597.0 361.3 L593.1 360.4 L579.7 364.5 L575.5 360.7 L573.0 349.6 L565.0 344.7 L561.5 337.1 L539.6 339.4 L528.4 336.8 L518.1 329.2 L517.1 321.6 L531.3 316.8 L527.0 303.2 L528.9 285.9 L519.9 281.3 L511.7 263.2 L512.4 252.6 L505.9 253.4 L502.2 247.6 Z"),
          new District(
              "jungnang",
              "중랑구",
              "M723.2 283.8 L756.4 273.1 L780.0 280.4 L801.9 271.8 L814.9 272.2 L828.0 278.8 L827.0 299.2 L830.9 302.1 L830.2 309.1 L820.6 319.7 L831.0 331.1 L819.5 334.9 L810.2 354.8 L792.1 356.8 L795.0 369.4 L787.7 382.6 L768.6 389.5 L739.7 384.2 L736.1 362.4 L722.7 345.4 L718.7 328.3 L726.2 313.2 L723.2 283.8 Z"),
          new District(
              "dongdaemun",
              "동대문구",
              "M612.5 370.0 L649.0 339.6 L652.5 330.7 L668.7 328.1 L674.7 317.1 L678.8 319.7 L695.1 317.3 L702.0 307.8 L717.1 303.8 L716.8 298.2 L721.1 298.2 L726.2 313.2 L718.7 328.3 L722.7 345.4 L736.1 362.4 L739.7 384.2 L724.8 411.3 L692.9 405.2 L669.4 387.6 L656.2 381.5 L646.4 381.8 L627.6 388.6 L613.0 384.5 L612.5 370.0 Z"),
          new District(
              "gwangjin",
              "광진구",
              "M739.7 384.2 L768.6 389.5 L791.0 380.3 L798.4 386.3 L791.5 411.3 L818.9 410.6 L820.7 426.7 L816.5 440.0 L786.0 485.6 L752.8 493.0 L726.5 494.7 L689.2 482.3 L728.5 412.7 L724.8 411.3 L739.7 384.2 Z"),
          new District(
              "seongdong",
              "성동구",
              "M613.0 384.5 L627.6 388.6 L646.4 381.8 L656.2 381.5 L669.4 387.6 L692.9 405.2 L728.5 412.7 L689.2 482.3 L667.1 470.6 L664.8 474.5 L627.9 459.9 L607.4 468.5 L593.3 459.8 L581.1 458.3 L578.5 453.0 L581.5 438.8 L597.9 426.6 L599.3 420.6 L611.8 416.3 L613.7 408.8 L619.6 404.5 L620.7 399.9 L613.6 399.5 L613.0 384.5 Z"),
          new District(
              "yongsan",
              "용산구",
              "M579.1 448.2 L581.1 458.3 L593.3 459.8 L607.4 468.5 L567.9 503.8 L535.0 519.9 L530.4 517.7 L513.8 521.3 L478.2 519.7 L461.6 514.0 L445.6 503.1 L437.1 487.0 L429.9 481.6 L433.4 468.9 L447.7 465.8 L461.1 453.2 L464.2 444.8 L476.7 435.0 L473.1 430.9 L480.8 424.9 L505.4 427.1 L507.0 422.9 L514.7 426.5 L526.0 425.7 L548.6 440.7 L556.0 434.5 L569.1 433.5 L570.5 443.1 L579.1 448.2 Z"),
          new District(
              "junggu",
              "중구",
              "M613.0 384.5 L613.6 399.5 L620.7 399.9 L619.6 404.5 L613.7 408.8 L611.8 416.3 L599.3 420.6 L597.9 426.6 L581.5 438.8 L579.1 448.2 L570.5 443.1 L569.1 433.5 L556.0 434.5 L548.6 440.7 L526.0 425.7 L514.7 426.5 L507.0 422.9 L505.4 427.1 L480.8 424.9 L473.1 430.9 L471.5 422.1 L474.7 419.6 L471.0 414.0 L489.6 406.8 L482.8 397.8 L488.4 392.6 L595.6 388.9 L613.0 384.5 Z"),
          new District(
              "jongno",
              "종로구",
              "M494.6 245.5 L502.2 247.6 L505.9 253.4 L512.4 252.6 L511.7 263.2 L519.9 281.3 L528.9 285.9 L527.0 303.2 L531.3 316.8 L517.1 321.6 L518.1 329.2 L528.4 336.8 L539.6 339.4 L561.5 337.1 L565.0 344.7 L573.0 349.6 L575.5 360.7 L579.7 364.5 L593.1 360.4 L597.0 361.3 L601.3 368.8 L612.5 370.0 L613.0 384.5 L595.6 388.9 L533.9 392.8 L497.2 390.3 L488.4 392.6 L482.8 397.8 L452.8 368.1 L466.9 359.8 L460.0 354.7 L463.9 342.6 L459.2 330.7 L461.1 323.6 L454.1 322.9 L451.0 318.7 L452.8 309.0 L444.2 299.4 L446.9 291.9 L443.2 279.0 L448.9 270.1 L449.6 262.7 L460.2 261.4 L466.6 254.3 L488.8 250.3 L494.6 245.5 Z"));
    }
  }
}
