/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.xml.data;

import com.haulmont.cuba.gui.components.DatasourceComponent;
import com.haulmont.cuba.gui.components.OptionsField;
import com.haulmont.cuba.gui.components.data.value.DatasourceValueSource;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.options.DatasourceOptions;
import com.haulmont.cuba.gui.xml.layout.loaders.ComponentLoaderContext;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.Optional;

/**
 * Provides helper methods to load datasource and options datasource. Is used only in legacy component loaders.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class DatasourceLoaderHelper {


    /**
     * Loads datasource with property if component does not have {@link ValueSource}.
     *
     * @param component        component to load datasource
     * @param element          component descriptor
     * @param context          loader context
     * @param componentContext component loader context
     * @return optional with {@link DatasourceValueSource}
     * @see #loadDatasource(String, Element, ComponentLoader.Context, ComponentLoaderContext)
     */
    public static Optional<DatasourceValueSource> loadDatasourceIfValueSourceNull(DatasourceComponent component,
                                                                                  Element element,
                                                                                  ComponentLoader.Context context,
                                                                                  ComponentLoaderContext componentContext) {
        if (component.getValueSource() != null) {
            return Optional.empty();
        }

        return loadDatasource(component.getId(), element, context, componentContext);
    }

    /**
     * Loads datasource with property and returns empty optional if datasource is not defined in the component or
     * it has already {@link ValueSource}
     *
     * @param componentId      component id
     * @param element          component descriptor
     * @param context          loader context
     * @param componentContext component loader context
     * @return optional with {@link DatasourceValueSource}
     */
    public static Optional<DatasourceValueSource> loadDatasource(String componentId,
                                                                 Element element,
                                                                 ComponentLoader.Context context,
                                                                 ComponentLoaderContext componentContext) {
        String datasource = element.attributeValue("datasource");
        if (!StringUtils.isEmpty(datasource)) {
            if (componentContext.getDsContext() == null) {
                throw new IllegalStateException("'datasource' attribute can be used only in screens with 'dsContext' element. " +
                        "In a screen with 'data' element use 'dataContainer' attribute.");
            }
            Datasource ds = componentContext.getDsContext().get(datasource);
            if (ds == null) {
                throw new GuiDevelopmentException(String.format("Datasource '%s' is not defined", datasource),
                        context, "Component ID", componentId);
            }
            String property = loadProperty(componentId, element, context);
            return Optional.of(new DatasourceValueSource(ds, property));
        }

        return Optional.empty();
    }

    /**
     * Loads property for datasource. Throws GuiDevelopmentException if property is empty.
     *
     * @param componentId component id
     * @param element     component element
     * @param context     loader context
     * @return property for datasource
     */
    public static String loadProperty(String componentId, Element element, ComponentLoader.Context context) {
        String property = element.attributeValue("property");
        if (StringUtils.isEmpty(property)) {
            throw new GuiDevelopmentException(
                    String.format("'property' attribute for '%s' component is not defined", componentId),
                    context);
        }

        return property;
    }


    /**
     * Loads options datasource if field does not have {@link ValueSource}.
     *
     * @param component        field to load options
     * @param element          field descriptor
     * @param componentContext component loader context
     * @return optional with {@link DatasourceOptions}
     * @see #loadOptionsDatasource(Element, ComponentLoaderContext)
     */
    public static Optional<DatasourceOptions> loadOptionsDatasourceIfOptionsNull(OptionsField component,
                                                                                 Element element,
                                                                                 ComponentLoaderContext componentContext) {
        if (component.getOptions() != null) {
            return Optional.empty();
        }

        return loadOptionsDatasource(element, componentContext);
    }

    /**
     * Loads options datasource.
     *
     * @param element          field descriptor
     * @param componentContext component loader context
     * @return optional with {@link DatasourceOptions} or empty if options datasource is not defined in the component
     */
    public static Optional<DatasourceOptions> loadOptionsDatasource(Element element,
                                                                    ComponentLoaderContext componentContext) {
        String datasource = element.attributeValue("optionsDatasource");
        if (!StringUtils.isEmpty(datasource)) {
            CollectionDatasource options = (CollectionDatasource) componentContext.getDsContext().get(datasource);
            return Optional.of(new DatasourceOptions(options));
        }

        return Optional.empty();
    }

    /**
     * Loads table datasource from rows element.
     *
     * @param element       table descriptor
     * @param rowsElement   rows element descriptor
     * @param context       loader context
     * @param loaderContext component loader context
     * @return collection datasource or throws an exception
     */
    public static CollectionDatasource loadTableDatasource(Element element,
                                                           Element rowsElement,
                                                           ComponentLoader.Context context,
                                                           ComponentLoaderContext loaderContext) {
        String datasourceId = rowsElement.attributeValue("datasource");
        if (StringUtils.isBlank(datasourceId)) {
            throw new GuiDevelopmentException("Table 'rows' element doesn't have 'datasource' attribute",
                    context, "Table ID", element.attributeValue("id"));
        }

        return loadCollectionDatasource(datasourceId, context, loaderContext);
    }

    /**
     * Loads collection datasource.
     *
     * @param datasourceId  datasource id
     * @param context       loader context
     * @param loaderContext component loader context
     * @return collection datasource or throws an exception
     */
    public static CollectionDatasource loadCollectionDatasource(String datasourceId,
                                                                ComponentLoader.Context context,
                                                                ComponentLoaderContext loaderContext) {
        Datasource datasource = loaderContext.getDsContext().get(datasourceId);
        if (datasource == null) {
            throw new GuiDevelopmentException("Can't find datasource by name: " + datasourceId, context);
        }

        if (!(datasource instanceof CollectionDatasource)) {
            throw new GuiDevelopmentException("Not a CollectionDatasource: " + datasourceId, context);
        }

        return (CollectionDatasource) datasource;
    }
}
