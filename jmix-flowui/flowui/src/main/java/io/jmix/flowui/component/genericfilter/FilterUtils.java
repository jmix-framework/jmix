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

    /**
     * Finds and returns the current owner of the specified {@link GenericFilter}.
     * The owner is determined by checking if the {@link GenericFilter} attached to
     * a fragment or a view.
     *
     * @param genericFilter the {@link GenericFilter} for which the current owner is to be found
     * @return the current owner of the {@link  GenericFilter}, or {@code null} if no owner is found
     */
    @Internal
    @Nullable
    public static Composite<?> findCurrentOwner(GenericFilter genericFilter) {
        Composite<?> currentOwner = UiComponentUtils.findFragment(genericFilter);
        if (currentOwner == null) {
            currentOwner = UiComponentUtils.findView(genericFilter);
        }

        return currentOwner;
    }

    /**
     * Retrieves a facet of the specified type from the current owner of the given {@link GenericFilter}.
     *
     * @param genericFilter the {@link GenericFilter} whose current owner is queried for the specified facet type
     * @param facetClass    the {@link Class} of the facet to be retrieved
     * @param <T>           the type of the facet to be returned
     * @return the facet of the specified type if found, or {@code null} if no such facet exists
     */
    @Internal
    @Nullable
    public static <T extends Facet> T getFacet(GenericFilter genericFilter, Class<T> facetClass) {
        Composite<?> currentOwner = findCurrentOwner(genericFilter);
        return getFacet(currentOwner, facetClass);
    }

    /**
     * Retrieves a facet of the specified type from the given owner.
     *
     * @param currentOwner the current owning {@link Composite} instance from which to retrieve the facet
     *                     (a {@link Fragment} or a {@link View}),
     *                     or {@code null} if no owner is currently available
     * @param facetClass   the {@link Class} of the facet to be retrieved, must not be {@code null}
     * @param <T>          the type of the facet to be returned
     * @return the facet of the specified type if found, or {@code null} if no such facet exists
     */
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
