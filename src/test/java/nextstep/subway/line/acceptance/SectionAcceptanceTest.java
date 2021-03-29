package nextstep.subway.line.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nextstep.subway.line.acceptance.LineSteps.*;
import static nextstep.subway.station.StationSteps.지하철역_등록되어_있음;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선에 구간 등록하기 위한 테스트")
public class SectionAcceptanceTest extends AcceptanceTest {

    private LineResponse 이호선;
    private StationResponse 을지로4가역;
    private StationResponse 을지로3가역;
    private StationResponse 을지로입구역;
    private StationResponse 시청역;
    private StationResponse 충정로역;

    private static final int DEFAULT_LINE_DISTANCE = 10;
    private static final int DEFAULT_SECTION_DISTANCE = 4;

    @BeforeEach
    public void setUp() {
        super.setUp();

        을지로4가역 = 지하철역_등록되어_있음("을지로4가역").as(StationResponse.class);
        을지로3가역 = 지하철역_등록되어_있음("을지로3가역").as(StationResponse.class);
        을지로입구역 = 지하철역_등록되어_있음("을지로입구역").as(StationResponse.class);
        시청역 = 지하철역_등록되어_있음("시청역").as(StationResponse.class);
        충정로역 = 지하철역_등록되어_있음("충정로역").as(StationResponse.class);

        Map<String, Object> lineRequestParam = createLineParams("이호선", "green", 을지로3가역.getId(), 시청역.getId(), DEFAULT_LINE_DISTANCE);
        이호선 = 지하철_노선_등록되어_있음(lineRequestParam).as(LineResponse.class);
    }

    @DisplayName("지하철 구간 등록 시 역 사이에 새로운 구간을 추가")
    @Test
    void addSectionWithUpStation() {
        //when
        //지하철 구간 등록
        지하철_노선에_지하철역_등록_요청(이호선, 을지로3가역, 을지로입구역, 3);

        //then
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(이호선);
        지하철_노선에_지하철역_순서_정렬됨(response, Arrays.asList(을지로3가역, 을지로입구역, 시청역));
    }

    @DisplayName("지하철 구간 등록 시 새로운 역을 상행 종점으로 등록한다.")
    @Test
    void addSectionWithUpStationAsDownStation() {
        //when
        //을지로3가 시청역
        지하철_노선에_지하철역_등록_요청(이호선, 을지로4가역, 을지로3가역, 5);

        //then
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(이호선);
        지하철_노선에_지하철역_순서_정렬됨(response, Arrays.asList(을지로4가역, 을지로3가역, 시청역));
    }

    @DisplayName("지하철 구간 등록 시 새로운 역을 하행 종점으로 등록한다.")
    @Test
    void addSectionWithDownStationAsUpStation() {
        //when
        지하철_노선에_지하철역_등록_요청(이호선, 시청역, 충정로역, 2);

        //then
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(이호선);
        지하철_노선에_지하철역_순서_정렬됨(response, Arrays.asList(을지로3가역, 시청역, 충정로역));
    }

    @DisplayName("구간 사이에 새로울 역을 등록하는데 기존 구간 길이와 같으면 등록 불가.")
    @Test
    void addSectionWithSameDistance(){
        //when
        지하철_노선에_지하철역_등록_요청(이호선, 을지로입구역, 시청역, DEFAULT_LINE_DISTANCE);

        //then
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(이호선);
        지하철_노선_조회시_지하철역_등록_되지_않은_상태(response, Arrays.asList(을지로3가역, 시청역));
    }

    @DisplayName("구간 사이에 새로울 역을 등록하는데 기존 구간 보다 길면 등록 불가.")
    @Test
    void addSectionWithLongerDistance(){
        //when
        ExtractableResponse<Response> response = 지하철_노선에_지하철역_등록_요청(이호선, 을지로3가역, 을지로입구역, DEFAULT_LINE_DISTANCE + 2);

        //then
        지하철_노선에_지하철역_등록_실패됨(response);
    }

