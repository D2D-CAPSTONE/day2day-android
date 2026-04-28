# Day2Day Android 프로젝트 학습 계획

이 문서는 **Java 기초 문법만 아는 상태**에서 `day2day-android` 프로젝트를 이해하기 위한 학습 순서입니다.

현재 프로젝트는 Java로 작성된 Android 앱이며, 화면은 XML 레이아웃으로 구성되어 있습니다. 빌드는 Gradle Kotlin DSL을 사용하고, UI는 AppCompat, Material Components, ConstraintLayout을 사용합니다. 지도 기능은 Naver Map SDK를 사용할 준비가 되어 있습니다.

## 1. 전체 학습 로드맵

1. Java 기초를 Android 코드 기준으로 복습하기
2. Android 프로젝트 구조 이해하기
3. Gradle, 의존성, 빌드 설정 이해하기
4. XML 레이아웃과 리소스 시스템 이해하기
5. Activity와 생명주기 이해하기
6. Intent로 화면 이동 이해하기
7. Fragment와 BottomNavigationView 이해하기
8. View 찾기와 이벤트 처리 이해하기
9. Android UI 단위, 스타일, drawable 이해하기
10. Insets, 상태바, 네비게이션바 처리 이해하기
11. 동적 UI 생성 이해하기
12. Naver Map SDK와 외부 라이브러리 이해하기
13. 프로젝트 전체 화면 흐름 정리하기
14. 직접 수정하며 실습하기

## 2. Java 기초 복습

Android 코드를 읽기 위해 Java 문법 전체를 깊게 다시 볼 필요는 없습니다. 이 프로젝트에서 자주 쓰이는 문법을 우선 복습하면 됩니다.

### 2.1 클래스와 상속

이 프로젝트의 화면 클래스는 대부분 다음과 같은 형태입니다.

```java
public class SplashActivity extends AppCompatActivity
```

`SplashActivity`는 독립적인 클래스가 아니라 Android가 제공하는 `AppCompatActivity`를 상속받아 화면 역할을 합니다.

공부할 개념:

- `class`
- `extends`
- 부모 클래스와 자식 클래스
- 메서드 오버라이딩
- `super`
- `public`, `private`, `protected`
- `final`
- `static`

관련 파일:

- `app/src/main/java/com/example/day2day/presentation/splash/SplashActivity.java`
- `app/src/main/java/com/example/day2day/presentation/start/StartActivity.java`
- `app/src/main/java/com/example/day2day/presentation/main/MainActivity.java`

### 2.2 익명 클래스와 람다식

이 프로젝트에는 익명 클래스와 람다식이 모두 사용됩니다.

익명 클래스 예시:

```java
new Runnable() {
  @Override
  public void run() {
    ...
  }
}
```

람다식 예시:

```java
v -> startActivity(...)
```

공부할 개념:

- 인터페이스
- 익명 클래스
- 람다식
- 콜백
- 이벤트 리스너

관련 파일:

- `SplashActivity.java`
- `StartActivity.java`
- `HomeFragment.java`

### 2.3 배열과 반복문

`HomeFragment`에는 코스 데이터가 2차원 배열로 들어 있습니다.

```java
private static final String[][] COURSES = {
  {"홍대 감성 데이트 코스", ...},
  ...
};
```

공부할 개념:

- 1차원 배열
- 2차원 배열
- `for` 반복문
- 문자열 `split`
- 인덱스 접근
- `static final` 상수

관련 파일:

- `app/src/main/java/com/example/day2day/presentation/main/home/HomeFragment.java`

## 3. Android 프로젝트 구조

프로젝트 구조는 대략 다음과 같습니다.

```text
app/
  src/main/
    java/com/example/day2day/
      presentation/
        splash/
        start/
        main/
        recommend/
        common/
    res/
      layout/
      drawable/
      values/
      menu/
    AndroidManifest.xml
```

각 디렉터리의 역할:

| 경로 | 역할 |
| --- | --- |
| `java/` | Java 코드 |
| `res/layout/` | 화면 XML |
| `res/drawable/` | 배경, 아이콘, shape |
| `res/values/` | 색상, 문자열, 테마 |
| `res/menu/` | 하단 네비게이션 메뉴 |
| `AndroidManifest.xml` | 앱 권한, 화면 등록, 시작 화면 설정 |

