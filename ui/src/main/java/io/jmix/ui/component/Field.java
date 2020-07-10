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

import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.data.HasValueSource;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.component.validation.Validator;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Base interface for "fields" - components intended to display and edit value of a certain entity attribute.
 */
public interface Field<V> extends HasValueSource<V>, Component.HasCaption,
        HasValue<V>, Component.Editable, Component.BelongToFrame, Validatable, Component.HasIcon,
        HasContextHelp, HasHtmlCaption, HasHtmlDescription, HasHtmlSanitizer {

    /**
     * @return whether the field must contain a non-null value
     */
    boolean isRequired();
    void setRequired(boolean required);

    @Nullable
    String getRequiredMessage();
    /**
     * A message that will be displayed to user if the field is required but has null value
     */
    void setRequiredMessage(@Nullable String msg);

    /**
     * Add validator instance.
     * {@link ValidationException} this exception must be thrown by the validator if the value is not valid.
     */
    void addValidator(Validator<? super V> validator);

    void removeValidator(Validator<V> validator);

    default void addValidators(Validator<? super V>... validators) {
        for (Validator<? super V> validator : validators) {
            addValidator(validator);
        }
    }

    /**
     * @return unmodifiable collection with Field validators
     */
    Collection<Validator<V>> getValidators();

    /**
     * @return datasource property
     * @deprecated Use {@link #getValueSource()} instead
     */
    @Deprecated
    @Nullable
    default MetaProperty getMetaProperty() {
        ValueSource<V> valueSource = getValueSource();
        if (valueSource instanceof ContainerValueSource) {
            return ((ContainerValueSource) valueSource).getMetaPropertyPath().getMetaProperty();
        }
        return null;
    }

    /**
     * @return datasource property path
     *
     * @deprecated Use {@link #getValueSource()} instead
     */
    @Deprecated
    @Nullable
    default MetaPropertyPath getMetaPropertyPath() {
        ValueSource<V> valueSource = getValueSource();
        if (valueSource instanceof ContainerValueSource) {
            return ((ContainerValueSource) valueSource).getMetaPropertyPath();
        }
        return null;
    }
}
