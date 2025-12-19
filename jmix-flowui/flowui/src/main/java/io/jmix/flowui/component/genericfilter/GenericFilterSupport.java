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

package io.jmix.flowui.component.genericfilter;

import com.google.common.collect.ImmutableSet;
import com.vaadin.flow.component.Composite;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.genericfilter.*;
import io.jmix.flowui.app.filter.condition.FilterConditionDetailView;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.filter.SingleFilterComponentBase;
import io.jmix.flowui.component.genericfilter.configuration.AbstractConfigurationDetail;
import io.jmix.flowui.component.genericfilter.configuration.FilterConfigurationDetail;
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration;
import io.jmix.flowui.component.genericfilter.configuration.UiDataFilterConfigurationDetail;
import io.jmix.flowui.component.genericfilter.model.FilterConfigurationModel;
import io.jmix.flowui.component.genericfilter.model.GenericFilterConfigurationConverter;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.facet.SettingsFacet;
import io.jmix.flowui.facet.settings.UiComponentSettings;
import io.jmix.flowui.facet.settings.component.GenericFilterSettings;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;

import static io.jmix.flowui.component.genericfilter.FilterUtils.generateFilterPath;

/**
 * Support bean that provides helper methods to work with {@link GenericFilter}.
 */
@Internal
@Component("flowui_GenericFilterSupport")
public class GenericFilterSupport {

    private static final Logger log = LoggerFactory.getLogger(GenericFilterSupport.class);

    protected static final String CONFIGURATION_CONTAINER_ID = "configurationDc";

    protected final Actions actions;
    protected final UiComponents uiComponents;

    @Nullable
    protected FilterConfigurationPersistence configurationPersistence;

    protected GenericFilterConfigurationConverter genericFilterConfigurationConverter;
    protected CurrentUserSubstitution currentUserSubstitution;
    protected DataComponents dataComponents;
    protected Metadata metadata;

    public GenericFilterSupport(Actions actions,
                                UiComponents uiComponents,
                                @Nullable FilterConfigurationPersistence configurationPersistence,
                                GenericFilterConfigurationConverter genericFilterConfigurationConverter,
                                CurrentUserSubstitution currentUserSubstitution,
                                DataComponents dataComponents,
                                Metadata metadata) {
        this.actions = actions;
        this.uiComponents = uiComponents;
        this.configurationPersistence = configurationPersistence;
        this.genericFilterConfigurationConverter = genericFilterConfigurationConverter;
        this.currentUserSubstitution = currentUserSubstitution;
        this.dataComponents = dataComponents;
        this.metadata = metadata;
    }

    public List<GenericFilterAction<?>> getDefaultFilterActions(GenericFilter filter) {
        List<GenericFilterAction<?>> filterActions = new ArrayList<>();
        for (String actionId : getDefaultFilterActionIds()) {
            filterActions.add(createFilterAction(actionId, filter));
        }
        return filterActions;
    }

    public Configuration saveCurrentFilterConfiguration(Configuration configuration,
                                                        boolean isNewConfiguration,
                                                        LogicalFilterComponent<?> rootFilterComponent,
                                                        AbstractConfigurationDetail configurationDetail) {
        if (configurationPersistence == null) {
            String id = "";
            String name = "";

            if (configurationDetail instanceof FilterConfigurationDetail) {
                id = ((FilterConfigurationDetail) configurationDetail).getConfigurationId();
                name = ((FilterConfigurationDetail) configurationDetail).getConfigurationName();
            }

            return initFilterConfiguration(id, name, configuration, isNewConfiguration, rootFilterComponent);
        } else {
            FilterConfigurationModel configurationModel = getFilterConfigurationModel(configuration, configurationDetail);

            Configuration resultConfiguration = initFilterConfiguration(configurationModel.getConfigurationId(),
                    configurationModel.getName(), configuration, isNewConfiguration, rootFilterComponent);
            resultConfiguration.setAvailableForAllUsers(configurationModel.getUsername() == null);

            saveConfigurationModel(resultConfiguration, configurationModel);

            return resultConfiguration;
        }
    }

    public void removeCurrentFilterConfiguration(GenericFilter filter) {
        FilterConfigurationModel configurationModel =
                loadFilterConfigurationModel(filter, filter.getCurrentConfiguration().getId());
        if (configurationModel != null) {
            getConfigurationPersistence().remove(configurationModel);
        }

        filter.removeConfiguration(filter.getCurrentConfiguration());
    }

