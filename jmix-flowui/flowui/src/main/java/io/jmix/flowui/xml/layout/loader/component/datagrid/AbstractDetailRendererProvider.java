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

import com.vaadin.flow.function.ValueProvider;
import io.jmix.core.ClassManager;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.component.grid.renderer.AbstractDetailRenderer;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * Base provider for loading detail view renderers from data grid XML declarations.
 *
 * @param <R> detail renderer type
 */
public abstract class AbstractDetailRendererProvider<R extends AbstractDetailRenderer<?, ?, ?>>
        implements RendererProvider<R> {

    @Autowired
    protected LoaderSupport loaderSupport;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected ClassManager classManager;

    /**
     * Creates and configures a detail renderer.
     *
     * @param context renderer creation context
     * @return configured detail renderer
     */
    @Override
    public R createRenderer(RendererCreationContext context) {
        R renderer = createRendererInternal(context);

        Element element = context.getElement();
        loaderSupport.loadString(element, "viewId")
                .ifPresent(renderer::withViewId);
        loadViewClass(element)
                .ifPresent(renderer::withViewClass);

        loaderSupport.loadString(element, "classNames")
                .ifPresent(renderer::withClassNames);
        loaderSupport.loadString(element, "css")
                .ifPresent(renderer::withCss);

        return renderer;
    }

    protected ValueProvider<Object, String> createTextProvider(RendererCreationContext context) {
        Optional<String> text = loadText(context.getElement(), context.getLoaderContext());
        if (text.isPresent()) {
            return item -> text.get();
        }

        if (context instanceof MetaPropertyPathRendererCreationContext mppContext) {
            return item -> {
                Object value = EntityValues.getValueEx(item, mppContext.getMetaPropertyPath());
                return metadataTools.format(value);
            };
        }

        throw new IllegalArgumentException("No text provider found for element: " + context.getElement());
    }

    abstract protected R createRendererInternal(RendererCreationContext context);

    protected Optional<String> loadText(Element element, ComponentLoader.Context context) {
        return loaderSupport.loadResourceString(element, "text", context.getMessageGroup());
    }

    protected Optional<Class<? extends View<?>>> loadViewClass(Element element) {
        return loaderSupport.loadString(element, "viewClass")
                .map(classManager::loadClass)
                .map(this::asViewClass);
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends View<?>> asViewClass(Class<?> viewClass) {
        if (!View.class.isAssignableFrom(viewClass)) {
            throw new IllegalArgumentException("View class must extend " + View.class.getName());
        }

        return (Class<? extends View<?>>) viewClass;
    }
}
