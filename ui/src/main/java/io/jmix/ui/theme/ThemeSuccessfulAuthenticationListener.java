package io.jmix.ui.theme;

import com.vaadin.server.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("ui_ThemeSuccessfulAuthenticationListener")
public class ThemeSuccessfulAuthenticationListener {

    @Autowired
    protected HeliumThemeVariantsManager variantsManager;

    @EventListener
    public void onAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        if (authentication.isAuthenticated()) {
            String modeCookie = variantsManager.getUserAppThemeMode();
            String mode = variantsManager.loadUserAppThemeModeSetting();

            String sizeCookie = variantsManager.getUserAppThemeSize();
            String size = variantsManager.loadUserAppThemeSizeSetting();

            if (!Objects.equals(modeCookie, mode)
                    || !Objects.equals(sizeCookie, size)) {
                // if either modes or sizes are not equal, user settings take precedence
                variantsManager.setUserAppThemeMode(mode);
                variantsManager.setUserAppThemeSize(size);

                Page.getCurrent().reload();
            }
        }
    }
}
