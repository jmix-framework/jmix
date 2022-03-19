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

import com.google.common.reflect.TypeToken;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.component.formatter.Formatter;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <V> type of value
 * @deprecated Use {@link io.jmix.ui.component.SuggestionField} instead
 */
@Deprecated
public interface SuggestionField<V> extends Field<V>, io.jmix.ui.component.SuggestionField<V>, HasCaptionMode,
        HasOptionsStyleProvider<V> {

    static <T> TypeToken<SuggestionField<T>> of(Class<T> valueClass) {
        return new TypeToken<SuggestionField<T>>() {};
    }

    /**
     * @return {@link EnterActionHandler} which handles ENTER key pressing
     * @deprecated Use {@link #getEnterPressHandler()} and {@link #setEnterPressHandler(Consumer)} instead
     */
    @Deprecated
    EnterActionHandler getEnterActionHandler();

    /**
     * Sets {@link EnterActionHandler} which handles ENTER key pressing.
     *
     * @param enterActionHandler EnterActionHandler instance
     * @deprecated Use {@link #setEnterPressHandler(Consumer)} instead
     */
    @Deprecated
    void setEnterActionHandler(EnterActionHandler enterActionHandler);

    /**
     * @return {@link ArrowDownActionHandler} which handles ARROW_DOWN key pressing
     * @deprecated Use {@link #getArrowDownHandler()} and {@link #setArrowDownHandler(Consumer)} instead
     */
    @Deprecated
    ArrowDownActionHandler getArrowDownActionHandler();

    /**
     * Sets {@link ArrowDownActionHandler} which handles ARROW_DOWN key pressing.
     *
     * @param arrowDownActionHandler ArrowDownActionHandler instance
     * @deprecated Use {@link #setArrowDownHandler(Consumer)} instead
     */
    @Deprecated
    void setArrowDownActionHandler(ArrowDownActionHandler arrowDownActionHandler);

    /**
     * ENTER key pressed listener.
     *
     * @deprecated Use {@link #setEnterPressHandler(Consumer)} instead
     */
    @Deprecated
    @FunctionalInterface
    interface EnterActionHandler {

        /**
         * Called by component if user entered a search string and pressed ENTER key without selection of a suggestion.
         *
         * @param searchString search string as is
         */
        void onEnterKeyPressed(String searchString);
    }

    /**
     * ARROW_DOWN key pressed listener.
     *
     * @deprecated Use {@link #setArrowDownHandler(Consumer)} instead
     */
    @Deprecated
    @FunctionalInterface
    interface ArrowDownActionHandler {

        /**
         * Called by component if user pressed ARROW_DOWN key without search action.
         *
         * @param searchString search string as is
         */
        void onArrowDownKeyPressed(String searchString);
    }

    /**
     * Custom suggestions search action interface.
     *
     * @param <E> items type
     */
    interface SearchExecutor<E> extends io.jmix.ui.component.SuggestionField.SearchExecutor<E> {

        /**
         * Executed on background thread.
         *
         * @param searchString search string as is
         * @param searchParams additional parameters, empty if SearchExecutor is not instance of {@link io.jmix.ui.component.SuggestionField.ParametrizedSearchExecutor}
         * @return list with found items. {@link OptionWrapper} instances can be used as items to provide
         * different value for displaying purpose.
         */
        @Override
        List search(String searchString, Map<String, Object> searchParams);
    }

    /**
     * Extended version of {@link io.jmix.ui.component.SuggestionField.SearchExecutor} that allows to pass parameters.
     *
     * @param <E> items type
     */
    interface ParametrizedSearchExecutor<E> extends SearchExecutor<E> {

        /**
         * Called by the execution environment in UI thread to prepare execution parameters for
         * {@link io.jmix.ui.component.SuggestionField.SearchExecutor#search(String, Map)}.
         *
         * @return map with parameters
         */
        Map<String, Object> getParams();
    }

    /**
     * Represents a value and its string representation.
     * @deprecated Use {@link #setFormatter(Formatter)} instead
     */
    @Deprecated
    class OptionWrapper<V> {

        protected String caption;
        protected V value;

        public OptionWrapper(String caption, V value) {
            Preconditions.checkNotNullArgument(caption, "Caption should not be null");
            Preconditions.checkNotNullArgument(value, "Value should not be null");

            this.caption = caption;
            this.value = value;
        }

        /**
         * @return string representation
         */
        public String getCaption() {
            return caption;
        }

        /**
         * @return value
         */
        public V getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            OptionWrapper<?> that = (OptionWrapper<?>) o;

            return caption.equals(that.caption)
                    && value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return 17 * caption.hashCode() + 31 * value.hashCode();
        }

        @Override
        public String toString() {
            return caption;
        }
    }
}
