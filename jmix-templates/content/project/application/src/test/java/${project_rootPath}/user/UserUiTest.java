package ${project_rootPackage}.user;

import ${project_rootPackage}.entity.User;
import ${project_rootPackage}.test_support.UiIntegrationTest;
import ${project_rootPackage}.view.user.UserDetailView;
import ${project_rootPackage}.view.user.UserListView;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.JmixPasswordField;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserUiTest extends UiIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Test
    void test_createUser() {
        // Navigate to user list view
        getViewNavigators()
                .view(UserListView.class)
                .navigate();

        UserListView userListView = getCurrentView();

        // click "Create" button
        JmixButton createBtn = findComponent(userListView, "createBtn");
        createBtn.click();

        // Get detail view
        UserDetailView userDetailView = getCurrentView();

        // Set username and password in the fields
        TypedTextField<String> usernameField = findComponent(userDetailView, "usernameField");
        String username = "test-user-" + System.currentTimeMillis();
        usernameField.setValue(username);

        JmixPasswordField passwordField = findComponent(userDetailView, "passwordField");
        passwordField.setValue("test-passwd");

        JmixPasswordField confirmPasswordField = findComponent(userDetailView, "confirmPasswordField");
        confirmPasswordField.setValue("test-passwd");

        // Click "OK"
        JmixButton commitAndCloseBtn = findComponent(userDetailView, "saveAndCloseBtn");
        commitAndCloseBtn.click();

        // Get navigated user list view
        userListView = getCurrentView();

        // Check the created user is shown in the table
        DataGrid<User> usersDataGrid = findComponent(userListView, "usersDataGrid");

        DataUnit usersDataGridItems = usersDataGrid.getItems();
        Assertions.assertNotNull(usersTableItems);

        //noinspection unchecked
        ((ContainerDataGridItems<User>) usersDataGridItems).getContainer()
                .getItems().stream()
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
