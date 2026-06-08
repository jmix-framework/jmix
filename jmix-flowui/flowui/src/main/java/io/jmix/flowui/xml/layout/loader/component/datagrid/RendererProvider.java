/*
 * Copyright 2022 Haulmont.
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
import com.vaadin.flow.data.renderer.Renderer;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.dom4j.Element;

/**
 * Provides renderers for data grid columns loaded from XML.
 *
 * @param <R> renderer type
 */
public interface RendererProvider<R extends Renderer<?>> {

    /**
     * Checks whether this provider supports an XML renderer element name.
     *
     * @param rendererName XML renderer element name
     * @return {@code true} if this provider can create a renderer for the element, {@code false} otherwise
     * @deprecated use {@link #supports(RendererCreationContext)} instead
     */
    @Deprecated(forRemoval = true, since = "3.0")
    boolean supports(String rendererName);

    /**
     * Creates a renderer using a column meta-property path.
     *
     * @param element          XML renderer element
     * @param metaPropertyPath column meta-property path
     * @param context          loader context
     * @return renderer instance
     * @deprecated use {@link #createRenderer(RendererCreationContext)} instead
     */
    @Deprecated(forRemoval = true, since = "3.0")
    R createRenderer(Element element, MetaPropertyPath metaPropertyPath, ComponentLoader.Context context);

    /**
     * Checks whether this provider supports renderer creation for the supplied context.
     *
     * @param context renderer creation context
     * @return {@code true} if this provider can create a renderer for the context, {@code false} otherwise
     */
    default boolean supports(RendererCreationContext context) {
        return context instanceof MetaPropertyPathRendererCreationContext
                && supports(context.getElement().getName());
    }

    /**
     * Creates a renderer using the supplied context.
     *
     * @param context renderer creation context
     * @return renderer instance
     */
    default R createRenderer(RendererCreationContext context) {
        if (context instanceof MetaPropertyPathRendererCreationContext mpContext) {
            return createRenderer(mpContext.getElement(), mpContext.getMetaPropertyPath(), mpContext.getLoaderContext());
        }

        throw new UnsupportedOperationException("Only MetaPropertyPathRendererCreationContext is supported");
    }

    /**
     * Renderer creation context that carries an entity metaClass without a column meta-property path.
     */
    class MetaClassRendererCreationContext implements RendererCreationContext {

        protected Element element;
        protected Component ownerComponent;
        protected MetaClass metaClass;
        protected ComponentLoader.Context context;

        /**
         * Creates a context for a renderer configured in a key-only column.
         *
         * @param element        XML renderer element
         * @param ownerComponent component that owns the rendered column
         * @param metaClass      grid entity metaClass
         * @param context        loader context
         */
        public MetaClassRendererCreationContext(Element element, Component ownerComponent,
                                                MetaClass metaClass, ComponentLoader.Context context) {
            this.element = element;
            this.ownerComponent = ownerComponent;
            this.metaClass = metaClass;
            this.context = context;
        }

        @Override
        public Element getElement() {
            return element;
        }

        @Override
        public Component getOwnerComponent() {
            return ownerComponent;
        }

        @Override
        public ComponentLoader.Context getLoaderContext() {
            return context;
        }

        /**
         * @return grid entity metaClass
         */
        public MetaClass getMetaClass() {
            return metaClass;
        }
    }

    /**
     * Renderer creation context that carries a column meta-property path.
     */
    class MetaPropertyPathRendererCreationContext implements RendererCreationContext {

        protected Element element;
        protected Component ownerComponent;
        protected MetaPropertyPath metaPropertyPath;
        protected ComponentLoader.Context loaderContext;

        /**
         * Creates a context for a renderer configured in a property column.
         *
         * @param element          XML renderer element
         * @param ownerComponent   component that owns the rendered column
         * @param metaPropertyPath column meta-property path
         * @param loaderContext    loader context
         */
        public MetaPropertyPathRendererCreationContext(Element element, Component ownerComponent,
                                                       MetaPropertyPath metaPropertyPath, ComponentLoader.Context loaderContext) {
            this.element = element;
            this.ownerComponent = ownerComponent;
            this.metaPropertyPath = metaPropertyPath;
            this.loaderContext = loaderContext;
        }

        @Override
        public Element getElement() {
            return element;
        }

        @Override
        public Component getOwnerComponent() {
            return ownerComponent;
        }

        @Override
        public ComponentLoader.Context getLoaderContext() {
            return loaderContext;
        }

        /**
         * @return column meta-property path
         */
        public MetaPropertyPath getMetaPropertyPath() {
            return metaPropertyPath;
        }
    }

    /**
     * Context used for creating a renderer from a data grid XML declaration.
     */
    interface RendererCreationContext {

        /**
         * @return XML renderer element
         */
        Element getElement();

        /**
         * @return component that owns the rendered column
         */
        Component getOwnerComponent();

        /**
         * @return loader context
         */
        ComponentLoader.Context getLoaderContext();
    }
}
