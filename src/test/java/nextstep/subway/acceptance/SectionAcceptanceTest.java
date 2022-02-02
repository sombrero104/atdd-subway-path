package nextstep.subway.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static nextstep.subway.acceptance.LineSteps.지하철_노선_생성_요청;
import static nextstep.subway.acceptance.SectionSteps.*;
import static nextstep.subway.acceptance.StationSteps.지하철역_생성_요청;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("구간 관리 기능")
public class SectionAcceptanceTest extends AcceptanceTest {

    private static final String 신분당선 = "신분당선";
    private static final String BG_RED_600 = "bg-red-600";

    private static final String 강남역 = "강남역";
    private static final String 양재역 = "양재역";
    private static final String 양재시민의숲역 = "양재시민의숲역";
    private static final String 동천역 = "동천역";
    private static final String 수지구청역 = "수지구청역";
    private static final int 강남역_양재역_거리 = 1;
    private static final int 양재역_양재시민의숲역_거리 = 2;
    private static final int 동천역_수지구청역_거리 = 3;

    private Long 강남역_아이디, 양재역_아이디, 양재시민의숲역_아이디, 동천역_아이디, 수지구청역_아이디, 신분당선_아이디;
    private String 신분당선_구간_URI;

    @BeforeEach
    void beforeEach() {
        강남역_아이디 = 지하철역_생성_요청(강남역).jsonPath().getLong("id");
        양재역_아이디 = 지하철역_생성_요청(양재역).jsonPath().getLong("id");
        양재시민의숲역_아이디 = 지하철역_생성_요청(양재시민의숲역).jsonPath().getLong("id");
        동천역_아이디 = 지하철역_생성_요청(동천역).jsonPath().getLong("id");
        수지구청역_아이디 = 지하철역_생성_요청(수지구청역).jsonPath().getLong("id");

        ExtractableResponse<Response> 지하철_노선_생성_요청 = 지하철_노선_생성_요청(신분당선, BG_RED_600, 강남역_아이디, 양재역_아이디, 강남역_양재역_거리);
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
        ExtractableResponse<Response> createResponse = 구간_생성_요청(신분당선_아이디, 양재역_아이디, 양재시민의숲역_아이디, 양재역_양재시민의숲역_거리);

        // then
        assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(createResponse.header("Location")).isNotBlank();
    }

    /**
     * When 구간 제거 요청을 하면
     * Then 구간 제거가 성공한다.
     */
    @DisplayName("구간 제거")
    @Test
    void deleteSection() {
        // given
        ExtractableResponse<Response> createResponse = 구간_생성_요청(신분당선_아이디, 양재역_아이디, 양재시민의숲역_아이디, 양재역_양재시민의숲역_거리);

        // when
        ExtractableResponse<Response> deleteResponse = 구간_제거_요청(createResponse.header("Location"), 양재시민의숲역_아이디);

        // then
        assertThat(deleteResponse.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

}
