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

package io.jmix.pivottable.model;

import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

/**
 * An object that describes the size.
 */
@StudioElement(
        caption = "Size",
        xmlElement = "size",
        icon = "io/jmix/pivottable/icon/component.svg",
        xmlns = "http://jmix.io/schema/ui/pivot-table",
        xmlnsAlias = "pivot"
)
public class Size extends AbstractPivotObject {

    private static final long serialVersionUID = -7892174283356404245L;

    private Double width;

    private Double height;

    /**
     * @return the width value
     */
    public Double getWidth() {
        return width;
    }

    /**
     * Sets the width value.
     *
     * @param width the width value
     * @return a reference to this object
     */
    @StudioProperty(type = PropertyType.DOUBLE)
    public Size setWidth(Double width) {
        this.width = width;
        return this;
    }

    /**
     * @return the height value
     */
    public Double getHeight() {
        return height;
    }

    /**
     * Sets the height value.
     *
     * @param height the height value
     * @return a reference to this object
     */
    @StudioProperty(type = PropertyType.DOUBLE)
    public Size setHeight(Double height) {
        this.height = height;
        return this;
    }
}
