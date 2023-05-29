/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helps to select a bean that belongs to the lower module in the hierarchy of {@code @JmixModule} dependencies (where
 * core is on top and an application is at bottom).
 */
@Component("core_JmixModulesAwareBeanSelector")
public class JmixModulesAwareBeanSelector {

    @Autowired
    private JmixModules modules;

    /**
     * Returns a bean that belongs to the lower module in the hierarchy of {@code @JmixModule} dependencies.
     *
     * @param beans collection of beans to select from
     * @return a bean instance or null if the input collection is empty
     */
    @Nullable
    public <T> T selectFrom(Collection<T> beans) {
        if (beans.isEmpty()) {
            return null;
        }
        if (beans.size() == 1) {
            return beans.iterator().next();
        }

        List<String> basePackages = modules.getAll().stream()
                .map(JmixModuleDescriptor::getBasePackage)
                .collect(Collectors.toList());

        List<T> beanList = new ArrayList<>(beans);

        beanList.sort((b1, b2) -> {
            int b1idx = 0;
            int b2idx = 0;
            for (int i = 0; i < basePackages.size(); i++) {
                if (b1.getClass().getPackage().getName().startsWith(basePackages.get(i))) {
                    b1idx = i;
                }
                if (b2.getClass().getPackage().getName().startsWith(basePackages.get(i))) {
                    b2idx = i;
                }
            }
            return b2idx - b1idx;
        });
        return beanList.get(0);
    }
}
