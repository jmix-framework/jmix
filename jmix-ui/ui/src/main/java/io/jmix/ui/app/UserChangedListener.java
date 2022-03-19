/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.app;

import io.jmix.core.security.event.UserSubstitutedEvent;
import io.jmix.ui.App;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Listener that handles different sources of user changes, e.g. interactive authentication or user substitution.
 */
@Component("ui_UserChangedListener")
public class UserChangedListener {

    @EventListener
    public void onAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event) {
        forceRefreshUI();
    }

    @EventListener
    public void onUserSubstituted(UserSubstitutedEvent event) {
        forceRefreshUI();
    }

    protected void forceRefreshUI() {
        if (App.isBound()) {
            App.getInstance().forceRefreshUIsExceptCurrent();
        }
    }
}
