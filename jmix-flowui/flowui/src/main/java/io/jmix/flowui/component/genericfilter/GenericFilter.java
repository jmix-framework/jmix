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

package io.jmix.flowui.component.genericfilter;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.Actions;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponentProperties;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.accesscontext.UiGenericFilterModifyConfigurationContext;
import io.jmix.flowui.action.genericfilter.GenericFilterAction;
import io.jmix.flowui.action.genericfilter.GenericFilterAddConditionAction;
import io.jmix.flowui.action.genericfilter.GenericFilterResetAction;
import io.jmix.flowui.app.filter.condition.AddConditionView;
import io.jmix.flowui.component.SupportsResponsiveSteps;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.filter.SingleFilterComponent;
import io.jmix.flowui.component.filter.SingleFilterComponentBase;
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration;
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration;
import io.jmix.flowui.component.logicalfilter.GroupFilter;
import io.jmix.flowui.component.logicalfilter.GroupFilterSupport;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.combobutton.ComboButton;
import io.jmix.flowui.kit.component.combobutton.ComboButtonVariant;
import io.jmix.flowui.kit.component.dropdownbutton.ActionItem;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonVariant;
import io.jmix.flowui.model.BaseCollectionLoader;
import io.jmix.flowui.model.DataLoader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A versatile tool for filtering the data it is bound to. The component enables
 * quick data filtering by arbitrary conditions, as well as creating configurations
 * for repeated use.
 */
