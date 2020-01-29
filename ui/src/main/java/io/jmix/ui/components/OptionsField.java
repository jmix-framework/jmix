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
package io.jmix.ui.components;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import io.jmix.core.metamodel.datatypes.impl.EnumClass;
import io.jmix.ui.components.data.Options;
import io.jmix.ui.components.data.options.EnumOptions;
import io.jmix.ui.components.data.options.ListOptions;
import io.jmix.ui.components.data.options.MapOptions;

import java.util.List;
import java.util.Map;

import static io.jmix.core.commons.util.Preconditions.checkNotNullArgument;

/**
 * UI component having options.
 *
 * @param <V> type of value
 * @param <I> type of option items
 */
public interface OptionsField<V, I> extends Field<V>, HasOptionCaptionProvider<I>, HasCaptionMode {

    /**
     * Sets options for UI component.
     *
     * @param options options
     * @see ListOptions
     */
    void setOptions(Options<I> options);
    /**
     * @return options object
     */
    Options<I> getOptions();

    /**
     * Sets options from the passed list.
     *
     * @param optionsList options
     * @see ListOptions#of(Object, Object[])
     */
    default void setOptionsList(List<I> optionsList) {
        setOptions(new ListOptions<>(optionsList));
    }

    /**
     * Sets options from the passed map and automatically applies option caption provider based on map keys.
     *
     * @param map options
     * @see ListOptions#of(Object, Object[])
     */
    default void setOptionsMap(Map<String, I> map) {
        checkNotNullArgument(map);

        BiMap<String, I> biMap = ImmutableBiMap.copyOf(map);

        setOptions(new MapOptions<>(map));
        setOptionCaptionProvider(v -> biMap.inverse().get(v));
    }

    /**
     * Sets options from the passed enum class. Enum class must be Java enumeration and implement {@link EnumClass}.
     *
     * @param optionsEnum enum class
     */
    @SuppressWarnings("unchecked")
    default void setOptionsEnum(Class<I> optionsEnum) {
        checkNotNullArgument(optionsEnum);

        if (!optionsEnum.isEnum()
                || !EnumClass.class.isAssignableFrom(optionsEnum)) {
            throw new IllegalArgumentException("Options class must be enumeration and implement EnumClass " + optionsEnum);
        }

        setOptions(new EnumOptions(optionsEnum));
    }

    /**
     * @return options list
     * @deprecated Use {@link #getOptions()} instead.
     */
    @Deprecated
    default List getOptionsList() {
        Options options = getOptions();
        if (options instanceof ListOptions) {
            return (List) ((ListOptions) options).getItemsCollection();
        }
        return null;
    }

    /**
     * @return options map
     * @deprecated Use {@link #getOptions()} instead.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    default Map<String, ?> getOptionsMap() {
        Options options = getOptions();
        if (options instanceof MapOptions) {
            return ((MapOptions) options).getItemsCollection();
        }
        return null;
    }

    /**
     * @return enumclass
     * @deprecated Use {@link #getOptions()} instead.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    default Class<? extends EnumClass> getOptionsEnum() {
        Options options = getOptions();
        if (options instanceof EnumOptions) {
            return ((EnumOptions) options).getEnumClass();
        }
        return null;
    }
}
