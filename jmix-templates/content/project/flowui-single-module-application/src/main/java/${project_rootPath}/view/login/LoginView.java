package ${project_rootPackage}.view.login;

import com.vaadin.flow.component.login.AbstractLogin.LoginEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.flowui.screen.StandardScreen;
import io.jmix.flowui.screen.Subscribe;
import io.jmix.flowui.screen.UiController;
import io.jmix.flowui.screen.UiDescriptor;
import io.jmix.securityflowui.authentication.AuthDetails;
import io.jmix.securityflowui.authentication.LoginScreenSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

@Route(value = "login")
@UiController("${normalizedPrefix_underscore}LoginView")
@UiDescriptor("login-view.xml")
public class LoginView extends StandardScreen {

    private final Logger log = LoggerFactory.getLogger(LoginView.class);

    @Autowired
    private LoginScreenSupport loginScreenSupport;

    @Subscribe("login")
    public void onLogin(LoginEvent event) {
        try {
            loginScreenSupport.authenticate(
                    AuthDetails.of(event.getUsername(), event.getPassword())
            );
        } catch (BadCredentialsException | DisabledException | LockedException | AccessDeniedException e) {
            log.info("Login failed", e);
            event.getSource().setError(true);
        }
    }
}
