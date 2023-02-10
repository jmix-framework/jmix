package ${project_rootPackage}.user

import ${project_rootPackage}.entity.User
import ${project_rootPackage}.test_support.AuthenticatedAsAdmin
import io.jmix.core.DataManager
import io.jmix.core.security.UserRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin::class)
class UserTest {

    @Autowired
    lateinit var dataManager: DataManager

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var userRepository: UserRepository

    var savedUser: User? = null

    @Test
    fun test_saveAndLoad() {
        // Create and save a new User
        val user = dataManager.create(User::class.java)
        user.username = "test-user-" + System.currentTimeMillis()
        user.password = passwordEncoder.encode("test-passwd")
        savedUser = dataManager.save(user)

        // Check the new user can be loaded
        val loadedUser = dataManager.load(User::class.java).id(user.id!!).one()
        Assertions.assertThat(loadedUser).isEqualTo(user)

        // Check the new user is available through UserRepository
        val userDetails = userRepository.loadUserByUsername(user.getUsername())
        Assertions.assertThat(userDetails).isEqualTo(user)
    }

    @AfterEach
    fun tearDown() {
        if (savedUser != null)
            dataManager.remove(savedUser)
    }
}