핵심 연결 방식:

```java
setContentView(R.layout.activity_start);
Button goMainButton = findViewById(R.id.btn_go_main);
```

위 코드는 `activity_start.xml`을 화면으로 사용하고, 그 XML 안에서 `btn_go_main`이라는 id를 가진 버튼을 찾는다는 뜻입니다.

## 4. Gradle과 의존성

빌드 설정은 다음 파일에 있습니다.

- `settings.gradle.kts`
- `app/build.gradle.kts`

주요 설정:

```kotlin
android {
    namespace = "com.example.day2day"

    defaultConfig {
        applicationId = "com.example.day2day"
        minSdk = 24
        targetSdk = 36
    }
}
```

공부할 개념:

- `applicationId`
- `namespace`
- `minSdk`
- `targetSdk`
- `compileSdk`
- `dependencies`
- `implementation`

주요 의존성:

```kotlin
implementation(libs.appcompat)
implementation("androidx.constraintlayout:constraintlayout:2.2.1")
implementation(libs.material)
implementation(libs.play.services.maps)
implementation("com.naver.maps:map-sdk:3.23.2")
```

의존성 역할:

| 의존성 | 역할 |
| --- | --- |
| AppCompat | 구형 Android와 호환되는 Activity 지원 |
| ConstraintLayout | XML 화면 배치 |
| Material Components | BottomNavigationView 등 Material UI |
| Google Play Services Maps | 지도 관련 기능 |
| Naver Map SDK | 네이버 지도 |

## 5. AndroidManifest.xml 이해

앱의 전체 등록 정보는 다음 파일에 있습니다.

- `app/src/main/AndroidManifest.xml`

인터넷 권한:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

앱 시작 화면:

```xml
<activity
    android:name=".presentation.splash.SplashActivity"
    android:exported="true"
    android:theme="@style/Theme.Day2day.Splash">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

이 설정 때문에 앱을 실행하면 `SplashActivity`가 가장 먼저 실행됩니다.

공부할 개념:

- `manifest`
- `application`
- `activity`
- `intent-filter`
- `MAIN`
- `LAUNCHER`
- `exported`
- `theme`
- `permission`
- `meta-data`

Naver Map SDK 키:

```xml
<meta-data
    android:name="com.naver.maps.map.NCP_KEY_ID"
    android:value="..." />
```

## 6. Activity와 생명주기

Android에서 화면 하나는 보통 `Activity`입니다.

이 프로젝트의 주요 Activity:

- `SplashActivity`
- `StartActivity`
- `MainActivity`
- `MapPageActivity`
- `MapSelectionActivity`
- `FilteringActivity`
- `CourseMapPageActivity`
- `CourseDetailPageActivity`

가장 중요한 메서드:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(...);
}
```

`onCreate`는 화면이 처음 만들어질 때 호출됩니다.

이 프로젝트에서 `onCreate`가 하는 일:

- XML 레이아웃 연결
- 버튼 찾기
- 클릭 이벤트 연결
- 화면 이동 설정
- 상태바, 네비게이션바 여백 처리
- Fragment 연결

우선 공부할 생명주기:

1. `onCreate`
2. `onStart`
3. `onResume`
4. `onPause`
5. `onStop`
6. `onDestroy`

이 프로젝트에서는 먼저 `onCreate`와 `onDestroy`를 중심으로 보면 됩니다.

## 7. Intent로 화면 이동 이해

Android에서 Activity 간 이동은 `Intent`로 합니다.

예시:

```java
Intent intent = new Intent(SplashActivity.this, StartActivity.class);
startActivity(intent);
finish();
```

뜻:

- `SplashActivity`에서 `StartActivity`로 이동한다.
- `finish()`로 현재 Splash 화면을 종료한다.

기본 화면 흐름:

```text
SplashActivity
  -> StartActivity
      -> MainActivity
```

추천 코스 흐름:

```text
HomeFragment
  -> MapPageActivity
      -> FilteringActivity
          -> CourseMapPageActivity
```

장소 선택 흐름:

