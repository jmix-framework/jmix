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

package com.haulmont.cuba.gui.components;

import com.google.common.reflect.TypeToken;
import com.haulmont.cuba.gui.components.compatibility.LookupFieldFilterPredicateAdapter;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.ui.component.ComboBox;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <V> type of options and value
 * @deprecated Use {@link ComboBox} instead
 */
@Deprecated
public interface LookupField<V> extends OptionsField<V, V>, ComboBox<V>, LookupComponent<V>,
        HasOptionsStyleProvider<V> {

    String NAME = "lookupField";

    TypeToken<LookupField<String>> TYPE_STRING = new TypeToken<LookupField<String>>(){};

    static <T> TypeToken<LookupField<T>> of(Class<T> valueClass) {
        return new TypeToken<LookupField<T>>() {};
    }

    /**
     * @deprecated Use {@link #getNullSelectionCaption()} instead
     */
    @Deprecated
    V getNullOption();

    /**
     * @deprecated Use {@link #setNullSelectionCaption(String)} instead
     */
    @Deprecated
    void setNullOption(V nullOption);

    /**
     * @return true if the component handles new options entered by user.
     * @see #setNewOptionHandler(Consumer)
     */
    @Deprecated
    boolean isNewOptionAllowed();
    /**
     * Makes the component handle new options entered by user.
     *
     * @see #setNewOptionHandler(Consumer)
     * @deprecated setting the new option handler enables new options
     */
    @Deprecated
    void setNewOptionAllowed(boolean newOptionAllowed);

    /**
     * @return current handler
     * @deprecated Use {@link #getEnterPressHandler()} instead
     */
    @Nullable
    @Deprecated
    Consumer<String> getNewOptionHandler();

    /**
     * Sets the handler that is called when user types a new item.
     *
     * @param newOptionHandler handler instance
     * @deprecated Use {@link #setEnterPressHandler(Consumer)} instead
     */
    @Deprecated
    void setNewOptionHandler(@Nullable Consumer<String> newOptionHandler);

    /**
     * Set the icon provider for LookupField.
     *
     * @param optionClass        class of the option
     * @param optionIconProvider provider which provides icons for options
     *
     * @deprecated Use {@link #setOptionIconProvider(Function)}
     */
    @Deprecated
    void setOptionIconProvider(Class<V> optionClass, Function<? super V, String> optionIconProvider);

    /**
     * Enables to setup how items should be filtered.
     *
     * @param filterPredicate items filter predicate
     * @deprecated Use {@link #setOptionsCaptionFilter(Predicate)} instead
     */
    @Deprecated
    default void setFilterPredicate(@Nullable FilterPredicate filterPredicate) {
        setOptionsCaptionFilter(new LookupFieldFilterPredicateAdapter(filterPredicate));
    }

    /**
     * @return items filter predicate
     * @deprecated Use {@link #setOptionsCaptionFilter(Predicate)} instead
     */
    @Deprecated
    @Nullable
    default FilterPredicate getFilterPredicate() {
        Predicate<OptionsCaptionFilteringContext> captionFilter = getOptionsCaptionFilter();
        if (captionFilter instanceof LookupFieldFilterPredicateAdapter) {
            return ((LookupFieldFilterPredicateAdapter) captionFilter).getFilterPredicate();
        }

        return null;
    }

    /**
     * Interface to be implemented if {@link #setNewOptionAllowed(boolean)} is set to true.
     */
    @Deprecated
    interface NewOptionHandler extends Consumer<String> {
        @Override
        default void accept(String caption) {
            addNewOption(caption);
        }

        /**
         * Called when user enters a value which is not in the options list, and presses Enter.
         * @param caption value entered by user
         */
        void addNewOption(String caption);
    }

    /**
     * Allows to set icons for particular elements in the options list.
     */
    @Deprecated
    interface OptionIconProvider<T> extends Function<T, String> {
        @Override
        default String apply(T item) {
            return getItemIcon(item);
        }

        /**
         * Called when component paints its content.
         *
         * @param item item from options list, options map or enum options
         * @return icon name or null to show no icon
         */
        String getItemIcon(T item);
    }

    /**
     * A predicate that tests whether an item with the given caption matches to the given search string.
     *
     * @deprecated Use {@link #setOptionsCaptionFilter(Predicate)} instead
     */
    @Deprecated
    @FunctionalInterface
    interface FilterPredicate {

        /**
         * @param itemCaption  a caption of item
         * @param searchString search string as is
         * @return true if item with the given caption matches to the given search string or false otherwise
         */
        boolean test(String itemCaption, String searchString);
    }
}
