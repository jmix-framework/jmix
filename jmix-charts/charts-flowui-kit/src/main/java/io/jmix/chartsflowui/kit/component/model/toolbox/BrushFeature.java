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
import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import jakarta.annotation.Nullable;

public class BrushFeature extends ToolboxFeature {

    protected BrushType[] types;

    protected Icon icon;

    protected Title title;

    public enum BrushType implements HasEnumId {
        RECT("rect"),
        POLYGON("polygon"),
        LINE_X("lineX"),
        LINE_Y("lineY"),
        KEEP("keep"),
        CLEAR("clear");

        private final String id;

        BrushType(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static BrushType fromId(String id) {
            for (BrushType at : BrushType.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public static class Icon extends ChartObservableObject {

        protected String rect;

        protected String polygon;

        protected String lineX;

        protected String lineY;

        protected String keep;

        protected String clear;

        public String getRect() {
            return rect;
        }

        public void setRect(String rect) {
            this.rect = rect;
            markAsDirty();
        }

        public String getPolygon() {
            return polygon;
        }

        public void setPolygon(String polygon) {
            this.polygon = polygon;
            markAsDirty();
        }

        public String getLineX() {
            return lineX;
        }

        public void setLineX(String lineX) {
            this.lineX = lineX;
            markAsDirty();
        }

        public String getLineY() {
            return lineY;
        }

        public void setLineY(String lineY) {
            this.lineY = lineY;
            markAsDirty();
        }

        public String getKeep() {
            return keep;
        }

        public void setKeep(String keep) {
            this.keep = keep;
            markAsDirty();
        }

        public String getClear() {
            return clear;
        }

        public void setClear(String clear) {
            this.clear = clear;
            markAsDirty();
        }

        public Icon withRect(String rect) {
            setRect(rect);
            return this;
        }

        public Icon withPolygon(String polygon) {
            setPolygon(polygon);
            return this;
        }

        public Icon withLineX(String lineX) {
            setLineX(lineX);
            return this;
        }

        public Icon withLineY(String lineY) {
            setLineY(lineY);
            return this;
        }

        public Icon withKeep(String keep) {
            setKeep(keep);
            return this;
        }

        public Icon withClear(String clear) {
            setClear(clear);
            return this;
        }
    }

    public static class Title extends ChartObservableObject {

        protected String rect;

        protected String polygon;

        protected String lineX;

        protected String lineY;

        protected String keep;

        protected String clear;

        public String getRect() {
            return rect;
        }

        public void setRect(String rect) {
            this.rect = rect;
            markAsDirty();
        }

        public String getPolygon() {
            return polygon;
        }

        public void setPolygon(String polygon) {
            this.polygon = polygon;
            markAsDirty();
        }

        public String getLineX() {
            return lineX;
        }

        public void setLineX(String lineX) {
            this.lineX = lineX;
            markAsDirty();
        }

        public String getLineY() {
            return lineY;
        }

        public void setLineY(String lineY) {
            this.lineY = lineY;
            markAsDirty();
        }

        public String getKeep() {
            return keep;
        }

        public void setKeep(String keep) {
            this.keep = keep;
            markAsDirty();
        }

        public String getClear() {
            return clear;
        }

        public void setClear(String clear) {
            this.clear = clear;
            markAsDirty();
        }

        public Title withRect(String rect) {
            setRect(rect);
            return this;
        }

        public Title withPolygon(String polygon) {
            setPolygon(polygon);
            return this;
        }

        public Title withLineX(String lineX) {
            setLineX(lineX);
            return this;
        }

        public Title withLineY(String lineY) {
            setLineY(lineY);
            return this;
        }

        public Title withKeep(String keep) {
            setKeep(keep);
            return this;
        }

        public Title withClear(String clear) {
            setClear(clear);
            return this;
        }
    }

    public BrushType[] getTypes() {
        return types;
    }

    public void setTypes(BrushType... types) {
        this.types = types;
        markAsDirty();
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

    public BrushFeature withTypes(BrushType... types) {
        setTypes(types);
        return this;
    }

    public BrushFeature withIcon(Icon icon) {
        setIcon(icon);
        return this;
    }

    public BrushFeature withTitle(Title title) {
        setTitle(title);
        return this;
    }

    @Override
    protected String getFeatureName() {
        return "brush";
    }
}
