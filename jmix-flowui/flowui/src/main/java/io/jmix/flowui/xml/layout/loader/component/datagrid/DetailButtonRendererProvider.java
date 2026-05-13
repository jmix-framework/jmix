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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.SerializableFunction;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.grid.renderer.DetailButtonRenderer;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.IconLoaderSupport;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Objects;
import java.util.Optional;

/**
 * Loads {@link DetailButtonRenderer} instances from {@code detailButtonRenderer} XML elements.
 */
@org.springframework.stereotype.Component("flowui_DetailButtonRendererProvider")
public class DetailButtonRendererProvider extends AbstractDetailRendererProvider<DetailButtonRenderer<Object>> {

    public static final String NAME = "detailButtonRenderer";

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected DialogWindows dialogWindows;

    @Override
    public boolean supports(String rendererName) {
        return NAME.equals(rendererName);
    }

    @Override
    public DetailButtonRenderer<Object> createRenderer(Element element,
                                                       MetaPropertyPath metaPropertyPath,
                                                       ComponentLoader.Context context) {
        return createRenderer(new MetaPropertyPathRendererCreationContext(element, null, metaPropertyPath, context));
    }

    @Override
    protected DetailButtonRenderer<Object> createRendererInternal(RendererCreationContext detailRendererContext) {
        DetailButtonRenderer<Object> renderer = new DetailButtonRenderer<>(uiComponents, viewNavigators, dialogWindows,
                getOwnerComponent(detailRendererContext),
                createTextProvider(detailRendererContext));

        loaderSupport.loadEnum(detailRendererContext.getElement(), OpenMode.class, "openMode")
                .ifPresent(renderer::withOpenMode);
        loaderSupport.loadString(detailRendererContext.getElement(), "themeNames")
                .ifPresent(renderer::withThemeNames);

        createIconProvider(detailRendererContext)
                .ifPresent(renderer::withIconProvider);

        return renderer;
    }

    @Override
    public boolean supports(RendererCreationContext context) {
        return NAME.equals(context.getElement().getName());
    }

    protected Optional<SerializableFunction<Object, Component>> createIconProvider(
            RendererCreationContext detailRendererContext) {
        if (detailRendererContext.getElement().element("icon") == null
                && loaderSupport.loadString(detailRendererContext.getElement(), "icon").isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(item -> {
            IconLoaderSupport iconLoaderSupport =
                    applicationContext.getBean(IconLoaderSupport.class, detailRendererContext.getLoaderContext());
            return iconLoaderSupport.loadIcon(detailRendererContext.getElement())
                    .orElse(null);
        });
    }

    @SuppressWarnings("unchecked")
    protected ListDataComponent<Object> getOwnerComponent(RendererCreationContext detailRendererContext) {
        return (ListDataComponent<Object>) Objects.requireNonNull(detailRendererContext.getOwnerComponent(),
                "Owner component is required for %s".formatted(DetailButtonRenderer.class.getSimpleName()));
    }
}
