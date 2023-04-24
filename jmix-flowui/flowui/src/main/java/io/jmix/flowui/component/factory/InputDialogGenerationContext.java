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

package io.jmix.flowui.component.factory;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.component.ComponentGenerationContext;

public class InputDialogGenerationContext extends ComponentGenerationContext {

    protected final InputParameter inputParameter;

    public InputDialogGenerationContext(MetaClass metaClass, String property, InputParameter inputParameter) {
        super(metaClass, property);
        this.inputParameter = inputParameter;
    }

    public InputParameter getInputParameter() {
        return inputParameter;
    }
}