    public AbstractConfigurationDetail createFilterConfigurationDetail(
            DialogWindow<? extends FilterConditionDetailView<?>> dialog,
            boolean isNewConfiguration,
            Configuration currentConfiguration) {
        if (configurationPersistence == null) {
            FilterConfigurationDetail configurationDetail = uiComponents.create(FilterConfigurationDetail.class);
            initFilterConfigurationDetail(configurationDetail, isNewConfiguration, currentConfiguration);

            return configurationDetail;
        } else {
            FilterConfigurationModel configurationModel = loadFilterConfigurationModel(isNewConfiguration, currentConfiguration);
            InstanceContainer<FilterConfigurationModel> configurationDc = registerConfigurationDc(configurationModel,
                    ViewControllerUtils.getViewData(dialog.getView()));

            UiDataFilterConfigurationDetail configurationDetail =
                    uiComponents.create(UiDataFilterConfigurationDetail.class);
            boolean defaultFormMeFieldVisible = isDefaultForMeFieldVisible(currentConfiguration, configurationModel);
            configurationDetail.setDefaultForMeFieldVisible(defaultFormMeFieldVisible);
            configurationDetail.setConfigurationDc(configurationDc);
            configurationDetail.setFilter(currentConfiguration.getOwner());

            dialog.addAfterOpenListener(afterOpenEvent -> {
                //lazy initialization is required because the state of the components depends on the data container
                configurationDetail.init();
                afterOpenEvent.getView().setReadOnly(configurationDetail.isViewReadOnly());
            });

            dialog.addAfterCloseListener(event -> {
                if (event.closedWith(StandardOutcome.SAVE)) {
                    configurationDetail.initUsername();
                }
            });

            return configurationDetail;
        }
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map<String, Object> initConfigurationValuesMap(Configuration configuration) {
        HashMap<String, Object> valuesMap = new HashMap<>();
        LogicalFilterComponent<?> rootLogicalComponent = configuration.getRootLogicalFilterComponent();

        for (FilterComponent filterComponent : rootLogicalComponent.getFilterComponents()) {
            if (filterComponent instanceof SingleFilterComponentBase) {
                SingleFilterComponentBase singleFilterComponent = ((SingleFilterComponentBase<?>) filterComponent);
                String parameterName = singleFilterComponent.getParameterName();
                valuesMap.put(parameterName, singleFilterComponent.getValue());
            }
        }

        return valuesMap;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void resetConfigurationValuesMap(Configuration configuration, Map<String, Object> valuesMap) {
        LogicalFilterComponent<?> rootLogicalComponent = configuration.getRootLogicalFilterComponent();

        for (FilterComponent filterComponent : rootLogicalComponent.getFilterComponents()) {
            if (filterComponent instanceof SingleFilterComponentBase) {
                SingleFilterComponentBase singleFilterComponent = (SingleFilterComponentBase) filterComponent;
                singleFilterComponent.setValue(valuesMap.get(singleFilterComponent.getParameterName()));
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void refreshConfigurationValuesMap(Configuration configuration, Map<String, Object> valuesMap) {
        LogicalFilterComponent<?> rootLogicalComponent = configuration.getRootLogicalFilterComponent();

        for (FilterComponent filterComponent : rootLogicalComponent.getFilterComponents()) {
            if (filterComponent instanceof SingleFilterComponentBase) {
                SingleFilterComponentBase singleFilterComponent = (SingleFilterComponentBase) filterComponent;

                String parameterName = singleFilterComponent.getParameterName();
                Object value = valuesMap.get(parameterName);
                Object defaultValue = configuration.getFilterComponentDefaultValue(parameterName);

                if (value == null && defaultValue != null) {
                    singleFilterComponent.setValue(defaultValue);
                } else {
                    try {
                        singleFilterComponent.setValue(value);
                    } catch (ClassCastException e) {
                        singleFilterComponent.setValue(defaultValue);
                    }
                }
            }
        }
    }

    public void refreshConfigurationDefaultValues(Configuration configuration) {
        configuration.resetAllDefaultValues();
        LogicalFilterComponent<?> rootLogicalComponent = configuration.getRootLogicalFilterComponent();

        for (FilterComponent filterComponent : rootLogicalComponent.getFilterComponents()) {
            if (filterComponent instanceof SingleFilterComponentBase) {
                configuration.setFilterComponentDefaultValue(
                        ((SingleFilterComponentBase<?>) filterComponent).getParameterName(),
                        ((SingleFilterComponentBase<?>) filterComponent).getValue()
                );
            }
        }
    }

    protected Configuration initFilterConfiguration(String id,
                                                    String name,
                                                    Configuration existedConfiguration,
                                                    boolean isNewConfiguration,
                                                    LogicalFilterComponent<?> rootFilterComponent) {
        Configuration resultConfiguration;
        GenericFilter owner = existedConfiguration.getOwner();

        if (isNewConfiguration) {
            resultConfiguration = new RunTimeConfiguration(id, rootFilterComponent, owner);
        } else if (!existedConfiguration.getId().equals(id)) {
            resultConfiguration = new RunTimeConfiguration(id, rootFilterComponent, owner);
            owner.removeConfiguration(existedConfiguration);
        } else {
            resultConfiguration = existedConfiguration;
            resultConfiguration.setRootLogicalFilterComponent(rootFilterComponent);
        }

        resultConfiguration.setName(name);
        resultConfiguration.setAvailableForAllUsers(existedConfiguration.isAvailableForAllUsers());

        return resultConfiguration;
    }

    protected void initFilterConfigurationDetail(FilterConfigurationDetail filterConfigurationDetail,
                                                 boolean isNewConfiguration,
                                                 Configuration currentConfiguration) {
        filterConfigurationDetail.setConfigurationName(currentConfiguration.getName());

        if (!isNewConfiguration) {
            filterConfigurationDetail.setConfigurationId(currentConfiguration.getId());
        }
    }

    /**
     * Saves filter configuration to the database.
     * <p>
     * If passed {@link FilterConfigurationModel} object is null, creates a new one from
     * passed {@link Configuration} object.
     *
     * @param configuration      filter configuration
     * @param configurationModel entity representing filter configuration to save to the database
     */
    public void saveConfigurationModel(Configuration configuration, @Nullable FilterConfigurationModel configurationModel) {
        if (configurationModel == null) {
            configurationModel = createFilterConfigurationModel(configuration);
        }

        configurationModel = genericFilterConfigurationConverter.toConfigurationModel(configuration, configurationModel);
        getConfigurationPersistence().save(configurationModel);
    }

    protected Set<String> getDefaultFilterActionIds() {
        if (configurationPersistence == null)
            return ImmutableSet.of(
                    GenericFilterEditAction.ID,
                    GenericFilterCopyAction.ID,
                    GenericFilterClearValuesAction.ID
            );
        else
            return ImmutableSet.of(
                    GenericFilterSaveAction.ID,
                    GenericFilterSaveWithValuesAction.ID,
                    GenericFilterSaveAsAction.ID,
                    GenericFilterEditAction.ID,
                    GenericFilterRemoveAction.ID,
                    GenericFilterMakeDefaultAction.ID,
                    GenericFilterCopyAction.ID,
                    GenericFilterClearValuesAction.ID
            );
    }

    public Map<Configuration, Boolean> getConfigurationsMap(GenericFilter filter) {
        if (configurationPersistence == null)
            return Collections.emptyMap();

        TreeMap<Configuration, Boolean> map = new TreeMap<>();
        List<FilterConfigurationModel> configurationModels = loadFilterConfigurationModels(filter);

        for (FilterConfigurationModel configurationModel : configurationModels) {
            try {
                Configuration configuration =
                        genericFilterConfigurationConverter.toConfiguration(configurationModel, filter);
                map.put(configuration, configurationModel.getDefaultForAll());
            } catch (RuntimeException e) {
                log.warn("Cannot create filter for configuration '{}'.", configurationModel.getName(), e);
            }
        }

        return map;
    }

    protected GenericFilterAction<?> createFilterAction(String filterActionId,
                                                        GenericFilter filter) {
        GenericFilterAction<?> filterAction = actions.create(filterActionId);
        filterAction.setTarget(filter);
        return filterAction;
    }

    @Nullable
    public FilterConfigurationModel loadFilterConfigurationModel(GenericFilter filter, String configurationId) {
        String componentId = generateFilterPath(filter);
        String username = currentUserSubstitution.getEffectiveUser().getUsername();
        return getConfigurationPersistence().load(configurationId, componentId, username);
    }

    protected List<FilterConfigurationModel> loadFilterConfigurationModels(GenericFilter filter) {
        String filterComponentId = generateFilterPath(filter);
        String username = currentUserSubstitution.getEffectiveUser().getUsername();
        return getConfigurationPersistence().load(filterComponentId, username);
    }

    protected FilterConfigurationModel loadFilterConfigurationModel(boolean isNewConfiguration,
                                                                    Configuration currentConfiguration) {
        FilterConfigurationModel configurationModel = null;
        if (!isNewConfiguration) {
            configurationModel = loadFilterConfigurationModel(currentConfiguration.getOwner(),
                    currentConfiguration.getId());
        }

        if (configurationModel == null) {
            configurationModel = metadata.create(FilterConfigurationModel.class);
            configurationModel.setUsername(currentUserSubstitution.getEffectiveUser().getUsername());
        }

        return configurationModel;
    }

    protected boolean isDefaultForMeFieldVisible(Configuration currentConfiguration,
                                                 FilterConfigurationModel configurationModel) {
        Composite<?> currentOwner = FilterUtils.findCurrentOwner(currentConfiguration.getOwner());
        if (UiComponentUtils.getComponentId(currentConfiguration.getOwner()).isEmpty() || currentOwner == null) {
            return false;
        }

        SettingsFacet<?> settingsFacet = FilterUtils.getFacet(currentOwner, SettingsFacet.class);

        if (settingsFacet == null) {
            return false;
        }

        UiComponentSettings<?> settings = settingsFacet.getSettings();

        if (settings != null) {
            settings.getSettings(UiComponentUtils.getComponentId(currentConfiguration.getOwner()).get(),
                            GenericFilterSettings.class)
                    .ifPresent(genericFilterSettings -> {
                        String defaultConfigurationId = genericFilterSettings.getDefaultConfigurationId();
                        if (defaultConfigurationId != null) {
                            boolean defaultForMe =
                                    defaultConfigurationId.equals(configurationModel.getConfigurationId());

                            configurationModel.setDefaultForMe(defaultForMe);
                        }
                    });
        }

        return true;
    }

    protected InstanceContainer<FilterConfigurationModel> registerConfigurationDc(FilterConfigurationModel configurationModel,
                                                                                  ViewData viewData) {
        InstanceContainer<FilterConfigurationModel> configurationDc =
                dataComponents.createInstanceContainer(FilterConfigurationModel.class);
        configurationDc.setItem(configurationModel);
        viewData.registerContainer(CONFIGURATION_CONTAINER_ID, configurationDc);

        return configurationDc;
    }

    protected FilterConfigurationModel getFilterConfigurationModel(Configuration configuration,
                                                                   AbstractConfigurationDetail configurationDetail) {
        GenericFilter genericFilter = configuration.getOwner();
        FilterConfigurationModel configurationModel;

        if (configurationDetail instanceof UiDataFilterConfigurationDetail dataConfigurationDetail) {
            InstanceContainer<FilterConfigurationModel> configurationDc = dataConfigurationDetail.getConfigurationDc();

            configurationModel = configurationDc.getItem();

            if (UiComponentUtils.getComponentId(genericFilter).isPresent()
                    && dataConfigurationDetail.isDefaultForMeFieldVisible()) {
                SettingsFacet<?> settingsFacet = FilterUtils.getFacet(genericFilter, SettingsFacet.class);

                if (settingsFacet != null) {
                    saveFilterSettings(settingsFacet, UiComponentUtils.getComponentId(genericFilter).get(), configurationModel);
                }
            }
        } else {
            configurationModel = loadFilterConfigurationModel(genericFilter, configuration.getId());
        }

        if (configurationModel == null) {
            configurationModel = createFilterConfigurationModel(configuration);
        }

        return configurationModel;
    }

    protected FilterConfigurationModel createFilterConfigurationModel(Configuration configuration) {
        FilterConfigurationModel configurationModel = metadata.create(FilterConfigurationModel.class);

        configurationModel.setConfigurationId(configuration.getId());
        configurationModel.setName(configuration.getName());
        configurationModel.setUsername(currentUserSubstitution.getEffectiveUser().getUsername());

        return configurationModel;
    }

    protected void saveFilterSettings(SettingsFacet<?> settingsFacet,
                                      String filterId,
                                      FilterConfigurationModel configurationModel) {
        UiComponentSettings<?> settings = settingsFacet.getSettings();
        Preconditions.checkNotNullArgument(settings,
                "%s is not attached to the view", SettingsFacet.class.getSimpleName());

        GenericFilterSettings genericFilterSettings = settings.getSettingsOrCreate(filterId, GenericFilterSettings.class);
        if (Objects.equals(genericFilterSettings.getDefaultConfigurationId(), configurationModel.getConfigurationId())
                && !configurationModel.getDefaultForMe()) {
            genericFilterSettings.setDefaultConfigurationId(null);
        }

        if (configurationModel.getDefaultForMe()) {
            genericFilterSettings.setDefaultConfigurationId(configurationModel.getConfigurationId());
        }

        settings.put(genericFilterSettings);
    }

    private FilterConfigurationPersistence getConfigurationPersistence() {
        if (configurationPersistence == null) {
            throw new IllegalStateException("FilterConfigurationPersistence is not available");
        }
        return configurationPersistence;
    }
}
