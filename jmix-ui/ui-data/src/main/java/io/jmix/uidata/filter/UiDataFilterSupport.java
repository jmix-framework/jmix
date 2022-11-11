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

package io.jmix.uidata.filter;

import com.google.common.collect.ImmutableSet;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.event.UserRemovedEvent;
import io.jmix.ui.Fragments;
import io.jmix.ui.action.filter.FilterAction;
import io.jmix.ui.action.filter.FilterClearValuesAction;
import io.jmix.ui.action.filter.FilterCopyAction;
import io.jmix.ui.action.filter.FilterEditAction;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.LogicalFilterComponent;
import io.jmix.ui.component.filter.FilterSupport;
import io.jmix.ui.model.DataComponents;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.settings.ScreenSettings;
import io.jmix.ui.settings.component.FilterSettings;
import io.jmix.ui.settings.facet.ScreenSettingsFacet;
import io.jmix.uidata.action.filter.*;
import io.jmix.uidata.app.filter.configuration.UiDataFilterConfigurationModelFragment;
import io.jmix.uidata.entity.FilterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Nullable;
import java.util.*;

import static io.jmix.ui.component.filter.FilterUtils.generateFilterPath;

@Internal
public class UiDataFilterSupport extends FilterSupport {

    protected static final String CONFIGURATION_CONTAINER_ID = "configurationDc";

    private static final Logger log = LoggerFactory.getLogger(UiDataFilterSupport.class);

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected FilterConfigurationConverter filterConfigurationConverter;
    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected DataComponents dataComponents;
    @Autowired
    protected Metadata metadata;

    @Override
    public Map<Filter.Configuration, Boolean> getConfigurationsMap(Filter filter) {
        Map<Filter.Configuration, Boolean> map = new TreeMap<>();
        List<FilterConfiguration> configurationModels = loadFilterConfigurationModels(filter);

        for (FilterConfiguration configurationModel : configurationModels) {
            try {
                Filter.Configuration configuration =
                        filterConfigurationConverter.toConfiguration(configurationModel, filter);
                map.put(configuration, configurationModel.getDefaultForAll());
            } catch (RuntimeException e) {
                log.warn("Cannot create filter for configuration '{}'.", configurationModel.getName(), e);
            }

        }
        return map;
    }

    @Override
    public void removeCurrentFilterConfiguration(Filter filter) {
        FilterConfiguration configurationModel =
                loadFilterConfigurationModel(filter, filter.getCurrentConfiguration().getId());
        if (configurationModel != null) {
            dataManager.remove(configurationModel);
        }

        super.removeCurrentFilterConfiguration(filter);
    }

    @Override
    public ScreenFragment createFilterConfigurationFragment(FrameOwner owner,
                                                            boolean isNewConfiguration,
                                                            Filter.Configuration currentConfiguration) {
        FilterConfiguration configurationModel = loadFilterConfigurationModel(isNewConfiguration, currentConfiguration);
        boolean defaultForMeFieldVisible = isDefaultForMeFieldVisible(currentConfiguration, configurationModel);
        registerConfigurationDc(configurationModel, owner);

        Fragments fragments = UiControllerUtils.getScreenContext(owner).getFragments();
        UiDataFilterConfigurationModelFragment fragment = fragments.create(owner,
                UiDataFilterConfigurationModelFragment.class);

        fragment.setDefaultForMeFieldVisible(defaultForMeFieldVisible);

        return fragment;
    }

