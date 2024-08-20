/*
 * Copyright 2024 Haulmont.
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

package io.jmix.reportsflowui.helper;

import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Provides a method for obtaining the base package.
 */
@Component("report_PackageHelper")
public class PackageHelper {

    private final ApplicationContext applicationContext;

    public PackageHelper(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /*
     * The method gets the base package
     */
    public Optional<String> getBasePackage() {
        List<String> packageList = AutoConfigurationPackages.get(applicationContext.getAutowireCapableBeanFactory());
        return packageList.stream().findFirst();
    }
}
