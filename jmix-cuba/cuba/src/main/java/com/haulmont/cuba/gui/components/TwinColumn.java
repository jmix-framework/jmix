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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.Entity;
import io.jmix.core.entity.EntityValues;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Function;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <V> value and options type for the component
 * @deprecated Use {@link io.jmix.ui.component.TwinColumn} instead
 */
@Deprecated
public interface TwinColumn<V> extends OptionsField<Collection<V>, V>, io.jmix.ui.component.TwinColumn<V> {

    /**
     * Gets the number of columns for the component.
     *
     * @see #setWidth(String)
     * @deprecated "Columns" does not reflect the exact number of characters that will be displayed. Use
     * {@link #getWidth()} instead.
     */
    @Deprecated
    int getColumns();

    /**
     * Sets the width of the component so that it displays approximately the given number of letters in each of the
     * two selects.
     *
     * @param columns the number of columns to set.
     * @deprecated "Columns" does not reflect the exact number of characters that will be displayed. Use
     * {@link #setWidth(String)} instead.
     */
    @Deprecated
    void setColumns(int columns);

    /**
     * @param styleProvider style provider
     * @deprecated use {@link #setOptionStyleProvider(Function)} instead
     */
    @Deprecated
    default void setStyleProvider(@Nullable StyleProvider styleProvider) {
        if (styleProvider == null) {
            setOptionStyleProvider((OptionStyleProvider) null);
        } else {
            setOptionStyleProvider((item, selected) -> {
                if (item instanceof Entity) {
                    return styleProvider.getStyleName(item, EntityValues.getId((item)), selected);
                } else {
                    return null;
                }
            });
        }
    }

    /**
     * Sets option style provider. It defines a style for each value.
     *
     * @param optionStyleProvider option style provider function
     * @deprecated use {@link #setOptionStyleProvider(Function)} instead
     */
    @Deprecated
    void setOptionStyleProvider(@Nullable OptionStyleProvider<V> optionStyleProvider);

    /**
     * @deprecated use {@link #setOptionStyleProvider(OptionStyleProvider)}
     */
    @Deprecated
    interface StyleProvider {
        @Deprecated
        String getStyleName(Object item, Object property, boolean selected);
    }

    /**
     * @param <V> option type
     * @deprecated use {@link #setOptionStyleProvider(OptionStyleProvider)}
     */
    @Deprecated
    interface OptionStyleProvider<V> {

        /**
         * Handles style name for the item.
         *
         * @param item     item to create style name
         * @param selected is item selected
         * @return style name for the item
         */
        @Nullable
        String getStyleName(V item, boolean selected);
    }
}
