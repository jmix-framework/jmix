/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.kit.component.model;

public class Size extends PivotTableOptionsObservable {

    protected Double width;
    protected Double height;

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
     */
    public void setWidth(Double width) {
        this.width = width;
        markAsChanged();
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
     */
    public void setHeight(Double height) {
        this.height = height;
        markAsChanged();
    }
}
