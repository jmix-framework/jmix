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

package io.jmix.flowui.component.pagination;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.component.PaginationComponent;
import io.jmix.flowui.facet.SettingsFacet;
import io.jmix.flowui.facet.settings.component.binder.SimplePaginationSettingsBinder;
import io.jmix.flowui.view.View;
import org.springframework.lang.Nullable;

/**
 * Class provides access to protected functionality from {@link PaginationComponent} to support
 * saving/restoring UI settings when the {@link View} contains {@link SettingsFacet}.
 *
 * @see SimplePaginationSettingsBinder
 */
@Internal
public final class PaginationSettingsUtils {

    private PaginationSettingsUtils() {
    }

    /**
     * INTERNAL.
     * <p>
     * Is used for {@link PaginationComponent} settings.
     *
     * @param component pagination component
     * @return items per page value or {@code null} if items per page is not visible
     */
    @Nullable
    @Internal
    public static Integer getItemsPerPageValue(PaginationComponent<?> component) {
        if (component instanceof SimplePagination) {
            return ((SimplePagination) component).getItemsPerPageValue();
        }
        return null;
    }

    /**
     * INTERNAL.
     * <p>
     * Is used for {@link PaginationComponent} settings.
     *
     * @param component pagination component
     * @param value items per page value
     */
    @Internal
    public static void setItemsPerPageValue(PaginationComponent<?> component, @Nullable Integer value) {
        if (component instanceof SimplePagination) {
            ((SimplePagination) component).setItemsPerPageValue(value);
        }
    }
}
