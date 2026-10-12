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

package io.jmix.flowui.devserver.theme;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Holds stylesheet URLs discovered while copying project legacy
 * {@code @Theme} theme files into the dev-server {@code META-INF/resources} folder.
 * Read by the main layout to register them on the page.
 */
public final class LegacyThemeStyleSheets {

    // Base themes first: project themes override them
    private static final List<String> DEFAULT_LUMO_STYLESHEETS =
            List.of(
                    // com.vaadin.flow.theme.lumo.Lumo.STYLESHEET
                    "lumo/lumo.css",
                    // io.jmix.flowui.theme.lumo.JmixLumo.STYLESHEET
                    "themes/jmix-lumo/jmix-lumo.css"
            );

    private static volatile List<String> styleSheets = List.of();

    private LegacyThemeStyleSheets() {
    }

    public static List<String> getStyleSheets() {
        return styleSheets;
    }

    public static void setStyleSheets(List<String> styleSheets) {
        Set<String> result = new LinkedHashSet<>(DEFAULT_LUMO_STYLESHEETS);
        result.addAll(styleSheets);
        LegacyThemeStyleSheets.styleSheets = List.copyOf(result);
    }
}
