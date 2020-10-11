/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.impl.method;

import io.jmix.core.security.CurrentAuthentication;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Locale;

/**
 * Allows resolving the current {@link Locale} as method argument.
 * Current user session locale will be returned if user is authorized otherwise will be returned default system locale
 */
@Component("core_LocaleArgumentResolver")
public class LocaleArgumentResolver extends TypedArgumentResolver<Locale> {

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    public LocaleArgumentResolver() {
        super(Locale.class);
    }

    @Override
    public Locale resolveArgument(MethodParameter parameter) {
        return getCurrentLocale();
    }

    protected Locale getCurrentLocale() {
        Locale locale;
        if (currentAuthentication.isSet()) {
            locale = currentAuthentication.getLocale();
        } else {
            locale = Locale.getDefault();
        }
        return locale;
    }
}
