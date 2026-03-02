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

package io.jmix.security.util;

import io.jmix.core.CoreProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Utility class for {@link io.jmix.core.security.ClientDetails} management
 */
@Component("sec_ClientDetailsSourceSupport")
public class ClientDetailsSourceSupport {

    @Autowired
    protected RequestLocaleProvider requestLocaleProvider;
    @Autowired
    protected CoreProperties coreProperties;

    /**
     * Returns the locale associated with the provided HTTP request. If no locale is specified,
     * the default locale is returned.
     *
     * @param request HTTP request
     * @return locale
     */
    public Locale getLocale(HttpServletRequest request) {
        Locale locale = requestLocaleProvider.getLocale(request);
        return locale == null ? getDefaultLocale() : locale;
    }

    protected Locale getDefaultLocale() {
        List<Locale> locales = coreProperties.getAvailableLocales();
        return locales.get(0);
    }
}