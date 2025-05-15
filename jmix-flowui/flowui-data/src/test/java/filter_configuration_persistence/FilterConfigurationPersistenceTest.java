/*
 * Copyright 2025 Haulmont.
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
import io.jmix.core.querycondition.*;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.FilterConfigurationPersistence;
import io.jmix.flowui.component.genericfilter.model.FilterConfigurationModel;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.SingleFilterSupport;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.entity.filter.*;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.FlowuiDataTestConfiguration;

import java.util.ArrayList;
import java.util.List;

@UiTest(viewBasePackages = "filter_configuration_persistence.view", authenticator = FilterConfigurationPersistenceTestAuthenticator.class)
@SpringBootTest(classes = {FlowuiDataTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class FilterConfigurationPersistenceTest {

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    FilterConfigurationPersistence configurationPersistence;
    @Autowired
    SingleFilterSupport singleFilterSupport;

    @Autowired
    DataManager dataManager;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    EntityStates entityStates;

    @AfterEach
    public void afterEach() {
        jdbcTemplate.update("delete from FLOWUI_FILTER_CONFIGURATION");
    }

    @Test
    @DisplayName("Save new FilterConfiguration")
    public void saveNewFilterConfiguration() {
        String componentId = "[FilterConfigurationPersistenceTestView]genericFilter";
        String configurationId = "projectConfiguration";
        String username = FilterConfigurationPersistenceTestAuthenticator.simpleUser;

        /*
         * Save new configuration.
         */
        FilterConfigurationModel newConfiguration = createNewConfiguration(configurationId, componentId, username);
        configurationPersistence.save(newConfiguration);

        /*
         * Navigate to a view and check that configuration is loaded.
         */
        navigationSupport.navigate(FilterConfigurationPersistenceTestView.class);
        FilterConfigurationPersistenceTestView view = UiTestUtils.getCurrentView();

        Configuration configuration = view.genericFilter.getConfiguration(configurationId);
        Assertions.assertNotNull(configuration);

        List<Condition> conditions = configuration.getQueryCondition().getConditions();
        Assertions.assertEquals(1, conditions.size());

        PropertyCondition nameCondition = (PropertyCondition) conditions.get(0);
        Assertions.assertEquals("name", nameCondition.getProperty());
    }

    @Test
    @DisplayName("Update existing FilterConfiguration")
    public void saveFilterConfigurationTwice() {
        String componentId = "[FilterConfigurationPersistenceTestView]genericFilter";
        String configurationId = "projectConfiguration";
        String username = FilterConfigurationPersistenceTestAuthenticator.simpleUser;
        String updatedName = "Updated name";

        /*
         * Save new configuration.
         */
        FilterConfigurationModel configurationModel = createNewConfiguration(configurationId, componentId, username);
        configurationPersistence.save(configurationModel);

        List<FilterConfigurationModel> configurations = configurationPersistence.load(componentId, username);
        Assertions.assertEquals(1, configurations.size());

        /*
         * 'configurationModel' is marked as 'NEW' for now. Update and save it.
         * There should be only 1 configuration after reloading.
         */

        addDescriptionPropertyFilter(configurationModel);
        configurationPersistence.save(configurationModel);

        configurations = configurationPersistence.load(componentId, username);
        Assertions.assertEquals(1, configurations.size());

        /*
         * Mark 'configurationModel' as NOT 'NEW'. Update and save it.
         * There should be only 1 configuration after reloading.
         */

        configurationModel.setName(updatedName);
        entityStates.setNew(configurationModel, false);

        configurationPersistence.save(configurationModel);

        configurations = configurationPersistence.load(componentId, username);
        Assertions.assertEquals(1, configurations.size());

        /*
         * Navigate to a view and check that updates are saved.
         */

        navigationSupport.navigate(FilterConfigurationPersistenceTestView.class);
        FilterConfigurationPersistenceTestView view = UiTestUtils.getCurrentView();

        Configuration configuration = view.genericFilter.getConfiguration(configurationId);

        Assertions.assertNotNull(configuration);
        Assertions.assertEquals(configurationId, configuration.getId());
        Assertions.assertEquals(updatedName, configuration.getName());

        LogicalCondition queryCondition = configuration.getQueryCondition();
        PropertyCondition condition = queryCondition.getConditions().stream()
                .filter(c -> c instanceof PropertyCondition pc && "description".equals(pc.getProperty()))
                .findFirst()
                .map(PropertyCondition.class::cast)
                .orElse(null);

        Assertions.assertNotNull(condition);
    }

    private FilterConfigurationModel createNewConfiguration(String configurationId, String componentId, String username) {
        FilterConfigurationModel model = dataManager.create(FilterConfigurationModel.class);
        model.setConfigurationId(configurationId);
        model.setComponentId(componentId);
        model.setName("Project configuration");
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

    private void addDescriptionPropertyFilter(FilterConfigurationModel model) {
        LogicalFilterCondition rootCondition = model.getRootCondition();

        PropertyFilterCondition descriptionCondition = dataManager.create(PropertyFilterCondition.class);
        descriptionCondition.setOperation(PropertyFilter.Operation.CONTAINS);
        descriptionCondition.setParameterName(PropertyConditionUtils.generateParameterName("description"));
        descriptionCondition.setProperty("description");
        descriptionCondition.setParent(rootCondition);

        FilterValueComponent valueComponent = dataManager.create(FilterValueComponent.class);
        valueComponent.setComponentId("descriptionFieldId");
        //noinspection JmixIncorrectCreateGuiComponent
        valueComponent.setComponentName(singleFilterSupport.getValueComponentName(new TypedTextField<>()));

        descriptionCondition.setValueComponent(valueComponent);
        descriptionCondition.setParent(rootCondition);

        List<FilterCondition> conditions = new ArrayList<>(rootCondition.getOwnFilterConditions());
        conditions.add(descriptionCondition);
        rootCondition.setOwnFilterConditions(conditions);
    }
}
