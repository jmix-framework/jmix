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

import com.vaadin.flow.component.html.AnchorTarget;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.renderer.DetailLinkRenderer;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("flowui_DetailLinkRendererProvider")
public class DetailLinkRendererProvider extends AbstractDetailRendererProvider<DetailLinkRenderer<?>> {

    public static final String NAME = "detailLinkRenderer";

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected RouteSupport routeSupport;
    @Autowired
    protected ViewRegistry viewRegistry;

    @Override
    public boolean supports(String rendererName) {
        return NAME.equals(rendererName);
    }

    @Override
    public DetailLinkRenderer<?> createRenderer(Element element,
                                                       MetaPropertyPath metaPropertyPath,
                                                       ComponentLoader.Context context) {
        return createRenderer(new RendererCreationContext(element, null, metaPropertyPath, context));
    }

    @Override
    protected DetailLinkRenderer<?> createRendererInternal(RendererCreationContext detailRendererContext) {
        MetaPropertyPath metaPropertyPath = detailRendererContext.getMetaPropertyPath();
        DetailLinkRenderer<?> renderer = new DetailLinkRenderer<>(uiComponents, routeSupport, viewRegistry,
                metaPropertyPath.getMetaClass(), createTextProvider(metaPropertyPath));

        loaderSupport.loadEnum(detailRendererContext.getElement(), AnchorTarget.class, "target")
                .ifPresent(renderer::withTarget);

        return renderer;
    }

    @Override
    public boolean supports(RendererCreationContext context) {
        return NAME.equals(context.getElement().getName());
    }
}
