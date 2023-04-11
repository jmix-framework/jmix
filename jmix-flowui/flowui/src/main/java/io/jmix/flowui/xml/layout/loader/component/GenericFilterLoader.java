/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.ComponentUtil;
import io.jmix.flowui.action.genericfilter.GenericFilterAction;
import io.jmix.flowui.component.filer.FilterComponent;
import io.jmix.flowui.component.filer.SingleFilterComponentBase;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.FilterUtils;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration;
import io.jmix.flowui.component.genericfilter.inspector.FilterPropertiesInspector;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;
import org.dom4j.Element;

import java.util.*;

public class GenericFilterLoader extends AbstractComponentLoader<GenericFilter> {

    protected ActionLoaderSupport actionLoaderSupport;

    @Override
    protected GenericFilter createComponent() {
        return factory.create(GenericFilter.class);
    }

    @Override
    public void loadComponent() {
        loadResourceString(element, "summaryText", context.getMessageGroup(), resultComponent::setSummaryText);

        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);

        getLoaderSupport().loadBoolean(element, "opened", resultComponent::setOpened);
        getLoaderSupport().loadBoolean(element, "autoApply", resultComponent::setAutoApply);

        loadDataLoader(resultComponent, element);
        componentLoader().loadResponsiveSteps(resultComponent, element);

        loadProperties(resultComponent, element);
        loadConditions(resultComponent, element);
        loadConfigurations(resultComponent, element);

        loadActions(resultComponent, element);
    }

    protected void loadDataLoader(GenericFilter component, Element element) {
        loadString(element, "dataLoader",
                (dataLoaderId) -> {
                    ViewData screenData = ViewControllerUtils.getViewData(getComponentContext().getView());
                    DataLoader dataLoader = screenData.getLoader(dataLoaderId);
                    component.setDataLoader(dataLoader);
                });
    }

    protected void loadProperties(GenericFilter resultComponent, Element element) {
        Element propertiesElement = element.element("properties");

        if (propertiesElement == null) {
            return;
        }

        FilterPropertiesInspector propertiesInspector = new FilterPropertiesInspector();

        loadString(propertiesElement, "include", propertiesInspector::setIncludedPropertiesRegexp);
        loadString(propertiesElement, "exclude", propertiesInspector::setExcludedPropertiesRegexp);
        loadString(propertiesElement, "excludeProperties", excludePropertiesString -> {
            List<String> excludeProperties =
                    Arrays.asList(excludePropertiesString.replace(" ", "").split(","));
            propertiesInspector.setExcludedProperties(excludeProperties);
        });
        loadBoolean(propertiesElement, "excludeRecursively",
                propertiesInspector::setExcludeRecursively);

        resultComponent.addPropertyFiltersPredicate(propertiesInspector);
    }

    protected void loadConditions(GenericFilter resultComponent, Element element) {
        Element conditionsElement = element.element("conditions");
        if (conditionsElement == null) {
            return;
        }

        for (Element filterElement : conditionsElement.elements()) {
            resultComponent.addCondition(loadFilterComponent(filterElement));
        }
    }

    protected void loadConfigurations(GenericFilter resultComponent, Element element) {
        Set<String> filterPaths = new HashSet<>();

        getComponentContext().addInitTask((context1, view) -> {
            ComponentUtil.findComponents(view.getElement(), component -> {
                if (component instanceof GenericFilter) {
                    String path = FilterUtils.generateFilterPath((GenericFilter) component);

                    if (filterPaths.contains(path)) {
                        throw new GuiDevelopmentException("Filters with the same component path should have different ids",
                                getComponentContext());
                    } else {
                        filterPaths.add(path);
                    }
                }
            });

            resultComponent.loadConfigurationsAndApplyDefault();
        });

        Element configurationsElement = element.element("configurations");
        if (configurationsElement != null) {
            for (Element configurationElement : configurationsElement.elements("configuration")) {
                loadConfiguration(resultComponent, configurationElement);
            }
        }
    }

    protected void loadConfiguration(GenericFilter resultComponent, Element configurationElement) {
        String id = loadString(configurationElement, "id")
                .orElseThrow(() -> new GuiDevelopmentException("Required 'id' is not found", context));
        String name = loadResourceString(configurationElement, "name", getContext().getMessageGroup())
                .orElse(null);
        Optional<LogicalFilterComponent.Operation> rootOperationOptional =
                loadEnum(configurationElement, LogicalFilterComponent.Operation.class, "operation");

        DesignTimeConfiguration designTimeConfiguration = rootOperationOptional
                .map(operation -> resultComponent.addConfiguration(id, name, operation))
                .orElseGet(() -> resultComponent.addConfiguration(id, name));

        loadConfigurationComponents(designTimeConfiguration, configurationElement);

        loadBoolean(configurationElement, "default", defaultValue -> {
            if (defaultValue) {
                getComponentContext().addInitTask((context1, view) -> {
                    if (resultComponent.getCurrentConfiguration() == resultComponent.getEmptyConfiguration()) {
                        resultComponent.setCurrentConfiguration(designTimeConfiguration);
                    }
                });
            }
        });
    }

    protected void loadConfigurationComponents(Configuration configuration, Element element) {
        LogicalFilterComponent<?> rootLogicalFilterComponent = configuration.getRootLogicalFilterComponent();
        for (Element filterElement : element.elements()) {
            FilterComponent filterComponent = loadFilterComponent(filterElement);
            rootLogicalFilterComponent.add(filterComponent);

            if (filterComponent instanceof SingleFilterComponentBase) {
                SingleFilterComponentBase<?> singleFilterComponent = (SingleFilterComponentBase<?>) filterComponent;

                configuration.setFilterComponentDefaultValue(
                        singleFilterComponent.getParameterName(),
                        singleFilterComponent.getValue()
                );
            }
        }
    }

    protected FilterComponent loadFilterComponent(Element element) {
        //noinspection DuplicatedCode
        ComponentLoader<?> filterComponentLoader = getLayoutLoader().createComponentLoader(element);
        filterComponentLoader.initComponent();

        FilterComponent filterResultComponent = (FilterComponent) filterComponentLoader.getResultComponent();

        filterResultComponent.setConditionModificationDelegated(true);
        filterResultComponent.setDataLoader(resultComponent.getDataLoader());

        filterComponentLoader.loadComponent();

        return filterResultComponent;
    }

    protected void loadActions(GenericFilter resultComponent, Element element) {
        Element actionsElement = element.element("actions");
        if (actionsElement == null) {
            return;
        }

        //clear default actions
        resultComponent.removeAllActions();
        for (Element actionElement : actionsElement.elements("action")) {
            Action action = getActionLoaderSupport().loadDeclarativeActionByType(actionElement)
                    .orElseGet(() ->
                            getActionLoaderSupport().loadDeclarativeAction(actionElement));

            if (action instanceof GenericFilterAction) {
                ((GenericFilterAction<?>) action).setTarget(resultComponent);
            }

            resultComponent.addAction(action);
        }

        getComponentContext().addInitTask((context1, view) ->
                resultComponent.getActions().forEach(Action::refreshState));
    }

    protected ActionLoaderSupport getActionLoaderSupport() {
        if (actionLoaderSupport == null) {
            actionLoaderSupport = applicationContext.getBean(ActionLoaderSupport.class, context);
        }

        return actionLoaderSupport;
    }
}
