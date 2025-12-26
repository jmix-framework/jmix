/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.facet.urlqueryparameters;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.lang.Nullable;


/**
 * Base class for URL query binder providers.
 *
 * @see UrlQueryParametersFacet
 * @see UrlQueryParametersBinderProvider
 */
public abstract class AbstractUrlQueryParametersBinderProvider implements UrlQueryParametersBinderProvider {

    protected UrlParamSerializer urlParamSerializer;
    protected LoaderSupport loaderSupport;

    public AbstractUrlQueryParametersBinderProvider(UrlParamSerializer urlParamSerializer,
                                                    LoaderSupport loaderSupport) {
        this.urlParamSerializer = urlParamSerializer;
        this.loaderSupport = loaderSupport;
    }

    protected String loadRequiredAttribute(Element element, String name, ComponentLoader.Context context) {
        return loaderSupport.loadString(element, name)
                .orElseThrow(() -> new GuiDevelopmentException(
                        String.format("'%s/%s' has no '%s' attribute",
                                UrlQueryParametersFacet.NAME, element.getName(), name), context));
    }

    @Nullable
    protected String loadAttribute(Element element, String name) {
        return loaderSupport.loadString(element, name).orElse(null);
    }

    protected static Component getComponent(Component owner, String componentId) {
        return UiComponentUtils.findComponent(owner, componentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Component with id '%s' not found", componentId)));
    }
}
