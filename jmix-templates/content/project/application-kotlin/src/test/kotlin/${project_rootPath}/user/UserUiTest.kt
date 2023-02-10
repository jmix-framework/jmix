package ${project_rootPackage}.user

import ${project_rootPackage}.entity.User
import ${project_rootPackage}.screen.user.UserBrowse
import ${project_rootPackage}.screen.user.UserEdit
import ${project_rootPackage}.test_support.UiIntegrationTest
import io.jmix.core.DataManager
import io.jmix.ui.component.Button
import io.jmix.ui.component.GroupTable
import io.jmix.ui.component.PasswordField
import io.jmix.ui.component.TextField
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UserUiTest : UiIntegrationTest() {

    @Autowired
    lateinit var dataManager: DataManager

    @Test
    fun test_createUser() {
        // open UserBrowse screen
        val userBrowseScreen = screens.create(UserBrowse::class.java)
        userBrowseScreen.show()

        // click "Create" button
        val createBtn = findComponent<Button>(userBrowseScreen, "createBtn")
        createBtn.click()

        // Get edit screen
        val userEditScreen = findOpenScreen(UserEdit::class.java)

        // Set username and password in the fields
        val usernameField = findComponent<TextField<String>>(userEditScreen, "usernameField")
        val username = "test-user-" + System.currentTimeMillis()
        usernameField.value = username
        val passwordField = findComponent<PasswordField>(userEditScreen, "passwordField")
        passwordField.value = "test-passwd"
        val confirmPasswordField = findComponent<PasswordField>(userEditScreen, "confirmPasswordField")
        confirmPasswordField.value = "test-passwd"

        // Click "OK"
        val commitAndCloseBtn = findComponent<Button>(userEditScreen, "commitAndCloseBtn")
        commitAndCloseBtn.click()

        // Check the created user is shown in the table
        val usersTable = findComponent<GroupTable<User>>(userBrowseScreen, "usersTable")
        val usersTableItems = usersTable.items
        val user = usersTableItems!!.items
            .find { u: User -> u.getUsername() == username }
        Assertions.assertThat(user).isNotNull
    }

    @AfterEach
    fun tearDown() {
        dataManager.load(User::class.java)
            .query("e.username like ?1", "test-user-%")
            .list()
            .forEach { u: User? -> dataManager.remove(u) }
    }
}