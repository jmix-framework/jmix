/*
 * Copyright 2024 Haulmont.
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

import com.google.common.base.Strings;
import io.jmix.core.CoreProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * REST API authentication utility class
 */
@Component("sec_RequestLocaleProvider")
public class RequestLocaleProvider {

    @Autowired
    protected CoreProperties coreProperties;

    private static final Logger log = LoggerFactory.getLogger(RequestLocaleProvider.class);

    /**
     * Method extracts locale information from the Accept-Language header. If no such header is specified or the
     * passed locale is not among application available locales, then null is returned
     */
    @Nullable
    public Locale getLocale(HttpServletRequest request) {
        if (Strings.isNullOrEmpty(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE))) {
            return null;
        }

        List<Locale> requestLocales = Collections.list(request.getLocales());
        List<Locale> availableLocales = coreProperties.getAvailableLocales();
        for (Locale requestLocale : requestLocales) {
            if (availableLocales.contains(requestLocale)) {
                return requestLocale;
            }
            Optional<Locale> foundLocaleOpt = availableLocales.stream()
                    .filter(l -> l.getLanguage().equals(requestLocale.getLanguage()))
                    .findFirst();
            if (foundLocaleOpt.isPresent()) {
                return foundLocaleOpt.get();
            }
        }
        log.debug("None of the locales {} passed in the Accept-Language header is supported by the application. " +
                "They were ignored.", requestLocales);
        return null;
    }
}