```text
HomeFragment
  -> MapSelectionActivity
      -> MapPageActivity
          -> FilteringActivity
              -> CourseMapPageActivity
```

코스 카드 클릭 흐름:

```text
HomeFragment
  -> CourseDetailPageActivity
      -> CourseMapPageActivity
```

공부할 개념:

- `Intent`
- `startActivity`
- `finish`
- Activity back stack
- `Intent.FLAG_ACTIVITY_CLEAR_TOP`
- `Intent.FLAG_ACTIVITY_SINGLE_TOP`

## 8. XML 레이아웃 이해

이 프로젝트는 화면을 대부분 XML로 만듭니다.

주요 XML 파일:

- `activity_splash.xml`
- `activity_start.xml`
- `activity_main.xml`
- `fragment_home.xml`
- `item_course_card.xml`
- `activity_map_page.xml`
- `activity_filtering.xml`
- `activity_course_map_page.xml`
- `activity_course_detail_page.xml`

먼저 익힐 View:

- `LinearLayout`
- `FrameLayout`
- `ConstraintLayout`
- `NestedScrollView`
- `TextView`
- `Button`
- `ImageView`
- `Toolbar`
- `BottomNavigationView`

먼저 익힐 속성:

- `android:id`
- `layout_width`
- `layout_height`
- `padding`
- `margin`
- `background`
- `textColor`
- `textSize`

Activity에서 XML 연결:

```java
setContentView(R.layout.activity_main);
```

Fragment에서 XML 연결:

```java
return inflater.inflate(R.layout.fragment_home, container, false);
```

Activity에서는 보통 `findViewById`를 바로 사용하지만, Fragment에서는 `view.findViewById`를 사용합니다.

## 9. Fragment 이해

`MainActivity`는 화면 전체를 직접 바꾸는 대신 Fragment를 교체합니다.

```java
private void showFragment(Fragment fragment) {
  getSupportFragmentManager()
      .beginTransaction()
      .replace(R.id.main_fragment_container, fragment)
      .commit();
}
```

하단 탭을 누르면 Fragment가 바뀝니다.

```java
if (itemId == R.id.menu_home) {
  showFragment(new HomeFragment());
  return true;
}
```

Fragment 종류:

- `HomeFragment`
- `FavoritesFragment`
- `RecordFragment`

공부할 개념:

- Activity와 Fragment의 차이
- `Fragment`
- `onCreateView`
- `onViewCreated`
- `FragmentManager`
- `FragmentTransaction`
- `replace`
- `commit`
- Fragment container

이 프로젝트 기준으로 보면 `MainActivity`는 큰 틀이고, `HomeFragment`, `FavoritesFragment`, `RecordFragment`는 그 안에 끼워지는 작은 화면입니다.

## 10. BottomNavigationView 이해

하단 메뉴는 Material Components의 `BottomNavigationView`입니다.

관련 파일:

- `app/src/main/java/com/example/day2day/presentation/main/MainActivity.java`
- `app/src/main/res/menu/menu_main_bottom_nav.xml`
- `app/src/main/res/layout/activity_main.xml`

핵심 코드:

```java
BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
bottomNavigation.setOnItemSelectedListener(...);
```

공부할 개념:

- menu XML
- `BottomNavigationView`
- `setOnItemSelectedListener`
- `item.getItemId`
- Fragment 교체

## 11. 이벤트 처리 이해

버튼 클릭은 대부분 다음 형태입니다.

```java
nextButton.setOnClickListener(
    v -> startActivity(new Intent(MapPageActivity.this, FilteringActivity.class)));
```

또는 다음처럼 익명 클래스를 사용합니다.

```java
goCourseMapButton.setOnClickListener(
    new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ...
      }
    });
```

공부할 개념:

- `View.OnClickListener`
- `onClick`
- 람다식
- 버튼 클릭 이벤트
- 화면 이동 이벤트

추천 읽기 순서:

1. `StartActivity.java`
2. `MapPageActivity.java`
3. `FilteringActivity.java`
4. `CourseDetailPageActivity.java`

## 12. 리소스 시스템 이해

