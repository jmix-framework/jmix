/*
 * Copyright (c) 2008-2020 Haulmont.
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

package io.jmix.ui.theme;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component(ThemeVariantsProvider.NAME)
public class HeliumThemeVariantsProvider implements ThemeVariantsProvider {

    @Autowired
    protected HeliumThemeVariantsManager variantsManager;

    @Override
    public List<String> getThemeVariants() {
        List<String> variants = new ArrayList<>(2);

        String themeMode = variantsManager.getUserAppThemeMode();
        if (!Strings.isNullOrEmpty(themeMode)) {
            variants.add(themeMode);
        }

        String themeSize = variantsManager.getUserAppThemeSize();
        if (!Strings.isNullOrEmpty(themeSize)) {
            variants.add(themeSize);
        }

        return variants;
    }
}
