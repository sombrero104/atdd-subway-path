package nextstep.subway.line.domain;

import nextstep.subway.station.domain.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class LineTest {

    private Station upStation;
    private Station downStation;
    private Line line;

    @BeforeEach
    void setUp() {
        upStation = new Station("강남역");
        downStation = new Station("교대역");
        line = new Line("2호선","green", upStation, downStation, 10);
    }

    @Test
    void getStations() {
        List<Station> stations = line.getSections().getStations();

        assertThat(stations.size()).isEqualTo(2);
    }

    @Test
    void addSection() {
        Station newDownStation = new Station("삼성역");
        line.addSection(downStation, newDownStation, 10);

        assertThat(line.getSections().size()).isEqualTo(2);
    }

    @DisplayName("구간 가운데 추가하는 구간의 거리가 기존 구간보다 거리가 크거나 같을 경우 에러 발생")
    @Test
    void addSectionInMiddle() {
        Station middleStation = new Station("삼성역");

        assertThatThrownBy(() -> line.addSection(upStation, middleStation, 10))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("추가하는 구간의 거리가 잘못되었습니다.");

    }

    @DisplayName("이미 존재하는 역 추가 시 에러 발생")
    @Test
    void addSectionAlreadyIncluded() {
        assertThatThrownBy(() -> line.addSection(upStation, downStation, 10))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("상행역과 하행역이 이미 존재합니다.");
    }

    @DisplayName("지하철 노선의 첫번째 역 제거")
    @Test
    void removeFirstSection() {
        // given
        Station newDownStation = new Station("삼성역");

        line.addSection(downStation, newDownStation, 10);

        // when
        line.removeSection(upStation);

        // then
        assertThat(line.getSections().size()).isEqualTo(1);
    }

    @DisplayName("지하철 노선의 마지막 역 제거")
    @Test
    void removeLastSection() {
        // given
        Station newDownStation = new Station("삼성역");

        line.addSection(downStation, newDownStation, 10);

        // when
        line.removeSection(newDownStation);

        // then
        assertThat(line.getSections().size()).isEqualTo(1);
    }

    @DisplayName("지하철 노선의 중간 역 제거")
    @Test
    void removeMiddleSection() {
        // given
        Station newMiddleStation = new Station("삼성역");

        line.addSection(newMiddleStation, downStation, 5);

        // when
        line.removeSection(newMiddleStation);

        // then
        assertThat(line.getSections().size()).isEqualTo(1);
        assertThat(line.getSections().getDistances().get(0)).isEqualTo(10);
    }

    @DisplayName("구간이 하나인 노선에서 역 삭제 시 에러 발생")
    @Test
    void removeSectionNotEndOfList() {
        // given
        // when, then
        assertThatThrownBy(() -> line.removeSection(downStation)).isInstanceOf(RuntimeException.class);
    }
}