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

package io.jmix.grapesjs.widget.grapesjshtmleditorcomponent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sanitizes HTML produced by the GrapesJS editor before it is stored in the server-side component state.
 */
public final class GrapesJsHtmlSanitizer {

    // Regular expression for matching all Apache FreeMarker pseudo-tags.
    private static final Pattern FM_PATTERN = Pattern.compile(
            "(<#--.*?-->)" +                               // FreeMarker comments
                    "|(<@[A-Za-z0-9_]+(?:\\s+[^>]*)?>)" + // <@macro ...>
                    "|(</@[A-Za-z0-9_]+>)" +              // </@macro>
                    "|(<#[A-Za-z0-9_]+(?:\\s+[^>]*)?>)" + // <#if ...>
                    "|(</#[A-Za-z0-9_]+>)" +              // </#if>
                    "|(\\$\\{[^}]*})",                   // ${...}
            Pattern.DOTALL
    );

    private static final String FM_PLACEHOLDER = "___FM_%s___";

    private GrapesJsHtmlSanitizer() {
        throw new UnsupportedOperationException("GrapesJsHtmlSanitizer is not supposed to be instantiated");
    }

    /**
     * Removes unsafe HTML and JavaScript from the passed HTML while preserving FreeMarker pseudo-tags.
     *
     * @param html HTML to sanitize, may be {@code null}
     * @return sanitized HTML, or {@code null} if the passed value is {@code null}
     */
    @Nullable
    public static String sanitize(@Nullable String html) {
        if (html == null) {
            return null;
        }

        List<String> placeholders = new ArrayList<>();
        Matcher matcher = FM_PATTERN.matcher(html);

        StringBuilder protectedHtml = new StringBuilder();
        while (matcher.find()) {
            String match = matcher.group();
            String placeholder = String.format(FM_PLACEHOLDER, placeholders.size());

            placeholders.add(match);
            matcher.appendReplacement(protectedHtml, Matcher.quoteReplacement(placeholder));
        }
        matcher.appendTail(protectedHtml);

        Document.OutputSettings settings = new Document.OutputSettings();
        settings.prettyPrint(false);

        String sanitizedHtml = Jsoup.clean(protectedHtml.toString(), "",
                Safelist.relaxed()
                        .addTags("body", "style", "video", "input", "form", "textarea",
                                "label", "button", "select", "option", "iframe")
                        .addAttributes(":all", "style", "id", "class", "src", "type", "method", "value")
                        .addProtocols("img", "src", "data"),
                settings);

        for (int i = 0; i < placeholders.size(); i++) {
            sanitizedHtml = sanitizedHtml.replace(String.format(FM_PLACEHOLDER, i), placeholders.get(i));
        }

        return sanitizedHtml;
    }
}
