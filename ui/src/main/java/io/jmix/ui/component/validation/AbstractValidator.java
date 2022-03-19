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

package io.jmix.ui.component.validation;

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.substitutor.StringSubstitutor;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Main class for validators.
 *
 * @param <T> value type
 */
public abstract class AbstractValidator<T> implements Validator<T> {

    protected Messages messages;
    protected CurrentAuthentication currentAuthentication;
    protected DatatypeRegistry datatypeRegistry;
    protected StringSubstitutor substitutor;

    protected String message;

    /**
     * @return custom error message
     */
    @Nullable
    public String getMessage() {
        return message;
    }

    /**
     * Sets custom error message that will be used instead of default message. For error message it uses template string
     * and it is possible to use values in message. Each validator has its own value keys for formatted output. See
     * JavaDocs for specific validator.
     *
     * @param message error message
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    public void setMessage(@Nullable String message) {
        this.message = message;
    }

    /**
     * @param errorMessage error message
     * @param valuesMap    values map
     * @return message with inserted values
     */
    protected String getTemplateErrorMessage(@Nullable String errorMessage, Map<String, Object> valuesMap) {
        return !Strings.isNullOrEmpty(errorMessage)
                ? substitutor.substitute(errorMessage, valuesMap)
                : "";
    }

    protected String formatValue(Object value) {
        Datatype<?> datatype = datatypeRegistry.find(value.getClass());
        if (datatype == null) {
            return value.toString();
        }

        return datatype.format(value, currentAuthentication.getLocale());
    }
}
