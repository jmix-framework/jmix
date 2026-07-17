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
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.FilterConfigurationPersistence;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.GenericFilterSupport;
import io.jmix.flowui.component.genericfilter.model.FilterConfigurationModel;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.SingleFilterSupport;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.entity.filter.FilterValueComponent;
import io.jmix.flowui.entity.filter.GroupFilterCondition;
import io.jmix.flowui.entity.filter.PropertyFilterCondition;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.FlowuiDataTestConfiguration;

import java.lang.reflect.Method;
import java.util.List;

@UiTest(viewBasePackages = "filter_configuration_persistence.view", authenticator = FilterConfigurationPersistenceTestAuthenticator.class)
@SpringBootTest(classes = {FlowuiDataTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class GenericFilterConfigurationNamePrefillTest {

    private static final String COMPONENT_ID = "[FilterConfigurationPersistenceTestView]genericFilter";

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    GenericFilterSupport genericFilterSupport;
    @Autowired
    FilterConfigurationPersistence configurationPersistence;
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
    @DisplayName("Existing in-memory configuration (not in DB): the editor model is pre-filled from it (#5451)")
    public void inMemoryConfigurationPrefillsNameAndId() throws Exception {
        GenericFilter filter = navigateAndGetFilter();

        // A named run-time configuration that is never persisted to the database.
        PropertyFilter<String> nameFilter = filter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("name")
                .operation(PropertyFilter.Operation.CONTAINS)
                .build();
        Configuration configuration = filter.runtimeConfigurationBuilder()
                .id("openProjects")
                .name("Open Projects")
                .add(nameFilter)
                .makeCurrent()
                .buildAndRegister();

        // isNewConfiguration = false, not found in the DB -> fallback model, pre-filled from the configuration.
        FilterConfigurationModel model = loadEditorModel(false, configuration);

        Assertions.assertTrue(entityStates.isNew(model), "expected a freshly created fallback model");
        Assertions.assertEquals("Open Projects", model.getName());
        Assertions.assertEquals("openProjects", model.getConfigurationId());
    }

    @Test
    @DisplayName("Existing persisted configuration: the stored model is returned unchanged (configurationModel != null)")
    public void persistedConfigurationReturnsStoredModel() throws Exception {
        String configurationId = "savedConfiguration";
        String username = FilterConfigurationPersistenceTestAuthenticator.simpleUser;

        configurationPersistence.save(createPersistedConfiguration(configurationId, username, "Saved configuration"));

        GenericFilter filter = navigateAndGetFilter();
        Configuration configuration = filter.getConfiguration(configurationId);
        Assertions.assertNotNull(configuration);

        // isNewConfiguration = false, found in the DB -> the stored model is returned, fallback is skipped.
        FilterConfigurationModel model = loadEditorModel(false, configuration);

        Assertions.assertFalse(entityStates.isNew(model), "expected the model loaded from the database, not a fallback");
        Assertions.assertEquals("Saved configuration", model.getName());
        Assertions.assertEquals(configurationId, model.getConfigurationId());
    }

    @Test
    @DisplayName("New configuration: the fallback model is left empty (isNewConfiguration = true)")
    public void newConfigurationDoesNotPrefillName() throws Exception {
        GenericFilter filter = navigateAndGetFilter();

        // Creating a new configuration starts from the empty configuration; the DB load is skipped
        // and the name/id must stay empty so the editor asks the user for a name.
        FilterConfigurationModel model = loadEditorModel(true, filter.getEmptyConfiguration());

        Assertions.assertTrue(entityStates.isNew(model));
        Assertions.assertNull(model.getName());
        Assertions.assertNull(model.getConfigurationId());
    }

    protected GenericFilter navigateAndGetFilter() {
        navigationSupport.navigate(FilterConfigurationPersistenceTestView.class);
        FilterConfigurationPersistenceTestView view = UiTestUtils.getCurrentView();
        return view.genericFilter;
    }

    protected FilterConfigurationModel loadEditorModel(boolean isNewConfiguration, Configuration configuration)
            throws Exception {
        Method method = GenericFilterSupport.class.getDeclaredMethod(
                "loadFilterConfigurationModel", boolean.class, Configuration.class);
        method.setAccessible(true);
        return (FilterConfigurationModel) method.invoke(genericFilterSupport, isNewConfiguration, configuration);
    }

    protected FilterConfigurationModel createPersistedConfiguration(String configurationId, String username, String name) {
        FilterConfigurationModel model = dataManager.create(FilterConfigurationModel.class);
        model.setConfigurationId(configurationId);
        model.setComponentId(COMPONENT_ID);
        model.setName(name);
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