Android는 색상, 문자열, 배경, 아이콘을 코드에 직접 넣기보다 `res` 아래에 둡니다.

주요 파일:

- `res/values/colors.xml`
- `res/values/strings.xml`
- `res/values/themes.xml`
- `res/drawable/*.xml`
- `res/layout/*.xml`
- `res/menu/*.xml`

Java에서는 다음처럼 접근합니다.

```java
R.color.rose
R.layout.fragment_home
R.id.cc_title
R.drawable.shape_tag_rose
```

공부할 개념:

- `R` 클래스
- `@color/...`
- `@string/...`
- `@drawable/...`
- `@layout/...`
- `@id/...`
- `@style/...`

`R`은 리소스 이름을 Java 코드에서 사용할 수 있게 해주는 자동 생성 클래스라고 이해하면 됩니다.

## 13. Drawable과 Shape 이해

이 프로젝트에는 XML drawable이 많이 있습니다.

예시:

- `shape_card.xml`
- `shape_tag_rose.xml`
- `bg_weather_banner.xml`
- `bg_splash_gradient.xml`
- `ic_splash_logo.xml`

공부할 개념:

- XML drawable
- `shape`
- `solid`
- `stroke`
- `corners`
- `gradient`
- vector drawable

Java 코드에서 drawable을 동적으로 만드는 예시:

```java
GradientDrawable thumbBg = new GradientDrawable();
thumbBg.setShape(GradientDrawable.RECTANGLE);
thumbBg.setCornerRadius(dpToPx(12));
thumbBg.setColor(Color.parseColor(course[4]));
thumb.setBackground(thumbBg);
```

관련 파일:

- `HomeFragment.java`

## 14. dp, px, sp 이해

Android UI에서 자주 쓰는 단위:

| 단위 | 의미 |
| --- | --- |
| `dp` | 화면 밀도와 무관하게 비슷한 크기로 보이게 하는 단위 |
| `sp` | 글자 크기용 단위 |
| `px` | 실제 픽셀 |

`HomeFragment`에는 `dp`를 `px`로 변환하는 메서드가 있습니다.

```java
private int dpToPx(int dp) {
  return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
}
```

공부할 개념:

- `dp`
- `sp`
- `px`
- `density`
- `DisplayMetrics`

## 15. Insets 이해

이 프로젝트에는 상태바와 네비게이션바에 UI가 가려지지 않도록 처리하는 코드가 있습니다.

공통 헬퍼:

- `app/src/main/java/com/example/day2day/presentation/common/NavigationBarInsetHelper.java`

예시:

```java
NavigationBarInsetHelper.applyBottomInset(rootView, nextButton);
```

이 코드는 하단 버튼이 Android 네비게이션바나 제스처 영역에 가려지지 않게 아래 margin을 조정합니다.

공부할 개념:

- status bar
- navigation bar
- gesture navigation
- WindowInsets
- `ViewCompat.setOnApplyWindowInsetsListener`
- padding과 margin의 차이

이 주제는 Activity, Fragment, XML을 먼저 이해한 뒤 보는 것이 좋습니다.

## 16. Handler와 지연 실행

`SplashActivity`에는 다음 코드가 있습니다.

```java
private final Handler handler = new Handler(Looper.getMainLooper());
```

```java
handler.postDelayed(navigationRunnable, 2000);
```

뜻:

- 메인 스레드에서
- 2초 뒤에
- `navigationRunnable`을 실행한다

공부할 개념:

- Thread
- Main Thread, UI Thread
- Handler
- Looper
- Runnable
- `postDelayed`
- `removeCallbacks`

관련 파일:

- `SplashActivity.java`

## 17. HomeFragment 집중 분석

이 프로젝트에서 가장 복잡한 파일은 `HomeFragment.java`입니다.

이 파일을 다음 순서로 나눠서 보면 됩니다.

### 17.1 상수와 데이터

```java
private static final int SCROLL_THRESHOLD_DP = 100;
private static final String[][] COURSES = { ... };
private static final int PER_PAGE = 5;
```

### 17.2 View 변수

```java
private View weatherBanner;
private View weatherFull;
private View weatherMini;
private LinearLayout courseList;
private View loadMore;
```

### 17.3 XML inflate

