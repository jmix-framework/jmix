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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.security.UserRepository;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.component.menubar.JmixMenuItem;
import io.jmix.flowui.kit.component.menubar.JmixSubMenu;
import io.jmix.flowui.kit.component.usermenu.JmixUserMenu;
import io.jmix.flowui.kit.component.usermenu.JmixUserMenuItemsDelegate;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.WindowBuilder;
import io.jmix.flowui.view.navigation.ViewNavigator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Objects;

/**
 * A component that displays a user menu as a dropdown.
 */
public class UserMenu extends JmixUserMenu<UserDetails> implements HasViewMenuItems,
        ApplicationContextAware, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(UserMenu.class);

    protected static final String SUBSTITUTED_THEME_NAME = "substituted";

    protected ApplicationContext applicationContext;

    protected UiComponents uiComponents;
    protected MetadataTools metadataTools;
    protected UserRepository userRepository;
    protected CurrentUserSubstitution currentUserSubstitution;

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
        uiComponents = applicationContext.getBean(UiComponents.class);
        metadataTools = applicationContext.getBean(MetadataTools.class);
        userRepository = applicationContext.getBean(UserRepository.class);
        currentUserSubstitution = applicationContext.getBean(CurrentUserSubstitution.class);
    }

    protected void initComponent() {
        setButtonRenderer(this::defaultButtonRenderer);

        UserDetails user = loadUser();
        setUser(user);
    }

    @Nullable
    protected Component defaultButtonRenderer(@Nullable UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }

        Div wrapper = uiComponents.create(Div.class);
        wrapper.setClassName(BASE_CLASS_NAME + "-button-content");

        Avatar avatar = uiComponents.create(Avatar.class);
        avatar.setName(userDetails.getUsername());
        avatar.getElement().setAttribute("tabindex", "-1");
        avatar.setClassName(BASE_CLASS_NAME + "-button-content-user-avatar");

        Span name = uiComponents.create(Span.class);
        name.setText(generateUserName(userDetails));
        name.setClassName(BASE_CLASS_NAME + "-button-content-user-name");

        wrapper.add(avatar, name);
        return wrapper;
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
        UserDetails authenticatedUser = currentUserSubstitution.getAuthenticatedUser();
        if (user == null || Objects.equals(authenticatedUser.getUsername(), user.getUsername())) {
            getThemeNames().remove(SUBSTITUTED_THEME_NAME);
        } else {
            getThemeNames().add(SUBSTITUTED_THEME_NAME);
        }
    }

    protected String generateUserName(UserDetails user) {
        if (EntityValues.isEntity(user)) {
            return metadataTools.getInstanceName(user);
        } else {
            return user.getUsername();
        }
    }

    @Override
    public ViewUserMenuItem addViewItem(String id, Class<? extends View<?>> viewClass, String text) {
        return getItemsDelegate().addViewItem(id, viewClass, text);
    }

    @Override
    public ViewUserMenuItem addViewItem(String id, Class<? extends View<?>> viewClass, String text, int index) {
        return getItemsDelegate().addViewItem(id, viewClass, text, index);
    }

    @Override
    public ViewUserMenuItem addViewItem(String id,
                                        Class<? extends View<?>> viewClass,
                                        String text, Component icon) {
        return getItemsDelegate().addViewItem(id, viewClass, text, icon);
    }

    @Override
    public ViewUserMenuItem addViewItem(String id,
                                        Class<? extends View<?>> viewClass,
                                        String text, Component icon,
                                        int index) {
        return getItemsDelegate().addViewItem(id, viewClass, text, icon, index);
    }

    @Override
    public ViewUserMenuItem addViewItem(String id, String viewId, String text) {
        return getItemsDelegate().addViewItem(id, viewId, text);
    }

    @Override
    public ViewUserMenuItem addViewItem(String id, String viewId, String text, int index) {
        return getItemsDelegate().addViewItem(id, viewId, text, index);
    }

    @Override
    public ViewUserMenuItem addViewItem(String id, String viewId,
                                        String text, Component icon) {
        return getItemsDelegate().addViewItem(id, viewId, text, icon);
    }

    @Override
    public ViewUserMenuItem addViewItem(String id, String viewId,
                                        String text, Component icon,
                                        int index) {
        return getItemsDelegate().addViewItem(id, viewId, text, icon, index);
    }

    @Override
    protected UserMenuItemsDelegate getItemsDelegate() {
        return (UserMenuItemsDelegate) super.getItemsDelegate();
    }

    @Override
    protected JmixUserMenuItemsDelegate createUserMenuItemsDelegate(JmixSubMenu subMenu) {
        return applicationContext.getBean(UserMenuItemsDelegate.class, this, subMenu);
    }

    protected static class ViewUserMenuItemImpl extends AbstractTextUserMenuItem implements ViewUserMenuItem {

        protected String viewId;
        protected Class<? extends View<?>> viewClass;

        protected OpenMode openMode;

        protected ViewNavigators viewNavigators;
        protected DialogWindows dialogWindows;

        public ViewUserMenuItemImpl(String id,
                                    JmixUserMenu<?> userMenu,
                                    JmixMenuItem item,
                                    String text,
                                    Class<? extends View<?>> viewClass,
                                    ViewNavigators viewNavigators,
                                    DialogWindows dialogWindows) {
            this(id, userMenu, item, text, viewNavigators, dialogWindows);

            this.viewClass = viewClass;
        }

        public ViewUserMenuItemImpl(String id,
                                    JmixUserMenu<?> userMenu,
                                    JmixMenuItem item,
                                    String text,
                                    String viewId,
                                    ViewNavigators viewNavigators,
                                    DialogWindows dialogWindows) {
            this(id, userMenu, item, text, viewNavigators, dialogWindows);

            this.viewId = viewId;
        }

        protected ViewUserMenuItemImpl(String id,
                                       JmixUserMenu<?> userMenu,
                                       JmixMenuItem item,
                                       String text,
                                       ViewNavigators viewNavigators,
                                       DialogWindows dialogWindows) {
            super(id, userMenu, item, text);

            this.viewNavigators = viewNavigators;
            this.dialogWindows = dialogWindows;

            initItem();
        }

        protected void initItem() {
            item.addClickListener(this::openView);
        }

        protected void openView(com.vaadin.flow.component.ClickEvent<MenuItem> event) {
            if (viewId == null && viewClass == null) {
                throw new IllegalStateException("Either 'viewId' or 'viewClass' must be set");
            }

            if (openMode == OpenMode.DIALOG
                    || UiComponentUtils.isComponentAttachedToDialog(userMenu)) {
                openDialog();
            } else {
                navigate();
            }
        }

        protected void openDialog() {
            View<?> origin = UiComponentUtils.getView(userMenu);

            WindowBuilder<?> builder = viewId != null
                    ? dialogWindows.view(origin, viewId)
                    : dialogWindows.view(origin, viewClass);

            builder.open();
        }

        protected void navigate() {
            View<?> origin = UiComponentUtils.getView(userMenu);

            ViewNavigator navigator = viewId != null
                    ? viewNavigators.view(origin, viewId)
                    : viewNavigators.view(origin, viewClass);

            navigator.navigate();
        }

        @Override
        public String getText() {
            return super.getText();
        }

        @Override
        public void setText(String text) {
            super.setText(text);
        }

        @Nullable
        @Override
        public Component getIcon() {
            return super.getIcon();
        }

        @Override
        public void setIcon(@Nullable Component icon) {
            super.setIcon(icon);
        }

        @Nullable
        @Override
        public OpenMode getOpenMode() {
            return openMode;
        }

        @Override
        public void setOpenMode(@Nullable OpenMode openMode) {
            this.openMode = openMode;
        }

        @Nullable
        @Override
        public String getViewId() {
            return viewId;
        }

        @Nullable
        @Override
        public Class<? extends View<?>> getViewClass() {
            return viewClass;
        }
    }
}
