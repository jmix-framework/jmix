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

public class C3RendererOptions extends PivotTableOptionsObservable {

    protected Size size;

    /**
     * @return size of chart renderer
     */
    public Size getSize() {
        return size;
    }

    /**
     * Sets size of chart renderer.
     *
     * @param size size of chart renderer
     */
    public void setSize(Size size) {
        this.size = size;
        markAsChanged();
    }
}
