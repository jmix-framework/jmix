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

import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Actions;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.UiComponents;
import io.jmix.ui.accesscontext.UiFilterModifyConfigurationContext;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.action.filter.FilterAction;
import io.jmix.ui.action.filter.FilterAddConditionAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.filter.FilterSupport;
import io.jmix.ui.component.filter.configuration.DesignTimeConfiguration;
import io.jmix.ui.component.filter.configuration.RunTimeConfiguration;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.BaseCollectionLoader;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.theme.ThemeClassNames;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@CompositeDescriptor("filter.xml")
public class FilterImpl extends CompositeComponent<GroupBoxLayout> implements Filter, CompositeWithHtmlCaption,
        CompositeWithHtmlDescription, CompositeWithContextHelp, CompositeWithIcon {

    protected static final String FILTER_STYLENAME = "jmix-filter";
    protected static final String FILTER_ROOT_COMPONENT_STYLENAME = "jmix-filter-root-component";

    protected Actions actions;
    protected CurrentAuthentication currentAuthentication;
    protected FilterSupport filterSupport;
    protected Messages messages;
    protected Metadata metadata;
    protected ScreenBuilders screenBuilders;
    protected UiComponents uiComponents;

    protected LogicalFilterComponent rootLogicalFilterComponent;
    protected ResponsiveGridLayout controlsLayout;
    protected CssLayout searchLayout;
    protected Button searchButton;
    protected PopupButton selectConfigurationButton;
    protected LinkButton addConditionButton;
    protected PopupButton settingsButton;

    protected DataLoader dataLoader;
    protected Condition initialDataLoaderCondition;
    protected boolean autoApply;
    protected int columnsCount;
    protected CaptionPosition captionPosition = CaptionPosition.LEFT;
    protected Predicate<MetaPropertyPath> propertiesFilterPredicate;

    protected Configuration emptyConfiguration;
    protected Configuration currentConfiguration;
    protected List<Configuration> configurations = new ArrayList<>();

    protected List<FilterComponent> conditions = new ArrayList<>();

    protected boolean configurationModifyPermitted;

    public FilterImpl() {
        addCreateListener(this::onCreate);
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
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setUiComponentProperties(UiComponentProperties componentProperties) {
        this.columnsCount = componentProperties.getFilterColumnsCount();
        this.autoApply = componentProperties.isFilterAutoApply();
    }

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        UiFilterModifyConfigurationContext context = new UiFilterModifyConfigurationContext();
        accessManager.applyRegisteredConstraints(context);
        configurationModifyPermitted = context.isPermitted();
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
        this.initialDataLoaderCondition = dataLoader.getCondition();

        initEmptyConfiguration();
    }

    @Override
    public boolean isAutoApply() {
        return autoApply;
    }

    @Override
    public void setAutoApply(boolean autoApply) {
        if (this.autoApply != autoApply) {
            this.autoApply = autoApply;

            updateSearchButtonCaption(autoApply);

            getCurrentConfiguration()
                    .getRootLogicalFilterComponent()
                    .getOwnFilterComponents()
                    .forEach(filterComponent -> filterComponent.setAutoApply(autoApply));
        }
    }

    @Override
    public void apply() {
        if (dataLoader != null) {
            setupLoaderFirstResult();
            if (isAutoApply()) dataLoader.load();
        }
    }

    @Override
    public CaptionPosition getCaptionPosition() {
        return captionPosition;
    }

    @Override
    public void setCaptionPosition(CaptionPosition position) {
        if (this.captionPosition != position) {
            this.captionPosition = position;

            LogicalFilterComponent rootComponent = getCurrentConfiguration().getRootLogicalFilterComponent();
            if (rootComponent instanceof SupportsCaptionPosition) {
                ((SupportsCaptionPosition) rootComponent).setCaptionPosition(position);
            }
        }
    }

    @Override
    public int getColumnsCount() {
        return columnsCount;
    }

    @Override
    public void setColumnsCount(int columnsCount) {
        if (this.columnsCount != columnsCount) {
            this.columnsCount = columnsCount;

            if (rootLogicalFilterComponent instanceof SupportsColumnsCount) {
                ((SupportsColumnsCount) rootLogicalFilterComponent).setColumnsCount(columnsCount);
            }
        }
    }

    @Override
    public void setFrame(@Nullable Frame frame) {
        super.setFrame(frame);

        if (frame != null) {
            for (Configuration configuration : configurations) {
                LogicalFilterComponent component = configuration.getRootLogicalFilterComponent();
                if (component instanceof BelongToFrame
                        && ((BelongToFrame) component).getFrame() == null) {
                    ((BelongToFrame) component).setFrame(frame);
                } else {
                    attachToFrame(component);
                }
            }
        }
    }

    protected void attachToFrame(Component childComponent) {
        ((FrameImplementation) frame).registerComponent(childComponent);
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
            if (this.currentConfiguration != currentConfiguration) {
                clearValues();
            }

            Configuration previousConfiguration = this.currentConfiguration;
            this.currentConfiguration = currentConfiguration;

            refreshCurrentConfigurationLayout();
            updateSelectConfigurationButton();

            if (currentConfiguration != previousConfiguration) {
                ConfigurationChangeEvent configurationChangeEvent =
                        new ConfigurationChangeEvent(this, currentConfiguration, previousConfiguration);
                publish(ConfigurationChangeEvent.class, configurationChangeEvent);
            }
        }
    }

    @Override
    public Configuration getCurrentConfiguration() {
        return currentConfiguration;
    }

    @Override
    public Configuration getConfiguration(String id) {
        return configurations.stream()
                .filter(configuration -> id.equals(configuration.getId()))
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
    public DesignTimeConfiguration addConfiguration(String id, @Nullable String name) {
        return addConfiguration(id, name, LogicalFilterComponent.Operation.AND);
    }

    @Override
    public DesignTimeConfiguration addConfiguration(String id, @Nullable String name,
                                                    LogicalFilterComponent.Operation rootOperation) {
        LogicalFilterComponent rootComponent = createConfigurationRootLogicalFilterComponent(rootOperation);

        DesignTimeConfiguration newConfiguration =
                new DesignTimeConfiguration(id, name, rootComponent, this);

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
        if (configuration != getEmptyConfiguration()
                && !(configuration instanceof DesignTimeConfiguration)) {
            configurations.remove(configuration);
            configuration.getRootLogicalFilterComponent().setParent(null);

            if (configuration == getCurrentConfiguration()) {
                setCurrentConfiguration(getEmptyConfiguration());
                apply();
            } else {
                updateSelectConfigurationButton();
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
    public Subscription addConfigurationChangeListener(Consumer<ConfigurationChangeEvent> listener) {
        return getEventHub().subscribe(ConfigurationChangeEvent.class, listener);
    }

    @Override
    public void loadConfigurationsAndApplyDefault() {
        Map<Configuration, Boolean> configurationsMap = filterSupport.getConfigurationsMap(this);
        boolean defaultForAllConfigurationApplied = false;
        for (Map.Entry<Configuration, Boolean> entry : configurationsMap.entrySet()) {
            addConfiguration(entry.getKey());
            if (!defaultForAllConfigurationApplied && entry.getValue()) {
                setCurrentConfiguration(entry.getKey());
                defaultForAllConfigurationApplied = true;
            }
        }
    }

    @Override
    public void refreshCurrentConfigurationLayout() {
        if (rootLogicalFilterComponent != null) {
            getComposition().remove(rootLogicalFilterComponent);
        }

        LogicalFilterComponent rootComponent = getCurrentConfiguration().getRootLogicalFilterComponent();
        boolean isAnyFilterComponentVisible = rootComponent.getFilterComponents().stream()
                .anyMatch(Component::isVisible);
        if (isAnyFilterComponentVisible) {
            updateRootLogicalFilterComponent(getCurrentConfiguration().getRootLogicalFilterComponent());
        } else {
            rootLogicalFilterComponent = rootComponent;
        }

        updateDataLoaderCondition();
        updateRootLayoutCaption();
        updateActionsState();
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

    @Override
    public void setCaption(@Nullable String caption) {
        getComposition().setCaption(caption);

        if (emptyConfiguration != null) {
            emptyConfiguration.setName(caption);
        }
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

        initRootComponent();
        initAddConditionButton();
        initSearchButton();
        initSettingsButton();
        initSelectConfigurationButton();
    }

    protected void initRootComponent() {
        root.unwrap(com.vaadin.ui.Component.class)
                .setPrimaryStyleName(FILTER_STYLENAME);
    }

    protected void initEmptyConfiguration() {
        LogicalFilterComponent configurationLogicalComponent =
                createConfigurationRootLogicalFilterComponent(LogicalFilterComponent.Operation.AND);
        emptyConfiguration =
                new RunTimeConfiguration("empty_configuration", configurationLogicalComponent, this);

        String emptyConfigurationName = StringUtils.isNotEmpty(getCaption())
                ? getCaption()
                : messages.getMessage("filter.emptyConfiguration.name");
        emptyConfiguration.setName(emptyConfigurationName);

        setCurrentConfiguration(emptyConfiguration);
    }

    protected void initAddConditionButton() {
        FilterAddConditionAction addConditionAction = actions.create(FilterAddConditionAction.class);
        addConditionAction.setFilter(this);
        addConditionButton.setAction(addConditionAction, false);
        addConditionButton.setIcon(null);
        addConditionButton.setVisible(configurationModifyPermitted);
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

        settingsButton.setVisible(configurationModifyPermitted);
    }

    protected LinkButton createConditionRemoveButton(SingleFilterComponent<?> singleFilter, String removeButtonId) {
        LinkButton conditionRemoveButton = uiComponents.create(LinkButton.NAME);
        conditionRemoveButton.setId(removeButtonId);
        conditionRemoveButton.setIconFromSet(JmixIcon.TIMES);
        conditionRemoveButton.addStyleName(ThemeClassNames.BUTTON_ICON_ONLY);
        conditionRemoveButton.setAlignment(Alignment.MIDDLE_CENTER);

        conditionRemoveButton.addClickListener(clickEvent -> {
            removeFilterComponent(singleFilter);
            refreshCurrentConfigurationLayout();
            apply();
        });

        return conditionRemoveButton;
    }

    protected void removeFilterComponent(FilterComponent filterComponent) {
        LogicalFilterComponent rootLogicalComponent = getCurrentConfiguration().getRootLogicalFilterComponent();
        if (filterComponent instanceof SingleFilterComponent) {
            getCurrentConfiguration().resetFilterComponentDefaultValue(((SingleFilterComponent<?>) filterComponent).getParameterName());
        }
        rootLogicalComponent.remove(filterComponent);
        getCurrentConfiguration().setFilterComponentModified(filterComponent, false);
    }

    protected Action createResetFilterAction() {
        return new BaseAction("filter_reset")
                .withCaption(messages.getMessage("actions.Filter.Reset"))
                .withHandler(actionPerformedEvent -> {
                    Configuration configuration = getEmptyConfiguration();
                    configuration.getRootLogicalFilterComponent().removeAll();
                    configuration.setModified(false);
                    setCurrentConfiguration(configuration);
                    apply();
                });
    }

    protected LogicalFilterComponent createConfigurationRootLogicalFilterComponent(
            LogicalFilterComponent.Operation rootOperation) {
        GroupFilter rootGroupFilter = uiComponents.create(GroupFilter.NAME);
        rootGroupFilter.setConditionModificationDelegated(true);
        rootGroupFilter.setOperation(rootOperation);
        rootGroupFilter.setOperationCaptionVisible(false);

        if (dataLoader != null) {
            rootGroupFilter.setDataLoader(dataLoader);
            rootGroupFilter.setAutoApply(autoApply);
        }

        rootGroupFilter.setFrame(getFrame());
        rootGroupFilter.setParent(this);

        return rootGroupFilter;
    }

    protected void addSelectConfigurationAction(Configuration configuration) {
        Action configurationAction = new BaseAction("filter_select_" + configuration.getId())
                .withCaption(getConfigurationCaption(configuration))
                .withHandler(actionPerformedEvent -> {
                    setCurrentConfiguration(configuration);
                    apply();
                });
        selectConfigurationButton.addAction(configurationAction);
    }

    protected String getConfigurationCaption(Configuration configuration) {
        String caption = configuration.getName();
        if (caption == null) {
            caption = messages.findMessage(configuration.getId(), null);
            if (caption == null) {
                caption = configuration.getId();
            }
        }

        return caption;
    }

    protected void updateRootLayoutCaption() {
        StringBuilder stringBuilder = new StringBuilder(getConfigurationCaption(getEmptyConfiguration()));
        if (!getEmptyConfiguration().equals(getCurrentConfiguration())) {
            stringBuilder.append(" : ")
                    .append(getConfigurationCaption(getCurrentConfiguration()));
        }

        getComposition().setCaption(stringBuilder.toString());
    }

    protected void updateSearchButtonCaption(boolean autoApply) {
        String caption = autoApply
                ? messages.getMessage("filter.searchButton.autoApply")
                : messages.getMessage("filter.searchButton");
        searchButton.setCaption(caption);
    }

    protected void updateDataLoaderCondition() {
        if (dataLoader != null) {
            LogicalFilterComponent logicalFilterComponent = getCurrentConfiguration().getRootLogicalFilterComponent();
            LogicalCondition filterCondition = logicalFilterComponent.getQueryCondition();

            LogicalCondition resultCondition;
            if (initialDataLoaderCondition instanceof LogicalCondition) {
                resultCondition = (LogicalCondition) initialDataLoaderCondition.copy();
                resultCondition.add(filterCondition);
            } else if (initialDataLoaderCondition != null) {
                resultCondition = LogicalCondition.and()
                        .add(initialDataLoaderCondition)
                        .add(filterCondition);
            } else {
                resultCondition = filterCondition;
            }

            dataLoader.setCondition(resultCondition);
        }
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
        logicalFilterComponent.addStyleName(FILTER_ROOT_COMPONENT_STYLENAME);

        rootLogicalFilterComponent = logicalFilterComponent;
        getComposition().add(rootLogicalFilterComponent, 0);

        if (rootLogicalFilterComponent instanceof SupportsColumnsCount) {
            ((SupportsColumnsCount) rootLogicalFilterComponent).setColumnsCount(getColumnsCount());
        }

        if (rootLogicalFilterComponent instanceof SupportsCaptionPosition) {
            ((SupportsCaptionPosition) rootLogicalFilterComponent).setCaptionPosition(getCaptionPosition());
        }

        rootLogicalFilterComponent.setAutoApply(isAutoApply());

        if (!(getCurrentConfiguration() instanceof DesignTimeConfiguration)) {
            for (FilterComponent filterComponent : rootLogicalFilterComponent.getFilterComponents()) {
                if (filterComponent instanceof SingleFilterComponent) {
                    updateConditionRemoveButton((SingleFilterComponent<?>) filterComponent);
                }

                if (filterComponent instanceof PropertyFilter) {
                    PropertyFilter<?> propertyFilter = (PropertyFilter<?>) filterComponent;
                    propertyFilter.addOperationChangeListener(operationChangeEvent -> {
                        updateConditionRemoveButton(propertyFilter);
                        resetFilterComponentDefaultValue(propertyFilter);
                    });
                }

                if (filterComponent instanceof BelongToFrame) {
                    ((BelongToFrame) filterComponent).setFrame(getFrame());
                }
            }
        }
    }

    protected void updateConditionRemoveButton(SingleFilterComponent<?> singleFilter) {
        String removeButtonPrefix = ((AbstractSingleFilterComponent<?>) singleFilter).getInnerComponentPrefix();
        String removeButtonId = removeButtonPrefix + "conditionRemoveButton";

        HBoxLayout singleFilterLayout = ((AbstractSingleFilterComponent<?>) singleFilter).getComposition();
        LinkButton removeButton = (LinkButton) singleFilterLayout.getComponent(removeButtonId);

        if (getCurrentConfiguration().isFilterComponentModified(singleFilter)) {
            // If the removeButton is added to the singleFilterLayout
            // but is not located at the end, then we delete it and
            // re-add it to the end.
            // This situation is possible when changing the type of operation.
            if (removeButton != null
                    && singleFilterLayout.indexOf(removeButton) != singleFilterLayout.getComponents().size() - 1) {
                singleFilterLayout.remove(removeButton);
                removeButton = null;
            }

            if (removeButton == null) {
                removeButton = createConditionRemoveButton(singleFilter, removeButtonId);
                singleFilterLayout.add(removeButton);
            }
        } else {
            if (removeButton != null) {
                singleFilterLayout.remove(removeButton);
            }
        }
    }

    protected void resetFilterComponentDefaultValue(PropertyFilter<?> propertyFilter) {
        getCurrentConfiguration().resetFilterComponentDefaultValue(propertyFilter.getParameterName());
    }

    protected void updateSelectConfigurationButton() {
        selectConfigurationButton.removeAllActions();
        initSelectConfigurationButton();
        for (Configuration configuration : getConfigurations()) {
            addSelectConfigurationAction(configuration);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void clearValues() {
        if (rootLogicalFilterComponent != null && !rootLogicalFilterComponent.getFilterComponents().isEmpty()) {
            for (FilterComponent component : rootLogicalFilterComponent.getFilterComponents()) {
                if (component instanceof SingleFilterComponent) {
                    SingleFilterComponent singleFilterComponent = (SingleFilterComponent) component;
                    singleFilterComponent.setValue(
                            currentConfiguration.getFilterComponentDefaultValue(singleFilterComponent.getParameterName()));
                    getDataLoader().removeParameter(singleFilterComponent.getParameterName());
                }
            }
        }
    }

    protected void setupLoaderFirstResult() {
        if (dataLoader instanceof BaseCollectionLoader) {
            ((BaseCollectionLoader) dataLoader).setFirstResult(0);
        }
    }
}
