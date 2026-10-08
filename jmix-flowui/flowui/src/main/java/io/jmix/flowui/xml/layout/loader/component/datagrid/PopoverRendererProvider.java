/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component.datagrid;

import com.vaadin.flow.component.popover.PopoverPosition;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.grid.renderer.PopoverRenderer;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Loads {@link PopoverRenderer} instances from {@code popoverRenderer} XML elements.
 */
@NullMarked
@Component("flowui_PopoverRendererProvider")
public class PopoverRendererProvider implements RendererProvider<PopoverRenderer<?>> {

    public static final String NAME = "popoverRenderer";

    @Autowired
    protected LoaderSupport loaderSupport;
    @Autowired
    protected MetadataTools metadataTools;

    @Override
    public boolean supports(String rendererName) {
        return NAME.equals(rendererName);
    }

    @Override
    public PopoverRenderer<?> createRenderer(Element element,
                                             MetaPropertyPath metaPropertyPath,
                                             ComponentLoader.Context context) {
        return createRenderer(new MetaPropertyPathRendererCreationContext(element,
                context.getOrigin(), metaPropertyPath, context));
    }

    @Override
    public PopoverRenderer<?> createRenderer(RendererCreationContext context) {
        MetaPropertyPath metaPropertyPath = ((MetaPropertyPathRendererCreationContext) context).getMetaPropertyPath();

        PopoverRenderer<?> renderer = new PopoverRenderer<>(item ->
                metadataTools.format(EntityValues.getValueEx(item, metaPropertyPath)));

        Element element = context.getElement();
        loaderSupport.loadEnum(element, PopoverPosition.class, "position")
                .ifPresent(renderer::withPosition);
        loaderSupport.loadString(element, "classNames")
                .ifPresent(renderer::withClassNames);
        loaderSupport.loadString(element, "css")
                .ifPresent(renderer::withCss);

        return renderer;
    }
}
