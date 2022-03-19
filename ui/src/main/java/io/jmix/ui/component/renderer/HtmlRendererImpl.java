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

import com.vaadin.data.ValueProvider;
import com.vaadin.ui.renderers.HtmlRenderer;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.impl.AbstractDataGrid;
import io.jmix.ui.sanitizer.HtmlSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * A renderer for presenting HTML content.
 */
@Component(DataGrid.HtmlRenderer.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HtmlRendererImpl
        extends AbstractDataGrid.AbstractRenderer<Object, String>
        implements DataGrid.HtmlRenderer {

    protected HtmlSanitizer htmlSanitizer;

    public HtmlRendererImpl() {
        this("");
    }

    public HtmlRendererImpl(String nullRepresentation) {
        super(nullRepresentation);
    }

    @Autowired
    public void setHtmlSanitizer(HtmlSanitizer htmlSanitizer) {
        this.htmlSanitizer = htmlSanitizer;
    }

    @Override
    public HtmlRenderer getImplementation() {
        return (HtmlRenderer) super.getImplementation();
    }

    @Override
    protected HtmlRenderer createImplementation() {
        return new HtmlRenderer(getNullRepresentation());
    }

    @Override
    protected void copy(DataGrid.Renderer existingRenderer) {
        if (existingRenderer instanceof HtmlRendererImpl) {
            setNullRepresentation(((HtmlRendererImpl) existingRenderer).getNullRepresentation());
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

    @Nullable
    @Override
    public ValueProvider<String, String> getPresentationValueProvider() {
        return (ValueProvider<String, String>) html ->
                getDataGrid() != null && getDataGrid().isHtmlSanitizerEnabled()
                        ? htmlSanitizer.sanitize(html)
                        : html;
    }
}
