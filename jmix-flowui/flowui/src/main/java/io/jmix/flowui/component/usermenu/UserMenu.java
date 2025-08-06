package io.jmix.flowui.component.usermenu;

import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.security.UserRepository;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.action.usermenu.UserMenuAction;
import io.jmix.flowui.kit.component.usermenu.ActionUserMenuItem;
import io.jmix.flowui.kit.component.usermenu.JmixUserMenu;
import io.jmix.flowui.kit.component.usermenu.UserMenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Objects;

public class UserMenu extends JmixUserMenu<UserDetails> implements ApplicationContextAware, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(UserMenu.class);

    protected static final String SUBSTITUTED_THEME_NAME = "substituted";

    protected ApplicationContext applicationContext;
    protected CurrentUserSubstitution currentUserSubstitution;
    protected UserRepository userRepository;
    protected MetadataTools metadataTools;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        currentUserSubstitution = applicationContext.getBean(CurrentUserSubstitution.class);
        userRepository = applicationContext.getBean(UserRepository.class);
        metadataTools = applicationContext.getBean(MetadataTools.class);
    }

    protected void initComponent() {
        UserDetails user = loadUser();
        setUser(user);
    }

    protected UserDetails loadUser() {
        UserDetails user = currentUserSubstitution.getEffectiveUser();
        try {
            user = userRepository.loadUserByUsername(user.getUsername());
        } catch (UsernameNotFoundException e) {
            log.error("User repository doesn't contain a user with username {}", user.getUsername());
        }

        return user;
    }

    @Override
    protected void userChangedInternal() {
        super.userChangedInternal();

        updateSubstitutedState();
    }

    protected void updateSubstitutedState() {
        // TODO: gg, probably move to the action
        UserDetails authenticatedUser = currentUserSubstitution.getAuthenticatedUser();
        if (user == null || Objects.equals(authenticatedUser.getUsername(), user.getUsername())) {
            getThemeNames().remove(SUBSTITUTED_THEME_NAME);
        } else {
            getThemeNames().add(SUBSTITUTED_THEME_NAME);
        }
    }

    protected String generateUserTitle(UserDetails user) {
        if (EntityValues.isEntity(user)) {
            return metadataTools.getInstanceName(user);
        } else {
            return user.getUsername();
        }
    }

    public UserMenuItem addItem(String id, Class<?> viewClass) {
        return addItem(id, viewClass, -1);
    }

    public UserMenuItem addItem(String id, Class<?> viewClass, int index) {
        // TODO: gg, implement
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void attachItem(UserMenuItem item) {
        super.attachItem(item);

        if (item instanceof ActionUserMenuItem actionUserMenuItem) {
            if (actionUserMenuItem.getAction() instanceof UserMenuAction userMenuAction) {
                userMenuAction.setMenuItem(actionUserMenuItem);
                userMenuAction.setTarget(this);
            }
        }
    }

    @Override
    protected void detachItem(UserMenuItem menuItem) {
        super.detachItem(menuItem);

        if (menuItem instanceof ActionUserMenuItem actionUserMenuItem) {
            if (actionUserMenuItem.getAction() instanceof UserMenuAction<?, ?> userMenuAction) {
                userMenuAction.setMenuItem(null);
                userMenuAction.setTarget(null);
            }
        }
    }
}
