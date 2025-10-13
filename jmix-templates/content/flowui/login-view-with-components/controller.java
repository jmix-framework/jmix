package ${packageName};

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.CoreProperties;
import io.jmix.core.MessageTools;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.JmixPasswordField;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.jmix.securityflowui.authentication.AuthDetails;
import io.jmix.securityflowui.authentication.LoginViewSupport;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

@Route(value = "login")
@ViewController(id = "${id}")
@ViewDescriptor(path = "${descriptorName}.xml")
@CssImport("./styles/login-view/login-view.css")
public class ${controllerName} extends StandardView implements LocaleChangeObserver {

    private static final Logger log = LoggerFactory.getLogger(${controllerName}.class);

    @ViewComponent
    private Div header;
    @ViewComponent
    private TypedTextField<String> usernameField;
    @ViewComponent
    private JmixPasswordField passwordField;
    @ViewComponent
    private JmixCheckbox rememberMe;
    @ViewComponent
    private JmixSelect<Locale> localeSelect;
    @ViewComponent
    private JmixButton submitBtn;
    @ViewComponent
    private Div errorMessage;
    @ViewComponent
    private H5 errorMessageTitle;
    @ViewComponent
    private Paragraph errorMessageDescription;

    @Autowired
    private CoreProperties coreProperties;
    @Autowired
    private LoginViewSupport loginViewSupport;
    @Autowired
    private MessageTools messageTools;
    @ViewComponent
    private MessageBundle messageBundle;
    @Autowired
    private ViewValidation viewValidation;

    @Value("\${ui.login.defaultUsername:}")
    private String defaultUsername;

    @Value("\${ui.login.defaultPassword:}")
    private String defaultPassword;

    @Subscribe
    public void onInit(final InitEvent event) {
        initLocales();
        initDefaultCredentials();
    }

    private void initLocales() {
        LinkedHashMap<Locale, String> locales = coreProperties.getAvailableLocales().stream()
                .collect(Collectors.toMap(Function.identity(), messageTools::getLocaleDisplayName, (s1, s2) -> s1,
                        LinkedHashMap::new));

        ComponentUtils.setItemsMap(localeSelect, locales);

        localeSelect.setValue(VaadinSession.getCurrent().getLocale());
    }

    private void initDefaultCredentials() {
        if (StringUtils.isNotBlank(defaultUsername)) {
            usernameField.setTypedValue(defaultUsername);
        }

        if (StringUtils.isNotBlank(defaultPassword)) {
            passwordField.setValue(defaultPassword);
        }
    }

    @Subscribe("localeSelect")
    public void onLocaleSelectComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixSelect<Locale>, Locale> event) {
        Locale locale = event.getValue();
        VaadinSession.getCurrent().setLocale(locale);
    }

    @Subscribe(id = "submitBtn", subject = "clickListener")
    public void onSubmitBtnClick(final ClickEvent<JmixButton> event) {
        errorMessage.setVisible(false);

        ValidationErrors validationErrors =
                viewValidation.validateUiComponents(List.of(usernameField, passwordField));

        String username = usernameField.getValue();
        String password = passwordField.getValue();

        if (!validationErrors.isEmpty()
                || Strings.isNullOrEmpty(username)
                || Strings.isNullOrEmpty(password)) {
            return;
        }

        try {
            loginViewSupport.authenticate(
                    AuthDetails.of(username, password)
                            .withLocale(localeSelect.getValue())
                            .withRememberMe(rememberMe.getValue())
            );
        } catch (final BadCredentialsException | DisabledException | LockedException | AccessDeniedException e) {
            log.warn("Login failed for user '{}': {}", username, e.toString());

            errorMessageTitle.setText(messageBundle.getMessage("loginForm.errorTitle"));
            errorMessageDescription.setText(messageBundle.getMessage("loginForm.badCredentials"));
            errorMessage.setVisible(true);
        }
    }

    @Override
    public void localeChange(final LocaleChangeEvent event) {
        UI.getCurrent().getPage().setTitle(messageBundle.getMessage("${studioUtils.decapitalize(controllerName)}.title"));

        header.setText(messageBundle.getMessage("loginForm.headerTitle"));
        usernameField.setLabel(messageBundle.getMessage("loginForm.username"));
        usernameField.setRequiredMessage(messageBundle.getMessage("loginForm.errorUsername"));
        passwordField.setLabel(messageBundle.getMessage("loginForm.password"));
        passwordField.setRequiredMessage(messageBundle.getMessage("loginForm.errorPassword"));
        rememberMe.setLabel(messageBundle.getMessage("loginForm.rememberMe"));
        submitBtn.setText(messageBundle.getMessage("loginForm.submit"));
    }
}