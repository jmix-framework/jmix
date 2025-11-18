/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import org.springframework.lang.Nullable;

import java.util.function.Consumer;

public class DialogAction extends SecuredBaseAction {

    public enum Type {
        OK("ok", "actions.Ok", JmixFontIcon.DIALOG_OK.create()),
        CANCEL("cancel", "actions.Cancel", JmixFontIcon.DIALOG_CANCEL.create()),
        YES("yes", "actions.Yes", JmixFontIcon.DIALOG_YES.create()),
        NO("no", "actions.No", JmixFontIcon.DIALOG_NO.create()),
        CLOSE("close", "actions.Close", JmixFontIcon.DIALOG_CLOSE.create());

        private final String id;
        private final String msgKey;
        private final Component icon;

        Type(String id, String msgKey, Component icon) {
            this.id = id;
            this.msgKey = msgKey;
            this.icon = icon;
        }

        public String getId() {
            return id;
        }

        public String getMsgKey() {
            return msgKey;
        }

        /**
         * @return an icon
         * @deprecated use {@link #getIcon()} instead
         */
        @Deprecated(since = "3.0", forRemoval = true)
        public VaadinIcon getVaadinIcon() {
            return switch (this) {
                case OK -> VaadinIcon.CHECK;
                case CANCEL -> VaadinIcon.BAN;
                case YES -> VaadinIcon.CHECK;
                case NO -> VaadinIcon.BAN;
                case CLOSE -> VaadinIcon.CLOSE;
            };
        }

        public Component getIcon() {
            return icon;
        }
    }

    protected final Type type;

    public DialogAction(Type type) {
        super(type.id);

        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public DialogAction withText(@Nullable String text) {
        setText(text);
        return this;
    }

    @Override
    public DialogAction withEnabled(boolean enabled) {
        setEnabled(enabled);
        return this;
    }

    @Override
    public DialogAction withVisible(boolean visible) {
        setVisible(visible);
        return this;
    }

    @Override
    public DialogAction withIcon(@Nullable Component icon) {
        return (DialogAction) super.withIcon(icon);
    }

    @Deprecated(since = "3.0", forRemoval = true)
    @Override
    public DialogAction withIcon(@Nullable Icon icon) {
        setIcon(icon);
        return this;
    }

    @Override
    public DialogAction withIcon(@Nullable VaadinIcon icon) {
        setIcon(ComponentUtils.convertToIcon(icon));
        return this;
    }

    @Override
    public DialogAction withTitle(@Nullable String title) {
        setDescription(title);
        return this;
    }

    @Override
    public DialogAction withVariant(ActionVariant actionVariant) {
        setVariant(actionVariant);
        return this;
    }

    @Override
    public DialogAction withShortcutCombination(@Nullable KeyCombination shortcutCombination) {
        setShortcutCombination(shortcutCombination);
        return this;
    }

    @Override
    public DialogAction withHandler(@Nullable Consumer<ActionPerformedEvent> handler) {
        if (handler == null) {
            if (getEventBus().hasListener(ActionPerformedEvent.class)) {
                getEventBus().removeListener(ActionPerformedEvent.class);
            }
        } else {
            addActionPerformedListener(handler);
        }

        return this;
    }

    @Override
    public DialogAction withEnabledByUiPermissions(boolean enabledByUiPermissions) {
        setEnabledByUiPermissions(enabledByUiPermissions);
        return this;
    }

    @Override
    public DialogAction withVisibleByUiPermissions(boolean visibleByUiPermissions) {
        setVisibleByUiPermissions(visibleByUiPermissions);
        return this;
    }
}
