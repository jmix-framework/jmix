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
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.dom4j.Element;

public interface RendererProvider<R extends Renderer<?>> {

    @Deprecated(forRemoval = true, since = "3.0")
    boolean supports(String rendererName);

    @Deprecated(forRemoval = true, since = "3.0")
    R createRenderer(Element element, MetaPropertyPath metaPropertyPath, ComponentLoader.Context context);

    default boolean supports(RendererCreationContext context) {
        return supports(context.getElement().getName());
    }

    default R createRenderer(RendererCreationContext context) {
        return createRenderer(context.getElement(), context.getMetaPropertyPath(), context.getLoaderContext());
    }

    class RendererCreationContext {

        protected Element element;
        protected Component ownerComponent;
        protected MetaPropertyPath metaPropertyPath;
        protected ComponentLoader.Context loaderContext;

        public RendererCreationContext(Element element,
                                       Component ownerComponent,
                                       MetaPropertyPath metaPropertyPath,
                                       ComponentLoader.Context loaderContext) {
            this.element = element;
            this.ownerComponent = ownerComponent;
            this.metaPropertyPath = metaPropertyPath;
            this.loaderContext = loaderContext;
        }

        public Element getElement() {
            return element;
        }

        public Component getOwnerComponent() {
            return ownerComponent;
        }

        public MetaPropertyPath getMetaPropertyPath() {
            return metaPropertyPath;
        }

        public ComponentLoader.Context getLoaderContext() {
            return loaderContext;
        }
    }
}
