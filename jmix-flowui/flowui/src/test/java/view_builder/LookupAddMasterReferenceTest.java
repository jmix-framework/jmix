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

package view_builder;

import component.standarddetailview.view.BlankTestView;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.FlowuiTestConfiguration;
import test_support.entity.company.Department;
import test_support.entity.company.Employee;
import view_builder.view.DepartmentDetailTestView;
import view_builder.view.EmployeeListTestView;

import java.util.List;

/**
 * Reproduces jmix-framework/jmix#5380: selected lookup items reference each other through the
 * {@code manager} attribute (the manager of one row is the same Java instance as another row), so merging
 * an item used to re-merge the previously merged items and overwrite the master reference already set
 * on them. All employees added to another department via the lookup must reference the new department.
 */
@UiTest(viewBasePackages = {"view_builder.view", "component.standarddetailview.view"})
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class LookupAddMasterReferenceTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    ViewNavigators viewNavigators;

    @Autowired
    ViewNavigationSupport navigationSupport;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DialogWindows dialogWindows;

    Department department1;
    Department department2;
    Employee director;
    Employee manager;
    Employee assistant;

    @BeforeEach
    void setUp() {
        department1 = dataManager.create(Department.class);
        department1.setName("department 1");
        department1 = dataManager.save(department1);

        department2 = dataManager.create(Department.class);
        department2.setName("department 2");
        department2 = dataManager.save(department2);

        // Management chain: director <- manager <- assistant, all in department 1
        director = dataManager.create(Employee.class);
        director.setName("director");
        director.setDepartment(department1);
        director = dataManager.save(director);

        manager = dataManager.create(Employee.class);
        manager.setName("manager");
        manager.setManager(director);
        manager.setDepartment(department1);
        manager = dataManager.save(manager);

        assistant = dataManager.create(Employee.class);
        assistant.setName("assistant");
        assistant.setManager(manager);
        assistant.setDepartment(department1);
        assistant = dataManager.save(assistant);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("update TEST_EMPLOYEE set MANAGER_ID = null");
        jdbcTemplate.execute("delete from TEST_EMPLOYEE");
        jdbcTemplate.execute("delete from TEST_DEPARTMENT");
    }

    @Test
    @DisplayName("Adding hierarchically linked entities to another master via lookup updates all of them")
    public void testAddLinkedEntitiesToAnotherMasterViaLookup() {
        navigationSupport.navigate(BlankTestView.class);
        View<?> origin = UiTestUtils.getCurrentView();

        viewNavigators.detailView(origin, Department.class)
                .withViewClass(DepartmentDetailTestView.class)
                .editEntity(department2)
                .navigate();

        DepartmentDetailTestView detailView = UiTestUtils.getCurrentView();
        Assertions.assertTrue(detailView.employeesDc.getItems().isEmpty());

        detailView.employeesDataGrid.getAction("addAction").actionPerform(detailView.employeesDataGrid);

        EmployeeListTestView lookupView = getCurrentDialogView();
        List<Employee> lookupItems = lookupView.employeesDc.getItems();
        Assertions.assertEquals(3, lookupItems.size());

        // Select in the order director, manager, assistant: every next item references
        // a previously selected one through the 'manager' attribute
        for (String name : List.of("director", "manager", "assistant")) {
            lookupView.employeesDataGrid.select(lookupItems.stream()
                    .filter(item -> name.equals(item.getName()))
                    .findFirst()
                    .orElseThrow());
        }
        lookupView.selectButton.click();

        Department editedDepartment = detailView.getEditedEntity();
        Assertions.assertEquals(3, detailView.employeesDc.getItems().size());
        for (Employee item : detailView.employeesDc.getItems()) {
            Assertions.assertEquals(editedDepartment, item.getDepartment(),
                    "in-memory: employee '" + item.getName() + "' must reference the edited department");
        }

        detailView.saveAndCloseButton.click();

        for (Employee employee : List.of(director, manager, assistant)) {
            Employee reloaded = dataManager.load(Id.of(employee))
                    .fetchPlan(fp -> fp.addAll("name", "department.name"))
                    .one();
            Assertions.assertNotNull(reloaded.getDepartment(),
                    "db: employee '" + reloaded.getName() + "' must have a department");
            Assertions.assertEquals("department 2", reloaded.getDepartment().getName(),
                    "db: employee '" + reloaded.getName() + "' must reference the new department");
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends View<?>> T getCurrentDialogView() {
        List<View<?>> dialogs = dialogWindows.getOpenedDialogWindows().getDialogs();
        Assertions.assertFalse(dialogs.isEmpty(), "lookup dialog must be opened");
        return (T) dialogs.get(dialogs.size() - 1);
    }
}
