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

package io.jmix.aitools;

import io.jmix.core.security.CurrentAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Resolves the human-readable response-language instruction substituted into LLM system prompts
 * through the {@code responseLanguage} template parameter.
 * <p>
 * A bare ISO language code (for example {@code "en"}) is a weak signal that less capable models tend
 * to ignore. To make the directive robust, this provider emits the language English name, its endonym
 * (the language written in itself) and the language tag, for example {@code "German / Deutsch (de)"}
 * or {@code "English (en)"}. The endonym is the strongest cue because the target language is shown in
 * the target language itself, while the tag keeps the value machine-unambiguous.
 * <p>
 * Applications may register their own bean with the same name to change the format.
 */
@Component("aitols_ResponseLanguageProvider")
public class ResponseLanguageProvider {

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    /**
     * Returns the response-language instruction for the current user's locale.
     *
     * @return formatted language descriptor for the current user's locale
     */
    public String getResponseLanguage() {
        return getResponseLanguage(currentAuthentication.getLocale());
    }

    /**
     * Formats a response-language instruction for the given locale, combining the English language
     * name, the endonym and the language tag.
     *
     * @param locale locale to describe
     * @return formatted language descriptor, or the bare language tag when the language name is unknown
     */
    public String getResponseLanguage(Locale locale) {
        String tag = locale.toLanguageTag();
        String englishName = locale.getDisplayLanguage(Locale.ENGLISH);
        String endonym = locale.getDisplayLanguage(locale);

        if (englishName.isBlank()) {
            return tag;
        }
        if (endonym.equalsIgnoreCase(englishName)) {
            return "%s (%s)".formatted(englishName, tag);
        }
        return "%s / %s (%s)".formatted(englishName, endonym, tag);
    }
}
