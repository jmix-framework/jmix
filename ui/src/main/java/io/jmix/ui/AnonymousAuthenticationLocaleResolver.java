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

package io.jmix.ui;

import io.jmix.core.JmixOrder;
import io.jmix.core.MessageTools;
import io.jmix.core.security.AuthenticationLocaleResolver;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Provides {@link Locale} from {@link App} instance that should be used if current authentication is anonymous.
 *
 * @see CurrentAuthentication
 */
@Component("ui_AnonymousAuthenticationLocaleResolver")
public class AnonymousAuthenticationLocaleResolver implements AuthenticationLocaleResolver {

    @Autowired
    protected MessageTools messageTools;

    @Override
    public boolean supports(Authentication authentication) {
        return authentication instanceof AnonymousAuthenticationToken;
    }

    @Override
    public Locale getLocale(Authentication authentication) {
        if (App.isBound()) {
            return App.getInstance().getLocale();
        }
        return messageTools.getDefaultLocale();
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 100;
    }
}
