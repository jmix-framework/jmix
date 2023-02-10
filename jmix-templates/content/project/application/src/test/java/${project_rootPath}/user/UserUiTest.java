package ${project_rootPackage}.user;

import ${project_rootPackage}.entity.User;
import ${project_rootPackage}.screen.user.UserBrowse;
import ${project_rootPackage}.screen.user.UserEdit;
import ${project_rootPackage}.test_support.UiIntegrationTest;
import io.jmix.core.DataManager;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.PasswordField;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.data.TableItems;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Sample UI integration test for the User entity.
 */
public class UserUiTest extends UiIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Test
    void test_createUser() {
        // open UserBrowse screen
        UserBrowse userBrowseScreen = getScreens().create(UserBrowse.class);
        userBrowseScreen.show();

        // click "Create" button
        Button createBtn = findComponent(userBrowseScreen, "createBtn");
        createBtn.click();

        // Get edit screen
        UserEdit userEditScreen = findOpenScreen(UserEdit.class);

        // Set username and password in the fields
        TextField<String> usernameField = findComponent(userEditScreen, "usernameField");
        String username = "test-user-" + System.currentTimeMillis();
        usernameField.setValue(username);

        PasswordField passwordField = findComponent(userEditScreen,"passwordField");
        passwordField.setValue("test-passwd");

        PasswordField confirmPasswordField = findComponent(userEditScreen,"confirmPasswordField");
        confirmPasswordField.setValue("test-passwd");

        // Click "OK"
        Button commitAndCloseBtn = findComponent(userEditScreen, "commitAndCloseBtn");
        commitAndCloseBtn.click();

        // Check the created user is shown in the table
        GroupTable<User> usersTable = findComponent(userBrowseScreen, "usersTable");
        TableItems<User> usersTableItems = usersTable.getItems();
        usersTableItems.getItems().stream()
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
