package ${project_rootPackage}.view.login

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.login.AbstractLogin.LoginEvent
import com.vaadin.flow.component.login.LoginI18n
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinSession
import io.jmix.core.CoreProperties
import io.jmix.core.MessageTools
import io.jmix.flowui.component.loginform.JmixLoginForm
import io.jmix.flowui.kit.component.ComponentUtils
import io.jmix.flowui.kit.component.loginform.JmixLoginI18n
import io.jmix.flowui.view.*
import io.jmix.securityflowui.authentication.AuthDetails
import io.jmix.securityflowui.authentication.LoginViewSupport
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

import java.util.Locale

@Route(value = "login")
@ViewController("${normalizedPrefix_underscore}LoginView")
@ViewDescriptor("login-view.xml")
open class LoginView : StandardView(), LocaleChangeObserver {

    @Autowired
    private lateinit var coreProperties: CoreProperties

    @Autowired
    private lateinit var loginViewSupport: LoginViewSupport

    @Autowired
    private lateinit var messageBundle: MessageBundle

    @Autowired
    private lateinit var messageTools: MessageTools

    @ViewComponent
    private lateinit var login: JmixLoginForm

    @Value("\\\${ui.login.defaultUsername:}")
    private lateinit var defaultUsername: String

    @Value("\\\${ui.login.defaultPassword:}")
    private lateinit var defaultPassword: String

    private val log = LoggerFactory.getLogger(LoginView::class.java)

    @Subscribe
    fun onInit(event: InitEvent) {
        initLocales()
        initDefaultCredentials()
    }

    protected open fun initLocales() {
        val locales: MutableMap<Locale, String> =
                coreProperties.availableLocales.associateByTo(
                        mutableMapOf(), { it }, messageTools::getLocaleDisplayName)

        ComponentUtils.setItemsMap(login, locales);

        login.selectedLocale = VaadinSession.getCurrent().locale
    }

    protected open fun initDefaultCredentials() {
        if (defaultUsername.isNotBlank()) {
            login.username = defaultUsername
        }

        if (defaultPassword.isNotBlank()) {
            login.password = defaultPassword
        }
    }

    @Subscribe("login")
    fun onLogin(event: LoginEvent) {
        try {
            loginViewSupport.authenticate(
                    AuthDetails.of(event.username, event.password)
                            .withLocale(login.selectedLocale)
                            .withRememberMe(login.isRememberMe)
            )
        } catch (e: Exception) {
            log.warn("Login failed for user '{}': {}", event.username, e.toString())
            event.source.isError = true
        }
    }

    override fun localeChange(event: LocaleChangeEvent) {
        UI.getCurrent().page.setTitle(messageBundle.getMessage("LoginView.title"))

        val loginI18n: JmixLoginI18n = JmixLoginI18n.createDefault()

        val form: LoginI18n.Form = LoginI18n.Form()
        form.setTitle(messageBundle.getMessage("loginForm.headerTitle"))
        form.setUsername(messageBundle.getMessage("loginForm.username"))
        form.setPassword(messageBundle.getMessage("loginForm.password"))
        form.setSubmit(messageBundle.getMessage("loginForm.submit"))
        form.setForgotPassword(messageBundle.getMessage("loginForm.forgotPassword"))
        loginI18n.form = form

        val jmixForm: LoginI18n.JmixForm = LoginI18n.JmixForm()
        jmixForm.setRememberMe(messageBundle.getMessage("loginForm.rememberMe"))
        loginI18n.jmixForm = jmixForm

        val errorMessage: LoginI18n.ErrorMessage = LoginI18n.ErrorMessage()
        errorMessage.title = messageBundle.getMessage("loginForm.errorTitle")
        errorMessage.message = messageBundle.getMessage("loginForm.badCredentials")
        errorMessage.username = messageBundle.getMessage("loginForm.errorUsername")
        errorMessage.password = messageBundle.getMessage("loginForm.errorPassword")
        loginI18n.errorMessage = errorMessage

        login.setI18n(loginI18n)
    }
}