```java
return inflater.inflate(R.layout.fragment_home, container, false);
```

### 17.4 View 연결

```java
weatherBanner = view.findViewById(R.id.weather_banner);
courseList = view.findViewById(R.id.course_list);
```

### 17.5 클릭 이벤트

```java
view.findViewById(R.id.btn_nearby_course)
    .setOnClickListener(
        v -> startActivity(new Intent(requireContext(), MapPageActivity.class)));
```

### 17.6 스크롤 이벤트

```java
scrollBody.setOnScrollChangeListener(...);
```

### 17.7 코스 카드 동적 추가

```java
View card = inflater.inflate(R.layout.item_course_card, courseList, false);
bindCourseCard(card, COURSES[i]);
courseList.addView(card);
```

### 17.8 카드에 데이터 바인딩

```java
((TextView) card.findViewById(R.id.cc_title)).setText(course[0]);
```

핵심은 XML에 빈 리스트 영역을 만들고, Java 코드에서 카드 View를 반복적으로 추가한다는 점입니다.

## 18. 동적 UI 생성 이해

`HomeFragment`의 `bindCourseCard`에서는 Java 코드로 View를 직접 만듭니다.

```java
TextView tagView = new TextView(requireContext());
tagView.setText(tag);
tagView.setTextColor(requireContext().getColor(R.color.rose));
tagView.setTextSize(9f);
tagView.setBackgroundResource(R.drawable.shape_tag_rose);
tagsLayout.addView(tagView);
```

공부할 개념:

- `new TextView(...)`
- `setText`
- `setTextColor`
- `setTextSize`
- `setPadding`
- `LayoutParams`
- `addView`

나중에 코스가 많아지면 이런 구조는 보통 `RecyclerView`로 바꿉니다. 지금은 동적 View 생성의 기본 예제로 이해하면 됩니다.

## 19. 스크롤에 따른 UI 변화

`HomeFragment`에는 스크롤하면 날씨 배너가 줄어드는 코드가 있습니다.

```java
float fraction = Math.min(1f, Math.max(0f, scrollY / threshold));
weatherFull.setAlpha(1f - fraction);
weatherMini.setAlpha(fraction);
```

뜻:

- 스크롤이 적을 때는 큰 날씨 배너가 보인다.
- 스크롤이 많아질수록 작은 날씨 배너가 보인다.
- `alpha`로 투명도를 조절한다.
- height도 같이 줄인다.

공부할 개념:

- `NestedScrollView`
- `scrollY`
- `alpha`
- View height 변경
- `requestLayout`
- 보간, interpolation

초반에는 완벽히 이해하지 않아도 됩니다. Activity, Fragment, XML을 먼저 이해한 뒤 다시 보면 됩니다.

## 20. Naver Map SDK 이해

지도 관련 파일:

- `CourseMapPageActivity.java`
- `activity_course_map_page.xml`
- `AndroidManifest.xml`
- `app/build.gradle.kts`

현재 코드는 지도 기능이 깊게 구현된 상태는 아닙니다.

```java
public class CourseMapPageActivity extends AppCompatActivity implements OnMapReadyCallback
```

```java
@Override
public void onMapReady(@NonNull NaverMap naverMap) {}
```

즉, 네이버 지도를 붙일 준비는 되어 있지만 `onMapReady` 안에는 아직 실제 로직이 없습니다.

공부할 개념:

- 외부 SDK
- Gradle 의존성 추가
- Manifest `meta-data`
- API key
- `OnMapReadyCallback`
- `NaverMap`
- 지도 Fragment 또는 MapView

지도는 프로젝트 이해의 마지막 단계로 미뤄도 됩니다.

## 21. 프로젝트 화면 흐름

앱 전체 흐름:

```text
앱 실행
  |
  v
SplashActivity
  - activity_splash.xml
  - 2초 대기
  |
  v
StartActivity
  - activity_start.xml
  - 시작 버튼 클릭
  |
  v
MainActivity
  - activity_main.xml
  - BottomNavigationView
  |
  +-- HomeFragment
  |     - fragment_home.xml
  |     - 추천 코스 카드
  |     - 근처 코스 버튼
  |     - 장소 선택 버튼
  |
  +-- FavoritesFragment
  |     - fragment_favorites.xml
  |
  +-- RecordFragment
        - fragment_record.xml
```

