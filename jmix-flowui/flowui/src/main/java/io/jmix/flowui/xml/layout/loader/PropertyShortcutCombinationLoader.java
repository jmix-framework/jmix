/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.xml.layout.loader;

import io.jmix.flowui.xml.layout.loader.shortcut.ShortcutAliasProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Loads and resolves shortcut combinations from multiple property sources.
 * <p>
 * Aggregates shortcut aliases from all registered {@link ShortcutAliasProvider} implementations
 * and provides methods to check for alias existence and resolve aliases to actual shortcut values.
 */
@Component("flowui_PropertyShortcutLoader")
public class PropertyShortcutCombinationLoader {

    protected List<ShortcutAliasProvider<?>> providers;

    @Autowired
    public void setProviders(List<ShortcutAliasProvider<?>> providers) {
        this.providers = providers;
    }

    public boolean contains(String alias) {
        for (ShortcutAliasProvider<?> aliasesProvider : providers) {
            if (aliasesProvider.getAliases().containsKey(alias)) {
                return true;
            }
        }
        return false;
    }

    public String getShortcut(String alias) {
        for (ShortcutAliasProvider<?> aliasesProvider : providers) {
            Map<String, ? extends Function<?, String>> aliases = aliasesProvider.getAliases();
            Function<Object, String> shortcutFunction = (Function<Object, String>) aliases.get(alias);

            if (shortcutFunction != null) {
                Object propertyClass = aliasesProvider.getPropertyClass();
                return shortcutFunction.apply(propertyClass);
            }
        }

        throw new IllegalStateException(String.format("There is no shortcutCombination for alias '%s'", alias));
    }
}
