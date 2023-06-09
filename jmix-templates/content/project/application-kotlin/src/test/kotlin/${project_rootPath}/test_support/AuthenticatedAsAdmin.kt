package ${project_rootPackage}.test_support

import io.jmix.core.security.SystemAuthenticator
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * JUnit extension for providing system authentication in integration tests.
 * Should be used in {@code @ExtendWith} annotation on the test class.
 */
class AuthenticatedAsAdmin : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        getSystemAuthenticator(context).begin("admin")
    }

    override fun afterEach(context: ExtensionContext) {
        getSystemAuthenticator(context).end()
    }

    private fun getSystemAuthenticator(context: ExtensionContext): SystemAuthenticator {
        val applicationContext = SpringExtension.getApplicationContext(context)
        return applicationContext.getBean(SystemAuthenticator::class.java)
    }
}
