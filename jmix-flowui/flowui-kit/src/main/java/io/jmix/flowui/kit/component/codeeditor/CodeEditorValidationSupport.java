/*
 * Copyright 2023 Haulmont.
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
package io.jmix.flowui.kit.component.codeeditor;

import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.data.binder.ValidationResult;

import java.io.Serializable;

public class CodeEditorValidationSupport implements Serializable {
    private final JmixCodeEditor field;
    private boolean required;

    CodeEditorValidationSupport(JmixCodeEditor field) {
        this.field = field;
    }

    void setRequired(boolean required) {
        this.required = required;
    }

    boolean isInvalid(String value) {
        ValidationResult requiredValidation = ValidationUtil.checkRequired(required, value, field.getEmptyValue());
        return requiredValidation.isError();
    }
}
