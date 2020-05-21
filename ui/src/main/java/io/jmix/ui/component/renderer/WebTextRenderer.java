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

package io.jmix.ui.component.renderer;

import com.vaadin.ui.renderers.TextRenderer;
import io.jmix.core.Entity;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.impl.WebAbstractDataGrid;

/**
 * A renderer for presenting simple plain-text string values.
 */
public class WebTextRenderer extends WebAbstractDataGrid.AbstractRenderer<Entity, Object> implements DataGrid.TextRenderer {

    public WebTextRenderer() {
        this("");
    }

    public WebTextRenderer(String nullRepresentation) {
        super(nullRepresentation);
    }

    @Override
    public TextRenderer getImplementation() {
        return (TextRenderer) super.getImplementation();
    }

    @Override
    protected TextRenderer createImplementation() {
        return new TextRenderer(getNullRepresentation());
    }

    @Override
    public String getNullRepresentation() {
        return super.getNullRepresentation();
    }

    @Override
    public void setNullRepresentation(String nullRepresentation) {
        super.setNullRepresentation(nullRepresentation);
    }
}
