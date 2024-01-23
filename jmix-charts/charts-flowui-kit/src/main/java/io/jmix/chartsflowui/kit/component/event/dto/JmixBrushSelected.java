package io.jmix.chartsflowui.kit.component.event.dto;

import java.util.List;

public class JmixBrushSelected {

    private Integer seriesIndex;

    private List<Integer> dataIndex;

    public Integer getSeriesIndex() {
        return seriesIndex;
    }

    public void setSeriesIndex(Integer seriesIndex) {
        this.seriesIndex = seriesIndex;
    }

    public List<Integer> getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(List<Integer> dataIndex) {
        this.dataIndex = dataIndex;
    }
}
