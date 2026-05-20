package com.example.day2day.data.local;

import android.graphics.Color;
import com.example.day2day.data.local.entity.Course;
import com.example.day2day.data.local.entity.CoursePlace;
import com.example.day2day.data.local.entity.PopularCourse;
import java.util.Arrays;
import java.util.List;

public final class CourseSeedData {
  private CourseSeedData() {}

  public static List<Course> getPopularCourses() {
    return Arrays.asList(
        new Course(
            "popular_001",
            "홍대 감성 데이트 코스",
            "홍대 > 연남동 > 망원",
            "#감성,#데이트",
            "★ 4.8",
            Color.parseColor("#FFD6D6")),
        new Course(
            "popular_002",
            "한강 피크닉 코스",
            "여의도 > 한강공원 > 노을공원",
            "#야외,#피크닉",
            "★ 4.6",
            Color.parseColor("#D6EAF8")),
        new Course(
            "popular_003",
            "성수 카페 투어 코스",
            "성수역 > 서울숲 > 뚝섬",
            "#카페,#힐링",
            "★ 4.7",
            Color.parseColor("#D5F5E3")),
        new Course(
            "popular_004",
            "북촌 한옥마을 코스",
            "경복궁 > 북촌 > 인사동",
            "#역사,#전통",
            "★ 4.5",
            Color.parseColor("#FEF9E7")),
        new Course(
            "popular_005",
            "강남 쇼핑 데이트",
            "코엑스 > 청담 > 압구정",
            "#쇼핑,#럭셔리",
            "★ 4.4",
            Color.parseColor("#F5EEF8")),
        new Course(
            "popular_006",
            "을지로 힙스터 코스",
            "을지로3가 > 을지로4가 > 황학동",
            "#힙,#복고",
            "★ 4.6",
            Color.parseColor("#D6DBDF")),
        new Course(
            "popular_007",
            "남산 야경 데이트",
            "명동 > 남산타워 > 이태원",
            "#야경,#데이트",
            "★ 4.9",
            Color.parseColor("#D7BDE2")),
        new Course(
            "popular_008",
            "익선동 복고 감성 코스",
            "익선동 > 종로3가 > 낙원상가",
            "#복고,#감성",
            "★ 4.5",
            Color.parseColor("#FDEBD0")),
        new Course(
            "popular_009",
            "연남동 브런치 코스",
            "연남동 > 경의선숲길 > 홍대",
            "#브런치,#카페",
            "★ 4.7",
            Color.parseColor("#D5F5E3")),
        new Course(
            "popular_010",
            "서촌 골목 투어",
            "경복궁역 > 서촌 > 통인시장",
            "#골목,#전통",
            "★ 4.4",
            Color.parseColor("#FEF9E7")),
        new Course(
            "popular_011",
            "동대문 쇼핑 코스",
            "동대문 > DDP > 신당동",
            "#쇼핑,#패션",
            "★ 4.3",
            Color.parseColor("#EAF2FF")),
        new Course(
            "popular_012",
            "가로수길 브랜드 투어",
            "신사역 > 가로수길 > 세로수길",
            "#쇼핑,#트렌디",
            "★ 4.5",
            Color.parseColor("#F5EEF8")),
        new Course(
            "popular_013",
            "합정 카페 거리 코스",
            "합정역 > 망원동 > 당인리",
            "#카페,#감성",
            "★ 4.6",
            Color.parseColor("#FFD6D6")),
        new Course(
            "popular_014",
            "상암 하늘공원 코스",
            "상암DMC > 하늘공원 > 노을공원",
            "#야외,#힐링",
            "★ 4.5",
            Color.parseColor("#D5F5E3")),
        new Course(
            "popular_015",
            "뚝섬 한강 자전거 코스",
            "뚝섬한강공원 > 자양동 > 건대입구",
            "#자전거,#야외",
            "★ 4.4",
            Color.parseColor("#D6EAF8")),
        new Course(
            "popular_016",
            "경리단길 레스토랑 투어",
            "이태원역 > 경리단길 > 녹사평",
            "#맛집,#이국적",
            "★ 4.7",
            Color.parseColor("#FDEBD0")),
        new Course(
            "popular_017",
            "낙산공원 야경 코스",
            "혜화역 > 낙산공원 > 이화마을",
            "#야경,#산책",
            "★ 4.6",
            Color.parseColor("#D7BDE2")),
        new Course(
            "popular_018",
            "광화문 역사 탐방",
            "광화문 > 청계천 > 종각",
            "#역사,#문화",
            "★ 4.3",
            Color.parseColor("#FEF9E7")),
        new Course(
            "popular_019",
            "건대 먹거리 코스",
            "건대입구 > 자양동 > 구의역",
            "#맛집,#야식",
            "★ 4.5",
            Color.parseColor("#FFD6D6")),
        new Course(
            "popular_020",
            "부암동 카페 코스",
            "경복궁역 > 부암동 > 세검정",
            "#카페,#한적",
            "★ 4.8",
            Color.parseColor("#D5F5E3")),
        new Course(
            "popular_021",
            "명동 뷰티 쇼핑",
            "명동역 > 명동거리 > 남대문",
            "#뷰티,#쇼핑",
            "★ 4.2",
            Color.parseColor("#F9EBEA")),
        new Course(
            "popular_022",
            "왕십리 맛집 투어",
            "왕십리역 > 행당동 > 마장동",
            "#맛집,#로컬",
            "★ 4.4",
            Color.parseColor("#EAFAF1")),
        new Course(
            "popular_023",
            "서울숲 피크닉 코스",
            "서울숲역 > 서울숲공원 > 성수동",
            "#피크닉,#힐링",
            "★ 4.7",
            Color.parseColor("#D6EAF8")),
        new Course(
            "popular_024",
            "신촌 대학가 코스",
            "신촌역 > 이화여대 > 연세로",
            "#젊음,#활기",
            "★ 4.3",
            Color.parseColor("#FEF9E7")),
        new Course(
            "popular_025",
            "잠실 롯데월드 데이트",
            "잠실역 > 롯데월드 > 석촌호수",
            "#놀이공원,#데이트",
            "★ 4.6",
            Color.parseColor("#FFD6D6")),
        new Course(
            "popular_026",
            "종로 전통시장 투어",
            "종로5가 > 광장시장 > 방산시장",
            "#시장,#먹거리",
            "★ 4.5",
            Color.parseColor("#FDEBD0")),
        new Course(
            "popular_027",
            "마포 힙플레이스 코스",
            "마포역 > 공덕동 > 아현동",
            "#힙,#로컬",
            "★ 4.4",
            Color.parseColor("#D6DBDF")),
        new Course(
            "popular_028",
            "도봉산 등산 코스",
            "도봉산역 > 도봉산 > 망월사",
            "#등산,#자연",
            "★ 4.6",
            Color.parseColor("#D5F5E3")),
        new Course(
            "popular_029",
            "압구정 로데오 코스",
            "압구정역 > 로데오거리 > 청담동",
            "#쇼핑,#감성",
            "★ 4.5",
            Color.parseColor("#F5EEF8")),
        new Course(
            "popular_030",
            "망원동 감성 투어",
            "망원역 > 망원시장 > 합정",
            "#감성,#로컬",
            "★ 4.7",
            Color.parseColor("#D7BDE2")),
        new Course(
            "popular_031",
            "노량진 수산시장 코스",
            "노량진역 > 수산시장 > 한강대교",
            "#해산물,#맛집",
            "★ 4.3",
            Color.parseColor("#EAF2FF")));
  }

