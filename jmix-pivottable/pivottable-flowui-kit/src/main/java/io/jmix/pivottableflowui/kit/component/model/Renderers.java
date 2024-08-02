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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration of renderers.
 */
public class Renderers extends PivotTableOptionsObservable {

    protected List<Renderer> renderers;
    protected Renderer selectedRenderer;

    /**
     * @return a list which will be converted to a dictionary of rendering functions
     */
    public List<Renderer> getRenderers() {
        return renderers;
    }

    /**
     * Sets a list which will be converted to a dictionary of rendering functions.
     *
     * @param renderers a list which will be converted to a dictionary of rendering functions
     */
    public void setRenderers(List<Renderer> renderers) {
        this.renderers = renderers;
        markAsChanged();
    }

    /**
     * Adds an array which will be converted to a dictionary of rendering functions.
     *
     * @param renderers an array which will be converted to a dictionary of rendering functions
     */
    public void addRenderers(Renderer... renderers) {
        if (renderers != null) {
            if (this.renderers == null) {
                this.renderers = new ArrayList<>();
            }
            this.renderers.addAll(Arrays.asList(renderers));
            markAsChanged();
        }
    }

    /**
     * @return a selected renderer
     */
    public Renderer getSelectedRenderer() {
        return selectedRenderer;
    }

    /**
     * Sets one of predefined renderers, which name will be
     * converted to {@code rendererName} - an renderer to
     * prepopulate in dropdown i.e. key to {@code renderers} object.
     *
     * @param selectedRenderer one of predefined renderers
     */
    public void setSelectedRenderer(Renderer selectedRenderer) {
        this.selectedRenderer = selectedRenderer;
    }
}
