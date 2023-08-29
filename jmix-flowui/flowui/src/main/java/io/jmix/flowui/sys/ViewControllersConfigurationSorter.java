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

package io.jmix.flowui.sys;

import io.jmix.core.JmixModuleDescriptor;
import io.jmix.core.JmixModules;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Class sorts the list of {@link ViewControllersConfiguration} in the same order as Jmix modules containing the screens
 * have been sorted.
 */
@Component("flowui_ViewControllersConfigurationSorter")
public class ViewControllersConfigurationSorter {

    private JmixModules jmixModules;

    public ViewControllersConfigurationSorter(JmixModules jmixModules) {
        this.jmixModules = jmixModules;
    }

    public List<ViewControllersConfiguration> sort(List<ViewControllersConfiguration> configurations) {
        List<ViewControllersConfiguration> sortedConfigurations = new ArrayList<>(configurations);
        List<JmixModuleDescriptor> sortedJmixModuleDescriptors = jmixModules.getAll();
        sortedConfigurations.sort((o1, o2) -> {
            JmixModuleDescriptor module1 = evaluateJmixModule(o1.getBasePackages());
            JmixModuleDescriptor module2 = evaluateJmixModule(o2.getBasePackages());
            if (module1 == null || module2 == null) return 0;
            return sortedJmixModuleDescriptors.indexOf(module1) - sortedJmixModuleDescriptors.indexOf(module2);
        });
        return sortedConfigurations;
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
        //first try to find jmix module with exactly the same base package
        JmixModuleDescriptor jmixModule = jmixModules.getAll().stream()
                .filter(module -> module.getBasePackage().equals(screensBasePackage))
                .findAny()
                .orElse(null);
        if (jmixModule != null) {
            return jmixModule;
        }

        //Find Jmix module where the screen base package is a subpackage of module base package.
        //If multiple modules found then the module with the longest base package (maximum number of dots) is selected,
        //i.e. com.company.app.addon will be selected in preference to com.company.app
        List<JmixModuleDescriptor> matchingModules = jmixModules.getAll().stream()
                .filter(module -> isNestedSubpackage(screensBasePackage, module.getBasePackage()))
                .sorted(Comparator.comparing(jm -> charsCount(jm.getBasePackage(), '.')))
                .collect(Collectors.toList());

        return !matchingModules.isEmpty() ?
                matchingModules.get(matchingModules.size() - 1) :
                null;
    }

    /**
     * Returns true if {@code package1} is a subpackage of {@code package2}, e.g. <i>com.company.greeting.hello</i> is a
     * subpackage of <i>com.company.greeting</i>
     */
    protected boolean isNestedSubpackage(String package1, String package2) {
        return Pattern.compile(package2.replace(".", "\\.") + "\\..+").matcher(package1).matches();
    }

    /**
     * Returns count of specific character in a string
     */
    protected long charsCount(String string, Character character) {
        return string.chars()
                .filter(ch -> ch == character)
                .count();
    }
}
