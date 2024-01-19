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
import io.jmix.chartsflowui.kit.component.model.HasPosition;
import io.jmix.chartsflowui.kit.component.model.Tooltip;
import io.jmix.chartsflowui.kit.component.model.shared.ItemStyle;
import io.jmix.chartsflowui.kit.component.model.shared.Orientation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Toolbox extends ChartObservableObject
        implements HasPosition<Toolbox> {

    protected String id;

    protected Boolean show;

    protected Orientation orientation;

    protected Integer itemSize;

    protected Integer itemGap;

    protected Boolean showTitle;

    protected Map<String, ToolboxFeature> features;

    protected ItemStyle iconStyle;

    protected Emphasis emphasis;

    protected Integer zLevel;

    protected Integer z;

    protected String left;

    protected String top;

    protected String right;

    protected String bottom;

    protected String width;

    protected String height;

    protected Tooltip tooltip;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        markAsDirty();
    }

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
        markAsDirty();
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
        markAsDirty();
    }

    public Integer getItemSize() {
        return itemSize;
    }

    public void setItemSize(Integer itemSize) {
        this.itemSize = itemSize;
        markAsDirty();
    }

    public Integer getItemGap() {
        return itemGap;
    }

    public void setItemGap(Integer itemGap) {
        this.itemGap = itemGap;
        markAsDirty();
    }

    public Boolean getShowTitle() {
        return showTitle;
    }

    public void setShowTitle(Boolean showTitle) {
        this.showTitle = showTitle;
        markAsDirty();
    }

    public Map<String, ToolboxFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<ToolboxFeature> features) {
        if (this.features != null) {
            this.features.values().forEach(this::removeChild);
        }

        if (features == null) {
            this.features = null;
        } else {
            this.features = features
                    .stream()
                    .collect(Collectors.toMap(ToolboxFeature::getFeatureName, Function.identity()));
            features.forEach(this::addChild);
        }
    }

    public void setFeatures(ToolboxFeature... features) {
        setFeatures(features == null ? null : List.of(features));
    }

    public void removeFeature(ToolboxFeature feature) {
        if (features != null && features.containsValue(feature) && features.remove(feature.getFeatureName()) != null) {
            removeChild(feature);
        }
    }

    public void addFeature(ToolboxFeature feature) {
        if (features == null) {
            features = new HashMap<>();
        }

        if (features.containsValue(feature)) {
            return;
        }

        if (feature != null) {
            features.put(feature.getFeatureName(), feature);
            addChild(feature);
        }
    }

    public ItemStyle getIconStyle() {
        return iconStyle;
    }

    public void setIconStyle(ItemStyle iconStyle) {
        if (this.iconStyle != null) {
            removeChild(this.iconStyle);
        }

        this.iconStyle = iconStyle;
        addChild(iconStyle);
    }

    public Emphasis getEmphasis() {
        return emphasis;
    }

    public void setEmphasis(Emphasis emphasis) {
        if (this.emphasis != null) {
            removeChild(this.emphasis);
        }

        this.emphasis = emphasis;
        addChild(emphasis);
    }

    public Integer getZLevel() {
        return zLevel;
    }

    public void setZLevel(Integer zLevel) {
        this.zLevel = zLevel;
        markAsDirty();
    }

    public Integer getZ() {
        return z;
    }

    public void setZ(Integer z) {
        this.z = z;
        markAsDirty();
    }

    @Override
    public String getLeft() {
        return left;
    }

    @Override
    public void setLeft(String left) {
        this.left = left;
        markAsDirty();
    }

    @Override
    public String getTop() {
        return top;
    }

    @Override
    public void setTop(String top) {
        this.top = top;
        markAsDirty();
    }

    @Override
    public String getRight() {
        return right;
    }

    @Override
    public void setRight(String right) {
        this.right = right;
        markAsDirty();
    }

    @Override
    public String getBottom() {
        return bottom;
    }

    @Override
    public void setBottom(String bottom) {
        this.bottom = bottom;
        markAsDirty();
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
        markAsDirty();
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
        markAsDirty();
    }

    public Tooltip getTooltip() {
        return tooltip;
    }

    public void setTooltip(Tooltip tooltip) {
        if (this.tooltip != null) {
            removeChild(this.tooltip);
        }

        this.tooltip = tooltip;
        addChild(tooltip);
    }

    public Toolbox withId(String id) {
        setId(id);
        return this;
    }

    public Toolbox withShow(Boolean show) {
        setShow(show);
        return this;
    }

    public Toolbox withOrientation(Orientation orientation) {
        setOrientation(orientation);
        return this;
    }

    public Toolbox withItemSize(Integer itemSize) {
        setItemSize(itemSize);
        return this;
    }

    public Toolbox withItemGap(Integer itemGap) {
        setItemGap(itemGap);
        return this;
    }

    public Toolbox withShowTitle(Boolean showTitle) {
        setShowTitle(showTitle);
        return this;
    }

    public Toolbox withFeatures(ToolboxFeature... features) {
        setFeatures(features);
        return this;
    }

    public Toolbox withFeature(ToolboxFeature feature) {
        addFeature(feature);
        return this;
    }

    public Toolbox withIconStyle(ItemStyle iconStyle) {
        setIconStyle(iconStyle);
        return this;
    }

    public Toolbox withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return this;
    }

    public Toolbox withZLevel(Integer zLevel) {
        setZLevel(zLevel);
        return this;
    }

    public Toolbox withZ(Integer z) {
        setZ(z);
        return this;
    }

    public Toolbox withWidth(String width) {
        setWidth(width);
        return this;
    }

    public Toolbox withHeight(String height) {
        setHeight(height);
        return this;
    }

    public Toolbox withTooltip(Tooltip tooltip) {
        setTooltip(tooltip);
        return this;
    }
}
