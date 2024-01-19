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

package io.jmix.chartsflowui.kit.component.model.toolbox;

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.datazoom.AbstractDataZoom;
import io.jmix.chartsflowui.kit.component.model.shared.ItemStyle;

public class DataZoomFeature extends AbstractFeature<DataZoomFeature> {

    protected Title title;

    protected Icon icon;

    protected AbstractDataZoom.FilterMode filterMode;

    protected Integer[] xAxisIndexes;

    protected Integer[] yAxisIndexes;

    protected ItemStyle brushStyle;

    public static class Title extends ChartObservableObject {

        protected String zoom;

        protected String back;

        public String getZoom() {
            return zoom;
        }

        public void setZoom(String zoom) {
            this.zoom = zoom;
            markAsDirty();
        }

        public String getBack() {
            return back;
        }

        public void setBack(String back) {
            this.back = back;
            markAsDirty();
        }

        public Title withZoom(String zoom) {
            setZoom(zoom);
            return this;
        }

        public Title withBack(String back) {
            setBack(back);
            return this;
        }
    }

    public static class Icon extends ChartObservableObject {

        protected String zoom;

        protected String back;

        public String getZoom() {
            return zoom;
        }

        public void setZoom(String zoom) {
            this.zoom = zoom;
            markAsDirty();
        }

        public String getBack() {
            return back;
        }

        public void setBack(String back) {
            this.back = back;
            markAsDirty();
        }

        public Icon withZoom(String zoom) {
            setZoom(zoom);
            return this;
        }

        public Icon withBack(String back) {
            setBack(back);
            return this;
        }
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        if (this.title != null) {
            removeChild(this.title);
        }

        this.title = title;
        addChild(title);
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        if (this.icon != null) {
            removeChild(this.icon);
        }

        this.icon = icon;
        addChild(icon);
    }

    public AbstractDataZoom.FilterMode getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(AbstractDataZoom.FilterMode filterMode) {
        this.filterMode = filterMode;
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

    public ItemStyle getBrushStyle() {
        return brushStyle;
    }

    public void setBrushStyle(ItemStyle brushStyle) {
        if (this.brushStyle != null) {
            removeChild(this.brushStyle);
        }

        this.brushStyle = brushStyle;
        addChild(brushStyle);
    }

    public DataZoomFeature withTitle(Title title) {
        setTitle(title);
        return this;
    }

    public DataZoomFeature withIcon(Icon icon) {
        setIcon(icon);
        return this;
    }

    public DataZoomFeature withFilterMode(AbstractDataZoom.FilterMode filterMode) {
        setFilterMode(filterMode);
        return this;
    }

    public DataZoomFeature withXAxisIndexes(Integer... xAxisIndexes) {
        setXAxisIndexes(xAxisIndexes);
        return this;
    }

    public DataZoomFeature withYAxisIndexes(Integer... yAxisIndexes) {
        setYAxisIndexes(yAxisIndexes);
        return this;
    }

    public DataZoomFeature withBrushStyle(ItemStyle brushStyle) {
        setBrushStyle(brushStyle);
        return this;
    }

    @Override
    protected String getFeatureName() {
        return "dataZoom";
    }
}
