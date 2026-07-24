/*
 * Copyright 2026 Haulmont.
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

package filter_configuration_persistence;

import filter_configuration_persistence.view.FilterConfigurationPersistenceTestView;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.FilterConfigurationPersistence;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.GenericFilterSupport;
import io.jmix.flowui.component.genericfilter.configuration.UiDataFilterConfigurationDetail;
import io.jmix.flowui.component.genericfilter.converter.FilterConverter;
import io.jmix.flowui.component.genericfilter.model.FilterConfigurationModel;
import io.jmix.flowui.component.genericfilter.registration.FilterComponents;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.SingleFilterSupport;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.entity.filter.FilterValueComponent;
import io.jmix.flowui.entity.filter.GroupFilterCondition;
import io.jmix.flowui.entity.filter.LogicalFilterCondition;
import io.jmix.flowui.entity.filter.PropertyFilterCondition;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.FlowuiDataTestConfiguration;

import java.util.List;

@UiTest(viewBasePackages = {"filter_configuration_persistence.view", "io.jmix.flowui.app.filter.condition"},
        authenticator = FilterConfigurationPersistenceTestAuthenticator.class)
@SpringBootTest(classes = {FlowuiDataTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class GenericFilterConfigurationNamePrefillTest {

    private static final String COMPONENT_ID = "[FilterConfigurationPersistenceTestView]genericFilter";

    private static final String RUNTIME_CONFIGURATION_ID = "openProjects";
    private static final String RUNTIME_CONFIGURATION_NAME = "Open Projects";
    private static final String PERSISTED_CONFIGURATION_ID = "savedConfiguration";
    private static final String PERSISTED_CONFIGURATION_NAME = "Saved configuration";

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    GenericFilterSupport genericFilterSupport;
    @Autowired
    FilterConfigurationPersistence configurationPersistence;
    @Autowired
    FilterComponents filterComponents;
    @Autowired
    DialogWindows dialogWindows;
    @Autowired
    SingleFilterSupport singleFilterSupport;
    @Autowired
    DataManager dataManager;
    @Autowired
    EntityStates entityStates;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    public void afterEach() {
        jdbcTemplate.update("delete from FLOWUI_FILTER_CONFIGURATION");
    }

    @Test
    @DisplayName("Editor pre-fills name and id from a named run-time configuration that is not in the DB (#5451)")
    public void editorPrefillsNameForInMemoryConfiguration() {
        GenericFilter filter = navigateAndGetFilter();
        Configuration configuration = registerRuntimeConfiguration(filter);

        UiDataFilterConfigurationDetail detail = openEditor(filter, configuration, false);
        FilterConfigurationModel model = detail.getConfigurationDc().getItem();

        Assertions.assertTrue(entityStates.isNew(model), "expected a freshly created fallback model");
        Assertions.assertEquals(RUNTIME_CONFIGURATION_NAME, model.getName());
        Assertions.assertEquals(RUNTIME_CONFIGURATION_ID, model.getConfigurationId());
    }

    @Test
    @DisplayName("Saving a named run-time configuration reuses its id: no duplicate, no re-created configuration (#5451)")
    public void savingInMemoryConfigurationReusesIdWithoutDuplicating() {
        GenericFilter filter = navigateAndGetFilter();
        Configuration configuration = registerRuntimeConfiguration(filter);

        UiDataFilterConfigurationDetail detail = openEditor(filter, configuration, false);
        Configuration result = genericFilterSupport.saveCurrentFilterConfiguration(
                configuration, false, configuration.getRootLogicalFilterComponent(), detail);

        // The existing configuration is updated by its own id, not dropped and re-created with a generated one.
        Assertions.assertEquals(RUNTIME_CONFIGURATION_ID, result.getId());
        Assertions.assertNotNull(filter.getConfiguration(RUNTIME_CONFIGURATION_ID),
                "the configuration must not be removed and re-created on save");

        List<FilterConfigurationModel> stored = configurationPersistence.load(
                COMPONENT_ID, FilterConfigurationPersistenceTestAuthenticator.simpleUser);
        Assertions.assertEquals(1, stored.size(), "exactly one configuration must be persisted");
        Assertions.assertEquals(RUNTIME_CONFIGURATION_ID, stored.get(0).getConfigurationId());
        Assertions.assertEquals(RUNTIME_CONFIGURATION_NAME, stored.get(0).getName());
    }

    @Test
    @DisplayName("New configuration: the editor model is left empty so the user is asked for a name")
    public void editorDoesNotPrefillForNewConfiguration() {
        GenericFilter filter = navigateAndGetFilter();

        UiDataFilterConfigurationDetail detail = openEditor(filter, filter.getEmptyConfiguration(), true);
        FilterConfigurationModel model = detail.getConfigurationDc().getItem();

        Assertions.assertTrue(entityStates.isNew(model));
        Assertions.assertNull(model.getName());
        Assertions.assertNull(model.getConfigurationId());
    }

    @Test
    @DisplayName("Existing persisted configuration: the stored model is returned unchanged")
    public void editorReturnsStoredModelForPersistedConfiguration() {
        String username = FilterConfigurationPersistenceTestAuthenticator.simpleUser;

        configurationPersistence.save(createPersistedConfiguration(username));

        GenericFilter filter = navigateAndGetFilter();
        Configuration configuration = filter.getConfiguration(PERSISTED_CONFIGURATION_ID);
        Assertions.assertNotNull(configuration);

        UiDataFilterConfigurationDetail detail = openEditor(filter, configuration, false);
        FilterConfigurationModel model = detail.getConfigurationDc().getItem();

        Assertions.assertFalse(entityStates.isNew(model), "expected the model loaded from the database, not a fallback");
        Assertions.assertEquals(PERSISTED_CONFIGURATION_NAME, model.getName());
        Assertions.assertEquals(PERSISTED_CONFIGURATION_ID, model.getConfigurationId());
    }

    protected GenericFilter navigateAndGetFilter() {
        navigationSupport.navigate(FilterConfigurationPersistenceTestView.class);
        FilterConfigurationPersistenceTestView view = UiTestUtils.getCurrentView();
        return view.genericFilter;
    }

    protected Configuration registerRuntimeConfiguration(GenericFilter filter) {
        PropertyFilter<String> nameFilter = filter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("name")
                .operation(PropertyFilter.Operation.CONTAINS)
                .build();
        return filter.runtimeConfigurationBuilder()
                .id(RUNTIME_CONFIGURATION_ID)
                .name(RUNTIME_CONFIGURATION_NAME)
                .add(nameFilter)
                .makeCurrent()
                .buildAndRegister();
    }

    /**
     * Opens the configuration editor the way {@code GenericFilterEditAction} does and returns the detail fragment, so
     * the editor model can be inspected through the public {@code getConfigurationDc()} API instead of reaching into
     * the private {@code loadFilterConfigurationModel} method.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected UiDataFilterConfigurationDetail openEditor(GenericFilter filter, Configuration configuration,
                                                         boolean isNewConfiguration) {
        View<?> parent = UiTestUtils.getCurrentView();
        LogicalFilterComponent<?> rootComponent = configuration.getRootLogicalFilterComponent();
        Class modelClass = filterComponents.getModelClass(rootComponent.getClass());
        FilterConverter converter = filterComponents.getConverterByComponentClass(rootComponent.getClass(), filter);
        LogicalFilterCondition model = (LogicalFilterCondition) converter.convertToModel(rootComponent);

        DialogWindow dialog = dialogWindows.detail(parent, modelClass)
                .withViewId(filterComponents.getDetailViewId(modelClass))
                .editEntity(model)
                .build();

        return (UiDataFilterConfigurationDetail) genericFilterSupport.createFilterConfigurationDetail(
                dialog, isNewConfiguration, configuration);
    }

    protected FilterConfigurationModel createPersistedConfiguration(String username) {
        FilterConfigurationModel model = dataManager.create(FilterConfigurationModel.class);
        model.setConfigurationId(PERSISTED_CONFIGURATION_ID);
        model.setComponentId(COMPONENT_ID);
        model.setName(PERSISTED_CONFIGURATION_NAME);
        model.setUsername(username);

        GroupFilterCondition rootCondition = dataManager.create(GroupFilterCondition.class);
        rootCondition.setOperation(LogicalFilterComponent.Operation.AND);

        PropertyFilterCondition nameCondition = dataManager.create(PropertyFilterCondition.class);
        nameCondition.setOperation(PropertyFilter.Operation.CONTAINS);
        nameCondition.setParameterName(PropertyConditionUtils.generateParameterName("name"));
        nameCondition.setProperty("name");
        nameCondition.setParent(rootCondition);

        FilterValueComponent valueComponent = dataManager.create(FilterValueComponent.class);
        valueComponent.setComponentId("testId");
        //noinspection JmixIncorrectCreateGuiComponent
        valueComponent.setComponentName(singleFilterSupport.getValueComponentName(new TypedTextField<>()));
        nameCondition.setValueComponent(valueComponent);

        rootCondition.setOwnFilterConditions(List.of(nameCondition));
        model.setRootCondition(rootCondition);

        return model;
    }
}
