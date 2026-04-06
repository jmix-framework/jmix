/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.sys.localeresolver;

import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.JmixOrder;
import io.jmix.core.security.AuthenticationLocaleResolver;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component("flowui_RememberMeAuthenticationLocaleResolver")
public class RememberMeAuthenticationLocaleResolver implements AuthenticationLocaleResolver {

    @Override
    public boolean supports(Authentication authentication) {
        return authentication instanceof RememberMeAuthenticationToken;
    }

    @Nullable
    @Override
    public Locale getLocale(Authentication authentication) {
        if (VaadinSession.getCurrent() != null) {
            return VaadinSession.getCurrent().getLocale();
        }

        return null;
    }

    @Override
    public int getOrder() {
        return JmixOrder.LOWEST_PRECEDENCE - 100;
    }
}
