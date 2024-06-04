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

package io.jmix.chartsflowui.kit.component.model.series.mark;

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.series.Label;
import io.jmix.chartsflowui.kit.component.model.shared.ItemStyle;

/**
 * Base class for mark elements.
 *
 * @param <T> origin element class type
 */
public abstract class AbstractMarkElement<T extends AbstractMarkElement<T>>
        extends ChartObservableObject {

    protected ItemStyle itemStyle;

    protected Label label;

    public ItemStyle getItemStyle() {
        return itemStyle;
    }

    public void setItemStyle(ItemStyle itemStyle) {
        if (this.itemStyle != null) {
            removeChild(this.itemStyle);
        }

        this.itemStyle = itemStyle;
        addChild(itemStyle);
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        if (this.label != null) {
            removeChild(this.label);
        }

        this.label = label;
        addChild(label);
    }

    @SuppressWarnings("unchecked")
    public T withItemStyle(ItemStyle itemStyle) {
        setItemStyle(itemStyle);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withLabel(Label label) {
        setLabel(label);
        return (T) this;
    }
}
