package nextstep.subway.unit;

import nextstep.subway.applicaion.LineService;
import nextstep.subway.applicaion.dto.LineRequest;
import nextstep.subway.applicaion.dto.LineResponse;
import nextstep.subway.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LineServiceMockTest {

    @Mock
    private LineRepository lineRepository;
    @Mock
    private StationRepository stationRepository;

    private Line 신분당선;
    private Station 강남역, 양재역, 양재시민의숲역;

    @BeforeEach
    void beforeEach() {
        신분당선 = new Line("신분당선", "bg-red-600");
        강남역 = new Station("강남역");
        양재역 = new Station("양재역");
        양재시민의숲역 = new Station("양재시민의숲역");
        ReflectionTestUtils.setField(신분당선, "id", 1L);
        ReflectionTestUtils.setField(강남역, "id", 1L);
        ReflectionTestUtils.setField(양재역, "id", 2L);
        ReflectionTestUtils.setField(양재시민의숲역, "id", 3L);
        신분당선.registSection(new Section(신분당선, 강남역, 양재시민의숲역, 7));
    }

    /**
     * When 기존 구간 사이에 새로운 구간 생성을 요청하면
     * Then 구간 생성이 성공한다.
     */
    @DisplayName("기존 구간 사이에 새로운 구간 생성")
    @Test
    void addSection() {
        // given
        LineService lineService = new LineService(lineRepository, stationRepository);
        when(lineRepository.findById(any())).thenReturn(Optional.ofNullable(신분당선));
        when(stationRepository.findById(강남역.getId())).thenReturn(Optional.ofNullable(강남역));
        when(stationRepository.findById(양재역.getId())).thenReturn(Optional.ofNullable(양재역));

        // when
        LineRequest lineRequest = new LineRequest(신분당선.getName(), 신분당선.getColor(), 강남역.getId(), 양재역.getId(), 4);
        LineResponse lineResponse = lineService.addSection(신분당선.getId(), lineRequest);

        // then
        assertThat(lineResponse.getId()).isNotNull();
        assertThat(lineResponse.getName()).isEqualTo(lineRequest.getName());
        assertThat(신분당선.getStationList()).containsExactly(강남역, 양재역, 양재시민의숲역);
    }

}
