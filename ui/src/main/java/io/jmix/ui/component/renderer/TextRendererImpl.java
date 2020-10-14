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
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.impl.AbstractDataGrid;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * A renderer for presenting simple plain-text string values.
 */
@Component(DataGrid.TextRenderer.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TextRendererImpl
        extends AbstractDataGrid.AbstractRenderer<Object, Object>
        implements DataGrid.TextRenderer {

    public TextRendererImpl() {
        this("");
    }

    public TextRendererImpl(String nullRepresentation) {
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
    protected void copy(DataGrid.Renderer existingRenderer) {
        if (existingRenderer instanceof TextRendererImpl) {
            setNullRepresentation(((TextRendererImpl) existingRenderer).getNullRepresentation());
        }
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
