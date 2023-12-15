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

package io.jmix.flowuidata.genericfilter;

import com.google.common.collect.ImmutableSet;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.event.UserRemovedEvent;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.genericfilter.GenericFilterClearValuesAction;
import io.jmix.flowui.action.genericfilter.GenericFilterCopyAction;
import io.jmix.flowui.action.genericfilter.GenericFilterEditAction;
import io.jmix.flowui.app.filter.condition.FilterConditionDetailView;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.GenericFilterSupport;
import io.jmix.flowui.component.genericfilter.configuration.AbstractConfigurationDetail;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.facet.SettingsFacet;
import io.jmix.flowui.facet.settings.ViewSettings;
import io.jmix.flowui.facet.settings.component.GenericFilterSettings;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowuidata.action.genericfilter.*;
import io.jmix.flowuidata.component.genericfilter.configuration.UiDataFilterConfigurationDetail;
import io.jmix.flowuidata.entity.FilterConfiguration;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;

import static io.jmix.flowui.component.genericfilter.FilterUtils.generateFilterPath;

@Internal
public class UiDataGenericFilterSupport extends GenericFilterSupport {

    protected static final String CONFIGURATION_CONTAINER_ID = "configurationDc";

    private static final Logger log = LoggerFactory.getLogger(UiDataGenericFilterSupport.class);

    protected DataManager dataManager;
    protected GenericFilterConfigurationConverter genericFilterConfigurationConverter;
    protected CurrentUserSubstitution currentUserSubstitution;
    protected DataComponents dataComponents;
    protected Metadata metadata;

    public UiDataGenericFilterSupport(Actions actions,
                                      UiComponents uiComponents,
                                      DataManager dataManager,
                                      GenericFilterConfigurationConverter genericFilterConfigurationConverter,
                                      CurrentUserSubstitution currentUserSubstitution,
                                      DataComponents dataComponents,
                                      Metadata metadata) {
        super(actions, uiComponents);
        this.dataManager = dataManager;
        this.genericFilterConfigurationConverter = genericFilterConfigurationConverter;
        this.currentUserSubstitution = currentUserSubstitution;
        this.dataComponents = dataComponents;
        this.metadata = metadata;
    }

