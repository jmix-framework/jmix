/*
 * Copyright 2021 Haulmont.
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

package com.haulmont.cuba.gui.components.filter;

import com.haulmont.cuba.security.entity.FilterEntity;
import com.haulmont.cuba.settings.binder.CubaFilterSettingsBinder;
import com.haulmont.cuba.settings.component.CubaFilterSettings;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.settings.facet.ScreenSettingsFacet;
import org.apache.commons.lang3.BooleanUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.annotation.Nullable;

/**
 * Class provides access to protected functionality from {@link FilterDelegateImpl} to
 * support the usage of {@link com.haulmont.cuba.gui.components.Filter} in Jmix screens
 * with {@link ScreenSettingsFacet}.
 *
 * @see CubaFilterSettingsBinder
 * @see CubaFilterSettings
 */
@Internal
public final class FilterDelegateSettingsUtils {

    private FilterDelegateSettingsUtils() {
    }

    /**
     * INTERNAL API. Is used by {@link CubaFilterSettingsBinder}.
     *
     * @param delegate filter delegate bean
     * @return default filter entitiy or {@code null}
     */
    @Internal
    @Nullable
    public static FilterEntity getDefaultFilter(FilterDelegate delegate) {
        if (delegate instanceof FilterDelegateImpl) {
            for (FilterEntity filter : ((FilterDelegateImpl) delegate).filterEntities) {
                if (BooleanUtils.toBoolean(filter.getIsDefault())) {
                    return filter;
                }
            }
        }
        return null;
    }

    /**
     * INTERNAL API. Is used by {@link CubaFilterSettingsBinder}.
     *
     * @param delegate filter delegate bean
     * @return {@code true} if maxResults from settings should be applied before loading data
     */
    @Internal
    public static boolean applyMaxResultsSettingsBeforeLoad(FilterDelegate delegate) {
        if (delegate instanceof FilterDelegateImpl) {
            FilterDelegateImpl.Adapter adapter = ((FilterDelegateImpl) delegate).adapter;
            return adapter != null && adapter.applyMaxResultsSettingsBeforeLoad();
        }
        return false;
    }

    /**
     * INTERNAL API. Is used by {@link CubaFilterSettingsBinder}.
     *
     * @param delegate   filter delegate bean
     * @param maxResults max result to apply
     */
    @Internal
    public static void applyMaxResultsSettings(FilterDelegate delegate, int maxResults) {
        if (delegate instanceof FilterDelegateImpl) {
            Element componentElement = DocumentHelper.createDocument().addElement("component");
            componentElement.addElement("maxResults")
                    .setText(String.valueOf(maxResults));

            ((FilterDelegateImpl) delegate).applyMaxResultsSettings(componentElement);
        }
    }

    /**
     * INTERNAL API. Is used by {@link CubaFilterSettingsBinder}.
     *
     * @param delegate filter delegate bean
     * @return {@code true} layout for maxResults field is visible
     */
    @Internal
    public static boolean isMaxResultsLayoutVisible(FilterDelegate delegate) {
        if (delegate instanceof FilterDelegateImpl) {
            return ((FilterDelegateImpl) delegate).isMaxResultsLayoutVisible();
        }
        return false;
    }

    /**
     * INTERNAL API. Is used by {@link CubaFilterSettingsBinder}.
     *
     * @param delegate filter delegate bean
     * @return maxResults value from the field or {@code null}
     */
    @Internal
    @Nullable
    public static Integer getMaxResultsValue(FilterDelegate delegate) {
        if (delegate instanceof FilterDelegateImpl) {
            if (((FilterDelegateImpl) delegate).maxResultsField != null) {
                return ((FilterDelegateImpl) delegate).maxResultsField.getValue();
            }
        }
        return null;
    }
}