추천 코스 흐름:

```text
HomeFragment
  |
  +-- 근처 코스 버튼
  |     v
  |   MapPageActivity
  |     v
  |   FilteringActivity
  |     v
  |   CourseMapPageActivity
  |
  +-- 장소 선택 버튼
  |     v
  |   MapSelectionActivity
  |     v
  |   MapPageActivity
  |     v
  |   FilteringActivity
  |     v
  |   CourseMapPageActivity
  |
  +-- 코스 카드 클릭
        v
      CourseDetailPageActivity
        v
      CourseMapPageActivity
```

## 22. 4주 학습 플랜

### 1주차: Android 기본 구조 익히기

목표: 앱이 어떻게 시작되고 화면이 어떻게 연결되는지 이해합니다.

| 날짜 | 학습 내용 | 볼 파일 |
| --- | --- | --- |
| 1일차 | Java 클래스, 상속, 오버라이딩, `onCreate`, `setContentView` | `SplashActivity.java`, `activity_splash.xml` |
| 2일차 | `AndroidManifest.xml`, 시작 Activity, `MAIN`, `LAUNCHER`, 권한 | `AndroidManifest.xml` |
| 3일차 | `Intent`, `startActivity`, `finish` | `SplashActivity.java`, `StartActivity.java` |
| 4일차 | XML 레이아웃 기초, `TextView`, `Button`, `ImageView` | `activity_start.xml`, `activity_splash.xml` |
| 5일차 | `findViewById`, 버튼 클릭 이벤트, 람다식 | `StartActivity.java`, `MapPageActivity.java`, `FilteringActivity.java` |
| 6일차 | Gradle 기본, SDK 버전, dependencies | `settings.gradle.kts`, `app/build.gradle.kts` |
| 7일차 | 복습, Activity와 XML 연결 표 만들기 | 전체 흐름 |

### 2주차: MainActivity, Fragment, 하단 네비게이션 이해

목표: 메인 화면 구조와 탭 전환 방식을 이해합니다.

| 날짜 | 학습 내용 | 볼 파일 |
| --- | --- | --- |
| 1일차 | Activity와 Fragment 차이 | `MainActivity.java`, `HomeFragment.java`, `FavoritesFragment.java`, `RecordFragment.java` |
| 2일차 | `activity_main.xml`, Fragment container, BottomNavigationView | `activity_main.xml`, `menu_main_bottom_nav.xml` |
| 3일차 | `BottomNavigationView`, `setOnItemSelectedListener`, `item.getItemId` | `MainActivity.java` |
| 4일차 | Fragment 교체 코드 | `MainActivity.java` |
| 5일차 | `onCreateView`, `inflater.inflate` | `HomeFragment.java`, `fragment_home.xml` |
| 6일차 | `onViewCreated`, `view.findViewById` | `HomeFragment.java` |
| 7일차 | 복습, 하단 탭 클릭 흐름 설명하기 | 메인 화면 전체 |

### 3주차: HomeFragment와 동적 UI 이해

목표: 프로젝트의 핵심 홈 화면을 이해합니다.

| 날짜 | 학습 내용 | 볼 파일 |
| --- | --- | --- |
| 1일차 | `COURSES` 2차원 배열, 코스 데이터 구조 | `HomeFragment.java` |
| 2일차 | `loadMoreCourses`, `currentPage`, `PER_PAGE`, 반복문 | `HomeFragment.java` |
| 3일차 | `item_course_card.xml`, 카드 inflate | `item_course_card.xml`, `HomeFragment.java` |
| 4일차 | `bindCourseCard`, 제목, 경로, 태그, 별점 바인딩 | `HomeFragment.java` |
| 5일차 | Java 코드로 `TextView` 생성, `addView`, `LayoutParams` | `HomeFragment.java` |
| 6일차 | 날씨 배너 스크롤 애니메이션, `NestedScrollView`, `alpha` | `HomeFragment.java`, `fragment_home.xml` |
| 7일차 | 복습, 코스 추가, 태그 색상 변경, 카드 클릭 흐름 확인 | 홈 화면 전체 |

