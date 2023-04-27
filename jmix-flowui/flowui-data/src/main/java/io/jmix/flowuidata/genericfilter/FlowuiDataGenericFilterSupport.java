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
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.event.UserRemovedEvent;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.genericfilter.GenericFilterAction;
import io.jmix.flowui.action.genericfilter.GenericFilterClearValuesAction;
import io.jmix.flowui.action.genericfilter.GenericFilterCopyAction;
import io.jmix.flowui.action.genericfilter.GenericFilterEditAction;
import io.jmix.flowui.app.filter.condition.FilterConditionDetailView;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.GenericFilterSupport;
import io.jmix.flowui.component.genericfilter.configuration.AbstractConfigurationDetail;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowuidata.action.genericfilter.GenericFilterRemoveAction;
import io.jmix.flowuidata.action.genericfilter.GenericFilterSaveAction;
import io.jmix.flowuidata.action.genericfilter.GenericFilterSaveAsAction;
import io.jmix.flowuidata.action.genericfilter.GenericFilterSaveWithValuesAction;
import io.jmix.flowuidata.component.genericfilter.configuration.FlowuiDataFilterConfigurationDetail;
import io.jmix.flowuidata.entity.FilterConfiguration;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static io.jmix.flowui.component.genericfilter.FilterUtils.generateFilterPath;

@Internal
public class FlowuiDataGenericFilterSupport extends GenericFilterSupport {

    protected static final String CONFIGURATION_CONTAINER_ID = "configurationDc";

    private static final Logger log = LoggerFactory.getLogger(FlowuiDataGenericFilterSupport.class);

    protected DataManager dataManager;
    protected GenericFilterConfigurationConverter genericFilterConfigurationConverter;
    protected CurrentAuthentication currentAuthentication;
    protected DataComponents dataComponents;
    protected Metadata metadata;

    public FlowuiDataGenericFilterSupport(Actions actions,
                                          UiComponents uiComponents,
                                          DataManager dataManager,
                                          GenericFilterConfigurationConverter genericFilterConfigurationConverter,
                                          CurrentAuthentication currentAuthentication,
                                          DataComponents dataComponents,
                                          Metadata metadata) {
        super(actions, uiComponents);
        this.dataManager = dataManager;
        this.genericFilterConfigurationConverter = genericFilterConfigurationConverter;
        this.currentAuthentication = currentAuthentication;
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

        FlowuiDataFilterConfigurationDetail configurationDetail =
                uiComponents.create(FlowuiDataFilterConfigurationDetail.class);
        boolean defaultFormMeFieldVisible = isDefaultForMeFieldVisible(currentConfiguration, configurationModel);
        configurationDetail.setDefaultForMeFieldVisible(defaultFormMeFieldVisible);
        configurationDetail.setConfigurationDc(configurationDc);

        dialog.addAfterOpenListener(afterOpenEvent -> {
            //lazy initialization is required because the state of the components depends on the data container
            configurationDetail.initFields();
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
    protected Set<Class<? extends GenericFilterAction<?>>> getDefaultFilterActionClasses() {
        return ImmutableSet.of(
                GenericFilterSaveAction.class,
                GenericFilterSaveWithValuesAction.class,
                GenericFilterSaveAsAction.class,
                GenericFilterEditAction.class,
                GenericFilterRemoveAction.class,
                GenericFilterCopyAction.class,
                GenericFilterClearValuesAction.class
        );
    }

    @Nullable
    public FilterConfiguration loadFilterConfigurationModel(GenericFilter filter, String configurationId) {
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

    protected List<FilterConfiguration> loadFilterConfigurationModels(GenericFilter filter) {
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
                                                               Configuration currentConfiguration) {
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
        //TODO: kremnevda, viewSettingsFacet? 04.04.2023
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

        if (configurationDetail instanceof FlowuiDataFilterConfigurationDetail) {
            InstanceContainer<FilterConfiguration> configurationDc =
                    ((FlowuiDataFilterConfigurationDetail) configurationDetail).getConfigurationDc();

            configurationModel = configurationDc.getItem();

            //TODO: kremnevda, viewSettingsFacet? 06.04.2023
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
        configurationModel.setUsername(currentAuthentication.getUser().getUsername());

        return configurationModel;
    }
}
