/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reports.impl.builder;

import io.jmix.core.LocaleResolver;
import io.jmix.core.MessageTools;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@Component("reports_AnnotatedBuilderUtils")
public class AnnotatedBuilderUtils {

    protected final MessageTools messageTools;

    public AnnotatedBuilderUtils(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    /**
     * Build a "localeNames" - compound String containing localized captions in Properties format,
     * supported by reports' entities.
     *
     * @param ref reference to message in the following format: {@code msg://group/message_id}
     * @return localized captions for all supported languages in Properties format
     *
     * @see io.jmix.reports.util.MsgBundleTools#getLocalizedValue(String, String)
     */
    public String buildLocaleNames(String ref) {
        // do reverse logic of io.jmix.reports.util.MsgBundleTools#getLocalizedValue()
        Map<String, Locale> allLocales = messageTools.getAvailableLocalesMap();
        Properties properties = new Properties();

        for (Locale locale : allLocales.values()) {
            String localizedName = messageTools.loadString(ref, locale);
            properties.put(LocaleResolver.localeToString(locale), localizedName);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            properties.store(new PrintWriter(baos, false, StandardCharsets.UTF_8), null);
            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
