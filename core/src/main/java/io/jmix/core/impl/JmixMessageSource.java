/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.impl;

import io.jmix.core.JmixModuleDescriptor;
import io.jmix.core.JmixModules;
import io.jmix.core.LocaleResolver;
import io.jmix.core.Resources;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ResourceLoader;

import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

public class JmixMessageSource extends ReloadableResourceBundleMessageSource {

    private Resources resources;

    public JmixMessageSource(JmixModules modules, Resources resources) {
        this.resources = resources;
        setDefaultEncoding("UTF-8");
        setResourceLoader(resources);
        setFallbackToSystemLocale(false);

        ListIterator<JmixModuleDescriptor> iterator = modules.getAll().listIterator(modules.getAll().size());
        while (iterator.hasPrevious()) {
            JmixModuleDescriptor module = iterator.previous();
            addBasenames(addModuleBasename(module.getBasePackage()));
        }
    }

    protected String addModuleBasename(String moduleBasePackage) {
        return moduleBasePackage.replace('.', '/') + "/messages";
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        // Always use the resource loader provided in constructor. Otherwise it gets overridden by ApplicationContext.
        super.setResourceLoader(this.resources);
    }

    @Override
    protected List<String> calculateFilenamesForLocale(String basename, Locale locale) {
        List<String> result = super.calculateFilenamesForLocale(basename, locale);
        if (!StringUtils.isEmpty(locale.getScript())) {
            result.add(0, basename + '_' + LocaleResolver.localeToString(locale));
        }
        return result;
    }
}