    @Override
    public Map<Configuration, Boolean> getConfigurationsMap(GenericFilter filter) {
        TreeMap<Configuration, Boolean> map = new TreeMap<>();
        List<FilterConfiguration> configurationModels = loadFilterConfigurationModels(filter);

        for (FilterConfiguration configurationModel : configurationModels) {
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

    @Override
    public void removeCurrentFilterConfiguration(GenericFilter filter) {
        FilterConfiguration configurationModel =
                loadFilterConfigurationModel(filter, filter.getCurrentConfiguration().getId());
        if (configurationModel != null) {
            dataManager.remove(configurationModel);
        }

        super.removeCurrentFilterConfiguration(filter);
    }

    @Override
    public AbstractConfigurationDetail createFilterConfigurationDetail(
            DialogWindow<? extends FilterConditionDetailView<?>> dialog,
            boolean isNewConfiguration,
            Configuration currentConfiguration) {
        FilterConfiguration configurationModel = loadFilterConfigurationModel(isNewConfiguration, currentConfiguration);
        InstanceContainer<FilterConfiguration> configurationDc = registerConfigurationDc(configurationModel,
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

    @Override
    public Configuration saveCurrentFilterConfiguration(Configuration configuration,
                                                        boolean isNewConfiguration,
                                                        LogicalFilterComponent<?> rootFilterComponent,
                                                        AbstractConfigurationDetail configurationDetail) {
        FilterConfiguration configurationModel = getFilterConfigurationModel(configuration, configurationDetail);

        Configuration resultConfiguration = initFilterConfiguration(configurationModel.getConfigurationId(),
                configurationModel.getName(), configuration, isNewConfiguration, rootFilterComponent);
        resultConfiguration.setAvailableForAllUsers(configurationModel.getUsername() == null);

        saveConfigurationModel(resultConfiguration, configurationModel);

        return resultConfiguration;
    }

    public void saveConfigurationModel(Configuration configuration, @Nullable FilterConfiguration configurationModel) {
        if (configurationModel == null) {
            configurationModel = createFilterConfigurationModel(configuration);
        }

        configurationModel = genericFilterConfigurationConverter.toConfigurationModel(configuration, configurationModel);
        dataManager.save(configurationModel);
    }

    @Override
    protected Set<String> getDefaultFilterActionIds() {
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

    @Nullable
    public FilterConfiguration loadFilterConfigurationModel(GenericFilter filter, String configurationId) {
        String componentId = generateFilterPath(filter);
        String username = currentUserSubstitution.getEffectiveUser().getUsername();
        return dataManager.load(FilterConfiguration.class)
                .condition(LogicalCondition.and()
                        .add(PropertyCondition.equal("configurationId", configurationId))
                        .add(PropertyCondition.equal("componentId", componentId))
                        .add(LogicalCondition.or()
                                .add(PropertyCondition.isSet("username", false))
                                .add(PropertyCondition.equal("username", username))))
                .optional()
                .orElse(null);
    }

    protected List<FilterConfiguration> loadFilterConfigurationModels(GenericFilter filter) {
        String filterComponentId = generateFilterPath(filter);
        String username = currentUserSubstitution.getEffectiveUser().getUsername();

        return dataManager.load(FilterConfiguration.class)
                .condition(LogicalCondition.and()
                        .add(PropertyCondition.equal("componentId", filterComponentId))
                        .add(LogicalCondition.or()
                                .add(PropertyCondition.isSet("username", false))
                                .add(PropertyCondition.equal("username", username))))
                .list();
    }

    protected FilterConfiguration loadFilterConfigurationModel(boolean isNewConfiguration,
                                                               Configuration currentConfiguration) {
        FilterConfiguration configurationModel = null;
        if (!isNewConfiguration) {
            configurationModel = loadFilterConfigurationModel(currentConfiguration.getOwner(),
                    currentConfiguration.getId());
        }

        if (configurationModel == null) {
            configurationModel = metadata.create(FilterConfiguration.class);
            configurationModel.setUsername(currentUserSubstitution.getEffectiveUser().getUsername());
        }

        return configurationModel;
    }

    @SuppressWarnings({"SpringEventListenerInspection"})
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    protected void onUserRemove(UserRemovedEvent event) {
        List<FilterConfiguration> configurations = dataManager.load(FilterConfiguration.class)
                .query("e.username = :username")
                .parameter("username", event.getUsername())
                .list();
        dataManager.remove(configurations.toArray());
    }

    protected boolean isDefaultForMeFieldVisible(Configuration currentConfiguration,
                                                 FilterConfiguration configurationModel) {
        View<?> currentView = UiComponentUtils.findView(currentConfiguration.getOwner());
        if (currentConfiguration.getOwner().getId().isPresent() && currentView != null) {
            SettingsFacet settingsFacet = ViewControllerUtils.getViewFacet(currentView, SettingsFacet.class);

            if (settingsFacet != null) {
                ViewSettings settings = settingsFacet.getSettings();

                if (settings != null) {
                    settings.getSettings(currentConfiguration.getOwner().getId().get(), GenericFilterSettings.class)
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
        }
        return false;
    }

    protected InstanceContainer<FilterConfiguration> registerConfigurationDc(FilterConfiguration configurationModel,
                                                                             ViewData viewData) {
        InstanceContainer<FilterConfiguration> configurationDc =
                dataComponents.createInstanceContainer(FilterConfiguration.class);
        configurationDc.setItem(configurationModel);
        viewData.registerContainer(CONFIGURATION_CONTAINER_ID, configurationDc);

        return configurationDc;
    }

    protected FilterConfiguration getFilterConfigurationModel(Configuration configuration,
                                                              AbstractConfigurationDetail configurationDetail) {
        GenericFilter genericFilter = configuration.getOwner();
        FilterConfiguration configurationModel;

        if (configurationDetail instanceof UiDataFilterConfigurationDetail dataConfigurationDetail) {
            InstanceContainer<FilterConfiguration> configurationDc = dataConfigurationDetail.getConfigurationDc();

            configurationModel = configurationDc.getItem();

            if (genericFilter.getId().isPresent()
                    && dataConfigurationDetail.isDefaultForMeFieldVisible()) {
                SettingsFacet settingsFacet = ViewControllerUtils
                        .getViewFacet(UiComponentUtils.getView(genericFilter), SettingsFacet.class);

                if (settingsFacet != null) {
                    saveFilterSettings(settingsFacet, genericFilter.getId().get(), configurationModel);
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

    protected FilterConfiguration createFilterConfigurationModel(Configuration configuration) {
        FilterConfiguration configurationModel = metadata.create(FilterConfiguration.class);

        configurationModel.setConfigurationId(configuration.getId());
        configurationModel.setName(configuration.getName());
        configurationModel.setUsername(currentUserSubstitution.getEffectiveUser().getUsername());

        return configurationModel;
    }

    protected void saveFilterSettings(SettingsFacet settingsFacet,
                                      String filterId,
                                      FilterConfiguration configurationModel) {
        ViewSettings settings = settingsFacet.getSettings();
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
}
