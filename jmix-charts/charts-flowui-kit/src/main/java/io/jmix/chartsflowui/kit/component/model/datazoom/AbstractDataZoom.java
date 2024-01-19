/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.kit.component.model.datazoom;

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import io.jmix.chartsflowui.kit.component.model.shared.Orientation;
import jakarta.annotation.Nullable;

public class AbstractDataZoom<T extends AbstractDataZoom<T>> extends ChartObservableObject {

    protected final DataZoomType type;

    protected String id;

    protected Integer[] xAxisIndexes;

    protected Integer[] yAxisIndexes;

    protected Integer[] radiusAxisIndexes;

    protected Integer[] angleAxisIndexes;

    protected FilterMode filterMode;

    protected Double start;

    protected Double end;

    protected String startValue;

    protected String endValue;

    protected Double minSpan;

    protected Double maxSpan;

    protected String minValueSpan;

    protected String maxValueSpan;

    protected Orientation orientation;

    protected Boolean zoomLock;

    protected Integer throttle;

    protected RangeMode[] rangeMode;

    protected AbstractDataZoom(DataZoomType type) {
        this.type = type;
    }

    public enum FilterMode implements HasEnumId {
        FILTER("filter"),
        WEAK_FILTER("weakFilter"),
        EMPTY("empty"),
        NONE("none");

        private final String id;

        FilterMode(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static FilterMode fromId(String id) {
            for (FilterMode at : FilterMode.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public enum RangeMode implements HasEnumId {
        VALUE("value"),
        PERCENT("percent");

        private final String id;

        RangeMode(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static RangeMode fromId(String id) {
            for (RangeMode at : RangeMode.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public DataZoomType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        markAsDirty();
    }

    public Integer[] getXAxisIndexes() {
        return xAxisIndexes;
    }

    public void setXAxisIndexes(Integer... xAxisIndexes) {
        this.xAxisIndexes = xAxisIndexes;
        markAsDirty();
    }

    public Integer[] getYAxisIndexes() {
        return yAxisIndexes;
    }

    public void setYAxisIndexes(Integer... yAxisIndexes) {
        this.yAxisIndexes = yAxisIndexes;
        markAsDirty();
    }

    public Integer[] getRadiusAxisIndexes() {
        return radiusAxisIndexes;
    }

    public void setRadiusAxisIndexes(Integer... radiusAxisIndexes) {
        this.radiusAxisIndexes = radiusAxisIndexes;
        markAsDirty();
    }

    public Integer[] getAngleAxisIndexes() {
        return angleAxisIndexes;
    }

    public void setAngleAxisIndexes(Integer... angleAxisIndexes) {
        this.angleAxisIndexes = angleAxisIndexes;
        markAsDirty();
    }

    public FilterMode getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
        markAsDirty();
    }

    public Double getStart() {
        return start;
    }

    public void setStart(Double start) {
        this.start = start;
        markAsDirty();
    }

    public Double getEnd() {
        return end;
    }

    public void setEnd(Double end) {
        this.end = end;
        markAsDirty();
    }

    public String getStartValue() {
        return startValue;
    }

    public void setStartValue(String startValue) {
        this.startValue = startValue;
        markAsDirty();
    }

    public String getEndValue() {
        return endValue;
    }

    public void setEndValue(String endValue) {
        this.endValue = endValue;
        markAsDirty();
    }

    public Double getMinSpan() {
        return minSpan;
    }

    public void setMinSpan(Double minSpan) {
        this.minSpan = minSpan;
        markAsDirty();
    }

    public Double getMaxSpan() {
        return maxSpan;
    }

    public void setMaxSpan(Double maxSpan) {
        this.maxSpan = maxSpan;
        markAsDirty();
    }

    public String getMinValueSpan() {
        return minValueSpan;
    }

    public void setMinValueSpan(String minValueSpan) {
        this.minValueSpan = minValueSpan;
        markAsDirty();
    }

    public String getMaxValueSpan() {
        return maxValueSpan;
    }

    public void setMaxValueSpan(String maxValueSpan) {
        this.maxValueSpan = maxValueSpan;
        markAsDirty();
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
        markAsDirty();
    }

    public Boolean getZoomLock() {
        return zoomLock;
    }

    public void setZoomLock(Boolean zoomLock) {
        this.zoomLock = zoomLock;
        markAsDirty();
    }

    public Integer getThrottle() {
        return throttle;
    }

    public void setThrottle(Integer throttle) {
        this.throttle = throttle;
        markAsDirty();
    }

    public RangeMode[] getRangeMode() {
        return rangeMode;
    }

    public void setRangeMode(RangeMode start, RangeMode end) {
        this.rangeMode = new RangeMode[]{start, end};
        markAsDirty();
    }

    @SuppressWarnings("unchecked")
    public T withId(String id) {
        setId(id);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withXAxisIndexes(Integer... xAxisIndexes) {
        setXAxisIndexes(xAxisIndexes);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withYAxisIndexes(Integer... yAxisIndexes) {
        setYAxisIndexes(yAxisIndexes);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withRadiusAxisIndexes(Integer... radiusAxisIndexes) {
        setRadiusAxisIndexes(radiusAxisIndexes);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAngleAxisIndexes(Integer... angleAxisIndexes) {
        setAngleAxisIndexes(angleAxisIndexes);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withFilterMode(FilterMode filterMode) {
        setFilterMode(filterMode);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withStart(Double start) {
        setStart(start);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withEnd(Double end) {
        setEnd(end);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withStartValue(String startValue) {
        setStartValue(startValue);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withEndValue(String endValue) {
        setEndValue(endValue);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMinSpan(Double minSpan) {
        setMinSpan(minSpan);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMaxSpan(Double maxSpan) {
        setMaxSpan(maxSpan);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMinValueSpan(String minValueSpan) {
        setMinValueSpan(minValueSpan);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMaxValueSpan(String maxValueSpan) {
        setMaxValueSpan(maxValueSpan);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withOrientation(Orientation orientation) {
        setOrientation(orientation);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withZoomLock(Boolean zoomLock) {
        setZoomLock(zoomLock);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withThrottle(Integer throttle) {
        setThrottle(throttle);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withRangeMode(RangeMode start, RangeMode end) {
        setRangeMode(start, end);
        return (T) this;
    }
}
