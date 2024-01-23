package io.jmix.chartsflowui.kit.component.event.dto;

import java.util.List;

public class JmixChartSelectedItem {

    private Integer geoIndex;

    private List<String> name;

    public Integer getGeoIndex() {
        return geoIndex;
    }

    public void setGeoIndex(Integer geoIndex) {
        this.geoIndex = geoIndex;
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }
}
