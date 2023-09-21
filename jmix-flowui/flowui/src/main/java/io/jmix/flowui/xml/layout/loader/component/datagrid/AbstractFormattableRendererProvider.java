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

import com.vaadin.flow.data.renderer.Renderer;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.ComponentLoader.Context;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;

public abstract class AbstractFormattableRendererProvider<R extends Renderer<?>> implements RendererProvider<R> {

    protected final LoaderSupport loaderSupport;
    protected final CurrentAuthentication currentAuthentication;

    protected AbstractFormattableRendererProvider(LoaderSupport loaderSupport,
                                                  CurrentAuthentication currentAuthentication) {
        this.loaderSupport = loaderSupport;
        this.currentAuthentication = currentAuthentication;
    }

    @Override
    public R createRenderer(Element element, MetaPropertyPath metaPropertyPath, Context context) {
        String format = loaderSupport.loadResourceString(element, "format", context.getMessageGroup())
                .orElseThrow(() -> new GuiDevelopmentException("'format' is required", context,
                        "Element", element.getName()));

        String nullRepresentation = loaderSupport
                .loadResourceString(element, "nullRepresentation", context.getMessageGroup())
                .orElse("");

        return createRendererInternal(metaPropertyPath, format, nullRepresentation);
    }

    abstract protected R createRendererInternal(MetaPropertyPath propertyPath,
                                                          String format, String nullRepresentation);
}
