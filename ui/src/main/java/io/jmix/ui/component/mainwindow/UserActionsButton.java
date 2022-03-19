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

package io.jmix.ui.component.mainwindow;

import io.jmix.ui.component.Component;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.StudioComponent;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * A component that combines link to Login screen and authenticated user menu.
 */
@StudioComponent(
        caption = "UserActionsButton",
        category = "Main window",
        xmlElement = "userActionsButton",
        icon = "io/jmix/ui/icon/mainwindow/userActionsButton.svg",
        canvasBehaviour = CanvasBehaviour.BUTTON,
        unsupportedProperties = {"box.expandRatio", "css", "responsive"}
)
public interface UserActionsButton extends Component.BelongToFrame, Component.HasIcon, Component.HasCaption {

    String NAME = "userActionsButton";

    /**
     * Sets the given {@code Consumer} as custom login action handler.
     */
    void setLoginHandler(@Nullable Consumer<LoginHandlerContext> loginHandler);

    /**
     * Sets the given {@code Consumer} as custom logout action handler.
     */
    void setLogoutHandler(@Nullable Consumer<LogoutHandlerContext> logoutHandler);

    class LoginHandlerContext {

        protected UserActionsButton source;

        public LoginHandlerContext(UserActionsButton source) {
            this.source = source;
        }

        public UserActionsButton getSource() {
            return source;
        }
    }

    class LogoutHandlerContext {

        protected UserActionsButton source;

        public LogoutHandlerContext(UserActionsButton source) {
            this.source = source;
        }

        public UserActionsButton getSource() {
            return source;
        }
    }
}
