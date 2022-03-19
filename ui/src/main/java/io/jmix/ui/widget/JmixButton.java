/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.widget;

import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import io.jmix.ui.AppUI;
import io.jmix.ui.widget.client.button.JmixButtonClientRpc;
import io.jmix.ui.widget.client.button.JmixButtonState;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class JmixButton extends com.vaadin.ui.Button {

    protected Consumer<MouseEventDetails> clickHandler;

    public JmixButton() {
    }

    public JmixButton(String caption) {
        super(caption);
    }

    public JmixButton(String caption, ClickListener listener) {
        super(caption, listener);
    }

    @Override
    protected JmixButtonState getState() {
        return (JmixButtonState) super.getState();
    }

    @Override
    protected JmixButtonState getState(boolean markAsDirty) {
        return (JmixButtonState) super.getState(markAsDirty);
    }

    @Override
    protected void fireClick() {
        // check if it cannot be clicked at all due to modal dialogs
        AppUI ui = (AppUI) getUI();
        if (ui.isAccessibleForUser(this)) {
            if (clickHandler != null) {
                clickHandler.accept(null);
            } else {
                super.fireClick();
            }
        } else {
            LoggerFactory.getLogger(JmixButton.class)
                    .debug("Ignore click because button is inaccessible for user");
        }
    }

    @Override
    protected void fireClick(MouseEventDetails details) {
        // check if it cannot be clicked at all due to modal dialogs
        AppUI ui = (AppUI) getUI();
        if (ui.isAccessibleForUser(this)) {
            try {
                if (clickHandler != null) {
                    clickHandler.accept(details);
                } else {
                    super.fireClick(details);
                }
            } finally {
                if (getState(false).useResponsePending) {
                    getRpcProxy(JmixButtonClientRpc.class).onClickHandled();
                }
            }
        } else {
            LoggerFactory.getLogger(JmixButton.class)
                    .debug("Ignore click because button is inaccessible for user");
        }
    }

    public Consumer<MouseEventDetails> getClickHandler() {
        return clickHandler;
    }

    public void setClickHandler(Consumer<MouseEventDetails> clickHandler) {
        this.clickHandler = clickHandler;
    }

    public boolean isUseResponsePending() {
        return getState(false).useResponsePending;
    }

    public void setUseResponsePending(boolean useResponsePending) {
        if (isUseResponsePending() != useResponsePending) {
            getState().useResponsePending = useResponsePending;
        }
    }

    @Override
    public void setClickShortcut(int keyCode, int... modifiers) {
        if (clickShortcut != null) {
            removeShortcutListener(clickShortcut);
        }
        clickShortcut = new JmixClickShortcut(this, keyCode, modifiers);
        addShortcutListener(clickShortcut);
        getState().clickShortcutKeyCode = clickShortcut.getKeyCode();
    }

    protected static class JmixClickShortcut extends ClickShortcut {
        public JmixClickShortcut(Button button, int keyCode, int... modifiers) {
            super(button, keyCode, modifiers);
        }

        @Override
        public void handleAction(Object sender, Object target) {
            if (target instanceof Component) {
                Component targetTopLevelComponent = AppUIUtils.getWindowOrUI((Component) target);
                Component buttonTopLevelComponent = AppUIUtils.getWindowOrUI(button);

                if (targetTopLevelComponent == buttonTopLevelComponent) {
                    super.handleAction(sender, target);
                }
            }
        }
    }
}