### 4주차: 추천 플로우, Insets, 지도 이해

목표: 홈에서 추천 코스 화면으로 이어지는 흐름과 지도 관련 구조를 이해합니다.

| 날짜 | 학습 내용 | 볼 파일 |
| --- | --- | --- |
| 1일차 | 추천 플로우 Activity 전체 읽기 | `MapPageActivity.java`, `MapSelectionActivity.java`, `FilteringActivity.java`, `CourseMapPageActivity.java`, `CourseDetailPageActivity.java` |
| 2일차 | 화면 이동 흐름 표 만들기 | 추천 플로우 전체 |
| 3일차 | Insets, 상태바, 네비게이션바 처리 | `NavigationBarInsetHelper.java`, `StartActivity.java`, `MapPageActivity.java`, `MapSelectionActivity.java` |
| 4일차 | 테마, Splash 테마, Main 테마, NoActionBar | `themes.xml`, `AndroidManifest.xml` |
| 5일차 | Naver Map SDK, Gradle 의존성, API key | `app/build.gradle.kts`, `AndroidManifest.xml`, `CourseMapPageActivity.java` |
| 6일차 | `OnMapReadyCallback`, `onMapReady` | `CourseMapPageActivity.java` |
| 7일차 | 전체 앱 흐름 설명하기 | 전체 프로젝트 |

## 23. 직접 해볼 실습

### 23.1 Splash 시간 바꾸기

파일:

- `SplashActivity.java`

수정할 코드:

```java
handler.postDelayed(navigationRunnable, 2000);
```

`2000`을 `1000`이나 `3000`으로 바꿔봅니다.

배우는 것:

- Handler
- 지연 실행
- Activity 이동

### 23.2 StartActivity 버튼 텍스트 바꾸기

파일:

- `activity_start.xml`
- `strings.xml`

배우는 것:

- XML 수정
- 문자열 리소스

### 23.3 홈 코스 데이터 추가하기

파일:

- `HomeFragment.java`

`COURSES` 배열에 새 코스를 추가해봅니다.

배우는 것:

- 배열
- 동적 카드 생성
- `loadMoreCourses`

### 23.4 카드 클릭 시 이동 화면 바꾸기

파일:

- `HomeFragment.java`

현재 이동 대상:

```java
CourseDetailPageActivity.class
```

이 값을 다른 Activity로 바꿔보며 화면 흐름을 확인합니다.

배우는 것:

- Intent
- Activity 이동

### 23.5 BottomNavigation 메뉴 추가하기

수정할 파일:

- `menu_main_bottom_nav.xml`
- `MainActivity.java`
- 새 Fragment Java 파일
- 새 fragment XML 파일

배우는 것:

- menu 리소스
- Fragment 추가
- 하단 탭 구조

### 23.6 태그 색상 바꾸기

수정할 파일:

- `colors.xml`
- `shape_tag_rose.xml`
- `HomeFragment.java`

배우는 것:

- color resource
- drawable resource
- Java에서 리소스 사용

### 23.7 NavigationBarInsetHelper 적용해보기

파일:

- `NavigationBarInsetHelper.java`
- 원하는 Activity 파일

배우는 것:

- 공통 유틸 클래스
- 상태바, 네비게이션바 대응
- 코드 재사용

## 24. 핵심 개념 우선순위

가장 먼저 이해할 것:

1. `Activity`
2. `onCreate`
3. `setContentView`
4. XML layout
5. `findViewById`
6. `Button.setOnClickListener`
7. `Intent`
8. `AndroidManifest.xml`

그 다음 이해할 것:

1. `Fragment`
2. `BottomNavigationView`
3. `FragmentManager`
4. `res` 리소스 시스템
5. `drawable`
6. `themes.xml`
7. `WindowInsets`

마지막에 이해할 것:

1. `Handler`
2. `NestedScrollView`
3. 동적 View 생성
4. Naver Map SDK
5. Activity back stack
6. Intent flags

## 25. 파일별 읽는 목적

