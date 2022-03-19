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
import com.vaadin.server.Resource;
import com.vaadin.ui.renderers.Renderer;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.impl.AbstractDataGrid;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.widget.renderer.JmixIconRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component(DataGrid.IconRenderer.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class IconRendererImpl<T>
        extends AbstractDataGrid.AbstractRenderer<T, Resource>
        implements DataGrid.IconRenderer<T> {

    protected Icons icons;
    protected IconResolver iconResolver;

    public IconRendererImpl() {
    }

    @Autowired
    public void setIcons(Icons icons) {
        this.icons = icons;
    }

    @Autowired
    public void setIconResolver(IconResolver iconResolver) {
        this.iconResolver = iconResolver;
    }

    @Override
    protected Renderer<Resource> createImplementation() {
        return new JmixIconRenderer();
    }

    @Override
    protected void copy(DataGrid.Renderer existingRenderer) {
        if (existingRenderer instanceof IconRendererImpl) {
            setNullRepresentation(((IconRendererImpl<?>) existingRenderer).getNullRepresentation());
        }
    }

    @Nullable
    @Override
    public ValueProvider<Icons.Icon, Resource> getPresentationValueProvider() {
        return (ValueProvider<Icons.Icon, Resource>) icon -> {
            String iconName = icons.get(icon);
            return iconResolver.getIconResource(iconName);
        };
    }
}
