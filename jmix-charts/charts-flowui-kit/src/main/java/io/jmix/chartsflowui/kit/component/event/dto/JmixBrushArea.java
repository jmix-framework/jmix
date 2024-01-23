package io.jmix.chartsflowui.kit.component.event.dto;

import java.util.List;

public class JmixBrushArea {

    private List<Integer> range;

    private List<Integer> coordRange;

    private List<List<Number>> coordRanges;

    public List<Integer> getRange() {
        return range;
    }

    public void setRange(List<Integer> range) {
        this.range = range;
    }

    public List<Integer> getCoordRange() {
        return coordRange;
    }

    public void setCoordRange(List<Integer> coordRange) {
        this.coordRange = coordRange;
    }

    public List<List<Number>> getCoordRanges() {
        return coordRanges;
    }

    public void setCoordRanges(List<List<Number>> coordRanges) {
        this.coordRanges = coordRanges;
    }
}
