package ${project_rootPackage}.user;

import ${project_rootPackage}.${project_classPrefix}Application;
import ${project_rootPackage}.entity.User;
import ${project_rootPackage}.view.user.UserDetailView;
import ${project_rootPackage}.view.user.UserListView;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.JmixPasswordField;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.tabbedmode.ViewBuilders;
import io.jmix.tabbedmode.testassist.TabbedModeTestAssistConfiguration;
import io.jmix.tabbedmode.testassist.UiTest;
import io.jmix.tabbedmode.testassist.UiTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Sample UI integration test for the User entity.
 */
@UiTest
@SpringBootTest(classes = {${project_classPrefix}Application.class, TabbedModeTestAssistConfiguration.class})
public class UserUiTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    ViewBuilders viewBuilders;

    @Test
    void test_createUser() {
        // Navigate to user list view
        viewBuilders.view(UiTestUtils.getCurrentView(), UserListView.class).open();

        UserListView userListView = UiTestUtils.getCurrentView();

        // click "Create" button
        JmixButton createBtn = UiTestUtils.getComponent(userListView, "createButton");
        createBtn.click();

        // Get detail view
        UserDetailView userDetailView = UiTestUtils.getCurrentView();

        // Set username and password in the fields
        TypedTextField<String> usernameField = UiTestUtils.getComponent(userDetailView, "usernameField");
        String username = "test-user-" + System.currentTimeMillis();
        usernameField.setValue(username);

        JmixPasswordField passwordField = UiTestUtils.getComponent(userDetailView, "passwordField");
        passwordField.setValue("test-passwd");

        JmixPasswordField confirmPasswordField = UiTestUtils.getComponent(userDetailView, "confirmPasswordField");
        confirmPasswordField.setValue("test-passwd");

        // Click "OK"
        JmixButton commitAndCloseBtn = UiTestUtils.getComponent(userDetailView, "saveAndCloseButton");
        commitAndCloseBtn.click();

        // Get navigated user list view
        userListView = UiTestUtils.getCurrentView();

        // Check the created user is shown in the table
        DataGrid<User> usersDataGrid = UiTestUtils.getComponent(userListView, "usersDataGrid");

        DataGridItems<User> usersDataGridItems = usersDataGrid.getItems();
        Assertions.assertNotNull(usersDataGridItems);

        usersDataGridItems.getItems().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow();
    }

    @AfterEach
    void tearDown() {
        dataManager.load(User.class)
                .query("e.username like ?1", "test-user-%")
                .list()
                .forEach(u -> dataManager.remove(u));
    }
}
