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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.vaadin.flow.component.Component;
import filter_configuration_persistence.view.FilterConfigurationTestFragment;
import filter_configuration_persistence.view.FragmentFilterConfigurationTestView;
import filter_configuration_persistence.view.ProgrammaticFilterConfigurationTestView;
import io.jmix.core.DataManager;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.FilterConfigurationPersistence;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.model.FilterConfigurationModel;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.SingleFilterSupport;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.entity.filter.FilterValueComponent;
import io.jmix.flowui.entity.filter.GroupFilterCondition;
import io.jmix.flowui.entity.filter.PropertyFilterCondition;
import io.jmix.flowui.kit.component.combobutton.ComboButton;
import io.jmix.flowui.kit.component.dropdownbutton.ActionItem;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.FlowuiDataTestConfiguration;

import java.util.List;
import java.util.stream.Stream;

import static filter_configuration_persistence.view.ProgrammaticFilterConfigurationTestView.CURRENT_CONFIGURATION_ID;
import static filter_configuration_persistence.view.ProgrammaticFilterConfigurationTestView.CURRENT_CONFIGURATION_NAME;

/**
 * A stored configuration can have the id of a configuration the application builds from code: a user of an
 * earlier version saved the configuration after editing it. The design-time configuration from code takes
 * precedence, and the stored one is ignored as a whole, including its "default for all users" mark, whichever
 * of the two the filter gets first (#5083).
 */
@UiTest(viewBasePackages = "filter_configuration_persistence.view",
        authenticator = FilterConfigurationPersistenceTestAuthenticator.class)
