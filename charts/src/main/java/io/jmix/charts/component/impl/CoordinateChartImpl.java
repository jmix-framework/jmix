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

import com.google.common.base.Strings;
import io.jmix.charts.model.chart.impl.CoordinateChartModelImpl;
import io.jmix.core.common.event.Subscription;
import io.jmix.charts.component.CoordinateChart;
import io.jmix.charts.model.*;
import io.jmix.charts.model.animation.AnimationEffect;
import io.jmix.charts.model.axis.ValueAxis;
import io.jmix.charts.model.graph.Graph;
import io.jmix.charts.widget.amcharts.events.axis.AxisZoomListener;
import io.jmix.charts.widget.amcharts.events.graph.listener.GraphClickListener;
import io.jmix.charts.widget.amcharts.events.graph.listener.GraphItemClickListener;
import io.jmix.charts.widget.amcharts.events.graph.listener.GraphItemRightClickListener;
import io.jmix.charts.widget.amcharts.events.roll.listener.RollOutGraphItemListener;
import io.jmix.charts.widget.amcharts.events.roll.listener.RollOutGraphListener;
import io.jmix.charts.widget.amcharts.events.roll.listener.RollOverGraphItemListener;
import io.jmix.charts.widget.amcharts.events.roll.listener.RollOverGraphListener;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public abstract class CoordinateChartImpl<T extends CoordinateChart,
        M extends CoordinateChartModelImpl> extends ChartImpl<T, M> implements CoordinateChart<T> {

    protected AxisZoomListener axisZoomHandler;

    protected GraphClickListener graphClickHandler;

    protected GraphItemClickListener graphItemClickHandler;

    protected GraphItemRightClickListener graphItemRightClickHandler;

    protected RollOutGraphListener rollOutGraphListener;
    protected RollOutGraphItemListener rollOutGraphItemListener;
    protected RollOverGraphListener rollOverGraphListener;
    protected RollOverGraphItemListener rollOverGraphItemListener;

    @Override
    public List<Color> getColors() {
        return getModel().getColors();
    }

    @Override
    public T setColors(List list) {
        getModel().setColors(list);
        return (T) this;
    }

    @Override
    public T addColors(Color... colors) {
        getModel().addColors(colors);
        return (T) this;
    }

    @Override
    public List<Graph> getGraphs() {
        return getModel().getGraphs();
    }

    @Override
    public T setGraphs(List list) {
        getModel().setGraphs(list);
        return (T) this;
    }

    @Override
    public T addGraphs(Graph... graphs) {
        getModel().addGraphs(graphs);
        return (T) this;
    }

    @Override
    public AnimationEffect getStartEffect() {
        return getModel().getStartEffect();
    }

    @Override
    public T setStartEffect(AnimationEffect startEffect) {
        getModel().setStartEffect(startEffect);
        return (T) this;
    }

    @Override
    public Double getStartDuration() {
        return getModel().getStartDuration();
    }

    @Override
    public T setStartDuration(Double startDuration) {
        getModel().setStartDuration(startDuration);
        return (T) this;
    }

    @Override
    public List<ValueAxis> getValueAxes() {
        return getModel().getValueAxes();
    }

    @Override
    public T setValueAxes(List valueAxes) {
        getModel().setValueAxes(valueAxes);
        return (T) this;
    }

    @Override
    public T addValueAxes(ValueAxis... valueAxes) {
        getModel().addValueAxes(valueAxes);
        return (T) this;
    }

    @Override
    public List<Guide> getGuides() {
        return getModel().getGuides();
    }

    @Override
    public T setGuides(List list) {
        getModel().setGuides(list);
        return (T) this;
    }

    @Override
    public T addGuides(Guide... guides) {
        getModel().addGuides(guides);
        return (T) this;
    }

    @Override
    public Boolean getGridAboveGraphs() {
        return getModel().getGridAboveGraphs();
    }

    @Override
    public T setGridAboveGraphs(Boolean gridAboveGraphs) {
        getModel().setGridAboveGraphs(gridAboveGraphs);
        return (T) this;
    }

    @Override
    public Boolean getSequencedAnimation() {
        return getModel().getSequencedAnimation();
    }

    @Override
    public T setSequencedAnimation(Boolean sequencedAnimation) {
        getModel().setSequencedAnimation(sequencedAnimation);
        return (T) this;
    }

    @Override
    public Double getStartAlpha() {
        return getModel().getStartAlpha();
    }

    @Override
    public T setStartAlpha(Double startAlpha) {
        getModel().setStartAlpha(startAlpha);
        return (T) this;
    }

    @Override
    public String getUrlTarget() {
        return getModel().getUrlTarget();
    }

    @Override
    public T setUrlTarget(String urlTarget) {
        getModel().setUrlTarget(urlTarget);
        return (T) this;
    }

    @Override
    public void zoomOutValueAxes() {
        component.zoomOutValueAxes();
    }

    @Override
    public void zoomOutValueAxis(String id) {
        component.zoomOutValueAxis(id);
    }

    @Override
    public void zoomOutValueAxis(int index) {
        component.zoomOutValueAxis(index);
    }

    @Override
    public void zoomValueAxisToValues(String id, Object startValue, Object endValue) {
        component.zoomValueAxisToValues(id, startValue, endValue);
    }

    @Override
    public void zoomValueAxisToValues(int index, Object startValue, Object endValue) {
        component.zoomValueAxisToValues(index, startValue, endValue);
    }

    @Override
    public Subscription addAxisZoomListener(Consumer<AxisZoomEvent> listener) {
        if (axisZoomHandler == null) {
            axisZoomHandler = this::onAxisZoomListener;
            component.addAxisZoomListener(axisZoomHandler);
        }
        return getEventHub().subscribe(AxisZoomEvent.class, listener);
    }

    protected void onAxisZoomListener(io.jmix.charts.widget.amcharts.events.axis.AxisZoomEvent e) {
        publish(AxisZoomEvent.class, new AxisZoomEvent(e.getAxisId(), e.getStartValue(), e.getEndValue()));
    }

    @Override
    public Subscription addGraphClickListener(Consumer<GraphClickEvent> listener) {
        if (graphClickHandler == null) {
            graphClickHandler = this::onGraphClick;
            component.addGraphClickListener(graphClickHandler);
        }

        return getEventHub().subscribe(GraphClickEvent.class, listener);
    }

    protected void onGraphClick(io.jmix.charts.widget.amcharts.events.graph.GraphClickEvent e) {
        publish(GraphClickEvent.class,
                new GraphClickEvent(this, e.getGraphId(), e.getX(), e.getY(),
                        e.getAbsoluteX(), e.getAbsoluteY()));
    }

    @Override
    public Subscription addGraphItemClickListener(Consumer<GraphItemClickEvent> listener) {
        if (graphItemClickHandler == null) {
            graphItemClickHandler = this::onGraphItemClick;
            component.addGraphItemClickListener(graphItemClickHandler);
        }

        return getEventHub().subscribe(GraphItemClickEvent.class, listener);
    }

    protected void onGraphItemClick(io.jmix.charts.widget.amcharts.events.graph.GraphItemClickEvent e) {
        publish(GraphItemClickEvent.class,
                new GraphItemClickEvent(this, getGraphById(e.getGraphId()), e.getGraphId(), e.getDataItem(),
                        e.getItemIndex(), e.getX(), e.getY(), e.getAbsoluteX(), e.getAbsoluteY()));
    }


    @Override
    public Subscription addGraphItemRightClickListener(Consumer<GraphItemRightClickEvent> listener) {
        if (graphItemRightClickHandler == null) {
            graphItemRightClickHandler = this::onGraphItemRightClick;
            component.addGraphItemRightClickListener(graphItemRightClickHandler);
        }

        return getEventHub().subscribe(GraphItemRightClickEvent.class, listener);
    }

    protected void onGraphItemRightClick(io.jmix.charts.widget.amcharts.events.graph.GraphItemRightClickEvent e) {
        publish(GraphItemRightClickEvent.class,
                new GraphItemRightClickEvent(this, getGraphById(e.getGraphId()), e.getGraphId(), e.getDataItem(),
                        e.getItemIndex(), e.getX(), e.getY(), e.getAbsoluteX(), e.getAbsoluteY()));
    }

    @Override
    public Subscription addRollOutGraphListener(Consumer<RollOutGraphEvent> listener) {
        if (rollOutGraphListener == null) {
            rollOutGraphListener = this::onRollOutGraphClick;
            component.addRollOutGraphListener(rollOutGraphListener);
        }

        return getEventHub().subscribe(RollOutGraphEvent.class, listener);
    }

    protected void onRollOutGraphClick(io.jmix.charts.widget.amcharts.events.roll.RollOutGraphEvent e) {
        publish(RollOutGraphEvent.class,
                new RollOutGraphEvent(this, getGraphById(e.getGraphId())));
    }

    @Override
    public Subscription addRollOutGraphItemListener(Consumer<RollOutGraphItemEvent> listener) {
        if (rollOutGraphItemListener == null) {
            rollOutGraphItemListener = this::onRollOutGraphItemClick;
            component.addRollOutGraphItemListener(rollOutGraphItemListener);
        }

        return getEventHub().subscribe(RollOutGraphItemEvent.class, listener);
    }

    protected void onRollOutGraphItemClick(io.jmix.charts.widget.amcharts.events.roll.RollOutGraphItemEvent e) {
        publish(RollOutGraphItemEvent.class,
                new RollOutGraphItemEvent(this, getGraphById(e.getGraphId()), e.getDataItem(), e.getItemIndex()));
    }

    @Override
    public Subscription addRollOverGraphListener(Consumer<RollOverGraphEvent> listener) {
        if (rollOverGraphListener == null) {
            rollOverGraphListener = this::onRollOverGraphClick;
            component.addRollOverGraphListener(rollOverGraphListener);
        }

        return getEventHub().subscribe(RollOverGraphEvent.class, listener);
    }

    protected void onRollOverGraphClick(io.jmix.charts.widget.amcharts.events.roll.RollOverGraphEvent e) {
        publish(RollOverGraphEvent.class,
                new RollOverGraphEvent(this, getGraphById(e.getGraphId())));
    }

    @Override
    public Subscription addRollOverGraphItemListener(Consumer<RollOverGraphItemEvent> listener) {
        if (rollOverGraphItemListener == null) {
            rollOverGraphItemListener = this::onRollOverGraphItemClick;
            component.addRollOverGraphItemListener(rollOverGraphItemListener);
        }

        return getEventHub().subscribe(RollOverGraphItemEvent.class, listener);
    }

    protected void onRollOverGraphItemClick(io.jmix.charts.widget.amcharts.events.roll.RollOverGraphItemEvent e) {
        publish(RollOverGraphItemEvent.class,
                new RollOverGraphItemEvent(this, getGraphById(e.getGraphId()), e.getDataItem(), e.getItemIndex()));
    }

    protected Graph getGraphById(String id) {
        if (Strings.isNullOrEmpty(id)
                || (getGraphs() == null || getGraphs().isEmpty())) {
            return null;
        }

        for (Graph graph : getGraphs()) {
            if (id.equals(graph.getId())) {
                return graph;
            }
        }
        return null;
    }
}