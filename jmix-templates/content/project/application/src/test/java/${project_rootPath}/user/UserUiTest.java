package ${project_rootPackage}.user;

import ${project_rootPackage}.${project_classPrefix}Application;
import ${project_rootPackage}.entity.User;
import ${project_rootPackage}.view.user.UserDetailView;
import ${project_rootPackage}.view.user.UserListView;
import com.vaadin.flow.component.Component;
import io.jmix.core.DataManager;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.JmixPasswordField;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

/**
 * Sample UI integration test for the User entity.
 */
@UiTest
@SpringBootTest(classes = {${project_classPrefix}Application.class, FlowuiTestAssistConfiguration.class})
public class UserUiTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    ViewNavigators viewNavigators;

    @Test
    void test_createUser() {
        // Navigate to user list view
        viewNavigators.view(UserListView.class).navigate();

        UserListView userListView = UiTestUtils.getCurrentView();

        // click "Create" button
        JmixButton createBtn = findComponent(userListView, "createBtn");
        createBtn.click();

        // Get detail view
        UserDetailView userDetailView = UiTestUtils.getCurrentView();

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
        userListView = UiTestUtils.getCurrentView();

        // Check the created user is shown in the table
        DataGrid<User> usersDataGrid = findComponent(userListView, "usersDataGrid");

        DataUnit usersDataGridItems = usersDataGrid.getItems();
        Assertions.assertNotNull(usersDataGridItems);

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

    /**
     * Returns a component defined in the screen by the component id.
     * Throws an exception if not found.
     */
    @SuppressWarnings("unchecked")
    private <T> T findComponent(View<?> view, String componentId) {
        Optional<Component> component = UiComponentUtils.findComponent(view, componentId);
        Assertions.assertTrue(component.isPresent());
        return (T) component.get();
    }
}
