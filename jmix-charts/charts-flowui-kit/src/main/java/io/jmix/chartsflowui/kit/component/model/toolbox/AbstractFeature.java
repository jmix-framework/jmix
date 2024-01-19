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

import io.jmix.chartsflowui.kit.component.model.shared.ItemStyle;

public abstract class AbstractFeature<T extends AbstractFeature<T>> extends ToolboxFeature {

    protected Boolean show;

    protected ItemStyle iconStyle;

    protected Emphasis emphasis;

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
        markAsDirty();
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

    @SuppressWarnings("unchecked")
    public T withShow(Boolean show) {
        setShow(show);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withIconStyle(ItemStyle iconStyle) {
        setIconStyle(iconStyle);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withEmphasis(Emphasis emphasis) {
        setEmphasis(emphasis);
        return (T) this;
    }
}
