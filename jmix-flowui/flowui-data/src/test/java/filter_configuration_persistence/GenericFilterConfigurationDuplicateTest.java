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

import com.vaadin.flow.component.Component;
import filter_configuration_persistence.view.DesignTimeFilterConfigurationTestView;
import filter_configuration_persistence.view.FilterConfigurationPersistenceTestView;
import filter_configuration_persistence.view.ImmutableFilterConfigurationTestView;
import filter_configuration_persistence.view.ProgrammaticFilterConfigurationTestView;
import io.jmix.core.DataManager;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.FilterConfigurationPersistence;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.GenericFilterSupport;
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration;
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration;
import io.jmix.flowui.component.genericfilter.model.FilterConfigurationModel;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.SingleFilterSupport;
import io.jmix.flowui.kit.component.combobutton.ComboButton;
import io.jmix.flowui.kit.component.dropdownbutton.ActionItem;
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

import java.util.List;
import java.util.stream.Stream;

import static filter_configuration_persistence.view.ProgrammaticFilterConfigurationTestView.CURRENT_CONFIGURATION_ID;
import static filter_configuration_persistence.view.ProgrammaticFilterConfigurationTestView.CURRENT_CONFIGURATION_NAME;
import static filter_configuration_persistence.view.ProgrammaticFilterConfigurationTestView.OTHER_CONFIGURATION_ID;

/**
 * A configuration registered before the stored ones are loaded, either programmatically in {@code onInit}
 * or declared in XML, has the same id as the configuration the user saved from it. Loading must keep one
 * configuration per id instead of registering the stored one next to the one already there (#5533).
 */
@UiTest(viewBasePackages = {"filter_configuration_persistence.view", "io.jmix.flowui.app.filter.condition"},
        authenticator = FilterConfigurationPersistenceTestAuthenticator.class)