| 파일 | 읽는 목적 |
| --- | --- |
| `SplashActivity.java` | 앱 시작, 2초 대기, 다음 화면 이동, Handler 이해 |
| `StartActivity.java` | 버튼 클릭, MainActivity 이동, 하단 inset 처리 이해 |
| `MainActivity.java` | 하단 네비게이션, Fragment 교체 구조 이해 |
| `HomeFragment.java` | 홈 화면 핵심 로직, 코스 카드 목록, 스크롤 UI, 동적 View 생성 이해 |
| `FavoritesFragment.java` | 가장 단순한 Fragment 예제 |
| `RecordFragment.java` | 단순 Fragment 구조 반복 학습 |
| `MapPageActivity.java` | 버튼 클릭 후 다음 추천 단계로 이동하는 단순 Activity |
| `FilteringActivity.java` | 추천 필터 화면에서 지도 화면으로 이동하는 흐름 이해 |
| `MapSelectionActivity.java` | 상태바, 툴바, 하단 버튼 inset 처리가 들어간 Activity 이해 |
| `CourseDetailPageActivity.java` | 코스 상세 화면, Intent flag, 화면 종료 흐름 이해 |
| `CourseMapPageActivity.java` | Naver Map SDK 연결 준비, 지도 화면 구조 이해 |
| `NavigationBarInsetHelper.java` | 상태바, 네비게이션바 처리 공통화 이해 |
| `AndroidManifest.xml` | 앱 권한, Activity 등록, 시작 화면, 지도 API key 확인 |
| `app/build.gradle.kts` | SDK 버전, 의존성, 빌드 설정 확인 |

## 26. 최종 이해도 체크리스트

아래 질문에 답할 수 있으면 이 프로젝트의 기본 구조를 이해한 것입니다.

- 앱을 실행하면 왜 `SplashActivity`가 먼저 뜨는가?
- `SplashActivity`에서 2초 후 `StartActivity`로 이동하는 코드는 어디인가?
- `StartActivity`의 버튼은 어떤 XML id와 연결되어 있는가?
- `MainActivity`는 왜 Fragment를 사용하고 있는가?
- 하단 탭을 누르면 어떤 코드가 Fragment를 바꾸는가?
- `HomeFragment`는 왜 `setContentView`가 아니라 `inflater.inflate`를 쓰는가?
- 코스 카드는 XML에 전부 박혀 있는가, Java 코드에서 추가되는가?
- `COURSES` 배열의 데이터가 화면에 어떻게 표시되는가?
- 버튼 클릭 시 Activity 이동은 어떤 코드로 이루어지는가?
- `AndroidManifest.xml`에서 앱 시작 화면은 어떻게 정해지는가?
- 지도 SDK는 어디에 의존성으로 추가되어 있는가?
- 네이버 지도 API key는 어디에 등록되어 있는가?
- 상태바나 하단 네비게이션바에 UI가 가려지지 않게 하는 코드는 어디인가?

## 27. 추천 학습 방식

이 프로젝트는 다음 순서로 직접 열어보며 공부하는 것이 가장 좋습니다.

1. `AndroidManifest.xml`에서 시작 Activity를 확인합니다.
2. `SplashActivity.java`와 `activity_splash.xml`을 함께 봅니다.
3. `StartActivity.java`와 `activity_start.xml`을 함께 봅니다.
4. `MainActivity.java`, `activity_main.xml`, `menu_main_bottom_nav.xml`을 함께 봅니다.
5. `HomeFragment.java`, `fragment_home.xml`, `item_course_card.xml`을 함께 봅니다.
6. 추천 플로우 Activity들을 화면 이동 순서대로 봅니다.
7. 마지막으로 `NavigationBarInsetHelper.java`와 지도 관련 코드를 봅니다.

코드를 읽을 때는 항상 다음 질문을 같이 던지면 좋습니다.

- 이 Java 파일은 어떤 XML 파일을 화면으로 쓰는가?
- 이 XML의 어떤 id를 Java 코드가 찾고 있는가?
- 버튼을 누르면 어디로 이동하는가?
- 이 화면은 Activity인가, Fragment인가?
- 화면에 보이는 데이터는 XML에 고정되어 있는가, Java 코드에서 동적으로 들어오는가?
