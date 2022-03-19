package ${packageName}

import com.vaadin.ui.Dependency
import org.springframework.beans.factory.annotation.Autowired
import java.util.Locale
import io.jmix.ui.Notifications
import io.jmix.core.MessageTools
import io.jmix.core.Messages
import io.jmix.securityui.authentication.LoginScreenSupport
import io.jmix.ui.security.UiLoginProperties
import io.jmix.ui.JmixApp
import io.jmix.securityui.authentication.AuthDetails
import io.jmix.ui.action.Action
import io.jmix.ui.component.*
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import io.jmix.ui.navigation.Route
import io.jmix.ui.screen.*
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.LockedException

@UiController("${id}")
@UiDescriptor("${descriptorName}.xml")
@Route(path = "login", root = true)
open class ${controllerName} : Screen() {
    @Autowired
    private lateinit var logoImage: Image<*>

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

    private val log = LoggerFactory.getLogger(javaClass)

    @Subscribe
    private fun onInit(event: InitEvent) {
        usernameField.focus()
        initLogoImage()
        initLocalesField()
        initDefaultCredentials()
        loadStyles()
    }

    private fun initLocalesField() {
        localesField.apply {
            setOptionsMap(messageTools.availableLocalesMap)
            value = app.locale
            addValueChangeListener(this@${controllerName}::onLocalesFieldValueChangeEvent)
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
        if (defaultUsername.isNotBlank() && "<disabled>" != defaultUsername) {
            usernameField.setValue(defaultUsername)
        } else {
            usernameField.setValue("")
        }
        val defaultPassword = loginProperties.defaultPassword
        if (!StringUtils.isBlank(defaultPassword) && "<disabled>" != defaultPassword) {
            passwordField.value = defaultPassword
        } else {
            passwordField.value = ""
        }
    }

    @Subscribe("submit")
    private fun onSubmitActionPerformed(event: Action.ActionPerformedEvent) {
        login()
    }

    private fun login() {
        val username = usernameField.value
        val password = passwordField.value
        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            notifications.create(Notifications.NotificationType.WARNING)
                .withCaption(messages.getMessage(javaClass, "emptyUsernameOrPassword"))
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
                    log.info("Login failed", e)
                    notifications.create(Notifications.NotificationType.ERROR)
                        .withCaption(messages.getMessage(javaClass, "loginFailed"))
                        .withDescription(messages.getMessage(javaClass, "badCredentials"))
                        .show()
                }
                else -> throw e
            }
        }
    }

    private fun loadStyles() {
        ScreenDependencyUtils.addScreenDependency(
            this,
            "vaadin://brand-login-screen/login.css", Dependency.Type.STYLESHEET
        )
    }

    private fun initLogoImage() {
        logoImage.setSource(RelativePathResource::class.java).path =
            "VAADIN/brand-login-screen/jmix-icon-login.svg"
    }
}
