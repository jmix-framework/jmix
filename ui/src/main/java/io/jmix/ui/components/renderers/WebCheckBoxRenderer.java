/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.components.renderers;

import com.vaadin.ui.renderers.Renderer;
import io.jmix.core.Entity;
import io.jmix.ui.components.DataGrid;
import io.jmix.ui.components.impl.WebAbstractDataGrid;
import io.jmix.ui.widgets.renderers.JmixCheckBoxRenderer;

/**
 * A renderer that represents a boolean values as a graphical check box icons.
 */
public class WebCheckBoxRenderer extends WebAbstractDataGrid.AbstractRenderer<Entity, Boolean> implements DataGrid.CheckBoxRenderer {

    @Override
    protected Renderer<Boolean> createImplementation() {
        return new JmixCheckBoxRenderer();
    }
}
