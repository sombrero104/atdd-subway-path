package nextstep.subway.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static nextstep.subway.acceptance.LineSteps.*;
import static nextstep.subway.acceptance.SectionSteps.*;
import static nextstep.subway.acceptance.StationSteps.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("구간 관리 기능")
public class SectionAcceptanceTest extends AcceptanceTest {

    private static final String 신분당선 = "신분당선";
    private static final String BG_RED_600 = "bg-red-600";

    private static final String 강남역 = "강남역";
    private static final String 양재역 = "양재역";
    private static final String 양재시민의숲역 = "양재시민의숲역";
    private static final String 미금역 = "미금역";
    private static final String 동천역 = "동천역";

    private static final int 강남역_양재시민의숲역_거리 = 7;
    private static final int 강남역_양재역_거리 = 4;
    private static final int 미금역_동천역_거리 = 3;

    private Long 강남역_아이디, 양재역_아이디, 양재시민의숲역_아이디, 미금역_아이디, 동천역_아이디, 신분당선_아이디;
    private String 신분당선_구간_URI;

    @BeforeEach
    void beforeEach() {
        강남역_아이디 = 지하철역_생성_요청(강남역).jsonPath().getLong("id");
        양재역_아이디 = 지하철역_생성_요청(양재역).jsonPath().getLong("id");
        양재시민의숲역_아이디 = 지하철역_생성_요청(양재시민의숲역).jsonPath().getLong("id");
        미금역_아이디 = 지하철역_생성_요청(미금역).jsonPath().getLong("id");
        동천역_아이디 = 지하철역_생성_요청(동천역).jsonPath().getLong("id");

        ExtractableResponse<Response> 지하철_노선_생성_요청 = 지하철_노선_생성_요청(신분당선, BG_RED_600, 강남역_아이디, 양재시민의숲역_아이디, 강남역_양재시민의숲역_거리);
        신분당선_아이디 = 지하철_노선_생성_요청.jsonPath().getLong("id");
        신분당선_구간_URI = 지하철_노선_생성_요청.header("Location") + "/sections";
    }

    /**
     * When 구간 생성을 요청 하면
     * Then 구간 생성이 성공한다.
     */
    @DisplayName("구간 생성")
    @Test
    void createSection() {
        // when
        구간_생성_요청(신분당선_아이디, 강남역_아이디, 양재역_아이디, 강남역_양재역_거리);

        // then
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getList("stations.id", Long.class)).containsExactly(강남역_아이디, 양재역_아이디, 양재시민의숲역_아이디);
    }

    /**
     * When 역 사이에 새로운 역을 등록할 경우 기존 역 사이 길이보다 크거나 같으면
     * Then 새로운 구간 등록이 실패한다
     */
    @DisplayName("기존 역 사이 길이보다 크거나 같은 구간을 등록")
    @Test
    void addLineSectionOverLength() {
        // when
        ExtractableResponse<Response> createRequest = 구간_생성_요청(신분당선_아이디, 강남역_아이디, 양재역_아이디, 강남역_양재시민의숲역_거리);

        // then
        assertThat(createRequest.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * When 상행역과 하행역이 이미 노선에 모두 등록되어 있다면
     * Then 새로운 구간 등록이 실패한다
     */
    @DisplayName("상행역과 하행역이 이미 노선에 모두 등록되어 있는 경우 구간을 등록")
    @Test
    void addDuplicateLineSection() {
        // when
        ExtractableResponse<Response> createRequest = 구간_생성_요청(신분당선_아이디, 강남역_아이디, 양재시민의숲역_아이디, 강남역_양재시민의숲역_거리);

        // then
        assertThat(createRequest.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * When 상행역과 하행역 둘 중 하나도 포함되어 있지 않으면
     * Then 새로운 구간 등록이 실패한다
     */
    @DisplayName("상행역과 하행역 둘 중 하나도 포함되어 있지 않은 경우 구간을 등록")
    @Test
    void addNonConnectableLineSection() {
        // when
        ExtractableResponse<Response> createRequest = 구간_생성_요청(신분당선_아이디, 미금역_아이디, 동천역_아이디, 미금역_동천역_거리);

        // then
        assertThat(createRequest.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * When 구간 제거 요청을 하면
     * Then 구간 제거가 성공한다.
     */
    @DisplayName("구간 제거")
    @Test
    void deleteSection() {
        // given
        구간_생성_요청(신분당선_아이디, 강남역_아이디, 양재역_아이디, 강남역_양재역_거리);

        // when
        구간_제거_요청(신분당선_구간_URI, 양재역_아이디);

        // then
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getList("stations.id", Long.class)).containsExactly(강남역_아이디, 양재시민의숲역_아이디);
    }

}