public class GenericFilter extends Composite<JmixDetails>
        implements SupportsResponsiveSteps, HasActions, HasEnabled, HasSize, HasStyle, HasTheme, HasTooltip,
        ApplicationContextAware, InitializingBean {

    protected static final String CONDITION_REMOVE_BUTTON_ID_SUFFIX = "conditionRemoveButton";

    protected static final String FILTER_CLASS_NAME = "jmix-generic-filter";
    protected static final String FILTER_CONTENT_WRAPPER_CLASS_NAME = FILTER_CLASS_NAME + "-content-wrapper";
    protected static final String FILTER_CONTROLS_LAYOUT_CLASS_NAME = FILTER_CLASS_NAME + "-controls-layout";

    protected ApplicationContext applicationContext;
    protected CurrentAuthentication currentAuthentication;
    protected UiComponents uiComponents;
    protected Actions actions;
    protected Messages messages;
    protected Metadata metadata;
    protected DialogWindows dialogWindows;
    protected GenericFilterSupport genericFilterSupport;
    protected GroupFilterSupport groupFilterSupport;

    protected boolean autoApply;
    protected String applyShortcut;
    protected DataLoader dataLoader;
    protected Condition initialDataLoaderCondition;
    protected Predicate<MetaPropertyPath> propertyFiltersPredicate;

    protected VerticalLayout contentWrapper;
    protected HorizontalLayout controlsLayout;
    protected ComboButton applyButton;
    protected JmixButton addConditionButton;
    protected DropdownButton settingsButton;
    protected List<ResponsiveStep> responsiveSteps;
    protected Registration openedChangeRegistration;

    protected LogicalFilterComponent<?> rootLogicalFilterComponent;
    protected Configuration emptyConfiguration;
    protected Configuration currentConfiguration;
    protected List<Configuration> configurations = new ArrayList<>();

    protected List<FilterComponent> conditions;

    protected boolean configurationModifyPermitted;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
        currentAuthentication = applicationContext.getBean(CurrentAuthentication.class);
        actions = applicationContext.getBean(Actions.class);
        messages = applicationContext.getBean(Messages.class);
        metadata = applicationContext.getBean(Metadata.class);
        dialogWindows = applicationContext.getBean(DialogWindows.class);
        genericFilterSupport = applicationContext.getBean(GenericFilterSupport.class);
        groupFilterSupport = applicationContext.getBean(GroupFilterSupport.class);
    }

    protected void initComponent() {
        UiComponentProperties uiComponentProperties = applicationContext.getBean(UiComponentProperties.class);
        this.autoApply = uiComponentProperties.isFilterAutoApply();
        this.applyShortcut = uiComponentProperties.getFilterApplyShortcut();

        initDefaultResponsiveSteps();
        initEmptyConfiguration();
        initLayout();
    }

    protected void initDefaultResponsiveSteps() {
        responsiveSteps = List.of(
                new ResponsiveStep("0", 1, ResponsiveStep.LabelsPosition.TOP),
                new ResponsiveStep("40em", 1),
                new ResponsiveStep("80em", 2),
                new ResponsiveStep("120em", 3)
        );
    }

    protected void initEmptyConfiguration() {
        LogicalFilterComponent<?> configurationLogicalComponent =
                createConfigurationRootLogicalFilterComponent(LogicalFilterComponent.Operation.AND);
        emptyConfiguration =
                new RunTimeConfiguration("empty_configuration", configurationLogicalComponent, this);

        String emptyConfigurationName = StringUtils.isNotEmpty(getSummaryText())
                ? getSummaryText()
                : messages.getMessage("genericFilter.emptyConfiguration.name");
        emptyConfiguration.setName(emptyConfigurationName);

        setCurrentConfigurationInternal(emptyConfiguration, false);
    }

    protected LogicalFilterComponent<?> createConfigurationRootLogicalFilterComponent(
            LogicalFilterComponent.Operation rootOperation) {
        GroupFilter rootGroupFilter = uiComponents.create(GroupFilter.class);
        rootGroupFilter.setConditionModificationDelegated(true);
        rootGroupFilter.setOperation(rootOperation);
        rootGroupFilter.setOperationTextVisible(false);

        if (dataLoader != null) {
            rootGroupFilter.setDataLoader(dataLoader);
            rootGroupFilter.setAutoApply(autoApply);
        }

        return rootGroupFilter;
    }

    @Override
    protected JmixDetails initContent() {
        JmixDetails root = super.initContent();

        root.setClassName(FILTER_CLASS_NAME);
        root.setOpened(true);
        root.setWidthFull();

        return root;
    }

    protected void initLayout() {
        contentWrapper = createContentWrapper();
        initContentWrapper(contentWrapper);
        getContent().setContent(contentWrapper);

        controlsLayout = createControlsLayout();
        initControlsLayout(controlsLayout);
        contentWrapper.add(controlsLayout);
    }

    protected VerticalLayout createContentWrapper() {
        return uiComponents.create(VerticalLayout.class);
    }

    protected void initContentWrapper(VerticalLayout contentWrapper) {
        contentWrapper.setPadding(false);
        contentWrapper.setClassName(FILTER_CONTENT_WRAPPER_CLASS_NAME);
    }

    protected HorizontalLayout createControlsLayout() {
        return uiComponents.create(HorizontalLayout.class);
    }

    protected void initControlsLayout(HorizontalLayout controlsLayout) {
        controlsLayout.setWidthFull();
        controlsLayout.setClassName(FILTER_CONTROLS_LAYOUT_CLASS_NAME);

        applyButton = createApplyButton();
        initApplyButton(applyButton);
        controlsLayout.add(applyButton);

        settingsButton = createSettingsButton();
        initSettingsButton(settingsButton);
        controlsLayout.add(settingsButton);

        addConditionButton = createAddConditionButton();
        initAddConditionButton(addConditionButton);
        controlsLayout.add(addConditionButton);
    }

    protected JmixButton createAddConditionButton() {
        return uiComponents.create(JmixButton.class);
    }

    protected void initAddConditionButton(JmixButton addConditionButton) {
        addConditionButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        GenericFilterAddConditionAction addConditionAction = actions.create(GenericFilterAddConditionAction.ID);
        addConditionAction.setTarget(this);
        addConditionAction.setText(messages.getMessage("genericFilter.addConditionButton.text"));
        addConditionAction.setIcon(null);
        addConditionButton.setAction(addConditionAction, false);
    }

    protected ComboButton createApplyButton() {
        return uiComponents.create(ComboButton.class);
    }

    protected void initApplyButton(ComboButton applyButton) {
        applyButton.addClickListener(this::onApplyButtonClick);
        applyButton.addThemeVariants(ComboButtonVariant.LUMO_SUCCESS, ComboButtonVariant.LUMO_PRIMARY);
        applyButton.setShortcutCombination(KeyCombination.create(applyShortcut));

        updateApplyButtonText(isAutoApply());

        initSelectConfigurationDropdown();
    }

    protected void initSelectConfigurationDropdown() {
        Action resetFilterAction = createResetFilterAction();
        applyButton.addItem(resetFilterAction.getId(), resetFilterAction);
    }

    protected void updateApplyButtonText(boolean autoApply) {
        String text = autoApply
                ? messages.getMessage("genericFilter.applyButton.autoApply")
                : messages.getMessage("genericFilter.applyButton");
        applyButton.setText(text);
    }

    protected void onApplyButtonClick(ClickEvent<MenuItem> clickEvent) {
        getDataLoader().load();
    }

    protected Action createResetFilterAction() {
        GenericFilterResetAction filterResetAction = actions.create(GenericFilterResetAction.ID);
        filterResetAction.setTarget(this);
        return filterResetAction;
    }

    protected DropdownButton createSettingsButton() {
        return uiComponents.create(DropdownButton.class);
    }

    protected void initSettingsButton(DropdownButton settingsButton) {
        settingsButton.addThemeVariants(DropdownButtonVariant.LUMO_ICON);
        settingsButton.setDropdownIndicatorVisible(false);
        settingsButton.setIcon(VaadinIcon.COG.create());

        List<GenericFilterAction<?>> defaultFilterActions = genericFilterSupport.getDefaultFilterActions(this);
        for (GenericFilterAction<?> filterAction : defaultFilterActions) {
            settingsButton.addItem(filterAction.getId(), filterAction);
        }

        UiGenericFilterModifyConfigurationContext context = new UiGenericFilterModifyConfigurationContext();
        applicationContext.getBean(AccessManager.class).applyRegisteredConstraints(context);
        configurationModifyPermitted = context.isPermitted();
        settingsButton.setVisible(configurationModifyPermitted);
    }

    public HasComponents getControlsLayout() {
        return controlsLayout;
    }

    /**
     * @return a {@link LogicalCondition} related to the configuration
     */
    public LogicalCondition getQueryCondition() {
        return getCurrentConfiguration().getQueryCondition();
    }

    /**
     * @return a {@link DataLoader} related to the filter
     */
    public DataLoader getDataLoader() {
        return dataLoader;
    }

    /**
     * Sets a {@link DataLoader} related to the filter.
     *
     * @param dataLoader a {@link DataLoader} to set
     */
    public void setDataLoader(DataLoader dataLoader) {
        checkState(this.dataLoader == null, "DataLoader has already been initialized");
        checkNotNull(dataLoader);

        this.dataLoader = dataLoader;
        this.initialDataLoaderCondition = dataLoader.getCondition();

        LogicalFilterComponent<?> rootLogicalFilterComponent = emptyConfiguration.getRootLogicalFilterComponent();
        rootLogicalFilterComponent.setDataLoader(dataLoader);
        rootLogicalFilterComponent.setAutoApply(autoApply);
    }

    /**
     * @return {@code true} if the filter should be automatically applied to
     * the {@link DataLoader} when the value component value is changed
     */
    public boolean isAutoApply() {
        return autoApply;
    }

    /**
     * Sets whether the filter should be automatically applied to the
     * {@link DataLoader} when the value component value is changed.
     *
     * @param autoApply {@code true} if the filter should be automatically
     *                  applied to the {@link DataLoader} when the value
     *                  component value is changed
     */
    public void setAutoApply(boolean autoApply) {
        if (this.autoApply != autoApply) {
            this.autoApply = autoApply;

            updateApplyButtonText(autoApply);
            updateCurrentConfigurationAutoApply(autoApply);
        }
    }

    /**
     * @return {@link KeyCombination} that is used to apply the filter
     */
    @Nullable
    public KeyCombination getApplyShortcut() {
        return applyButton.getShortcutCombination();
    }

    /**
     * Sets a new {@link KeyCombination} to apply the filter.
     *
     * @param applyShortcut string representation of a {@link KeyCombination}
     */
    public void setApplyShortcut(String applyShortcut) {
        if (!Objects.equals(this.applyShortcut, applyShortcut)) {
            this.applyShortcut = applyShortcut;

            applyButton.setShortcutCombination(KeyCombination.create(applyShortcut));
        }
    }

    protected void updateCurrentConfigurationAutoApply(boolean autoApply) {
        getCurrentConfiguration()
                .getRootLogicalFilterComponent()
                .getOwnFilterComponents()
                .forEach(filterComponent -> filterComponent.setAutoApply(autoApply));
    }

    /**
     * Applies the current configuration.
     */
    public void apply() {
        if (dataLoader != null) {
            setupLoaderFirstResult();
            if (isAutoApply()) dataLoader.load();
        }
    }

    protected void setupLoaderFirstResult() {
        if (dataLoader instanceof BaseCollectionLoader) {
            ((BaseCollectionLoader) dataLoader).setFirstResult(0);
        }
    }

    @Override
    public List<ResponsiveStep> getResponsiveSteps() {
        return Collections.unmodifiableList(responsiveSteps);
    }

    @Override
    public void setResponsiveSteps(List<ResponsiveStep> steps) {
        this.responsiveSteps = steps;

        LogicalFilterComponent<?> rootComponent = getCurrentConfiguration().getRootLogicalFilterComponent();
        if (rootComponent instanceof SupportsResponsiveSteps) {
            ((SupportsResponsiveSteps) rootComponent).setResponsiveSteps(steps);
        }
    }

    /**
     * @return this component summary text
     */
    public String getSummaryText() {
        return getContent().getSummaryText();
    }

    /**
     * Sets this component summary text
     *
     * @param summary text to set
     */
    public void setSummaryText(String summary) {
        getContent().setSummaryText(summary);
    }

    /**
     * @return whether this component is expanded or collapsed
     */
    public boolean isOpened() {
        return getContent().isOpened();
    }

    /**
     * Sets whether this component is expanded or collapsed
     *
     * @param opened whether this component is expanded or collapsed
     */
    public void setOpened(boolean opened) {
        getContent().setOpened(opened);
    }

    /**
     * Adds a listener for {@code opened-changed} events fired by the component.
     *
     * @param listener the listener to set
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addOpenedChangeListener(ComponentEventListener<OpenedChangeEvent> listener) {
        if (openedChangeRegistration == null) {
            openedChangeRegistration = getContent().addOpenedChangeListener(this::onOpenedChanged);
        }

        Registration registration = getEventBus().addListener(OpenedChangeEvent.class, listener);
        return Registration.once(() -> removeOpenedChangeListener(registration));
    }

    protected void removeOpenedChangeListener(Registration registration) {
        registration.remove();
        if (!getEventBus().hasListener(OpenedChangeEvent.class)) {
            openedChangeRegistration.remove();
            openedChangeRegistration = null;
        }
    }

    protected void onOpenedChanged(Details.OpenedChangeEvent openedChangeEvent) {
        OpenedChangeEvent event = new OpenedChangeEvent(this, openedChangeEvent.isFromClient());
        getEventBus().fireEvent(event);
    }

    @Override
    public void addAction(Action action, int index) {
        if (action instanceof GenericFilterAction) {
            ((GenericFilterAction<?>) action).setTarget(this);
        }

        settingsButton.addItem(action.getId(), action, index);
    }

    @Override
    public void removeAction(Action action) {
        removeAction(action.getId());
    }

    @Override
    public void removeAction(String id) {
        settingsButton.remove(id);
    }

    @Override
    public void removeAllActions() {
        settingsButton.removeAll();
    }

    @Override
    public Collection<Action> getActions() {
        return settingsButton.getItems().stream()
                .filter(item -> item instanceof ActionItem)
                .map(item -> ((ActionItem) item).getAction())
                .collect(Collectors.toUnmodifiableList());
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        ActionItem item = (ActionItem) settingsButton.getItem(id);

        return item != null
                ? item.getAction()
                : null;
    }

    /**
     * @return a properties filter predicate
     */
    @Nullable
    public Predicate<MetaPropertyPath> getPropertyFiltersPredicate() {
        return propertyFiltersPredicate;
    }

    /**
     * Sets a predicate that tests whether a property with the given path should be
     * available for filtering.
     *
     * @param propertyFiltersPredicate a predicate to set
     */
    public void setPropertyFiltersPredicate(@Nullable Predicate<MetaPropertyPath> propertyFiltersPredicate) {
        this.propertyFiltersPredicate = propertyFiltersPredicate;
    }

    /**
     * Adds a predicate to the current properties filter predicate. The result predicate
     * is a composed predicate that represents a short-circuiting logical AND of given
     * predicate and current properties filter predicate.
     *
     * @param propertyFiltersPredicate a predicate to add
     */
    public void addPropertyFiltersPredicate(Predicate<MetaPropertyPath> propertyFiltersPredicate) {
        if (this.propertyFiltersPredicate == null) {
            setPropertyFiltersPredicate(propertyFiltersPredicate);
        } else {
            this.propertyFiltersPredicate = this.propertyFiltersPredicate.and(propertyFiltersPredicate);
        }
    }

    /**
     * Gets the current configuration that is currently displayed inside the filter.
     *
     * @return a current configuration
     */
    public Configuration getCurrentConfiguration() {
        return currentConfiguration != null
                ? currentConfiguration
                : emptyConfiguration;
    }

    /**
     * Sets the given configuration as current and displays filter components from the current
     * configuration.
     *
     * @param currentConfiguration a configuration
     */
    public void setCurrentConfiguration(Configuration currentConfiguration) {
        setCurrentConfigurationInternal(currentConfiguration, false);
    }

    protected void setCurrentConfigurationInternal(Configuration currentConfiguration, boolean fromClient) {
        if (configurations.contains(currentConfiguration)
                || getEmptyConfiguration().equals(currentConfiguration)) {
            if (this.currentConfiguration != currentConfiguration) {
                clearValues();
            }

            Configuration previousConfiguration = this.currentConfiguration;
            this.currentConfiguration = currentConfiguration;

            refreshCurrentConfigurationLayout();
            updateSelectConfigurationDropdown();

            if (currentConfiguration != previousConfiguration) {
                ConfigurationChangeEvent configurationChangeEvent =
                        new ConfigurationChangeEvent(this, currentConfiguration, previousConfiguration, fromClient);
                getEventBus().fireEvent(configurationChangeEvent);
            }
        }
    }

    protected void refreshCurrentConfigurationLayout() {
        if (rootLogicalFilterComponent != null) {
            contentWrapper.remove(((Component) rootLogicalFilterComponent));
        }

        LogicalFilterComponent<?> rootComponent = getCurrentConfiguration().getRootLogicalFilterComponent();
        boolean isAnyFilterComponentVisible = rootComponent.getFilterComponents().stream()
                .anyMatch(filterComponent -> ((Component) filterComponent).isVisible());
        if (isAnyFilterComponentVisible) {
            updateRootLogicalFilterComponent(getCurrentConfiguration().getRootLogicalFilterComponent());
        } else {
            rootLogicalFilterComponent = rootComponent;
        }

        updateDataLoaderCondition();
        updateRootLayoutSummaryText();

        ConfigurationRefreshEvent configurationRefreshedEvent =
                new ConfigurationRefreshEvent(this, currentConfiguration, false);
        getEventBus().fireEvent(configurationRefreshedEvent);
    }

    protected void updateRootLogicalFilterComponent(LogicalFilterComponent<?> logicalFilterComponent) {
        rootLogicalFilterComponent = logicalFilterComponent;
        contentWrapper.addComponentAsFirst(((Component) rootLogicalFilterComponent));

        if (rootLogicalFilterComponent instanceof SupportsResponsiveSteps) {
            ((SupportsResponsiveSteps) rootLogicalFilterComponent).setResponsiveSteps(getResponsiveSteps());
        }

        rootLogicalFilterComponent.setAutoApply(isAutoApply());

        if (!(getCurrentConfiguration() instanceof DesignTimeConfiguration)) {
            for (FilterComponent filterComponent : rootLogicalFilterComponent.getFilterComponents()) {
                if (filterComponent instanceof SingleFilterComponentBase) {
                    updateSingleConditionRemoveButton((SingleFilterComponentBase<?>) filterComponent);
                } else if (filterComponent instanceof GroupFilter) {
                    updateGroupConditionButtons(((GroupFilter) filterComponent));
                }

                if (filterComponent instanceof PropertyFilter<?> propertyFilter) {
                    propertyFilter.addOperationChangeListener(operationChangeEvent -> {
                        updateSingleConditionRemoveButton(propertyFilter);
                        resetFilterComponentDefaultValue(propertyFilter);
                    });
                }
            }
        }
    }

    protected void resetFilterComponentDefaultValue(PropertyFilter<?> propertyFilter) {
        getCurrentConfiguration().resetFilterComponentDefaultValue(propertyFilter.getParameterName());
    }

    protected void updateSingleConditionRemoveButton(SingleFilterComponentBase<?> singleFilter) {
        String removeButtonPrefix = singleFilter.getInnerComponentPrefix();
        String removeButtonId = removeButtonPrefix + CONDITION_REMOVE_BUTTON_ID_SUFFIX;

        HorizontalLayout singleFilterLayout = singleFilter.getRoot();
        Optional<Component> existingRemoveButton = UiComponentUtils.findComponent(singleFilterLayout, removeButtonId);

        if (getCurrentConfiguration().isFilterComponentModified(singleFilter)) {
            // If the removeButton is added to the singleFilterLayout
            // but is not located at the end, then we delete it and
            // re-add it to the end. This situation is possible when
            // changing the type of operation.
            if (existingRemoveButton.isPresent()
                    && singleFilterLayout.indexOf(existingRemoveButton.get()) != singleFilterLayout.getComponentCount() - 1) {
                singleFilterLayout.remove(existingRemoveButton.get());
                existingRemoveButton = Optional.empty();
            }

            if (existingRemoveButton.isEmpty()) {
                Component newRemoveButton = createConditionRemoveButton(singleFilter, removeButtonId);
                singleFilterLayout.add(newRemoveButton);
            }
        } else {
            existingRemoveButton.ifPresent(singleFilterLayout::remove);
        }
    }

    protected void updateGroupConditionButtons(GroupFilter groupFilter) {
        Label summaryComponent = groupFilterSupport.getGroupFilterSummaryComponent(groupFilter);

        if (summaryComponent != null) {
            String removeButtonId = CONDITION_REMOVE_BUTTON_ID_SUFFIX;
            Optional<Component> existingRemoveButton = UiComponentUtils.findComponent(summaryComponent, removeButtonId);

            if (getCurrentConfiguration().isFilterComponentModified(groupFilter)) {

                if (existingRemoveButton.isEmpty()) {
                    summaryComponent.add(createConditionRemoveButton(groupFilter, removeButtonId));
                }
            } else {
                existingRemoveButton.ifPresent(summaryComponent::remove);
            }
        }
    }

    protected Component createConditionRemoveButton(FilterComponent filterComponent, String removeButtonId) {
        JmixButton conditionRemoveButton = uiComponents.create(JmixButton.class);
        conditionRemoveButton.setId(removeButtonId);
        conditionRemoveButton.setIcon(VaadinIcon.TRASH.create());
        conditionRemoveButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);

        conditionRemoveButton.addClickListener(clickEvent -> {
            removeFilterComponent(filterComponent);
            refreshCurrentConfigurationLayout();
            apply();
        });

        return conditionRemoveButton;
    }

    protected void removeFilterComponent(FilterComponent filterComponent) {
        LogicalFilterComponent<?> rootLogicalComponent = getCurrentConfiguration().getRootLogicalFilterComponent();
        if (filterComponent instanceof SingleFilterComponent) {
            getCurrentConfiguration()
                    .resetFilterComponentDefaultValue(((SingleFilterComponent<?>) filterComponent).getParameterName());
        }
        rootLogicalComponent.remove(filterComponent);
        getCurrentConfiguration().setFilterComponentModified(filterComponent, false);
    }

    protected void updateRootLayoutSummaryText() {
        StringBuilder stringBuilder = new StringBuilder(getConfigurationName(getEmptyConfiguration()));
        if (!getEmptyConfiguration().equals(getCurrentConfiguration())) {
            stringBuilder.append(" : ")
                    .append(getConfigurationName(getCurrentConfiguration()));
        }

        setSummaryText(stringBuilder.toString());
    }

    protected String getConfigurationName(Configuration configuration) {
        String caption = configuration.getName();
        if (caption == null) {
            caption = messages.findMessage(configuration.getId(), null);
            if (caption == null) {
                caption = configuration.getId();
            }
        }

        return caption;
    }

    protected void updateDataLoaderCondition() {
        if (dataLoader != null) {
            LogicalFilterComponent<?> logicalFilterComponent = getCurrentConfiguration().getRootLogicalFilterComponent();
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

    /**
     * Gets an empty configuration that is used when the user has not
     * selected any of the existing configurations.
     *
     * @return an empty configuration
     */
    public Configuration getEmptyConfiguration() {
        return emptyConfiguration;
    }

    /**
     * Gets a configuration by id.
     *
     * @param id the configuration id
     * @return the configuration of {@code null} if not found
     */
    @Nullable
    public Configuration getConfiguration(String id) {
        return configurations.stream()
                .filter(configuration -> id.equals(configuration.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return a list of all configurations related to the filter
     */
    public List<Configuration> getConfigurations() {
        return Collections.unmodifiableList(configurations);
    }

    /**
     * Adds design-time configuration with given id and name. A configuration is a set
     * of {@link FilterComponent}s. The configuration does not store a reference to all
     * components, but stores a reference only to the root element {@link LogicalFilterComponent}
     * from which the rest {@link FilterComponent}s can be obtained. The root
     * {@link LogicalFilterComponent} is generated with a {@link LogicalFilterComponent.Operation#AND}
     * operation.
     * <p>
     * The configuration defined in XML is a {@link DesignTimeConfiguration}.
     *
     * @param id   a configuration id. Must be unique within this filter
     * @param name a configuration name
     * @return {@link DesignTimeConfiguration}
     * @see LogicalFilterComponent
     */
    public DesignTimeConfiguration addConfiguration(String id, @Nullable String name) {
        return addConfiguration(id, name, LogicalFilterComponent.Operation.AND);
    }

    /**
     * Adds design-time configuration with given id and name. A configuration is a set
     * of {@link FilterComponent}s. The configuration does not store a reference to all
     * components, but stores a reference only to the root element {@link LogicalFilterComponent}
     * from which the rest {@link FilterComponent}s can be obtained. The root
     * {@link LogicalFilterComponent} is generated with a given operation.
     * <p>
     * The configuration defined in XML is a {@link DesignTimeConfiguration}.
     *
     * @param id            a configuration id. Must be unique within this filter
     * @param name          a configuration name
     * @param rootOperation an operation of root {@link LogicalFilterComponent}
     * @return {@link DesignTimeConfiguration}
     */
    public DesignTimeConfiguration addConfiguration(String id, @Nullable String name,
                                                    LogicalFilterComponent.Operation rootOperation) {
        LogicalFilterComponent<?> rootComponent = createConfigurationRootLogicalFilterComponent(rootOperation);
        DesignTimeConfiguration newConfiguration = new DesignTimeConfiguration(id, name, rootComponent, this);

        addConfiguration(newConfiguration);

        return newConfiguration;
    }

    /**
     * Adds a configuration to the filter.
     *
     * @param configuration configuration to add
     * @see DesignTimeConfiguration
     * @see RunTimeConfiguration
     */
    public void addConfiguration(Configuration configuration) {
        configurations.add(configuration);
        addSelectConfigurationAction(configuration);
    }

    /**
     * Removes a configuration from filter.
     *
     * @param configuration configuration to remove
     */
    public void removeConfiguration(Configuration configuration) {
        if (configuration != getEmptyConfiguration()
                && !(configuration instanceof DesignTimeConfiguration)) {
            configurations.remove(configuration);
            configuration.getRootLogicalFilterComponent().getElement().removeFromParent();

            if (configuration == getCurrentConfiguration()) {
                setCurrentConfigurationInternal(getEmptyConfiguration(), false);
                apply();
            } else {
                updateSelectConfigurationDropdown();
            }
        }
    }

    protected void updateSelectConfigurationDropdown() {
        if (applyButton == null) {
            return;
        }

        applyButton.removeAll();
        initSelectConfigurationDropdown();
        for (Configuration configuration : getConfigurations()) {
            addSelectConfigurationAction(configuration);
        }
    }

    protected void addSelectConfigurationAction(Configuration configuration) {
        Action configurationAction = createConfigurationAction(configuration);
        applyButton.addItem(configurationAction.getId(), configurationAction);
    }

    protected Action createConfigurationAction(Configuration configuration) {
        return new BaseAction("genericFilter_select_" + configuration.getId())
                .withText(getConfigurationName(configuration))
                .withHandler(actionPerformedEvent -> {
                    setCurrentConfigurationInternal(configuration, true);
                    apply();
                });
    }

    /**
     * Adds a condition to the filter. A condition is a {@link FilterComponent} that is
     * not initially added to any of the configurations, but the user can select this
     * component in the {@link AddConditionView} in the {@code Conditions} section and
     * add it to the {@link RunTimeConfiguration}.
     *
     * @param filterComponent a filter component to add to conditions
     * @see FilterComponent
     * @see AddConditionView
     * @see RunTimeConfiguration
     */
    public void addCondition(FilterComponent filterComponent) {
        if (conditions == null) {
            conditions = new ArrayList<>();
        }

        conditions.add(filterComponent);
    }

    /**
     * @return a list of all conditions related to the filter
     */
    public List<FilterComponent> getConditions() {
        return conditions != null
                ? Collections.unmodifiableList(conditions)
                : Collections.emptyList();
    }

    /**
     * Removes a condition from filter.
     *
     * @param filterComponent a filter component to remove from conditions
     */
    public void removeCondition(FilterComponent filterComponent) {
        if (conditions == null) {
            return;
        }

        conditions.remove(filterComponent);

        if (conditions.isEmpty()) {
            conditions = null;
        }
    }

    public Registration addConfigurationChangeListener(ComponentEventListener<ConfigurationChangeEvent> listener) {
        return getEventBus().addListener(ConfigurationChangeEvent.class, listener);
    }

    public Registration addConfigurationRefreshListener(ComponentEventListener<ConfigurationRefreshEvent> listener) {
        return getEventBus().addListener(ConfigurationRefreshEvent.class, listener);
    }

    public void loadConfigurationsAndApplyDefault() {
        Map<Configuration, Boolean> configurationsMap = genericFilterSupport.getConfigurationsMap(this);
        boolean defaultForAllConfigurationApplied = false;

        for (Map.Entry<Configuration, Boolean> entry : configurationsMap.entrySet()) {
            addConfiguration(entry.getKey());

            if (!defaultForAllConfigurationApplied && entry.getValue()) {
                setCurrentConfiguration(entry.getKey());
                defaultForAllConfigurationApplied = true;
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void clearValues() {
        if (rootLogicalFilterComponent == null
                || rootLogicalFilterComponent.getFilterComponents().isEmpty()) {
            return;
        }

        for (FilterComponent component : rootLogicalFilterComponent.getFilterComponents()) {
            if (component instanceof SingleFilterComponentBase singleFilterComponent) {
                singleFilterComponent.setValue(getCurrentConfiguration()
                        .getFilterComponentDefaultValue(singleFilterComponent.getParameterName()));
                getDataLoader().removeParameter(singleFilterComponent.getParameterName());
            }
        }
    }

    /**
     * Event sent when the {@link Configuration} is changed.
     */
    public static class ConfigurationChangeEvent extends ComponentEvent<GenericFilter> {

        protected final Configuration newConfiguration;
        protected final Configuration previousConfiguration;

        public ConfigurationChangeEvent(GenericFilter source,
                                        Configuration newConfiguration,
                                        @Nullable Configuration previousConfiguration,
                                        boolean fromClient) {
            super(source, fromClient);
            this.newConfiguration = newConfiguration;
            this.previousConfiguration = previousConfiguration;
        }

        /**
         * @return new configuration value
         */
        public Configuration getNewConfiguration() {
            return newConfiguration;
        }

        /**
         * @return previous configuration value
         */
        @Nullable
        public Configuration getPreviousConfiguration() {
            return previousConfiguration;
        }
    }

    /**
     * Event sent when the {@link Configuration} is updated.
     */
    public static class ConfigurationRefreshEvent extends ComponentEvent<GenericFilter> {

        protected final Configuration configuration;

        public ConfigurationRefreshEvent(GenericFilter source,
                                         Configuration configuration,
                                         boolean fromClient) {
            super(source, fromClient);
            this.configuration = configuration;
        }

        /**
         * @return updatedConfiguration
         */
        public Configuration getUpdatedConfiguration() {
            return configuration;
        }

    }

    public static class OpenedChangeEvent extends ComponentEvent<GenericFilter> {
        private final boolean opened;

        public OpenedChangeEvent(GenericFilter source, boolean fromClient) {
            super(source, fromClient);

            this.opened = source.isOpened();
        }

        public boolean isOpened() {
            return opened;
        }
    }
}
