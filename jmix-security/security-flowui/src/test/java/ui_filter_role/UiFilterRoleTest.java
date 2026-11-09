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
package ui_filter_role;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap;
import com.vaadin.flow.internal.nodefeature.PropertyChangeDeniedException;
import io.jmix.core.Metadata;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.flowui.OpenedDialogWindows;
import io.jmix.flowui.action.genericfilter.GenericFilterEditAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.configuration.AbstractConfigurationDetail;
import io.jmix.flowui.component.genericfilter.converter.FilterConverter;
import io.jmix.flowui.component.genericfilter.registration.FilterComponents;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.entity.filter.FilterValueComponent;
import io.jmix.flowui.entity.filter.PropertyFilterCondition;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.util.OperationResult;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.flowuidata.entity.FilterConfiguration;
import io.jmix.securityflowui.role.UiFilterRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.SecurityFlowuiTestConfiguration;
import test_support.TestUserFilterUiAuthenticator;
import test_support.view.TestUserFilterView;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The user has {@link UiFilterRole} and no other policy of the generic filter.
 */
@UiTest(viewBasePackages = {"io.jmix.flowui.app", "test_support.view"},
        authenticator = TestUserFilterUiAuthenticator.class)
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class UiFilterRoleTest {

    @Autowired
    ViewNavigationSupport viewNavigationSupport;
    @Autowired
    UnconstrainedDataManager dataManager;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    Metadata metadata;
    @Autowired
    FilterComponents filterComponents;
    @Autowired
    ApplicationContext applicationContext;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from FLOWUI_FILTER_CONFIGURATION");
    }

    @Test
    void editConfiguration_userWithUiFilterRole_savesNameAndId() throws PropertyChangeDeniedException {
        viewNavigationSupport.navigate(TestUserFilterView.class);
        GenericFilter filter = UiTestUtils.getComponent(UiTestUtils.getCurrentView(), "genericFilter");
        filter.getCurrentConfiguration().getRootLogicalFilterComponent()
                .add(createCondition(filter, "username"));

        filter.getAction(GenericFilterEditAction.ID).actionPerform(filter);
        StandardDetailView<?> configurationView = (StandardDetailView<?>) applicationContext
                .getBean(OpenedDialogWindows.class)
                .getCurrentDialog()
                .orElseThrow();
        enterFromClient(getNameField(configurationView), "Active users");

        assertThat(configurationView.closeWithSave().getStatus()).isEqualTo(OperationResult.Status.SUCCESS);
        FilterConfiguration configuration = dataManager.load(FilterConfiguration.class).all().one();
        assertThat(configuration.getName()).isEqualTo("Active users");
        assertThat(configuration.getConfigurationId()).isNotEmpty();
    }

    /**
     * Builds a condition the way the 'Add condition' action does: through the converter of the condition model.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    FilterComponent createCondition(GenericFilter filter, String property) {
        PropertyFilterCondition model = metadata.create(PropertyFilterCondition.class);
        model.setProperty(property);
        model.setOperation(PropertyFilter.Operation.CONTAINS);
        model.setParameterName(PropertyConditionUtils.generateParameterName(property));
        model.setValueComponent(metadata.create(FilterValueComponent.class));

        FilterConverter converter = filterComponents.getConverterByModelClass(PropertyFilterCondition.class, filter);
        return (FilterComponent) converter.convertToComponent(model);
    }

    /**
     * Returns the name field of the configuration form that the 'Edit' action puts at the top of the condition
     * editor.
     */
    @SuppressWarnings("unchecked")
    TypedTextField<String> getNameField(View<?> configurationView) {
        Component configurationForm = configurationView.getContent().getChildren()
                .filter(AbstractConfigurationDetail.class::isInstance)
                .flatMap(Component::getChildren)
                .findFirst()
                .orElseThrow();
        return (TypedTextField<String>) UiComponentUtils.findComponent(configurationForm, "nameField").orElseThrow();
    }

    /**
     * Enters the text as the user does: a field the user cannot edit does not take it.
     */
    void enterFromClient(TypedTextField<String> field, String text) throws PropertyChangeDeniedException {
        field.getElement().getNode().getFeature(ElementPropertyMap.class)
                .deferredUpdateFromClient("value", text)
                .run();
    }
}
