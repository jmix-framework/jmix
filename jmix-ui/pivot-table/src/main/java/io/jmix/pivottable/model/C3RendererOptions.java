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

import io.jmix.ui.meta.StudioElement;

@StudioElement(
        caption = "C3",
        xmlElement = "c3",
        icon = "io/jmix/pivottable/icon/component.svg",
        xmlns = "http://jmix.io/schema/ui/pivot-table",
        xmlnsAlias = "pivot"
)
public class C3RendererOptions extends AbstractPivotObject {

    private static final long serialVersionUID = -5273273454206199279L;

    private Size size;

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
     * @return a reference to this object
     */
    @StudioElement
    public C3RendererOptions setSize(Size size) {
        this.size = size;
        return this;
    }
}
