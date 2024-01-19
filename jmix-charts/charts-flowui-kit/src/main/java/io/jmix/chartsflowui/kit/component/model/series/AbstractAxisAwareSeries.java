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

package io.jmix.chartsflowui.kit.component.model.series;

import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import io.jmix.chartsflowui.kit.component.model.series.mark.MarkArea;
import io.jmix.chartsflowui.kit.component.model.series.mark.MarkLine;
import io.jmix.chartsflowui.kit.component.model.series.mark.MarkPoint;
import jakarta.annotation.Nullable;

public abstract class AbstractAxisAwareSeries<T extends AbstractAxisAwareSeries<T>> extends AbstractSeries<T> {

    protected AbstractAxisAwareSeries(SeriesType type) {
        super(type);
    }

    protected Encode encode;

    protected Boolean legendHoverLink;

    protected SeriesLayoutType seriesLayoutBy;

    protected Integer datasetIndex;

    protected MarkPoint markPoint;

    protected MarkLine markLine;

    protected MarkArea markArea;

    public enum SeriesLayoutType implements HasEnumId {
        COLUMN("column"),
        ROW("row");

        private final String id;

        SeriesLayoutType(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static SeriesLayoutType fromId(String id) {
            for (SeriesLayoutType at : SeriesLayoutType.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public Encode getEncode() {
        return encode;
    }

    public void setEncode(Encode encode) {
        this.encode = encode;
        markAsDirty();
    }

    public Boolean getLegendHoverLink() {
        return legendHoverLink;
    }

    public void setLegendHoverLink(Boolean legendHoverLink) {
        this.legendHoverLink = legendHoverLink;
        markAsDirty();
    }

    public SeriesLayoutType getSeriesLayoutBy() {
        return seriesLayoutBy;
    }

    public void setSeriesLayoutBy(SeriesLayoutType seriesLayoutBy) {
        this.seriesLayoutBy = seriesLayoutBy;
        markAsDirty();
    }

    public Integer getDatasetIndex() {
        return datasetIndex;
    }

    public void setDatasetIndex(Integer datasetIndex) {
        this.datasetIndex = datasetIndex;
        markAsDirty();
    }

    public MarkPoint getMarkPoint() {
        return markPoint;
    }

    public void setMarkPoint(MarkPoint markPoint) {
        this.markPoint = markPoint;
        markAsDirty();
    }

    public MarkLine getMarkLine() {
        return markLine;
    }

    public void setMarkLine(MarkLine markLine) {
        this.markLine = markLine;
        markAsDirty();
    }

    public MarkArea getMarkArea() {
        return markArea;
    }

    public void setMarkArea(MarkArea markArea) {
        this.markArea = markArea;
        markAsDirty();
    }

    @SuppressWarnings("unchecked")
    public T withEncode(Encode encode) {
        setEncode(encode);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withLegendHoverLink(Boolean legendHoverLink) {
        setLegendHoverLink(legendHoverLink);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withSeriesLayoutBy(SeriesLayoutType seriesLayoutBy) {
        setSeriesLayoutBy(seriesLayoutBy);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withDatasetIndex(Integer datasetIndex) {
        setDatasetIndex(datasetIndex);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMarkPoint(MarkPoint markPoint) {
        setMarkPoint(markPoint);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMarkLine(MarkLine markLine) {
        setMarkLine(markLine);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMarkArea(MarkArea markArea) {
        setMarkArea(markArea);
        return (T) this;
    }
}
