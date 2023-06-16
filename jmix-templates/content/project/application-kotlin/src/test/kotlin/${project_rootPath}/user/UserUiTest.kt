package ${project_rootPackage}.user

import ${project_rootPackage}.entity.User
import ${project_rootPackage}.test_support.UiIntegrationTest
import ${project_rootPackage}.view.user.UserDetailView
import ${project_rootPackage}.view.user.UserListView
import io.jmix.core.DataManager
import io.jmix.flowui.component.grid.DataGrid
import io.jmix.flowui.component.textfield.JmixPasswordField
import io.jmix.flowui.component.textfield.TypedTextField
import io.jmix.flowui.data.grid.ContainerDataGridItems
import io.jmix.flowui.kit.component.button.JmixButton
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UserUiTest : UiIntegrationTest() {

    @Autowired
    lateinit var dataManager: DataManager

    @Test
    fun test_createUser() {
        // Navigate to user list view
        viewNavigators.view(UserListView::class.java)
                .navigate()

        var userListView = getCurrentView<UserListView>()

        // click "Create" button
        val createBtn = findComponent<JmixButton>(userListView, "createBtn")
        createBtn.click()

        // Get detail view
        val userDetailView = getCurrentView<UserDetailView>()

        // Set username and password in the fields
        val usernameField = findComponent<TypedTextField<String>>(userDetailView, "usernameField")
        val username = "test-user-" + System.currentTimeMillis()
        usernameField.value = username

        val passwordField = findComponent<JmixPasswordField>(userDetailView, "passwordField")
        passwordField.value = "test-passwd"

        val confirmPasswordField = findComponent<JmixPasswordField>(userDetailView, "confirmPasswordField")
        confirmPasswordField.value = "test-passwd"

        // Click "OK"
        val commitAndCloseBtn = findComponent<JmixButton>(userDetailView, "saveAndCloseBtn")
        commitAndCloseBtn.click()

        // Get navigated user list view
        userListView = getCurrentView()

        // Check the created user is shown in the table
        val usersDataGrid = findComponent<DataGrid<User>>(userListView, "usersDataGrid")

        val usersDataGridItems = usersDataGrid.items
        Assertions.assertNotNull(usersDataGridItems)

        @Suppress("UNCHECKED_CAST")
        (usersDataGridItems as ContainerDataGridItems<User>).container
                .items.stream()
                .filter { u: User -> u.getUsername().equals(username) }
                .findFirst()
                .orElseThrow()
    }

    @AfterEach
    fun tearDown() {
        dataManager.load(User::class.java)
                .query("e.username like ?1", "test-user-%")
                .list()
                .forEach { u: User? -> dataManager.remove(u) }
    }
}