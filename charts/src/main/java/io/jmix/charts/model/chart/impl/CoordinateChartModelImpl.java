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

import io.jmix.charts.model.*;
import io.jmix.charts.model.animation.AnimationEffect;
import io.jmix.charts.model.axis.ValueAxis;
import io.jmix.charts.model.chart.ChartType;
import io.jmix.charts.model.chart.CoordinateChartModel;
import io.jmix.charts.model.graph.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * See documentation for properties of AmCoordinateChart JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmCoordinateChart">http://docs.amcharts.com/3/javascriptcharts/AmCoordinateChart</a>
 */
@SuppressWarnings("unchecked")
public abstract class CoordinateChartModelImpl<T extends CoordinateChartModelImpl> extends AbstractChart<T>
        implements CoordinateChartModel<T> {

    private static final long serialVersionUID = -8779874684644002376L;

    private List<Color> colors;

    private List<Graph> graphs;

    private Boolean gridAboveGraphs;

    private List<Guide> guides;

    private Boolean sequencedAnimation;

    private Double startAlpha;

    private Double startDuration;

    private AnimationEffect startEffect;

    private String urlTarget;

    private List<ValueAxis> valueAxes;

    public CoordinateChartModelImpl(ChartType type) {
        super(type);
    }

    @Override
    public List<Color> getColors() {
        return colors;
    }

    @Override
    public T setColors(List<Color> colors) {
        this.colors = colors;
        return (T) this;
    }

    @Override
    public T addColors(Color... colors) {
        if (colors != null) {
            if (this.colors == null) {
                this.colors = new ArrayList<>();
            }
            this.colors.addAll(Arrays.asList(colors));
        }
        return (T) this;
    }

    @Override
    public List<Graph> getGraphs() {
        return graphs;
    }

    @Override
    public T setGraphs(List<Graph> graphs) {
        this.graphs = graphs;
        return (T) this;
    }

    @Override
    public T addGraphs(Graph... graphs) {
        if (graphs != null) {
            if (this.graphs == null) {
                this.graphs = new ArrayList<>();
            }
            this.graphs.addAll(Arrays.asList(graphs));
        }
        return (T) this;
    }

    @Override
    public AnimationEffect getStartEffect() {
        return startEffect;
    }

    @Override
    public T setStartEffect(AnimationEffect startEffect) {
        this.startEffect = startEffect;
        return (T) this;
    }

    @Override
    public Double getStartDuration() {
        return startDuration;
    }

    @Override
    public T setStartDuration(Double startDuration) {
        this.startDuration = startDuration;
        return (T) this;
    }

    @Override
    public List<ValueAxis> getValueAxes() {
        return valueAxes;
    }

    @Override
    public T setValueAxes(List<ValueAxis> valueAxes) {
        this.valueAxes = valueAxes;
        return (T) this;
    }

    @Override
    public T addValueAxes(ValueAxis... valueAxes) {
        if (valueAxes != null) {
            if (this.valueAxes == null) {
                this.valueAxes = new ArrayList<>();
            }
            this.valueAxes.addAll(Arrays.asList(valueAxes));
        }
        return (T) this;
    }

    @Override
    public List<Guide> getGuides() {
        return guides;
    }

    @Override
    public T setGuides(List<Guide> guides) {
        this.guides = guides;
        return (T) this;
    }

    @Override
    public T addGuides(Guide... guides) {
        if (guides != null) {
            if (this.guides == null) {
                this.guides = new ArrayList<>();
            }
            this.guides.addAll(Arrays.asList(guides));
        }
        return (T) this;
    }

    @Override
    public Boolean getGridAboveGraphs() {
        return gridAboveGraphs;
    }

    @Override
    public T setGridAboveGraphs(Boolean gridAboveGraphs) {
        this.gridAboveGraphs = gridAboveGraphs;
        return (T) this;
    }

    @Override
    public Boolean getSequencedAnimation() {
        return sequencedAnimation;
    }

    @Override
    public T setSequencedAnimation(Boolean sequencedAnimation) {
        this.sequencedAnimation = sequencedAnimation;
        return (T) this;
    }

    @Override
    public Double getStartAlpha() {
        return startAlpha;
    }

    @Override
    public T setStartAlpha(Double startAlpha) {
        this.startAlpha = startAlpha;
        return (T) this;
    }

    @Override
    public String getUrlTarget() {
        return urlTarget;
    }

    @Override
    public T setUrlTarget(String urlTarget) {
        this.urlTarget = urlTarget;
        return (T) this;
    }

    @Override
    public List<String> getWiredFields() {
        List<String> wiredFields = new ArrayList<>(super.getWiredFields());

        if (graphs != null) {
            for (Graph g : graphs) {
                wiredFields.addAll(g.getWiredFields());
            }
        }

        return wiredFields;
    }
}