@SpringBootTest(classes = {FlowuiDataTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class GenericFilterConfigurationDuplicateTest {

    private static final String PROGRAMMATIC_COMPONENT_ID = "[ProgrammaticFilterConfigurationTestView]genericFilter";
    private static final String DESIGN_TIME_COMPONENT_ID = "[DesignTimeFilterConfigurationTestView]genericFilter";
    private static final String IMMUTABLE_COMPONENT_ID = "[ImmutableFilterConfigurationTestView]genericFilter";

    private static final String SAVED_NAME = "Open Projects (saved)";
    private static final String SAVED_PROPERTY = "description";
    private static final String SAVED_PARAMETER = PropertyConditionUtils.generateParameterName(SAVED_PROPERTY);
    private static final String SAVED_DEFAULT_VALUE = "saved default";

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
    JdbcTemplate jdbcTemplate;

    @AfterEach
    public void afterEach() {
        jdbcTemplate.update("delete from FLOWUI_FILTER_CONFIGURATION");
    }

    @Test
    @DisplayName("A stored configuration supersedes the current programmatic one instead of duplicating it (#5533)")
    public void storedConfigurationSupersedesCurrentProgrammaticConfiguration() {
        configurationPersistence.save(createConfigurationModel(CURRENT_CONFIGURATION_ID, PROGRAMMATIC_COMPONENT_ID));

        GenericFilter filter = navigateToProgrammaticView();

        Assertions.assertEquals(1, countConfigurations(filter, CURRENT_CONFIGURATION_ID));

        Configuration configuration = filter.getConfiguration(CURRENT_CONFIGURATION_ID);
        Assertions.assertNotNull(configuration);
        Assertions.assertEquals(SAVED_NAME, configuration.getName());
        assertSingleConditionOn(configuration, SAVED_PROPERTY);

        Assertions.assertSame(configuration, filter.getCurrentConfiguration(),
                "the stored configuration must take over as current");
        Assertions.assertEquals(List.of(SAVED_NAME), selectItemTexts(filter, CURRENT_CONFIGURATION_ID),
                "the configuration dropdown must offer the configuration once, under its stored name");
        assertLoaderFiltersOn(filter, SAVED_PROPERTY);
    }

    @Test
    @DisplayName("A stored configuration supersedes a programmatic one that is not current (#5533)")
    public void storedConfigurationSupersedesNonCurrentProgrammaticConfiguration() {
        configurationPersistence.save(createConfigurationModel(OTHER_CONFIGURATION_ID, PROGRAMMATIC_COMPONENT_ID));

        GenericFilter filter = navigateToProgrammaticView();

        Assertions.assertEquals(1, countConfigurations(filter, OTHER_CONFIGURATION_ID));

        Configuration configuration = filter.getConfiguration(OTHER_CONFIGURATION_ID);
        Assertions.assertNotNull(configuration);
        assertSingleConditionOn(configuration, SAVED_PROPERTY);

        Assertions.assertEquals(List.of(SAVED_NAME), selectItemTexts(filter, OTHER_CONFIGURATION_ID),
                "the configuration dropdown must offer the configuration once, under its stored name");

        Configuration currentConfiguration = filter.getConfiguration(CURRENT_CONFIGURATION_ID);
        Assertions.assertNotNull(currentConfiguration);
        Assertions.assertEquals(CURRENT_CONFIGURATION_NAME, currentConfiguration.getName(),
                "a configuration without a stored counterpart must be left alone");
        Assertions.assertSame(currentConfiguration, filter.getCurrentConfiguration());
    }

    @Test
    @DisplayName("Saving a programmatic configuration and reopening the view leaves a single configuration (#5533)")
    public void savedProgrammaticConfigurationIsNotDuplicatedOnReopening() {
        GenericFilter filter = navigateToProgrammaticView();
        Configuration configuration = filter.getConfiguration(CURRENT_CONFIGURATION_ID);
        Assertions.assertNotNull(configuration);

        // The user saves the configuration without renaming it, so it is stored under its own id.
        genericFilterSupport.saveConfigurationModel(configuration, null);

        navigationSupport.navigate(FilterConfigurationPersistenceTestView.class);
        GenericFilter reopenedFilter = navigateToProgrammaticView();

        Assertions.assertEquals(1, countConfigurations(reopenedFilter, CURRENT_CONFIGURATION_ID));
        Assertions.assertEquals(List.of(CURRENT_CONFIGURATION_NAME),
                selectItemTexts(reopenedFilter, CURRENT_CONFIGURATION_ID),
                "the configuration must be offered once, not twice under the same name");
    }

    @Test
    @DisplayName("A configuration registered programmatically keeps its deletion protection when superseded")
    public void supersededConfigurationKeepsDeletionProtection() {
        configurationPersistence.save(createConfigurationModel(CURRENT_CONFIGURATION_ID, PROGRAMMATIC_COMPONENT_ID));

        GenericFilter filter = navigateToProgrammaticView();

        Configuration configuration = filter.getConfiguration(CURRENT_CONFIGURATION_ID);
        Assertions.assertInstanceOf(RunTimeConfiguration.class, configuration);
        Assertions.assertTrue(((RunTimeConfiguration) configuration).isProtectedFromUserDeletion(),
                "the deletion protection set by the application must survive the load");
    }

    @Test
    @DisplayName("A stored configuration does not displace a design-time configuration with the same id")
    public void storedConfigurationDoesNotDisplaceDesignTimeConfiguration() {
        configurationPersistence.save(createConfigurationModel(DesignTimeFilterConfigurationTestView.CONFIGURATION_ID,
                DESIGN_TIME_COMPONENT_ID));

        navigationSupport.navigate(DesignTimeFilterConfigurationTestView.class);
        DesignTimeFilterConfigurationTestView view = UiTestUtils.getCurrentView();
        GenericFilter filter = view.genericFilter;

        Assertions.assertEquals(1,
                countConfigurations(filter, DesignTimeFilterConfigurationTestView.CONFIGURATION_ID));

        Configuration configuration =
                filter.getConfiguration(DesignTimeFilterConfigurationTestView.CONFIGURATION_ID);
        Assertions.assertInstanceOf(DesignTimeConfiguration.class, configuration,
                "the configuration declared in XML must stay");
        assertSingleConditionOn(configuration, "name");
    }

    @Test
    @DisplayName("The configuration the application holds a reference to keeps working after the load (#5533)")
    public void applicationReferenceStaysValidAfterLoad() {
        configurationPersistence.save(createConfigurationModel(CURRENT_CONFIGURATION_ID, PROGRAMMATIC_COMPONENT_ID));

        navigationSupport.navigate(ProgrammaticFilterConfigurationTestView.class);
        ProgrammaticFilterConfigurationTestView view = UiTestUtils.getCurrentView();
        GenericFilter filter = view.genericFilter;

        Configuration reference = view.currentConfigurationReference;
        Assertions.assertSame(reference, filter.getConfiguration(CURRENT_CONFIGURATION_ID),
                "the registered configuration must take the stored state, not be replaced by another instance");
        Assertions.assertEquals(SAVED_NAME, reference.getName());
        assertSingleConditionOn(reference, SAVED_PROPERTY);
        Assertions.assertEquals(SAVED_DEFAULT_VALUE, reference.getFilterComponentDefaultValue(SAVED_PARAMETER),
                "the stored default value must be registered for the loaded condition");
        Assertions.assertFalse(reference.isModified(),
                "the flags of the replaced conditions must not be left behind");

        // The application activates the configuration it built.
        filter.setCurrentConfiguration(reference);

        Assertions.assertSame(reference, filter.getCurrentConfiguration());
        assertSingleConditionOn(filter.getCurrentConfiguration(), SAVED_PROPERTY);
    }

    @Test
    @DisplayName("A stored configuration that is default for all users activates the registered instance")
    public void storedDefaultForAllConfigurationActivatesRegisteredInstance() {
        configurationPersistence.save(
                createGlobalDefaultConfigurationModel(OTHER_CONFIGURATION_ID, PROGRAMMATIC_COMPONENT_ID));

        GenericFilter filter = navigateToProgrammaticView();

        Configuration configuration = filter.getConfiguration(OTHER_CONFIGURATION_ID);
        Assertions.assertNotNull(configuration);
        Assertions.assertSame(configuration, filter.getCurrentConfiguration(),
                "the configuration registered in the filter must be activated, not the loaded copy");
        Assertions.assertTrue(configuration.isAvailableForAllUsers());
        assertSingleConditionOn(configuration, SAVED_PROPERTY);
    }

    @Test
    @DisplayName("A stored configuration does not overwrite a registered configuration that cannot be modified")
    public void storedConfigurationDoesNotOverwriteImmutableConfiguration() {
        configurationPersistence.save(createConfigurationModel(
                ImmutableFilterConfigurationTestView.CONFIGURATION_ID, IMMUTABLE_COMPONENT_ID));

        navigationSupport.navigate(ImmutableFilterConfigurationTestView.class);
        ImmutableFilterConfigurationTestView view = UiTestUtils.getCurrentView();
        GenericFilter filter = view.genericFilter;

        Assertions.assertEquals(1,
                countConfigurations(filter, ImmutableFilterConfigurationTestView.CONFIGURATION_ID));
        Assertions.assertSame(view.immutableConfiguration,
                filter.getConfiguration(ImmutableFilterConfigurationTestView.CONFIGURATION_ID),
                "a configuration that cannot take the stored state must be left as it is");
        assertSingleConditionOn(view.immutableConfiguration, "name");
    }

    protected GenericFilter navigateToProgrammaticView() {
        navigationSupport.navigate(ProgrammaticFilterConfigurationTestView.class);
        ProgrammaticFilterConfigurationTestView view = UiTestUtils.getCurrentView();
        return view.genericFilter;
    }

    protected long countConfigurations(GenericFilter filter, String configurationId) {
        return filter.getConfigurations().stream()
                .filter(configuration -> configurationId.equals(configuration.getId()))
                .count();
    }

    /**
     * Returns the texts of the dropdown items that activate the configuration with the given id, that is,
     * what the user sees in the filter's configuration dropdown.
     */
    protected List<String> selectItemTexts(GenericFilter filter, String configurationId) {
        String itemId = "genericFilter_select_" + configurationId;

        return flatten(filter)
                .filter(ComboButton.class::isInstance)
                .flatMap(component -> ((ComboButton) component).getItems().stream())
                .filter(item -> itemId.equals(item.getId()))
                .map(item -> ((ActionItem) item).getAction().getText())
                .toList();
    }

    protected Stream<Component> flatten(Component component) {
        return Stream.concat(Stream.of(component), component.getChildren().flatMap(this::flatten));
    }

    /**
     * Asserts that the data loader is filtered by the given property, that is, that the state shown by the
     * filter reached the loader.
     */
    protected void assertLoaderFiltersOn(GenericFilter filter, String property) {
        Condition loaderCondition = filter.getDataLoader().getCondition();
        Assertions.assertInstanceOf(LogicalCondition.class, loaderCondition);

        List<Condition> conditions = ((LogicalCondition) loaderCondition).getConditions();
        Assertions.assertEquals(1, conditions.size());
        Assertions.assertEquals(property, ((PropertyCondition) conditions.get(0)).getProperty());
    }

    protected void assertSingleConditionOn(Configuration configuration, String property) {
        List<Condition> conditions = configuration.getQueryCondition().getConditions();
        Assertions.assertEquals(1, conditions.size());
        Assertions.assertEquals(property, ((PropertyCondition) conditions.get(0)).getProperty());
    }

    /**
     * Creates the configuration the user saved earlier: the same id as a configuration the view registers,
     * but a different name and condition, so the two are told apart after loading.
     */
    /**
     * Creates the same configuration as {@link #createConfigurationModel(String, String)}, but shared by all
     * users and marked as their default, so that loading it activates the configuration.
     */
    protected FilterConfigurationModel createGlobalDefaultConfigurationModel(String configurationId,
                                                                            String componentId) {
        FilterConfigurationModel model = createConfigurationModel(configurationId, componentId);
        model.setUsername(null);
        model.setDefaultForAll(true);

        return model;
    }

    protected FilterConfigurationModel createConfigurationModel(String configurationId, String componentId) {
        FilterConfigurationModel model = dataManager.create(FilterConfigurationModel.class);
        model.setConfigurationId(configurationId);
        model.setComponentId(componentId);
        model.setName(SAVED_NAME);
        model.setUsername(FilterConfigurationPersistenceTestAuthenticator.simpleUser);

        GroupFilterCondition rootCondition = dataManager.create(GroupFilterCondition.class);
        rootCondition.setOperation(LogicalFilterComponent.Operation.AND);

        PropertyFilterCondition propertyCondition = dataManager.create(PropertyFilterCondition.class);
        propertyCondition.setOperation(PropertyFilter.Operation.CONTAINS);
        propertyCondition.setParameterName(SAVED_PARAMETER);
        propertyCondition.setProperty(SAVED_PROPERTY);
        propertyCondition.setParent(rootCondition);

        FilterValueComponent valueComponent = dataManager.create(FilterValueComponent.class);
        valueComponent.setComponentId("testId");
        valueComponent.setDefaultValue(SAVED_DEFAULT_VALUE);
        //noinspection JmixIncorrectCreateGuiComponent
        valueComponent.setComponentName(singleFilterSupport.getValueComponentName(new TypedTextField<>()));
        propertyCondition.setValueComponent(valueComponent);

        rootCondition.setOwnFilterConditions(List.of(propertyCondition));
        model.setRootCondition(rootCondition);

        return model;
    }
}