    @DisplayName("상행역과 하행역이 노선에 모두 등록되어 있다면 추가할 수 없음")
    @Test
    void addSectionWithDuplicateBothUpAndDownStations() {
        //given
        지하철_노선에_지하철역_등록_요청(이호선, 시청역, 충정로역, DEFAULT_SECTION_DISTANCE);

        //when
        ExtractableResponse<Response> response = 지하철_노선에_지하철역_등록_요청(이호선, 시청역, 충정로역, 2);

        //then
        지하철_노선에_지하철역_등록_실패됨(response);
    }


    @DisplayName("상행역과 하행역이 노선에 모두 등록되어 있다면 추가할 수 없음")
    @Test
    void addSectionWithDuplicateBothUpAndDownStations2() {
        //given
        지하철_노선에_지하철역_등록_요청(이호선, 시청역, 충정로역, DEFAULT_SECTION_DISTANCE);

        //when
        ExtractableResponse<Response> response = 지하철_노선에_지하철역_등록_요청(이호선, 을지로3가역, 충정로역, 4);

        //then
        지하철_노선에_지하철역_등록_실패됨(response);
    }

    @DisplayName("상행역과 하행역이 노선에 모두 등록되어 있지 않으면 추가할 수 없음")
    @Test
    void addSectionWithNotRegistedBothUpAndDownStations2() {
        //given
        지하철_노선에_지하철역_등록_요청(이호선, 을지로입구역, 시청역, DEFAULT_SECTION_DISTANCE);

        //when
        ExtractableResponse<Response> response = 지하철_노선에_지하철역_등록_요청(이호선, 을지로4가역, 충정로역, 4);

        //then
        지하철_노선에_지하철역_등록_실패됨(response);
    }

    @DisplayName("마지막 구간 삭제시 정상 삭제")
    @Test
    void removeLastSection() {
        //given
        지하철_노선에_지하철역_등록_요청(이호선, 시청역, 충정로역, DEFAULT_SECTION_DISTANCE);

        //when
        ExtractableResponse<Response> response = 지하철_구간_제거_요청(이호선.getId(), 충정로역.getId());

        //then
        지하철_노선_삭제됨(response);
    }

    @DisplayName("지하철 중간 노선을 제거한다.")
    @Test
    void removeSectionInMiddle() {
        //given
        지하철_노선에_지하철역_등록_요청(이호선, 시청역, 충정로역, DEFAULT_SECTION_DISTANCE);

        // when
        ExtractableResponse<Response> response = 지하철_구간_제거_요청(이호선.getId(), 시청역.getId());

        // then
        지하철_노선_삭제됨(response);
    }

    private Map<String, Object> createLineParams(String lineName, String color, Long upStationId, Long downStationId, int distance) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", lineName);
        params.put("color", color);
        params.put("upStationId", upStationId);
        params.put("downStationId", downStationId);
        params.put("distance", distance);

        return params;
    }

    public static void 지하철_노선에_지하철역_등록됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    public static void 지하철_노선에_지하철역_등록_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    public static void 지하철_노선_삭제됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    public static void 지하철_노선_조회시_지하철역_등록_되지_않은_상태(ExtractableResponse<Response> response, List<StationResponse> expectedStations) {
        LineResponse line = response.as(LineResponse.class);
        List<Long> stationIds = line.getStations().stream()
                .map(it -> it.getId())
                .collect(Collectors.toList());

        List<Long> expectedStationIds = expectedStations.stream()
                .map(it -> it.getId())
                .collect(Collectors.toList());

        assertThat(stationIds).containsExactlyElementsOf(expectedStationIds);
    }

    public static void 지하철_노선에_지하철역_순서_정렬됨(ExtractableResponse<Response> response, List<StationResponse> expectedStations) {
        LineResponse line = response.as(LineResponse.class);
        List<Long> stationIds = line.getStations().stream()
                .map(it -> it.getId())
                .collect(Collectors.toList());

        List<Long> expectedStationIds = expectedStations.stream()
                .map(it -> it.getId())
                .collect(Collectors.toList());

        assertThat(stationIds).containsExactlyElementsOf(expectedStationIds);
    }

    public static void 지하철_노선에_지하철역_제외됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    public static void 지하철_노선에_지하철역_제외_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
