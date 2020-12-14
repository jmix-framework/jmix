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

package io.jmix.ui.component.impl;

import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Actions;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.action.filter.FilterAction;
import io.jmix.ui.action.filter.FilterAddConditionAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.filter.FilterSupport;
import io.jmix.ui.component.filter.configuration.DesignTimeConfiguration;
import io.jmix.ui.component.filter.configuration.RunTimeConfiguration;
import io.jmix.ui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.theme.ThemeClassNames;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@CompositeDescriptor("filter.xml")
public class FilterImpl extends CompositeComponent<GroupBoxLayout> implements Filter, CompositeWithHtmlCaption,
        CompositeWithHtmlDescription, CompositeWithContextHelp, CompositeWithIcon {

    protected Actions actions;
    protected CurrentAuthentication currentAuthentication;
    protected FilterSupport filterSupport;
    protected Messages messages;
    protected Metadata metadata;
    protected PropertyFilterSupport propertyFilterSupport;
    protected ScreenBuilders screenBuilders;
    protected UiComponents uiComponents;
    protected UiProperties uiProperties;

    protected LogicalFilterComponent rootLogicalFilterComponent;
    protected ResponsiveGridLayout controlsLayout;
    protected CssLayout searchLayout;
    protected Button searchButton;
    protected PopupButton selectConfigurationButton;
    protected LinkButton addConditionButton;
    protected PopupButton settingsButton;

    protected DataLoader dataLoader;
    protected Boolean autoApply;
    protected Integer columnsCount;
    protected Predicate<MetaPropertyPath> propertiesFilterPredicate;

    protected Configuration emptyConfiguration;
    protected Configuration currentConfiguration;
    protected List<Configuration> configurations = new ArrayList<>();

    protected List<FilterComponent> conditions = new ArrayList<>();

    public FilterImpl() {
        addCreateListener(this::onCreate);
        addAttachListener(this::onAttach);
    }

    @Autowired
    public void setActions(Actions actions) {
        this.actions = actions;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Autowired
    public void setFilterSupport(FilterSupport filterSupport) {
        this.filterSupport = filterSupport;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setPropertyFilterSupport(PropertyFilterSupport propertyFilterSupport) {
        this.propertyFilterSupport = propertyFilterSupport;
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setUiProperties(UiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    @Override
    public boolean isExpanded() {
        return getComposition().isExpanded();
    }

    @Override
    public void setExpanded(boolean expanded) {
        getComposition().setExpanded(expanded);
    }

    @Override
    public boolean isCollapsable() {
        return getComposition().isCollapsable();
    }

    @Override
    public void setCollapsable(boolean collapsable) {
        getComposition().setCollapsable(collapsable);
    }

    @Override
    public Subscription addExpandedStateChangeListener(Consumer<ExpandedStateChangeEvent> listener) {
        return getComposition().addExpandedStateChangeListener(listener);
    }

    @Override
    public DataLoader getDataLoader() {
        return dataLoader;
    }

    @Override
    public void setDataLoader(DataLoader dataLoader) {
        checkState(this.dataLoader == null, "DataLoader has already been initialized");
        checkNotNull(dataLoader);

        this.dataLoader = dataLoader;
        initDataLoader();
    }

    @Override
    public boolean isAutoApply() {
        return autoApply != null ? autoApply : uiProperties.isGenericFilterAutoApply();
    }

    @Override
    public void setAutoApply(boolean autoApply) {
        if (this.autoApply == null || this.autoApply != autoApply) {
            this.autoApply = autoApply;

            updateSearchButtonCaption(autoApply);

            getCurrentConfiguration()
                    .getRootLogicalFilterComponent()
                    .getFilterComponents()
                    .forEach(filterComponent -> filterComponent.setAutoApply(autoApply));
        }
    }

    @Override
    public int getColumnsCount() {
        return columnsCount != null ? columnsCount : uiProperties.getGenericFilterColumnsCount();
    }

    @Override
    public void setColumnsCount(int columnsCount) {
        if (this.columnsCount == null || this.columnsCount != columnsCount) {
            this.columnsCount = columnsCount;

            getCurrentConfiguration().getRootLogicalFilterComponent().setColumnsCount(columnsCount);
        }
    }

    @Nullable
    @Override
    public Predicate<MetaPropertyPath> getPropertiesFilterPredicate() {
        return propertiesFilterPredicate;
    }

    @Override
    public void setPropertiesFilterPredicate(@Nullable Predicate<MetaPropertyPath> propertiesFilterPredicate) {
        this.propertiesFilterPredicate = propertiesFilterPredicate;
    }

    @Override
    public void addPropertiesFilterPredicate(Predicate<MetaPropertyPath> propertiesFilterPredicate) {
        if (this.propertiesFilterPredicate == null) {
            setPropertiesFilterPredicate(propertiesFilterPredicate);
        } else {
            this.propertiesFilterPredicate = this.propertiesFilterPredicate.and(propertiesFilterPredicate);
        }
    }

    @Override
    public void setCurrentConfiguration(Configuration currentConfiguration) {
        if (configurations.contains(currentConfiguration) || getEmptyConfiguration().equals(currentConfiguration)) {
            this.currentConfiguration = currentConfiguration;

            clearValues();
            updateConditionsLayout();
        }
    }

    @Override
    public Configuration getCurrentConfiguration() {
        return currentConfiguration;
    }

    @Override
    public Configuration getConfiguration(String code) {
        return configurations.stream()
                .filter(configuration -> code.equals(configuration.getCode()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Configuration getEmptyConfiguration() {
        return emptyConfiguration;
    }

    @Override
    public List<Configuration> getConfigurations() {
        return configurations;
    }

    @Override
    public Configuration addConfiguration(String code, @Nullable String caption) {
        return addConfiguration(code, caption, LogicalFilterComponent.Operation.AND);
    }

    @Override
    public Configuration addConfiguration(String code, @Nullable String caption, LogicalFilterComponent.Operation rootOperation) {
        LogicalFilterComponent rootComponent = createConfigurationRootLogicalFilterComponent(rootOperation);

        Configuration newConfiguration =
                new DesignTimeConfiguration(code, caption, rootComponent, this);

        addConfiguration(newConfiguration);

        return newConfiguration;
    }

    @Override
    public void addConfiguration(Configuration configuration) {
        configurations.add(configuration);

        addSelectConfigurationAction(configuration);
    }

    @Override
    public void removeConfiguration(Configuration configuration) {
        if (configuration != getEmptyConfiguration()) {
            configurations.remove(configuration);

            if (configuration == getCurrentConfiguration()) {
                setCurrentConfiguration(getEmptyConfiguration());
            }
        }
    }

    @Override
    public void addCondition(FilterComponent filterComponent) {
        conditions.add(filterComponent);
    }

    @Override
    public List<FilterComponent> getConditions() {
        return conditions;
    }

    @Override
    public void removeCondition(FilterComponent filterComponent) {
        conditions.remove(filterComponent);
    }

    @Override
    public void addAction(Action action) {
        settingsButton.addAction(action);
    }

    @Override
    public void addAction(Action action, int index) {
        settingsButton.addAction(action, index);
    }

    @Override
    public void removeAction(Action action) {
        settingsButton.removeAction(action);
    }

    @Override
    public void removeAction(String id) {
        settingsButton.removeAction(id);
    }

    @Override
    public void removeAllActions() {
        settingsButton.removeAllActions();
    }

    @Override
    public Collection<Action> getActions() {
        return settingsButton.getActions();
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        return settingsButton.getAction(id);
    }

    public Condition getQueryCondition() {
        return currentConfiguration.getQueryCondition();
    }

    protected void onCreate(CreateEvent createEvent) {
        controlsLayout = getInnerComponent("filter_controlsLayout");
        searchLayout = getInnerComponent("filter_searchLayout");
        searchButton = getInnerComponent("filter_searchButton");
        selectConfigurationButton = getInnerComponent("filter_selectConfigurationButton");
        addConditionButton = getInnerComponent("filter_addConditionButton");
        settingsButton = getInnerComponent("filter_settingsButton");

        initAddConditionButton();
        initSearchButton();
        initSettingsButton();
        initSelectConfigurationButton();
    }

    protected void onAttach(AttachEvent attachEvent) {
        initDefaultConfiguration();
    }

    protected void initDefaultConfiguration() {
        LogicalFilterComponent configurationLogicalComponent =
                createConfigurationRootLogicalFilterComponent(LogicalFilterComponent.Operation.AND);
        emptyConfiguration =
                new RunTimeConfiguration("empty_configuration", configurationLogicalComponent, this);
        emptyConfiguration.setCaption(messages.getMessage("filter.emptyConfiguration.code"));
        setCurrentConfiguration(emptyConfiguration);
    }

    protected void initAddConditionButton() {
        FilterAddConditionAction addConditionAction = actions.create(FilterAddConditionAction.class);
        addConditionAction.setFilter(this);
        addConditionButton.setAction(addConditionAction, false);
        addConditionButton.setIcon(null);
    }

    protected void initSearchButton() {
        searchButton.addClickListener(clickEvent -> getDataLoader().load());
        updateSearchButtonCaption(isAutoApply());
    }

    protected void initSelectConfigurationButton() {
        selectConfigurationButton.addAction(createResetFilterAction());
    }

    protected void initSettingsButton() {
        if (getActions().isEmpty()) {
            List<FilterAction> defaultFilterActions = filterSupport.getDefaultFilterActions(this);
            for (FilterAction filterAction : defaultFilterActions) {
                addAction(filterAction);
            }
        }
    }

    protected void initDataLoader() {
        getEmptyConfiguration().getRootLogicalFilterComponent().setDataLoader(dataLoader);

        List<Configuration> configurations = filterSupport.getConfigurations(this);
        for (Configuration configuration : configurations) {
            addConfiguration(configuration);
        }
    }

    protected Button createConditionRemoveButton(PropertyFilter<?> propertyFilter, String removeButtonId) {
        Button conditionRemoveButton = uiComponents.create(Button.NAME);
        conditionRemoveButton.setId(removeButtonId);
        conditionRemoveButton.setIconFromSet(JmixIcon.TIMES);
        conditionRemoveButton.addStyleName(ThemeClassNames.BUTTON_ICON_ONLY);
        conditionRemoveButton.addStyleName(ThemeClassNames.BUTTON_BORDERLESS);

        conditionRemoveButton.addClickListener(clickEvent -> {
            removeFilterComponent(propertyFilter);
            updateConditionsLayout();
        });

        return conditionRemoveButton;
    }

    protected void removeFilterComponent(FilterComponent filterComponent) {
        LogicalFilterComponent rootLogicalComponent = getCurrentConfiguration().getRootLogicalFilterComponent();
        rootLogicalComponent.remove(filterComponent);
        getCurrentConfiguration().setModified(filterComponent, false);
    }

    protected Action createResetFilterAction() {
        return new BaseAction("filter_reset")
                .withCaption(messages.getMessage("actions.Filter.Reset"))
                .withHandler(actionPerformedEvent -> {
                    Configuration configuration = getEmptyConfiguration();
                    configuration.getRootLogicalFilterComponent().removeAll();
                    configuration.setModified(false);
                    setCurrentConfiguration(configuration);
                });
    }

    protected LogicalFilterComponent createConfigurationRootLogicalFilterComponent(LogicalFilterComponent.Operation rootOperation) {
        GroupFilter rootGroupFilter = uiComponents.create(GroupFilter.NAME);
        rootGroupFilter.setOperation(rootOperation);
        rootGroupFilter.setDataLoader(getDataLoader());

        ((GroupFilterImpl) rootGroupFilter).getComposition().setShowAsPanel(true);
        rootGroupFilter.setStyleName("borderless");

        return rootGroupFilter;
    }

    protected void addSelectConfigurationAction(Configuration configuration) {
        configuration.getCaption();
        Action configurationAction = new BaseAction("filter_select_" + configuration.getCode())
                .withCaption(configuration.getCaption())
                .withHandler(actionPerformedEvent -> setCurrentConfiguration(configuration));
        selectConfigurationButton.addAction(configurationAction);
    }

    protected void updateRootLayoutCaption() {
        StringBuilder stringBuilder = new StringBuilder(getEmptyConfiguration().getCaption());
        if (!getEmptyConfiguration().equals(getCurrentConfiguration())) {
            stringBuilder.append(" : ")
                    .append(getCurrentConfiguration().getCaption());
        }

        getComposition().setCaption(stringBuilder.toString());
    }

    protected void updateSearchButtonCaption(boolean autoApply) {
        String caption = autoApply
                ? messages.getMessage("filter.searchButton.autoApply")
                : messages.getMessage("filter.searchButton");
        searchButton.setCaption(caption);
    }

    protected void updateConditionsLayout() {
        if (rootLogicalFilterComponent != null) {
            getComposition().remove(rootLogicalFilterComponent);
        }

        if (!getCurrentConfiguration().getRootLogicalFilterComponent().getFilterComponents().isEmpty()) {
            updateRootLogicalFilterComponent(getCurrentConfiguration().getRootLogicalFilterComponent());
        }

        updateRootLayoutCaption();
        updateActionsState();
    }

    protected void updateActionsState() {
        settingsButton.getActions().forEach(Action::refreshState);

        if (addConditionButton.getAction() != null) {
            addConditionButton.getAction().refreshState();
        }
    }

    protected void updateRootLogicalFilterComponent(LogicalFilterComponent logicalFilterComponent) {
        logicalFilterComponent.setParent(null);
        ComponentsHelper.getComposition(logicalFilterComponent).setParent(null);

        rootLogicalFilterComponent = logicalFilterComponent;
        getComposition().add(rootLogicalFilterComponent, 0);

        rootLogicalFilterComponent.setColumnsCount(getColumnsCount());
        rootLogicalFilterComponent.setAutoApply(isAutoApply());

        if (!(getCurrentConfiguration() instanceof DesignTimeConfiguration)) {
            for (FilterComponent filterComponent : rootLogicalFilterComponent.getFilterComponents()) {
                if (filterComponent instanceof PropertyFilter) {
                    PropertyFilter<?> propertyFilter = (PropertyFilter<?>) filterComponent;
                    propertyFilter.addOperationChangeListener(operationChangeEvent ->
                            updateConditionRemoveButton(propertyFilter));
                    updateConditionRemoveButton(propertyFilter);
                }
            }
        }

        if (dataLoader != null) {
            dataLoader.setCondition(logicalFilterComponent.getQueryCondition());
            if (isAutoApply()) {
                getDataLoader().load();
            }
        }
    }

    protected void updateConditionRemoveButton(PropertyFilter<?> propertyFilter) {
        String removeButtonPrefix = propertyFilterSupport.getPropertyFilterPrefix(propertyFilter.getId(),
                propertyFilter.getProperty());
        String removeButtonId = removeButtonPrefix + "conditionRemoveButton";

        HBoxLayout propertyFilterLayout = ((PropertyFilterImpl<?>) propertyFilter).getComposition();
        Button removeButton = (Button) propertyFilterLayout.getComponent(removeButtonId);

        if (getCurrentConfiguration().isModified(propertyFilter)) {
            // If the removeButton is added to the propertyFilterLayout
            // but is not located at the end, then we delete it and
            // re-add it to the end.
            // This situation is possible when changing the type of operation.
            if (removeButton != null
                    && propertyFilterLayout.indexOf(removeButton) != propertyFilterLayout.getComponents().size() - 1) {
                propertyFilterLayout.remove(removeButton);
                removeButton = null;
            }

            if (removeButton == null) {
                removeButton = createConditionRemoveButton(propertyFilter, removeButtonId);
                propertyFilterLayout.add(removeButton);
            }
        } else {
            if (removeButton != null) {
                propertyFilterLayout.remove(removeButton);
            }
        }
    }

    protected void clearValues() {
        if (rootLogicalFilterComponent != null && !rootLogicalFilterComponent.getFilterComponents().isEmpty()) {
            for (FilterComponent component : rootLogicalFilterComponent.getFilterComponents()) {
                if (component instanceof HasValue) {
                    ((HasValue<?>) component).setValue(null);
                }
            }
        }
    }
}
