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

package io.jmix.ui.component;

import io.jmix.ui.component.validation.Validator;
import io.jmix.ui.meta.StudioElementsGroup;

import java.util.Collection;

/**
 * Interface to be implemented by a component that can have validators.
 *
 * @param <V> value type
 */
public interface HasValidator<V> {

    /**
     * Adds a validator instance to the component. The {@link ValidationException} must be thrown by the validator
     * if the value is not valid.
     *
     * @param validator a validator to add
     * @see ValidationException
     */
    @StudioElementsGroup(caption = "Validators",
            xmlElement = "validators",
            icon = "io/jmix/ui/icon/element/validators.svg")
    void addValidator(Validator<? super V> validator);

    /**
     * Removes a validator instance from the component.
     *
     * @param validator a validator to remove
     */
    void removeValidator(Validator<V> validator);

    /**
     * Sequentially adds validators to the component.
     *
     * @param validators validators to add
     */
    default void addValidators(Validator<? super V>... validators) {
        for (Validator<? super V> validator : validators) {
            addValidator(validator);
        }
    }

    /**
     * @return unmodifiable collection of component validators.
     */
    Collection<Validator<V>> getValidators();
}
