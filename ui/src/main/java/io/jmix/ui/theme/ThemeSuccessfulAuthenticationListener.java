package io.jmix.ui.theme;

import com.vaadin.server.Page;
import io.jmix.ui.AppUI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("ui_ThemeSuccessfulAuthenticationListener")
public class ThemeSuccessfulAuthenticationListener {

    @Autowired
    protected ThemeVariantsManager variantsManager;

    @EventListener
    public void onAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        if (AppUI.getCurrent() != null && authentication.isAuthenticated()) {
            String modeCookie = variantsManager.getThemeModeCookieValue();
            String mode = variantsManager.getThemeModeUserSetting();

            String sizeCookie = variantsManager.getThemeSizeCookieValue();
            String size = variantsManager.getThemeSizeUserSetting();

            if (!Objects.equals(modeCookie, mode)
                    || !Objects.equals(sizeCookie, size)) {
                // if either modes or sizes are not equal, user settings take precedence
                variantsManager.setThemeMode(mode);
                variantsManager.setThemeSize(size);

                Page.getCurrent().reload();
            }
        }
    }
}