    @Nullable
    public FilterConfiguration loadFilterConfigurationModel(Filter filter, String configurationId) {
        String componentId = generateFilterPath(filter);
        String username = currentAuthentication.getUser().getUsername();
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

    @Override
    public Filter.Configuration saveCurrentFilterConfiguration(Filter.Configuration configuration,
                                                               boolean isNewConfiguration,
                                                               LogicalFilterComponent rootFilterComponent,
                                                               ScreenFragment configurationFragment) {
        FilterConfiguration configurationModel = getFragmentFilterConfigurationModel(configuration, configurationFragment);

        Filter.Configuration resultConfiguration = initFilterConfiguration(configurationModel.getConfigurationId(),
                configurationModel.getName(), configuration, isNewConfiguration, rootFilterComponent);

        saveConfigurationModel(resultConfiguration, configurationModel);
        return resultConfiguration;
    }

    public void saveConfigurationModel(Filter.Configuration configuration,
                                       @Nullable FilterConfiguration configurationModel) {
        if (configurationModel == null) {
            configurationModel = createFilterConfigurationModel(configuration);
        }
        configurationModel = filterConfigurationConverter.toConfigurationModel(configuration, configurationModel);
        dataManager.save(configurationModel);
    }

    @Override
    protected Set<Class<? extends FilterAction>> getDefaultFilterActionClasses() {
        return ImmutableSet.<Class<? extends FilterAction>>builder()
                .add(FilterSaveAction.class)
                .add(FilterSaveWithValuesAction.class)
                .add(FilterSaveAsAction.class)
                .add(FilterEditAction.class)
                .add(FilterRemoveAction.class)
                .add(FilterMakeDefaultAction.class)
                .add(FilterCopyAction.class)
                .add(FilterClearValuesAction.class)
                .build();
    }

    protected List<FilterConfiguration> loadFilterConfigurationModels(Filter filter) {
        String filterComponentId = generateFilterPath(filter);
        String username = currentAuthentication.getUser().getUsername();
        return dataManager.load(FilterConfiguration.class)
                .condition(LogicalCondition.and()
                        .add(PropertyCondition.equal("componentId", filterComponentId))
                        .add(LogicalCondition.or()
                                .add(PropertyCondition.isSet("username", false))
                                .add(PropertyCondition.equal("username", username))))
                .list();
    }

    protected FilterConfiguration loadFilterConfigurationModel(boolean isNewConfiguration,
                                                               Filter.Configuration currentConfiguration) {
        FilterConfiguration configurationModel = null;
        if (!isNewConfiguration) {
            configurationModel = loadFilterConfigurationModel(currentConfiguration.getOwner(),
                    currentConfiguration.getId());
        }

        if (configurationModel == null) {
            configurationModel = metadata.create(FilterConfiguration.class);
            configurationModel.setUsername(currentAuthentication.getUser().getUsername());
        }

        return configurationModel;
    }

    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    private void onUserRemove(UserRemovedEvent event) {
        List<FilterConfiguration> configurations = dataManager.load(FilterConfiguration.class)
                .query("e.username = :username")
                .parameter("username", event.getUsername())
                .list();
        dataManager.remove(configurations.toArray());
    }

    protected boolean isDefaultForMeFieldVisible(Filter.Configuration currentConfiguration,
                                                 FilterConfiguration configurationModel) {
        Frame filterFrame = currentConfiguration.getOwner().getFrame();
        if (currentConfiguration.getOwner().getId() != null && filterFrame != null) {
            ScreenSettingsFacet settingsFacet = UiControllerUtils.getFacet(filterFrame, ScreenSettingsFacet.class);

            if (settingsFacet != null) {
                ScreenSettings settings = settingsFacet.getSettings();
                if (settings != null) {
                    settings.getSettings(currentConfiguration.getOwner().getId(), FilterSettings.class)
                            .ifPresent(filterSettings -> {
                                String defaultConfigurationId = filterSettings.getDefaultConfigurationId();
                                if (defaultConfigurationId != null) {
                                    boolean defaultForMe = defaultConfigurationId
                                            .equals(configurationModel.getConfigurationId());
                                    configurationModel.setDefaultForMe(defaultForMe);
                                }
                            });
                }

                return true;
            }
        }

        return false;
    }

    protected void registerConfigurationDc(FilterConfiguration configurationModel,
                                           FrameOwner owner) {
        InstanceContainer<FilterConfiguration> configurationDc =
                dataComponents.createInstanceContainer(FilterConfiguration.class);
        configurationDc.setItem(configurationModel);
        ScreenData screenData = UiControllerUtils.getScreenData(owner);
        screenData.registerContainer(CONFIGURATION_CONTAINER_ID, configurationDc);
    }

    protected FilterConfiguration getFragmentFilterConfigurationModel(Filter.Configuration configuration,
                                                                      ScreenFragment configurationFragment) {
        Filter filter = configuration.getOwner();
        FilterConfiguration configurationModel;
        if (configurationFragment instanceof UiDataFilterConfigurationModelFragment) {
            ScreenData screenData = UiControllerUtils.getScreenData(configurationFragment.getHostController());
            InstanceContainer<FilterConfiguration> configurationDc = screenData.getContainer(CONFIGURATION_CONTAINER_ID);
            configurationModel = configurationDc.getItem();

            if (filter.getId() != null
                    && filter.getFrame() != null
                    && ((UiDataFilterConfigurationModelFragment) configurationFragment).getDefaultForMeFieldVisible()) {
                ScreenSettingsFacet settingsFacet = UiControllerUtils
                        .getFacet(filter.getFrame(), ScreenSettingsFacet.class);

                if (settingsFacet != null) {
                    saveFilterSettings(settingsFacet, filter.getId(), configurationModel);
                }
            }
        } else {
            configurationModel = loadFilterConfigurationModel(filter, configuration.getId());
        }

        if (configurationModel == null) {
            configurationModel = createFilterConfigurationModel(configuration);
        }

        return configurationModel;
    }

    protected FilterConfiguration createFilterConfigurationModel(Filter.Configuration configuration) {
        FilterConfiguration configurationModel = metadata.create(FilterConfiguration.class);
        configurationModel.setConfigurationId(configuration.getId());
        configurationModel.setName(configuration.getName());
        configurationModel.setUsername(currentAuthentication.getUser().getUsername());
        return configurationModel;
    }

    protected void saveFilterSettings(ScreenSettingsFacet screenSettingsFacet,
                                      String filterId,
                                      FilterConfiguration configurationModel) {
        ScreenSettings settings = screenSettingsFacet.getSettings();
        if (settings == null) {
            throw new IllegalStateException("ScreenSettingsFacet is not attached to the frame");
        }

        FilterSettings filterSettings = settings.getSettingsOrCreate(filterId, FilterSettings.class);
        if (Objects.equals(filterSettings.getDefaultConfigurationId(), configurationModel.getConfigurationId())
                && !configurationModel.getDefaultForMe()) {
            filterSettings.setDefaultConfigurationId(null);
        }

        if (configurationModel.getDefaultForMe()) {
            filterSettings.setDefaultConfigurationId(configurationModel.getConfigurationId());
        }

        settings.put(filterSettings);
    }
}