@SpringBootTest(classes = {FlowuiDataTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class GenericFilterStoredConfigurationTest {

    static final String PROGRAMMATIC_COMPONENT_ID = "[ProgrammaticFilterConfigurationTestView]genericFilter";
    static final String FRAGMENT_COMPONENT_ID = "[projectsFragment]genericFilter";
    static final String USER_CONFIGURATION_ID = "userProjects";
    // The property of the conditions in the configurations the application registers.
    static final String CODE_PROPERTY = "name";
    // The dropdown marks a configuration available to all users, as every design-time configuration is.
    static final String GLOBAL_NAME_POSTFIX = " *";
    static final String SAVED_NAME = "Open Projects (saved)";
    static final String SAVED_PROPERTY = "description";
    static final String SAVED_PARAMETER = PropertyConditionUtils.generateParameterName(SAVED_PROPERTY);
    static final String SAVED_DEFAULT_VALUE = "saved default";

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

    @AfterEach
    void afterEach() {
        jdbcTemplate.update("delete from FLOWUI_FILTER_CONFIGURATION");
    }

    @Test
    @DisplayName("A stored configuration with the id of a builder configuration is ignored with a warning")
    void loadConfigurationsAndApplyDefault_idOfBuilderConfiguration_storedIgnoredWithWarning() {
        configurationPersistence.save(createConfigurationModel(CURRENT_CONFIGURATION_ID, PROGRAMMATIC_COMPONENT_ID));

        List<String> warnings = captureFilterWarnings(this::navigateToProgrammaticView);
        ProgrammaticFilterConfigurationTestView view = UiTestUtils.getCurrentView();

        assertCodeConfigurationKept(view.genericFilter, view.builtConfiguration,
                CURRENT_CONFIGURATION_NAME + GLOBAL_NAME_POSTFIX, PROGRAMMATIC_COMPONENT_ID);
        Assertions.assertSame(view.builtConfiguration, view.genericFilter.getCurrentConfiguration(),
                "the configuration made current in code must stay current");
        assertIgnoredConfigurationReported(warnings, CURRENT_CONFIGURATION_ID, PROGRAMMATIC_COMPONENT_ID);
    }

    @Test
    @DisplayName("A stored default with the id of a builder configuration is ignored with its mark")
    void loadConfigurationsAndApplyDefault_defaultForAllWithIdOfBuilderConfiguration_storedIgnoredWithMark() {
        configurationPersistence.save(
                createGlobalDefaultConfigurationModel(CURRENT_CONFIGURATION_ID, PROGRAMMATIC_COMPONENT_ID));
        configurationPersistence.save(
                createGlobalDefaultConfigurationModel(USER_CONFIGURATION_ID, PROGRAMMATIC_COMPONENT_ID));

        ProgrammaticFilterConfigurationTestView view = navigateToProgrammaticView();
        GenericFilter filter = view.genericFilter;

        assertCodeConfigurationKept(filter, view.builtConfiguration,
                CURRENT_CONFIGURATION_NAME + GLOBAL_NAME_POSTFIX, PROGRAMMATIC_COMPONENT_ID);

        // The stored configurations are loaded in the order of their ids, and CURRENT_CONFIGURATION_ID sorts before
        // USER_CONFIGURATION_ID, so the ignored one comes first. Its mark does not count, so the next configuration
        // marked as the default for all users is applied.
        Configuration userConfiguration = filter.getConfiguration(USER_CONFIGURATION_ID);
        Assertions.assertNotNull(userConfiguration);
        Assertions.assertSame(userConfiguration, filter.getCurrentConfiguration());
    }

    @Test
    @DisplayName("Stored configurations loaded before the builder runs in a fragment give way to it")
    void buildAndRegister_storedConfigurationsLoadedInFragment_replacesThem() {
        // Once loaded, the first one is current as the default for all users, so the empty configuration takes its
        // place. The other one is not current.
        configurationPersistence.save(createGlobalDefaultConfigurationModel(
                FilterConfigurationTestFragment.CONFIGURATION_ID, FRAGMENT_COMPONENT_ID));
        configurationPersistence.save(createConfigurationModel(
                FilterConfigurationTestFragment.OTHER_CONFIGURATION_ID, FRAGMENT_COMPONENT_ID));

        List<String> warnings = captureFilterWarnings(() ->
                navigationSupport.navigate(FragmentFilterConfigurationTestView.class));
        FragmentFilterConfigurationTestView view = UiTestUtils.getCurrentView();
        FilterConfigurationTestFragment fragment = view.projectsFragment;
        GenericFilter filter = fragment.genericFilter;

        Assertions.assertEquals(FilterConfigurationTestFragment.CONFIGURATION_ID,
                fragment.currentConfigurationBeforeBuild.getId(),
                "the stored configuration must be current when the builder runs");

        assertCodeConfigurationKept(filter, fragment.builtConfiguration,
                FilterConfigurationTestFragment.CONFIGURATION_NAME + GLOBAL_NAME_POSTFIX, FRAGMENT_COMPONENT_ID);
        assertCodeConfigurationKept(filter, fragment.otherBuiltConfiguration,
                FilterConfigurationTestFragment.OTHER_CONFIGURATION_NAME + GLOBAL_NAME_POSTFIX, FRAGMENT_COMPONENT_ID);
        assertIgnoredConfigurationReported(warnings, FilterConfigurationTestFragment.CONFIGURATION_ID,
                FRAGMENT_COMPONENT_ID);
        assertIgnoredConfigurationReported(warnings, FilterConfigurationTestFragment.OTHER_CONFIGURATION_ID,
                FRAGMENT_COMPONENT_ID);
        Assertions.assertSame(filter.getEmptyConfiguration(), filter.getCurrentConfiguration(),
                "the replaced configuration must not stay current");
    }

    @Test
    @DisplayName("A non-current stored configuration gives way to the builder without changing the current one")
    void buildAndRegister_notCurrentStoredConfigurationLoadedInFragment_keepsCurrentConfiguration() {
        configurationPersistence.save(
                createGlobalDefaultConfigurationModel(USER_CONFIGURATION_ID, FRAGMENT_COMPONENT_ID));
        configurationPersistence.save(createConfigurationModel(
                FilterConfigurationTestFragment.OTHER_CONFIGURATION_ID, FRAGMENT_COMPONENT_ID));

        navigationSupport.navigate(FragmentFilterConfigurationTestView.class);
        FragmentFilterConfigurationTestView view = UiTestUtils.getCurrentView();
        FilterConfigurationTestFragment fragment = view.projectsFragment;
        GenericFilter filter = fragment.genericFilter;

        Configuration userConfiguration = filter.getConfiguration(USER_CONFIGURATION_ID);
        Assertions.assertNotNull(userConfiguration);
        Assertions.assertSame(userConfiguration, fragment.currentConfigurationBeforeBuild,
                "the user's configuration must be current when the builder runs");
        Assertions.assertSame(userConfiguration, filter.getCurrentConfiguration());
    }

    /**
     * Runs the action and returns the warnings the filter logs meanwhile.
     */
    List<String> captureFilterWarnings(Runnable action) {
        ListAppender<ILoggingEvent> logAppender = new ListAppender<>();
        Logger logger = (Logger) LoggerFactory.getLogger(GenericFilter.class);
        logAppender.start();
        logger.addAppender(logAppender);
        try {
            action.run();
        } finally {
            logger.detachAppender(logAppender);
        }

        return logAppender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
    }

    void assertIgnoredConfigurationReported(List<String> warnings, String configurationId, String componentId) {
        Assertions.assertTrue(warnings.stream()
                        .anyMatch(warning -> warning.contains("'" + configurationId + "'")
                                && warning.contains(componentId)),
                "the ignored stored configuration must be reported with the filter it belongs to");
    }

    ProgrammaticFilterConfigurationTestView navigateToProgrammaticView() {
        navigationSupport.navigate(ProgrammaticFilterConfigurationTestView.class);
        return UiTestUtils.getCurrentView();
    }

    /**
     * Asserts that the filter offers the configuration registered by the application, once and under the name given
     * in code, and that the stored configuration with the same id is ignored but kept in the storage.
     */
    void assertCodeConfigurationKept(GenericFilter filter, Configuration codeConfiguration, String dropdownName,
                                     String componentId) {
        String id = codeConfiguration.getId();

        Assertions.assertEquals(1, countConfigurations(filter, id));
        Assertions.assertSame(codeConfiguration, filter.getConfiguration(id),
                "the configuration registered by the application must stay registered");
        List<Condition> conditions = codeConfiguration.getQueryCondition().getConditions();
        Assertions.assertEquals(1, conditions.size());
        Assertions.assertEquals(CODE_PROPERTY, ((PropertyCondition) conditions.get(0)).getProperty());
        Assertions.assertEquals(List.of(dropdownName), selectItemTexts(filter, id),
                "the configuration dropdown must offer the configuration once, under the name given in code");
        Assertions.assertNotNull(configurationPersistence.load(id, componentId,
                        FilterConfigurationPersistenceTestAuthenticator.simpleUser),
                "the stored configuration must be ignored, not deleted");
    }

    long countConfigurations(GenericFilter filter, String configurationId) {
        return filter.getConfigurations().stream()
                .filter(configuration -> configurationId.equals(configuration.getId()))
                .count();
    }

    /**
     * Returns the texts of the dropdown items that activate the configuration with the given id, that is, what the
     * user sees in the filter's configuration dropdown.
     */
    List<String> selectItemTexts(GenericFilter filter, String configurationId) {
        String itemId = "genericFilter_select_" + configurationId;

        return flatten(filter)
                .filter(ComboButton.class::isInstance)
                .flatMap(component -> ((ComboButton) component).getItems().stream())
                .filter(item -> itemId.equals(item.getId()))
                .map(item -> ((ActionItem) item).getAction().getText())
                .toList();
    }

    Stream<Component> flatten(Component component) {
        return Stream.concat(Stream.of(component), component.getChildren().flatMap(this::flatten));
    }

    /**
     * Creates the same configuration as {@link #createConfigurationModel(String, String)}, but shared by all users and
     * marked as their default, so that loading it activates the configuration.
     */
    FilterConfigurationModel createGlobalDefaultConfigurationModel(String configurationId, String componentId) {
        FilterConfigurationModel model = createConfigurationModel(configurationId, componentId);
        model.setUsername(null);
        model.setDefaultForAll(true);

        return model;
    }

    /**
     * Creates the configuration the user saved earlier: the same id as a configuration the application registers, but
     * a different name and condition, so the two are told apart after loading.
     */
    FilterConfigurationModel createConfigurationModel(String configurationId, String componentId) {
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
