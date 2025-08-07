/*
 * Copyright 2025 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.flowui.component.usermenu;

import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.security.UserRepository;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.kit.component.menubar.JmixSubMenu;
import io.jmix.flowui.kit.component.usermenu.JmixUserMenu;
import io.jmix.flowui.kit.component.usermenu.JmixUserMenuItemsDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Objects;

public class UserMenu extends JmixUserMenu<UserDetails> implements HasViewMenuItems,
        ApplicationContextAware, InitializingBean {

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

    @Override
    public ViewUserMenuItem addItem(String id, Class<?> viewClass) {
        return getItemsDelegate().addItem(id, viewClass);
    }

    @Override
    public ViewUserMenuItem addItem(String id, Class<?> viewClass, int index) {
        return getItemsDelegate().addItem(id, viewClass, -1);
    }

    @Override
    protected UserMenuItemsDelegate getItemsDelegate() {
        return (UserMenuItemsDelegate) super.getItemsDelegate();
    }

    @Override
    protected JmixUserMenuItemsDelegate createUserMenuItemsDelegate(JmixSubMenu subMenu) {
        return new UserMenuItemsDelegate(this, subMenu);
    }
}
