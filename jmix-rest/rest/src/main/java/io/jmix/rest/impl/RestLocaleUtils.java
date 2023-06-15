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

package io.jmix.rest.impl;

import com.google.common.base.Strings;
import io.jmix.core.CoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;

/**
 * REST API authentication utility class
 */
@Component("rest_RestLocaleUtils")
public class RestLocaleUtils {

    @Autowired
    protected CoreProperties coreProperties;

    private static final Logger log = LoggerFactory.getLogger(RestLocaleUtils.class);

    /**
     * Method extracts locale information from the Accept-Language header. If no such header is specified or the
     * passed locale is not among application available locales, then null is returned
     */
    @Nullable
    public Locale extractLocaleFromRequestHeader(HttpServletRequest request) {
        Locale locale = null;
        if (!Strings.isNullOrEmpty(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE))) {
            Locale requestLocale = request.getLocale();

            List<Locale> availableLocales = coreProperties.getAvailableLocales();
            if (availableLocales.contains(requestLocale)
                    || availableLocales.stream().anyMatch(l -> l.getLanguage().equals(requestLocale.getLanguage()))) {
                locale = requestLocale;
            } else {
                log.warn("Locale {} passed in the Accept-Language header is not supported by the application. It was ignored.", requestLocale);
            }
        }
        return locale;
    }
}
