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

package io.jmix.flowui.view;


import io.jmix.flowui.component.LookupComponent;
import io.jmix.flowui.util.OperationResult;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Interface of views that display a list of entities and can return instances selected by the user.
 *
 * @param <E> entity class
 */
public interface LookupView<E> {

    /**
     * Handle selected entities.
     */
    OperationResult handleSelection();

    /**
     * Close the view without selection.
     */
    OperationResult closeWithDiscard();

    /**
     * @return a component that is used to select entities of this view
     * @throws IllegalStateException if such a component is not defined
     */
    LookupComponent<E> getLookupComponent();

    /**
     * @return an optional component that is used to select entities of this view
     */
    Optional<LookupComponent<E>> findLookupComponent();

    /**
     * @return selection handler
     */
    Optional<Consumer<Collection<E>>> getSelectionHandler();

    /**
     * Sets selection handler for screen.
     *
     * @param selectionHandler selection handler
     */
    void setSelectionHandler(@Nullable Consumer<Collection<E>> selectionHandler);

    /**
     * @return selection validator
     */
    Optional<Predicate<ValidationContext<E>>> getSelectionValidator();

    /**
     * Sets selection validator.
     *
     * @param selectionValidator selection validator
     */
    void setSelectionValidator(@Nullable Predicate<ValidationContext<E>> selectionValidator);

    /**
     * Context object which is passed to the selection validator set by {@link #setSelectionValidator(Predicate)}.
     *
     * @param <E> type of entity
     */
    class ValidationContext<E> {
        protected final View<?> view;
        protected final Collection<E> selectedItems;

        public ValidationContext(View<?> view, Collection<E> selectedItems) {
            this.view = view;
            this.selectedItems = selectedItems;
        }

        /**
         * @return the lookup view
         */
        public View<?> getView() {
            return view;
        }

        /**
         * @return a collection of selected entities
         */
        public Collection<E> getSelectedItems() {
            return selectedItems;
        }
    }
}
