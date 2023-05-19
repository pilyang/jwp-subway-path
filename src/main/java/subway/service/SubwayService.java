package subway.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import subway.domain.LineDirection;
import subway.domain.Subway;
import subway.domain.line.Line;
import subway.domain.station.Station;
import subway.repository.LineRepository;
import subway.repository.StationRepository;
import subway.service.dto.LineDto;
import subway.ui.dto.LineRequest;
import subway.ui.dto.StationInsertRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubwayService {

    private final LineRepository lineRepository;
    private final StationRepository stationRepository;

    public SubwayService(final LineRepository lineRepository, final StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional
    public Long create(final LineRequest lineRequest) {
        final Subway subway = loadSubwayFromRepository();
        final Station upStation = subway.getStation(lineRequest.getUpStationId());
        final Station downStation = subway.getStation(lineRequest.getDownStationId());

        final Line line = Line.of(lineRequest.getName(), lineRequest.getColor(),
                upStation.getId(), downStation.getId(), lineRequest.getDistance());
        final Long lineId = lineRepository.create(line);
        subway.addLine(Line.of(
                lineId,
                line.getName(),
                line.getColor(),
                line.getStationEdges().toSet()
        ));

        return lineId;
    }

    @Transactional
    public void insertStation(final StationInsertRequest stationInsertRequest) {
        final Subway subway = loadSubwayFromRepository();
        subway.insertStationToLine(
                stationInsertRequest.getLineId(),
                stationInsertRequest.getStationId(),
                stationInsertRequest.getAdjacentStationId(),
                LineDirection.valueOf(stationInsertRequest.getDirection()),
                stationInsertRequest.getDistance()
        );
        final Line insertedLine = subway.getLine(stationInsertRequest.getLineId());

        lineRepository.updateStationEdges(insertedLine);
    }

    private Subway loadSubwayFromRepository() {
        final List<Line> lines = lineRepository.findAll();
        final List<Station> stations = stationRepository.findAll();
        return Subway.of(lines, stations);
    }

    @Transactional
    public LineDto findLineById(final Long id) {
        final Subway subway = loadSubwayFromRepository();

        final Line line = subway.getLine(id);
        return getLineDto(subway, line);
    }

    private LineDto getLineDto(final Subway subway, final Line line) {
        final List<Long> stationIdsByOrder = line.getStationIdsByOrder();
        final List<Station> stationsByOrder = stationIdsByOrder.stream()
                .map(subway::getStation)
                .collect(Collectors.toUnmodifiableList());

        return LineDto.of(line, stationsByOrder);
    }

    @Transactional
    public void deleteStation(final Long lineId, final Long stationId) {
        final Subway subway = loadSubwayFromRepository();
        subway.removeStationFromLine(lineId, stationId);
        final Line line = subway.getLine(lineId);
        lineRepository.updateStationEdges(line);
        if (line.size() == 0) {
            lineRepository.deleteById(line.getId());
        }
    }

    @Transactional
    public List<LineDto> findAll() {
        final Subway subway = loadSubwayFromRepository();
        return subway.getLines().stream()
                .map(line -> getLineDto(subway, line))
                .collect(Collectors.toUnmodifiableList());
    }
}