  public static List<CoursePlace> getCoursePlaces() {
    return Arrays.asList(
        // popular_001: 홍대 감성 데이트 코스
        new CoursePlace("popular_001", "홍대입구역", 37.5573, 126.9243, 0),
        new CoursePlace("popular_001", "연남동", 37.5608, 126.9228, 1),
        new CoursePlace("popular_001", "망원동", 37.5560, 126.9072, 2),
        // popular_002: 한강 피크닉 코스
        new CoursePlace("popular_002", "여의도", 37.5219, 126.9245, 0),
        new CoursePlace("popular_002", "한강공원", 37.5282, 126.9327, 1),
        new CoursePlace("popular_002", "노을공원", 37.5706, 126.8775, 2),
        // popular_003: 성수 카페 투어 코스
        new CoursePlace("popular_003", "성수역", 37.5447, 127.0556, 0),
        new CoursePlace("popular_003", "서울숲", 37.5445, 127.0374, 1),
        new CoursePlace("popular_003", "뚝섬", 37.5479, 127.0666, 2),
        // popular_004: 북촌 한옥마을 코스
        new CoursePlace("popular_004", "경복궁", 37.5796, 126.9770, 0),
        new CoursePlace("popular_004", "북촌", 37.5826, 126.9852, 1),
        new CoursePlace("popular_004", "인사동", 37.5739, 126.9855, 2),
        // popular_005: 강남 쇼핑 데이트
        new CoursePlace("popular_005", "코엑스", 37.5126, 127.0595, 0),
        new CoursePlace("popular_005", "청담동", 37.5218, 127.0474, 1),
        new CoursePlace("popular_005", "압구정", 37.5272, 127.0285, 2),
        // popular_006: 을지로 힙스터 코스
        new CoursePlace("popular_006", "을지로3가", 37.5664, 126.9935, 0),
        new CoursePlace("popular_006", "을지로4가", 37.5651, 126.9998, 1),
        new CoursePlace("popular_006", "황학동", 37.5714, 127.0113, 2),
        // popular_007: 남산 야경 데이트
        new CoursePlace("popular_007", "명동", 37.5636, 126.9868, 0),
        new CoursePlace("popular_007", "남산타워", 37.5511, 126.9882, 1),
        new CoursePlace("popular_007", "이태원", 37.5344, 126.9945, 2),
        // popular_008: 익선동 복고 감성 코스
        new CoursePlace("popular_008", "익선동", 37.5745, 126.9999, 0),
        new CoursePlace("popular_008", "종로3가", 37.5716, 126.9916, 1),
        new CoursePlace("popular_008", "낙원상가", 37.5748, 126.9891, 2),
        // popular_009: 연남동 브런치 코스
        new CoursePlace("popular_009", "연남동", 37.5608, 126.9228, 0),
        new CoursePlace("popular_009", "경의선숲길", 37.5617, 126.9215, 1),
        new CoursePlace("popular_009", "홍대", 37.5573, 126.9243, 2),
        // popular_010: 서촌 골목 투어
        new CoursePlace("popular_010", "경복궁역", 37.5796, 126.9770, 0),
        new CoursePlace("popular_010", "서촌", 37.5802, 126.9688, 1),
        new CoursePlace("popular_010", "통인시장", 37.5805, 126.9671, 2),
        // popular_011: 동대문 쇼핑 코스
        new CoursePlace("popular_011", "동대문", 37.5700, 127.0095, 0),
        new CoursePlace("popular_011", "DDP", 37.5670, 127.0094, 1),
        new CoursePlace("popular_011", "신당동", 37.5630, 127.0135, 2),
        // popular_012: 가로수길 브랜드 투어
        new CoursePlace("popular_012", "신사역", 37.5171, 127.0196, 0),
        new CoursePlace("popular_012", "가로수길", 37.5208, 127.0223, 1),
        new CoursePlace("popular_012", "세로수길", 37.5195, 127.0205, 2),
        // popular_013: 합정 카페 거리 코스
        new CoursePlace("popular_013", "합정역", 37.5499, 126.9137, 0),
        new CoursePlace("popular_013", "망원동", 37.5560, 126.9072, 1),
        new CoursePlace("popular_013", "당인리", 37.5540, 126.9091, 2),
        // popular_014: 상암 하늘공원 코스
        new CoursePlace("popular_014", "상암DMC", 37.5802, 126.8898, 0),
        new CoursePlace("popular_014", "하늘공원", 37.5706, 126.8775, 1),
        new CoursePlace("popular_014", "노을공원", 37.5706, 126.8760, 2),
        // popular_015: 뚝섬 한강 자전거 코스
        new CoursePlace("popular_015", "뚝섬한강공원", 37.5299, 127.0617, 0),
        new CoursePlace("popular_015", "자양동", 37.5350, 127.0748, 1),
        new CoursePlace("popular_015", "건대입구", 37.5395, 127.0694, 2),
        // popular_016: 경리단길 레스토랑 투어
        new CoursePlace("popular_016", "이태원역", 37.5344, 126.9945, 0),
        new CoursePlace("popular_016", "경리단길", 37.5390, 126.9938, 1),
        new CoursePlace("popular_016", "녹사평", 37.5400, 126.9912, 2),
        // popular_017: 낙산공원 야경 코스
        new CoursePlace("popular_017", "혜화역", 37.5826, 127.0015, 0),
        new CoursePlace("popular_017", "낙산공원", 37.5817, 127.0080, 1),
        new CoursePlace("popular_017", "이화마을", 37.5808, 127.0058, 2),
        // popular_018: 광화문 역사 탐방
        new CoursePlace("popular_018", "광화문", 37.5759, 126.9769, 0),
        new CoursePlace("popular_018", "청계천", 37.5700, 126.9784, 1),
        new CoursePlace("popular_018", "종각", 37.5700, 126.9815, 2),
        // popular_019: 건대 먹거리 코스
        new CoursePlace("popular_019", "건대입구", 37.5395, 127.0694, 0),
        new CoursePlace("popular_019", "자양동", 37.5350, 127.0748, 1),
        new CoursePlace("popular_019", "구의역", 37.5413, 127.0854, 2),
        // popular_020: 부암동 카페 코스
        new CoursePlace("popular_020", "경복궁역", 37.5796, 126.9770, 0),
        new CoursePlace("popular_020", "부암동", 37.5975, 126.9610, 1),
        new CoursePlace("popular_020", "세검정", 37.6060, 126.9618, 2),
        // popular_021: 명동 뷰티 쇼핑
        new CoursePlace("popular_021", "명동역", 37.5636, 126.9868, 0),
        new CoursePlace("popular_021", "명동거리", 37.5630, 126.9862, 1),
        new CoursePlace("popular_021", "남대문", 37.5596, 126.9776, 2),
        // popular_022: 왕십리 맛집 투어
        new CoursePlace("popular_022", "왕십리역", 37.5613, 127.0375, 0),
        new CoursePlace("popular_022", "행당동", 37.5540, 127.0368, 1),
        new CoursePlace("popular_022", "마장동", 37.5633, 127.0405, 2),
        // popular_023: 서울숲 피크닉 코스
        new CoursePlace("popular_023", "서울숲역", 37.5447, 127.0556, 0),
        new CoursePlace("popular_023", "서울숲공원", 37.5445, 127.0374, 1),
        new CoursePlace("popular_023", "성수동", 37.5510, 127.0561, 2),
        // popular_024: 신촌 대학가 코스
        new CoursePlace("popular_024", "신촌역", 37.5553, 126.9373, 0),
        new CoursePlace("popular_024", "이화여대", 37.5620, 126.9474, 1),
        new CoursePlace("popular_024", "연세로", 37.5561, 126.9391, 2),
        // popular_025: 잠실 롯데월드 데이트
        new CoursePlace("popular_025", "잠실역", 37.5133, 127.1001, 0),
        new CoursePlace("popular_025", "롯데월드", 37.5111, 127.0985, 1),
        new CoursePlace("popular_025", "석촌호수", 37.5097, 127.1025, 2),
        // popular_026: 종로 전통시장 투어
        new CoursePlace("popular_026", "종로5가", 37.5700, 126.9964, 0),
        new CoursePlace("popular_026", "광장시장", 37.5702, 126.9997, 1),
        new CoursePlace("popular_026", "방산시장", 37.5672, 127.0000, 2),
        // popular_027: 마포 힙플레이스 코스
        new CoursePlace("popular_027", "마포역", 37.5431, 126.9496, 0),
        new CoursePlace("popular_027", "공덕동", 37.5455, 126.9517, 1),
        new CoursePlace("popular_027", "아현동", 37.5528, 126.9501, 2),
        // popular_028: 도봉산 등산 코스
        new CoursePlace("popular_028", "도봉산역", 37.6893, 127.0467, 0),
        new CoursePlace("popular_028", "도봉산", 37.7150, 127.0468, 1),
        new CoursePlace("popular_028", "망월사", 37.7055, 127.0522, 2),
        // popular_029: 압구정 로데오 코스
        new CoursePlace("popular_029", "압구정역", 37.5272, 127.0285, 0),
        new CoursePlace("popular_029", "로데오거리", 37.5276, 127.0394, 1),
        new CoursePlace("popular_029", "청담동", 37.5218, 127.0474, 2),
        // popular_030: 망원동 감성 투어
        new CoursePlace("popular_030", "망원역", 37.5556, 126.9091, 0),
        new CoursePlace("popular_030", "망원시장", 37.5530, 126.9103, 1),
        new CoursePlace("popular_030", "합정", 37.5499, 126.9137, 2),
        // popular_031: 노량진 수산시장 코스
        new CoursePlace("popular_031", "노량진역", 37.5135, 126.9435, 0),
        new CoursePlace("popular_031", "수산시장", 37.5140, 126.9421, 1),
        new CoursePlace("popular_031", "한강대교", 37.5196, 126.9310, 2));
  }

  public static List<PopularCourse> getPopularCourseIds() {
    List<PopularCourse> list = new java.util.ArrayList<>();
    for (Course course : getPopularCourses()) {
      list.add(new PopularCourse(course.courseId));
    }
    return list;
  }
}
