package ${project_rootPackage}.screen.login

import io.jmix.core.MessageTools
import io.jmix.core.Messages
import io.jmix.securityui.authentication.AuthDetails
import io.jmix.securityui.authentication.LoginScreenSupport
import io.jmix.ui.JmixApp
import io.jmix.ui.Notifications
import io.jmix.ui.action.Action.ActionPerformedEvent
import io.jmix.ui.component.*
import io.jmix.ui.navigation.Route
import io.jmix.ui.screen.*
import io.jmix.ui.security.UiLoginProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import java.util.*

@UiController("${normalizedPrefix_underscore}LoginScreen")
@UiDescriptor("login-screen.xml")
@Route(path = "login", root = true)
open class LoginScreen : Screen() {

    @Autowired
    private lateinit var usernameField: TextField<String>

    @Autowired
    private lateinit var passwordField: PasswordField

    @Autowired
    private lateinit var rememberMeCheckBox: CheckBox

    @Autowired
    private lateinit var localesField: ComboBox<Locale>

    @Autowired
    private lateinit var notifications: Notifications

    @Autowired
    private lateinit var messages: Messages

    @Autowired
    private lateinit var messageTools: MessageTools

    @Autowired
    private lateinit var loginScreenSupport: LoginScreenSupport

    @Autowired
    private lateinit var loginProperties: UiLoginProperties

    @Autowired
    private lateinit var app: JmixApp

    private val log = LoggerFactory.getLogger(LoginScreen::class.java)

    @Subscribe
    private fun onInit(event: InitEvent) {
        usernameField.focus()
        initLocalesField()
        initDefaultCredentials()
    }

    private fun initLocalesField() {
        localesField.apply {
            setOptionsMap(messageTools.availableLocalesMap)
            value = app.locale
            addValueChangeListener(this@LoginScreen::onLocalesFieldValueChangeEvent)
        }
    }

    private fun onLocalesFieldValueChangeEvent(event: HasValue.ValueChangeEvent<Locale>) {
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        app.locale = event.value
        UiControllerUtils.getScreenContext(this).screens
                .create(this.javaClass, OpenMode.ROOT)
                .show()
    }

    private fun initDefaultCredentials() {
        val defaultUsername = loginProperties.defaultUsername
        if (!defaultUsername.isNullOrBlank() && "<disabled>" != defaultUsername) {
            usernameField.setValue(defaultUsername)
        } else {
            usernameField.value = ""
        }
        val defaultPassword = loginProperties.defaultPassword
        if (!defaultPassword.isNullOrBlank() && "<disabled>" != defaultPassword) {
            passwordField.value = defaultPassword
        } else {
            passwordField.value = ""
        }
    }

    private fun getMessage(key: String) =
        messages.getMessage(javaClass, key) ?: ""

    @Subscribe("submit")
    private fun onSubmitActionPerformed(event: ActionPerformedEvent) {
        login()
    }

    private fun login() {
        val username = usernameField.value
        val password = passwordField.value
        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            notifications.create(Notifications.NotificationType.WARNING)
                .withCaption(getMessage("emptyUsernameOrPassword"))
                .show()
            return
        }
        try {
            loginScreenSupport.authenticate(
                AuthDetails.of(username, password)
                    .withLocale(localesField.value)
                    .withRememberMe(rememberMeCheckBox.isChecked), this
            )
        } catch (e: Exception) {
            when (e) {
                is BadCredentialsException,
                is DisabledException,
                is LockedException -> {
                    log.warn("Login failed for user '{}': {}", username, e.toString())
                    notifications.create(Notifications.NotificationType.ERROR)
                        .withCaption(getMessage("loginFailed"))
                        .withDescription(getMessage("badCredentials"))
                        .show()
                }
                else -> throw e
            }
        }
    }
}