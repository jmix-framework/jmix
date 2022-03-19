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

package io.jmix.charts.component.impl;


import io.jmix.charts.component.FunnelChart;
import io.jmix.charts.model.FunnelValueRepresentation;
import io.jmix.charts.model.chart.impl.FunnelChartModelImpl;
import io.jmix.charts.model.label.LabelPosition;

public class FunnelChartImpl extends SlicedChartImpl<FunnelChart, FunnelChartModelImpl> implements FunnelChart {
    @Override
    protected FunnelChartModelImpl createChartConfiguration() {
        return new FunnelChartModelImpl();
    }

    @Override
    public String getBalloonText() {
        return getModel().getBalloonText();
    }

    @Override
    public FunnelChart setBalloonText(String balloonText) {
        getModel().setBalloonText(balloonText);
        return this;
    }

    @Override
    public String getBaseWidth() {
        return getModel().getBaseWidth();
    }

    @Override
    public FunnelChart setBaseWidth(String baseWidth) {
        getModel().setBaseWidth(baseWidth);
        return this;
    }

    @Override
    public LabelPosition getLabelPosition() {
        return getModel().getLabelPosition();
    }

    @Override
    public FunnelChart setLabelPosition(LabelPosition labelPosition) {
        getModel().setLabelPosition(labelPosition);
        return this;
    }

    @Override
    public String getLabelText() {
        return getModel().getLabelText();
    }

    @Override
    public FunnelChart setLabelText(String labelText) {
        getModel().setLabelText(labelText);
        return this;
    }

    @Override
    public String getNeckHeight() {
        return getModel().getNeckHeight();
    }

    @Override
    public FunnelChart setNeckHeight(String neckHeight) {
        getModel().setNeckHeight(neckHeight);
        return this;
    }

    @Override
    public String getNeckWidth() {
        return getModel().getNeckWidth();
    }

    @Override
    public FunnelChart setNeckWidth(String neckWidth) {
        getModel().setNeckWidth(neckWidth);
        return this;
    }

    @Override
    public String getPullDistance() {
        return getModel().getPullDistance();
    }

    @Override
    public FunnelChart setPullDistance(String pullDistance) {
        getModel().setPullDistance(pullDistance);
        return this;
    }

    @Override
    public Integer getStartX() {
        return getModel().getStartX();
    }

    @Override
    public FunnelChart setStartX(Integer startX) {
        getModel().setStartX(startX);
        return this;
    }

    @Override
    public Integer getStartY() {
        return getModel().getStartY();
    }

    @Override
    public FunnelChart setStartY(Integer startY) {
        getModel().setStartY(startY);
        return this;
    }

    @Override
    public FunnelValueRepresentation getValueRepresents() {
        return getModel().getValueRepresents();
    }

    @Override
    public FunnelChart setValueRepresents(FunnelValueRepresentation valueRepresents) {
        getModel().setValueRepresents(valueRepresents);
        return this;
    }

    @Override
    public Boolean getRotate() {
        return getModel().getRotate();
    }

    @Override
    public FunnelChart setRotate(Boolean rotate) {
        getModel().setRotate(rotate);
        return this;
    }

    @Override
    public Integer getAngle() {
        return getModel().getAngle();
    }

    @Override
    public FunnelChart setAngle(Integer angle) {
        getModel().setAngle(angle);
        return this;
    }

    @Override
    public Integer getDepth3D() {
        return getModel().getDepth3D();
    }

    @Override
    public FunnelChart setDepth3D(Integer depth3D) {
        getModel().setDepth3D(depth3D);
        return this;
    }
}