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

package com.haulmont.cuba.gui.xml;


import com.google.common.collect.ImmutableMap;
import com.haulmont.cuba.CubaProperties;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.UiScreenProperties;
import io.jmix.ui.xml.PropertyShortcutLoader;

import java.util.Map;
import java.util.function.Function;

public class CubaPropertyShortcutLoader extends PropertyShortcutLoader {

    protected static final Map<String, Function<CubaProperties, String>> CUBA_SHORTCUT_ALIASES =
            ImmutableMap.<String, Function<CubaProperties, String>>builder()
                    .put("FILTER_APPLY_SHORTCUT", CubaProperties::getFilterApplyShortcut)
                    .put("FILTER_SELECT_SHORTCUT", CubaProperties::getFilterSelectShortcut)
                    .build();

    protected CubaProperties cubaProperties;

    public CubaPropertyShortcutLoader(UiComponentProperties componentProperties,
                                      UiScreenProperties screenProperties,
                                      CubaProperties cubaProperties) {
        super(componentProperties, screenProperties);

        this.cubaProperties = cubaProperties;
    }

    @Override
    public boolean contains(String alias) {
        return super.contains(alias) || CUBA_SHORTCUT_ALIASES.containsKey(alias);
    }

    @Override
    public String getShortcut(String alias) {
        Function<CubaProperties, String> cubaShortcut = CUBA_SHORTCUT_ALIASES.get(alias);
        if (cubaShortcut != null) {
            return cubaShortcut.apply(cubaProperties);
        }

        return super.getShortcut(alias);
    }
}
