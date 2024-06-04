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

import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import io.jmix.chartsflowui.kit.component.model.shared.Color;
import jakarta.annotation.Nullable;

/**
 * A tool feature for exporting a chart to an image.
 * More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#toolbox.feature.saveAsImage">SaveAsImage documentation</a>
 */
public class SaveAsImageFeature extends AbstractFeature<SaveAsImageFeature> {

    protected SaveType type;

    protected String name;

    protected Color backgroundColor;

    protected Color connectedBackgroundColor;

    protected String[] excludeComponents;

    protected String title;

    protected String icon;

    protected Integer pixelRatio;

    /**
     * Type of the exported image.
     */
    public enum SaveType implements HasEnumId {
        PNG("png"),
        JPG("jpg"),
        SVG("svg");

        private final String id;

        SaveType(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static SaveType fromId(String id) {
            for (SaveType at : SaveType.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public SaveType getType() {
        return type;
    }

    public void setType(SaveType type) {
        this.type = type;
        markAsDirty();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        markAsDirty();
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        markAsDirty();
    }

    public Color getConnectedBackgroundColor() {
        return connectedBackgroundColor;
    }

    public void setConnectedBackgroundColor(Color connectedBackgroundColor) {
        this.connectedBackgroundColor = connectedBackgroundColor;
        markAsDirty();
    }

    public String[] getExcludeComponents() {
        return excludeComponents;
    }

    public void setExcludeComponents(String... excludeComponents) {
        this.excludeComponents = excludeComponents;
        markAsDirty();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        markAsDirty();
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
        markAsDirty();
    }

    public Integer getPixelRatio() {
        return pixelRatio;
    }

    public void setPixelRatio(Integer pixelRatio) {
        this.pixelRatio = pixelRatio;
        markAsDirty();
    }

    public SaveAsImageFeature withType(SaveType type) {
        setType(type);
        return this;
    }

    public SaveAsImageFeature withName(String name) {
        setName(name);
        return this;
    }

    public SaveAsImageFeature withBackgroundColor(Color backgroundColor) {
        setBackgroundColor(backgroundColor);
        return this;
    }

    public SaveAsImageFeature withConnectedBackgroundColor(Color connectedBackgroundColor) {
        setConnectedBackgroundColor(connectedBackgroundColor);
        return this;
    }

    public SaveAsImageFeature withExcludeComponents(String... excludeComponents) {
        setExcludeComponents(excludeComponents);
        return this;
    }

    public SaveAsImageFeature withTitle(String title) {
        setTitle(title);
        return this;
    }

    public SaveAsImageFeature withIcon(String icon) {
        setIcon(icon);
        return this;
    }

    public SaveAsImageFeature withPixelRatio(Integer pixelRatio) {
        setPixelRatio(pixelRatio);
        return this;
    }

    @Override
    protected String getFeatureName() {
        return "saveAsImage";
    }
}
