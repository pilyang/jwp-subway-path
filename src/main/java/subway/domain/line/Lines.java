package subway.domain.line;

import subway.domain.line.edge.StationEdge;
import subway.domain.line.edge.StationEdges;
import subway.exception.DuplicatedLineNameException;
import subway.exception.StationEdgeNotFoundException;
import subway.exception.StationNotFoundException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Lines {

    private final Map<Long, Line> lines;

    public Lines() {
        this.lines = new HashMap<>();
    }

    public void add(final Line line) {
        validateDuplicatedName(line.getName());
        lines.put(line.getId(), line);
    }

    public void add(final Collection<Line> lines) {
        lines.forEach(existLine -> {
            validateDuplicatedName(existLine.getName());
            this.lines.put(existLine.getId(), existLine);
        });
    }

    private void validateDuplicatedName(final String name) {
        if (lines.values().stream().anyMatch(line -> line.getName().equals(name))) {
            throw new DuplicatedLineNameException(name);
        }
    }

    public Line get(final Long id) {
        validateIsExist(id);
        return lines.get(id);
    }

    private void validateIsExist(final Long id) {
        if (!lines.containsKey(id)) {
            throw new StationNotFoundException();
        }
    }

    public Long getLineIdBySection(final Long upStationId, final Long downStationId) {
        return toSet().stream()
                .filter(line -> line.contains(upStationId, downStationId))
                .mapToLong(Line::getId)
                .findFirst()
                .orElseThrow(StationEdgeNotFoundException::new);
    }

    public Set<StationEdge> getAllStationEdges() {
        return toSet().stream()
                .map(Line::getStationEdges)
                .map(StationEdges::toSet)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    public Set<Line> toSet() {
        return new HashSet<>(lines.values());
    }
}