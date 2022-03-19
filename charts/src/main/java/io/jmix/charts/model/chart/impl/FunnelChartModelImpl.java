/*
 * Copyright 2021 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.charts.model.chart.impl;


import io.jmix.charts.model.FunnelValueRepresentation;
import io.jmix.charts.model.chart.ChartType;
import io.jmix.charts.model.chart.FunnelChartModel;
import io.jmix.charts.model.label.LabelPosition;

/**
 * See documentation for properties of AmFunnelChart JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmFunnelChart">http://docs.amcharts.com/3/javascriptcharts/AmFunnelChart</a>
 */
public class FunnelChartModelImpl extends SlicedChartModelImpl<FunnelChartModelImpl> implements FunnelChartModel<FunnelChartModelImpl> {

    private static final long serialVersionUID = -8733320599720737456L;

    private Integer angle;

    private String balloonText;

    private String baseWidth;

    private Integer depth3D;

    private LabelPosition labelPosition;

    private String labelText;

    private String neckHeight;

    private String neckWidth;

    private String pullDistance;

    private Boolean rotate;

    private Integer startX;

    private Integer startY;

    private FunnelValueRepresentation valueRepresents;

    public FunnelChartModelImpl() {
        super(ChartType.FUNNEL);
    }

    @Override
    public String getBalloonText() {
        return balloonText;
    }

    @Override
    public FunnelChartModelImpl setBalloonText(String balloonText) {
        this.balloonText = balloonText;
        return this;
    }

    @Override
    public String getBaseWidth() {
        return baseWidth;
    }

    @Override
    public FunnelChartModelImpl setBaseWidth(String baseWidth) {
        this.baseWidth = baseWidth;
        return this;
    }

    @Override
    public LabelPosition getLabelPosition() {
        return labelPosition;
    }

    @Override
    public FunnelChartModelImpl setLabelPosition(LabelPosition labelPosition) {
        this.labelPosition = labelPosition;
        return this;
    }

    @Override
    public String getLabelText() {
        return labelText;
    }

    @Override
    public FunnelChartModelImpl setLabelText(String labelText) {
        this.labelText = labelText;
        return this;
    }

    @Override
    public String getNeckHeight() {
        return neckHeight;
    }

    @Override
    public FunnelChartModelImpl setNeckHeight(String neckHeight) {
        this.neckHeight = neckHeight;
        return this;
    }

    @Override
    public String getNeckWidth() {
        return neckWidth;
    }

    @Override
    public FunnelChartModelImpl setNeckWidth(String neckWidth) {
        this.neckWidth = neckWidth;
        return this;
    }

    @Override
    public String getPullDistance() {
        return pullDistance;
    }

    @Override
    public FunnelChartModelImpl setPullDistance(String pullDistance) {
        this.pullDistance = pullDistance;
        return this;
    }

    @Override
    public Integer getStartX() {
        return startX;
    }

    @Override
    public FunnelChartModelImpl setStartX(Integer startX) {
        this.startX = startX;
        return this;
    }

    @Override
    public Integer getStartY() {
        return startY;
    }

    @Override
    public FunnelChartModelImpl setStartY(Integer startY) {
        this.startY = startY;
        return this;
    }

    @Override
    public FunnelValueRepresentation getValueRepresents() {
        return valueRepresents;
    }

    @Override
    public FunnelChartModelImpl setValueRepresents(FunnelValueRepresentation valueRepresents) {
        this.valueRepresents = valueRepresents;
        return this;
    }

    @Override
    public Boolean getRotate() {
        return rotate;
    }

    @Override
    public FunnelChartModelImpl setRotate(Boolean rotate) {
        this.rotate = rotate;
        return this;
    }

    @Override
    public Integer getAngle() {
        return angle;
    }

    @Override
    public FunnelChartModelImpl setAngle(Integer angle) {
        this.angle = angle;
        return this;
    }

    @Override
    public Integer getDepth3D() {
        return depth3D;
    }

    @Override
    public FunnelChartModelImpl setDepth3D(Integer depth3D) {
        this.depth3D = depth3D;
        return this;
    }
}