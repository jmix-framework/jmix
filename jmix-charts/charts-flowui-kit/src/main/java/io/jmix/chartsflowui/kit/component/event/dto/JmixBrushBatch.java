package io.jmix.chartsflowui.kit.component.event.dto;

import java.util.List;

public class JmixBrushBatch {

    private String brushId;

    private Integer brushIndex;

    private String brushName;

    private List<JmixBrushArea> areas;

    private List<JmixBrushSelected> selected;


    public String getBrushId() {
        return brushId;
    }

    public void setBrushId(String brushId) {
        this.brushId = brushId;
    }

    public Integer getBrushIndex() {
        return brushIndex;
    }

    public void setBrushIndex(Integer brushIndex) {
        this.brushIndex = brushIndex;
    }

    public String getBrushName() {
        return brushName;
    }

    public void setBrushName(String brushName) {
        this.brushName = brushName;
    }

    public List<JmixBrushArea> getAreas() {
        return areas;
    }

    public void setAreas(List<JmixBrushArea> areas) {
        this.areas = areas;
    }

    public List<JmixBrushSelected> getSelected() {
        return selected;
    }

    public void setSelected(List<JmixBrushSelected> selected) {
        this.selected = selected;
    }
}
