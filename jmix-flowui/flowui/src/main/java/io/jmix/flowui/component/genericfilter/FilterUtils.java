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
import com.vaadin.flow.component.Composite;
import io.jmix.core.annotation.Internal;
import io.jmix.core.querycondition.Condition;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
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
        Composite<?> owner = findCurrentOwner(filter);
        return (owner != null ? "[" + owner.getId().orElse("ownerWithoutId") + "]" : "")
                + UiComponentUtils.getComponentId(filter).orElse("filterWithoutId");
    }

    public static void setCurrentConfiguration(GenericFilter filter, Configuration currentConfiguration, boolean fromClient) {
        filter.setCurrentConfigurationInternal(currentConfiguration, fromClient);
    }

    @Internal
    public static void updateDataLoaderInitialCondition(GenericFilter genericFilter, @Nullable Condition condition) {
        genericFilter.updateDataLoaderInitialCondition(condition);
    }

    @Internal
    @Nullable
    public static Composite<?> findCurrentOwner(GenericFilter genericFilter) {
        Composite<?> currentOwner = UiComponentUtils.findFragment(genericFilter);
        if (currentOwner == null) {
            currentOwner = UiComponentUtils.findView(genericFilter);
        }

        return currentOwner;
    }

    @Internal
    @Nullable
    public static <T extends Facet> T getFacet(GenericFilter genericFilter, Class<T> facetClass) {
        Composite<?> currentOwner = findCurrentOwner(genericFilter);
        return getFacet(currentOwner, facetClass);
    }

    @Internal
    @Nullable
    public static <T extends Facet> T getFacet(@Nullable Composite<?> currentOwner, Class<T> facetClass) {
        return currentOwner instanceof Fragment<?> fragment
                ? FragmentUtils.getFragmentFacet(fragment, facetClass)
                : currentOwner instanceof View<?> view
                ? ViewControllerUtils.getViewFacet(view, facetClass)
                : null;
    }
}
