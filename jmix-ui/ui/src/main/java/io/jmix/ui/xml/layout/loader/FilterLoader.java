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

package io.jmix.ui.xml.layout.loader;

import com.google.common.base.Strings;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.filter.FilterAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.filter.FilterUtils;
import io.jmix.ui.component.filter.inspector.FilterPropertiesInspector;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.dom4j.Element;

import java.util.*;

public class FilterLoader extends ActionsHolderLoader<Filter> {
    @Override
    public void createComponent() {
        resultComponent = factory.create(Filter.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignXmlDescriptor(resultComponent, element);
        assignFrame(resultComponent);

        loadHtmlSanitizerEnabled(resultComponent, element);

        loadString(element, "captionWidth", resultComponent::setCaptionWidth);
        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);
        loadContextHelp(resultComponent, element);
        loadIcon(resultComponent, element);

        loadStyleName(resultComponent, element);
        loadCss(resultComponent, element);
        loadResponsive(resultComponent, element);
        loadAlign(resultComponent, element);

        loadVisible(resultComponent, element);
        loadEnable(resultComponent, element);

        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);

        loadDataLoader(resultComponent, element);
        loadBoolean(element, "autoApply", resultComponent::setAutoApply);
        loadApplyShortcut(resultComponent, element);

        loadCollapsible(resultComponent, element, true);
        loadInteger(element, "columnsCount", resultComponent::setColumnsCount);
        loadEnum(element, SupportsCaptionPosition.CaptionPosition.class, "captionPosition",
                resultComponent::setCaptionPosition);

        loadProperties(resultComponent, element);

        loadConditions(resultComponent, element);
        loadConfigurations(resultComponent, element);

        loadActions(resultComponent, element);
    }

    @Override
    protected void loadActions(ActionsHolder actionsHolder, Element element) {
        Element actionsEl = element.element("actions");
        if (actionsEl == null) {
            return;
        }

        actionsHolder.removeAllActions();
        for (Element actionEl : actionsEl.elements("action")) {
            Action action = loadDeclarativeAction(actionsHolder, actionEl);
            if (action instanceof FilterAction) {
                ((FilterAction) action).setFilter(resultComponent);
            }
            actionsHolder.addAction(action);
        }

        getComponentContext().addPostInitTask((context1, window) ->
                actionsHolder.getActions().forEach(Action::refreshState));
    }

    protected void loadDataLoader(Filter component, Element element) {
        loadString(element, "dataLoader",
                (dataLoaderId) -> {
                    FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
                    ScreenData screenData = UiControllerUtils.getScreenData(frameOwner);
                    DataLoader dataLoader = screenData.getLoader(dataLoaderId);
                    getComponentContext().addInitTask((context, window) ->
                            component.setDataLoader(dataLoader)
                    );
                });
    }

    protected void loadProperties(Filter component, Element element) {
        Element propertiesElement = element.element("properties");
        if (propertiesElement != null) {
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

            component.addPropertiesFilterPredicate(propertiesInspector);
        }
    }

    protected void loadConditions(Filter component, Element element) {
        Element conditionsElement = element.element("conditions");
        if (conditionsElement != null) {
            for (Element filterElement : conditionsElement.elements()) {
                component.addCondition(loadFilterComponent(filterElement));
            }
        }
    }

    protected void loadConfigurations(Filter component, Element element) {
        Set<String> filterPaths = new HashSet<>();
        getComponentContext().addPostInitTask((context1, window) -> {
            ComponentsHelper.walkComponents(window, (visitingComponent, name) -> {
                if (visitingComponent instanceof Filter) {
                    String path = FilterUtils.generateFilterPath((Filter) visitingComponent);
                    if (filterPaths.contains(path)) {
                        throw new GuiDevelopmentException("Filters with the same component path should have different ids",
                                getComponentContext());
                    } else {
                        filterPaths.add(path);
                    }
                }
            });
            component.loadConfigurationsAndApplyDefault();
        });

        Element configurationsElement = element.element("configurations");
        if (configurationsElement != null) {
            for (Element configurationElement : configurationsElement.elements("configuration")) {
                loadConfiguration(component, configurationElement);
            }
        }
    }

    protected void loadConfiguration(Filter component, Element configurationElement) {
        String id = configurationElement.attributeValue("id");
        Optional<LogicalFilterComponent.Operation> rootOperationOptional =
                loadEnum(configurationElement, LogicalFilterComponent.Operation.class, "operation");
        String name = loadResourceString(configurationElement.attributeValue("name"));

        Filter.Configuration configuration = rootOperationOptional
                .map(operation -> getResultComponent().addConfiguration(id, name, operation))
                .orElseGet(() -> getResultComponent().addConfiguration(id, name));

        loadConfigurationComponents(configuration, configurationElement);

        loadBoolean(configurationElement, "default", defaultValue -> {
            if (defaultValue) {
                getComponentContext().addPostInitTask((context1, window) -> {
                    if (component.getCurrentConfiguration() == component.getEmptyConfiguration()) {
                        component.setCurrentConfiguration(configuration);
                    }
                });
            }
        });
    }

    protected void loadConfigurationComponents(Filter.Configuration configuration, Element element) {
        LogicalFilterComponent rootGroupFilterComponent = configuration.getRootLogicalFilterComponent();
        for (Element filterElement : element.elements()) {
            FilterComponent filterComponent = loadFilterComponent(filterElement);
            rootGroupFilterComponent.add(filterComponent);

            if (filterComponent instanceof SingleFilterComponent) {
                configuration.setFilterComponentDefaultValue(
                        ((SingleFilterComponent<?>) filterComponent).getParameterName(),
                        ((SingleFilterComponent<?>) filterComponent).getValue());
            }
        }
    }

    protected FilterComponent loadFilterComponent(Element element) {
        ComponentLoader<?> filterComponentLoader = getLayoutLoader().createComponent(element);
        ((FilterComponent) filterComponentLoader.getResultComponent())
                .setConditionModificationDelegated(true);
        ((FilterComponent) filterComponentLoader.getResultComponent())
                .setDataLoader(resultComponent.getDataLoader());
        filterComponentLoader.loadComponent();
        return (FilterComponent) filterComponentLoader.getResultComponent();
    }

    protected void loadApplyShortcut(Filter component, Element element) {
        String applyShortcutAttribute = element.attributeValue("applyShortcut");
        if (!Strings.isNullOrEmpty(applyShortcutAttribute)) {
            String shortcut = loadShortcut(applyShortcutAttribute);
            component.setApplyShortcut(shortcut);
        }
    }
}
