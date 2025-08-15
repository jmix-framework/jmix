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

package io.jmix.flowui.action.usermenu;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.page.WebStorage;
import io.jmix.core.Messages;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.component.usermenu.UserMenu;
import io.jmix.flowui.kit.component.usermenu.TextUserMenuItem;
import io.jmix.flowui.kit.component.usermenu.UserMenuItem;
import io.jmix.flowui.kit.component.usermenu.UserMenuItem.HasSubMenu;
import io.jmix.flowui.kit.theme.ThemeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * An action for switching visual themes in a user menu. It requires the
 * {@code @JsModule("./src/theme/color-scheme-switching-support.js")} import to be
 * added to the main application class.
 */
@ActionType(UserMenuThemeSwitchAction.ID)
public class UserMenuThemeSwitchAction extends UserMenuAction<UserMenuThemeSwitchAction, UserMenu> {

    public static final String ID = "userMenu_themeSwitch";

    protected static final String SYSTEM_THEME = "system";
    protected static final String LIGHT_THEME = "light";
    protected static final String DARK_THEME = "dark";
    protected static final String MESSAGE_KEY = "actions.userMenu.ThemeSwitch";

    protected Messages messages;

    protected final Map<String, UserMenuItem> menuItems = new HashMap<>(3);
    protected HasSubMenu.SubMenu subMenu;

    public UserMenuThemeSwitchAction() {
        this(ID);
    }

    public UserMenuThemeSwitchAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        icon = createIcon(SYSTEM_THEME);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;

        this.text = messages.getMessage(MESSAGE_KEY);
    }

    @Override
    protected void setMenuItemInternal(@Nullable UserMenuItem menuItem) {
        super.setMenuItemInternal(menuItem);

        if (subMenu != null) {
            subMenu.removeAll();
            subMenu = null;
        }

        if (menuItem == null) {
            return;
        }

        if (!(menuItem instanceof HasSubMenu hasSubMenu)) {
            throw new IllegalStateException("%s does not implement %s"
                    .formatted(menuItem, HasSubMenu.class.getSimpleName()));
        }

        subMenu = hasSubMenu.getSubMenu();
        initItems(subMenu);

        WebStorage.getItem(WebStorage.Storage.LOCAL_STORAGE,
                ThemeUtils.THEME_STORAGE_KEY,
                this::updateState);
    }

    protected void initItems(HasSubMenu.SubMenu subMenu) {
        menuItems.put(SYSTEM_THEME, createItem(subMenu, SYSTEM_THEME, this::selectSystemTheme));
        menuItems.put(LIGHT_THEME, createItem(subMenu, LIGHT_THEME, this::selectLightTheme));
        menuItems.put(DARK_THEME, createItem(subMenu, DARK_THEME, this::selectDarkTheme));
    }

    protected UserMenuItem createItem(HasSubMenu.SubMenu subMenu, String theme,
                                      Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener) {
        String itemId = "%s_%sUserMenuItem".formatted(ID, theme);
        UserMenuItem menuItem = subMenu.addTextItem(itemId,
                messages.getMessage("%s.%s".formatted(MESSAGE_KEY, theme)),
                createIcon(theme),
                listener);
        menuItem.setCheckable(true);

        return menuItem;
    }

    protected Icon createIcon(String theme) {
        return switch (theme) {
            case SYSTEM_THEME -> VaadinIcon.ADJUST.create();
            case LIGHT_THEME -> VaadinIcon.SUN_O.create();
            case DARK_THEME -> VaadinIcon.MOON_O.create();
            default -> throw new IllegalStateException("Unknown theme: " + theme);
        };
    }

    protected void selectSystemTheme(UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem> event) {
        ThemeUtils.applySystemTheme();
        updateState(SYSTEM_THEME);
    }

    protected void selectLightTheme(UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem> event) {
        ThemeUtils.applyLightTheme();
        updateState(LIGHT_THEME);
    }

    protected void selectDarkTheme(UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem> event) {
        ThemeUtils.applyDarkTheme();
        updateState(DARK_THEME);
    }

    protected void updateState(@Nullable String theme) {
        String value = theme == null ? SYSTEM_THEME : theme.toLowerCase();
        menuItems.forEach((key, menuItem) ->
                menuItem.setChecked(key.equals(value)));

        setIcon(createIcon(value));
    }

    @Override
    public void execute() {
        // do nothing
    }
}
