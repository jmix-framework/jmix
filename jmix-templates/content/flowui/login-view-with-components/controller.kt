package ${packageName}

import com.google.common.base.Strings
import com.vaadin.flow.component.AbstractField
import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H5
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinSession
import io.jmix.core.CoreProperties
import io.jmix.core.MessageTools
import io.jmix.core.security.AccessDeniedException
import io.jmix.flowui.component.checkbox.JmixCheckbox
import io.jmix.flowui.component.select.JmixSelect
import io.jmix.flowui.component.textfield.JmixPasswordField
import io.jmix.flowui.component.textfield.TypedTextField
import io.jmix.flowui.component.validation.ValidationErrors
import io.jmix.flowui.kit.component.ComponentUtils
import io.jmix.flowui.kit.component.button.JmixButton
import io.jmix.flowui.view.*
import io.jmix.securityflowui.authentication.AuthDetails
import io.jmix.securityflowui.authentication.LoginViewSupport
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException

import java.util.LinkedHashMap
import java.util.Locale
import java.util.function.Function
import java.util.stream.Collectors

@Route(value = "login")
@ViewController(id = "${id}")
@ViewDescriptor(path = "${descriptorName}.xml")
open class ${controllerName} : StandardView(), LocaleChangeObserver {

    @ViewComponent
    private lateinit var header: Div

    @ViewComponent
    private lateinit var usernameField: TypedTextField<String>

    @ViewComponent
    private lateinit var passwordField: JmixPasswordField

    @ViewComponent
    private lateinit var rememberMe: JmixCheckbox

    @ViewComponent
    private lateinit var localeSelect: JmixSelect<Locale>

    @ViewComponent
    private lateinit var submitBtn: JmixButton

    @ViewComponent
    private lateinit var errorMessage: Div

    @ViewComponent
    private lateinit var errorMessageTitle: H5

    @ViewComponent
    private lateinit var errorMessageDescription: Paragraph

    @Autowired
    private lateinit var coreProperties: CoreProperties

    @Autowired
    private lateinit var loginViewSupport: LoginViewSupport

    @Autowired
    private lateinit var messageTools: MessageTools

    @ViewComponent
    private lateinit var messageBundle: MessageBundle

    @Autowired
    private lateinit var viewValidation: ViewValidation

    @Value("\\\${ui.login.defaultUsername:}")
    private lateinit var defaultUsername: String

    @Value("\\\${ui.login.defaultPassword:}")
    private lateinit var defaultPassword: String

    private val log = LoggerFactory.getLogger(${controllerName}::class.java)

    @Subscribe
    fun onInit(event: InitEvent) {
        initLocales()
        initDefaultCredentials()
    }

    private fun initLocales() {
        val locales: MutableMap<Locale, String> =
            coreProperties.availableLocales.associateByTo(
                mutableMapOf(), { it }, messageTools::getLocaleDisplayName)

        ComponentUtils.setItemsMap(localeSelect, locales);

        localeSelect.value = VaadinSession.getCurrent().locale
    }

    private fun initDefaultCredentials() {
        if (defaultUsername.isNotBlank()) {
            usernameField.typedValue = defaultUsername
        }

        if (defaultPassword.isNotBlank()) {
            passwordField.value = defaultPassword
        }
    }

    @Subscribe("localeSelect")
    fun onLocaleSelectComponentValueChange(event: AbstractField.ComponentValueChangeEvent<JmixSelect<Locale>, Locale>) {
        event.value?.let { locale ->
            VaadinSession.getCurrent().locale = locale
        }
    }

    @Subscribe(id = "submitBtn", subject = "clickListener")
    fun onSubmitBtnClick(event: ClickEvent<JmixButton>) {
        errorMessage.isVisible = false

        val validationErrors = viewValidation.validateUiComponents(listOf(usernameField, passwordField))

        val username = usernameField.value
        val password = passwordField.value

        if (validationErrors.isNotEmpty() || username.isNullOrEmpty() || password.isNullOrEmpty()) {
            return
        }

        try {
            loginViewSupport.authenticate(
                AuthDetails.of(username, password)
                    .withLocale(localeSelect.value)
                    .withRememberMe(rememberMe.value)
            )
        } catch (e: Exception) {
            log.warn("Login failed for user '{}': {}", event.username, e.toString())

            errorMessageTitle.text = messageBundle.getMessage("loginForm.errorTitle")
            errorMessageDescription.text = messageBundle.getMessage("loginForm.badCredentials")
            errorMessage.isVisible = true
        }
    }

    override fun localeChange(event: LocaleChangeEvent) {
        UI.getCurrent().page.setTitle(messageBundle.getMessage("${studioUtils.decapitalize(controllerName)}.title"))

        header.text = messageBundle.getMessage("loginForm.headerTitle")
        usernameField.label = messageBundle.getMessage("loginForm.username")
        usernameField.requiredMessage = messageBundle.getMessage("loginForm.errorUsername")
        passwordField.label = messageBundle.getMessage("loginForm.password")
        passwordField.requiredMessage = messageBundle.getMessage("loginForm.errorPassword")
        rememberMe.label = messageBundle.getMessage("loginForm.rememberMe")
        submitBtn.text = messageBundle.getMessage("loginForm.submit")
    }
}