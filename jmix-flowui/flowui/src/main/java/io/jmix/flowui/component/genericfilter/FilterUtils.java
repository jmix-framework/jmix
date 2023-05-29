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

package io.jmix.flowui.component.genericfilter;

import com.google.common.base.Strings;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.View;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.WordUtils;

import org.springframework.lang.Nullable;

public class FilterUtils {

    public static String generateConfigurationId(@Nullable String configurationName) {
        return WordUtils.capitalize(Strings.nullToEmpty(configurationName))
                .replaceAll(" ", "")
                + RandomStringUtils.randomAlphabetic(8);
    }

    public static String generateFilterPath(GenericFilter filter) {
        View<?> view = UiComponentUtils.findView(filter);
        return (view != null ? "[" + view.getId().orElse("viewWithoutId") + "]" : "")
                + filter.getId().orElse("filterWithoutId");
    }

    public static void setCurrentConfiguration(GenericFilter filter, Configuration currentConfiguration, boolean fromClient) {
        filter.setCurrentConfigurationInternal(currentConfiguration, fromClient);
    }
}
