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

package io.jmix.ui.component;

import org.springframework.core.ParameterizedTypeReference;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A filtering dropdown single-select. Items are filtered based on user input using asynchronous data loading.
 *
 * @param <V> type of value
 */
public interface SuggestionField<V> extends Field<V>,
        Component.Focusable, HasInputPrompt, HasOptionStyleProvider<V>, HasFormatter<V> {

    String NAME = "suggestionField";

    String POPUP_AUTO_WIDTH = "auto";
    String POPUP_PARENT_WIDTH = "parent";

    static <T> ParameterizedTypeReference<SuggestionField<T>> of(Class<T> valueClass) {
        return new ParameterizedTypeReference<SuggestionField<T>>() {};
    }

    /**
     * Custom suggestions search action interface.
     *
     * @param <E> items type
     */
    interface SearchExecutor<E> {

        /**
         * Executed on background thread.
         *
         * @param searchString search string as is
         * @param searchParams additional parameters, empty if SearchExecutor is not instance of {@link ParametrizedSearchExecutor}
         * @return list with found items.
         * different value for displaying purpose.
         */
        List<E> search(String searchString, Map<String, Object> searchParams);
    }

    /**
     * Extended version of {@link SuggestionField.SearchExecutor} that allows to pass parameters.
     *
     * @param <E> items type
     */
    interface ParametrizedSearchExecutor<E> extends SearchExecutor<E> {

        /**
         * Called by the execution environment in UI thread to prepare execution parameters for
         * {@link SearchExecutor#search(String, Map)}.
         *
         * @return map with parameters
         */
        Map<String, Object> getParams();
    }

    /**
     * @return delay between the last key press action and async search
     */
    int getAsyncSearchDelayMs();

    /**
     * Sets delay between the last key press action and async search.
     *
     * @param asyncSearchDelayMs delay in ms
     */
    void setAsyncSearchDelayMs(int asyncSearchDelayMs);

    /**
     * @return {@link SearchExecutor} which performs search
     */
    @Nullable
    SearchExecutor<V> getSearchExecutor();

    /**
     * Sets {@link SearchExecutor} which performs search.
     *
     * @param searchExecutor SearchExecutor instance
     */
    void setSearchExecutor(@Nullable SearchExecutor<V> searchExecutor);

    /**
     * @return an ENTER press handler
     */
    Consumer<String> getEnterActionHandler();

    /**
     * Sets an ENTER press handler.
     *
     * @param handler an ENTER press handler to set
     */
    void setEnterActionHandler(Consumer<String> handler);

    /**
     * @return an ARROW_DOWN press handler
     */
    Consumer<String> getArrowDownActionHandler();

    /**
     * Sets an ARROW_DOWN press handler.
     *
     * @param handler an ARROW_DOWN press handler to set
     */
    void setArrowDownActionHandler(Consumer<String> handler);

    /**
     * @return min string length to perform suggestions search
     */
    int getMinSearchStringLength();

    /**
     * Sets min string length which is required to perform suggestions search.
     *
     * @param minSearchStringLength required string length to perform search
     */
    void setMinSearchStringLength(int minSearchStringLength);

    /**
     * @return limit of suggestions which will be shown
     */
    int getSuggestionsLimit();

    /**
     * Sets limit of suggestions which will be shown.
     *
     * @param suggestionsLimit integer limit value
     */
    void setSuggestionsLimit(int suggestionsLimit);

    /**
     * Show passed suggestions in popup.
     *
     * @param suggestions suggestions to show
     */
    void showSuggestions(List<V> suggestions);

    /**
     * Sets the given {@code width} to the component popup. There are two predefined settings available:
     * {@link SuggestionField#POPUP_AUTO_WIDTH} and {@link SuggestionField#POPUP_PARENT_WIDTH}.
     *
     * @param width width of the component popup
     */
    void setPopupWidth(String width);

    /**
     * @return component popup width
     */
    String getPopupWidth();
}
