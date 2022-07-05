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

package io.jmix.ui.sys;

import io.jmix.core.JmixModuleDescriptor;
import io.jmix.core.JmixModules;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Class sorts the list of {@link UiControllersConfiguration} in the same order as Jmix modules containing the screens
 * have been sorted.
 */
@Component("ui_UiControllersConfigurationSorter")
public class UiControllersConfigurationSorter {

    private JmixModules jmixModules;

    public UiControllersConfigurationSorter(JmixModules jmixModules) {
        this.jmixModules = jmixModules;
    }

    public void sort(List<UiControllersConfiguration> configurations) {
        configurations.sort((o1, o2) -> {
            JmixModuleDescriptor module1 = evaluateJmixModule(o1.getBasePackages());
            JmixModuleDescriptor module2 = evaluateJmixModule(o2.getBasePackages());
            if (module1 == null || module2 == null) return 0;
            return module1.dependsOn(module2) ? 1 : -1;
        });
    }

    /**
     * Finds Jmix module with a base package that matches any of given {@code screenBasePackages}. Jmix module matches
     * the screen package if its base package equals screen base package or if screen base package is a "subpackage" of
     * Jmix module base package.
     */
    @Nullable
    protected JmixModuleDescriptor evaluateJmixModule(Collection<String> screensBasePackages) {
        return screensBasePackages.stream()
                .map(this::evaluateJmixModule)
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }

    @Nullable
    protected JmixModuleDescriptor evaluateJmixModule(String screensBasePackage) {
        return jmixModules.getAll().stream()
                .filter(module -> module.getBasePackage().equals(screensBasePackage) ||
                        isNestedSubpackage(module.getBasePackage(), screensBasePackage))
                .findAny()
                .orElse(null);
    }

    /**
     * Returns true if {@code package1} is a subpackage of {@code package2}, e.g. <i>com.company.greeting.hello</i> is a
     * subpackage of <i>com.company.greeting</i>
     */
    protected boolean isNestedSubpackage(String package1, String package2) {
        return Pattern.compile(package1.replace(".", "\\.") + "\\..+").matcher(package2).matches();
    }
}
